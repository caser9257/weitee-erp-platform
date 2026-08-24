package cn.weitee.erp.module.bpm.service.task;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.bpm.controller.admin.task.vo.task.BpmTaskPageReqVO;
import cn.weitee.erp.module.bpm.framework.flowable.core.util.FlowableUtils;
import org.flowable.engine.TaskService;
import org.flowable.task.api.TaskQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BpmTaskServiceImplTest {

    private BpmTaskServiceImpl taskService;
    private TaskService taskServiceProxy;
    private TaskQueryProbe taskQueryProbe;

    @BeforeEach
    public void setUp() throws Exception {
        taskService = new BpmTaskServiceImpl();
        taskQueryProbe = new TaskQueryProbe();
        taskServiceProxy = (TaskService) Proxy.newProxyInstance(
                TaskService.class.getClassLoader(),
                new Class[] {TaskService.class},
                new TaskServiceHandler(taskQueryProbe));
        setField(taskService, "taskService", taskServiceProxy);
    }

    @Test
    public void testGetTaskTodoPage_skipTenantFilterWhenTenantMissing() {
        clearLoginUser();
        BpmTaskPageReqVO pageVO = new BpmTaskPageReqVO();

        PageResult<?> pageResult = taskService.getTaskTodoPage(910204L, pageVO);

        assertEquals(0, pageResult.getTotal());
        assertNull(taskQueryProbe.tenantId);
    }

    private static void clearLoginUser() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static final class TaskServiceHandler implements InvocationHandler {

        private final TaskQueryProbe probe;

        private TaskServiceHandler(TaskQueryProbe probe) {
            this.probe = probe;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if ("createTaskQuery".equals(method.getName())) {
                return probe.proxy;
            }
            return defaultValue(method.getReturnType());
        }
    }

    private static final class TaskQueryProbe implements InvocationHandler {

        private final TaskQuery proxy;
        private String tenantId;

        private TaskQueryProbe() {
            this.proxy = (TaskQuery) Proxy.newProxyInstance(
                    TaskQuery.class.getClassLoader(),
                    new Class[] {TaskQuery.class},
                    this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "taskAssignee":
                case "active":
                case "includeProcessVariables":
                case "taskTenantId":
                case "orderByTaskCreateTime":
                case "desc":
                    if ("taskTenantId".equals(method.getName())) {
                        tenantId = (String) args[0];
                    }
                    return this.proxy;
                case "count":
                    return 0L;
                case "listPage":
                    return java.util.Collections.emptyList();
                default:
                    return defaultValue(method.getReturnType());
            }
        }
    }

    private static Object defaultValue(Class<?> returnType) {
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == float.class) {
            return 0F;
        }
        if (returnType == double.class) {
            return 0D;
        }
        if (returnType == char.class) {
            return '\0';
        }
        return null;
    }
}
