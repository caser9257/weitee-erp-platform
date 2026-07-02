package cn.weitee.erp.module.erp.service.project;

import cn.weitee.erp.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.weitee.erp.module.erp.dal.dataobject.project.ErpProjectRoleTaskDO;
import cn.weitee.erp.module.erp.dal.mysql.project.ErpProjectMapper;
import cn.weitee.erp.module.erp.dal.mysql.project.ErpProjectRoleTaskMapper;
import cn.weitee.erp.module.erp.enums.ErpProjectRoleCodeConstants;
import cn.weitee.erp.module.erp.enums.ErpProjectRoleTaskStatusConstants;
import cn.weitee.erp.module.erp.enums.ErpProjectRoleTaskTypeConstants;
import cn.weitee.erp.module.system.service.notify.NotifySendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ErpProjectRoleTaskServiceImplTest {

    private final AtomicReference<ErpProjectDO> selectProjectResult = new AtomicReference<>();
    private final AtomicReference<ErpProjectRoleTaskDO> selectTodoTaskResult = new AtomicReference<>();
    private final AtomicReference<List<ErpProjectRoleTaskDO>> selectTodoListResult = new AtomicReference<>(List.of());
    private final List<ErpProjectRoleTaskDO> insertedTasks = new ArrayList<>();
    private final List<ErpProjectRoleTaskDO> updatedTasks = new ArrayList<>();
    private final AtomicReference<Long> notifyUserId = new AtomicReference<>();
    private final AtomicReference<String> notifyTemplateCode = new AtomicReference<>();
    private final AtomicReference<Map<String, Object>> notifyTemplateParams = new AtomicReference<>();

    private ErpProjectRoleTaskServiceImpl taskService;

    @BeforeEach
    void setUp() throws Exception {
        taskService = new ErpProjectRoleTaskServiceImpl();
        selectProjectResult.set(null);
        selectTodoTaskResult.set(null);
        selectTodoListResult.set(List.of());
        insertedTasks.clear();
        updatedTasks.clear();
        notifyUserId.set(null);
        notifyTemplateCode.set(null);
        notifyTemplateParams.set(null);
        setField(taskService, "erpProjectMapper", createProjectMapperProxy());
        setField(taskService, "projectRoleTaskMapper", createTaskMapperProxy());
        setField(taskService, "notifySendService", createNotifySendServiceProxy());
    }

    @Test
    void testCreateOrRefreshPcTaskInsertShouldNotify() {
        selectProjectResult.set(new ErpProjectDO().setId(1L).setNo("PJ-001").setName("Project-A")
                .setPlanCoordinatorId(12L).setDeliveryDate(LocalDate.of(2026, 4, 20)));

        taskService.createOrRefreshPcTask(1L, 1001L, LocalDate.of(2026, 4, 18));

        assertEquals(1, insertedTasks.size());
        ErpProjectRoleTaskDO task = insertedTasks.get(0);
        assertEquals(1L, task.getProjectId());
        assertEquals(ErpProjectRoleCodeConstants.PC, task.getRoleCode());
        assertEquals(ErpProjectRoleTaskTypeConstants.SALE_APPROVED_PLAN_CONFIRM, task.getTaskType());
        assertEquals(ErpProjectRoleTaskStatusConstants.TODO, task.getTaskStatus());
        assertEquals(12L, task.getAssigneeUserId());
        assertEquals("SALE_ORDER", task.getSourceType());
        assertEquals(1001L, task.getSourceId());
        assertNotNull(task.getDueTime());

        assertEquals(12L, notifyUserId.get());
        assertEquals("erp_project_role_task_assigned", notifyTemplateCode.get());
        assertEquals("PJ-001", notifyTemplateParams.get().get("projectNo"));
        assertEquals("Project-A", notifyTemplateParams.get().get("projectName"));
        assertEquals("PC确认", notifyTemplateParams.get().get("roleName"));
        assertEquals(task.getSummary(), notifyTemplateParams.get().get("taskSummary"));
    }

    @Test
    void testCreateOrRefreshPcTaskUpdateExistingTodoShouldSkipNotifyWhenUnchanged() {
        selectProjectResult.set(new ErpProjectDO().setId(1L).setNo("PJ-001").setName("Project-A")
                .setPlanCoordinatorId(22L));
        selectTodoTaskResult.set(new ErpProjectRoleTaskDO().setId(9001L).setProjectId(1L)
                .setRoleCode(ErpProjectRoleCodeConstants.PC)
                .setTaskType(ErpProjectRoleTaskTypeConstants.SALE_APPROVED_PLAN_CONFIRM)
                .setTaskStatus(ErpProjectRoleTaskStatusConstants.TODO)
                .setAssigneeUserId(22L)
                .setSourceId(2002L)
                .setSummary("销售订单审批已通过，请 PC 确认计划交付节点")
                .setDueTime(LocalDate.of(2026, 4, 25).atStartOfDay()));

        taskService.createOrRefreshPcTask(1L, 2002L, LocalDate.of(2026, 4, 25));

        assertEquals(0, insertedTasks.size());
        assertEquals(1, updatedTasks.size());
        assertEquals(9001L, updatedTasks.get(0).getId());
        assertEquals(22L, updatedTasks.get(0).getAssigneeUserId());
        assertEquals(2002L, updatedTasks.get(0).getSourceId());
        assertEquals(null, notifyUserId.get());
    }

    @Test
    void testCreateOrRefreshMcTaskInsertShouldNotify() {
        selectProjectResult.set(new ErpProjectDO().setId(2L).setNo("PJ-002").setName("Project-B")
                .setMaterialControllerId(33L).setDeliveryDate(LocalDate.of(2026, 4, 30)));

        taskService.createOrRefreshMcTask(2L, 3003L, "MRP suggestions generated");

        assertEquals(1, insertedTasks.size());
        ErpProjectRoleTaskDO task = insertedTasks.get(0);
        assertEquals(33L, task.getAssigneeUserId());
        assertEquals(33L, notifyUserId.get());
        assertEquals("erp_project_role_task_assigned", notifyTemplateCode.get());
        assertEquals("MC确认", notifyTemplateParams.get().get("roleName"));
        assertEquals("MRP suggestions generated", notifyTemplateParams.get().get("taskSummary"));
    }

    @Test
    void testCompleteMcTaskShouldMarkTodoAsDone() {
        selectTodoListResult.set(List.of(
                new ErpProjectRoleTaskDO().setId(7001L).setProjectId(2L)
                        .setRoleCode(ErpProjectRoleCodeConstants.MC)
                        .setTaskStatus(ErpProjectRoleTaskStatusConstants.TODO),
                new ErpProjectRoleTaskDO().setId(7002L).setProjectId(2L)
                        .setRoleCode(ErpProjectRoleCodeConstants.PC)
                        .setTaskStatus(ErpProjectRoleTaskStatusConstants.TODO)
        ));

        taskService.completeMcTask(2L, "MC processed");

        assertEquals(1, updatedTasks.size());
        assertEquals(7001L, updatedTasks.get(0).getId());
        assertEquals(ErpProjectRoleTaskStatusConstants.DONE, updatedTasks.get(0).getTaskStatus());
        assertEquals("MC processed", updatedTasks.get(0).getRemark());
        assertNotNull(updatedTasks.get(0).getFinishTime());
    }

    @SuppressWarnings("unchecked")
    private <T> T createProjectMapperProxy() {
        return (T) Proxy.newProxyInstance(ErpProjectMapper.class.getClassLoader(),
                new Class<?>[]{ErpProjectMapper.class},
                (proxy, method, args) -> {
                    if ("selectById".equals(method.getName())) {
                        return selectProjectResult.get();
                    }
                    return null;
                });
    }

    @SuppressWarnings("unchecked")
    private <T> T createTaskMapperProxy() {
        return (T) Proxy.newProxyInstance(ErpProjectRoleTaskMapper.class.getClassLoader(),
                new Class<?>[]{ErpProjectRoleTaskMapper.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "selectTodoTask":
                            return selectTodoTaskResult.get();
                        case "selectTodoListByProjectId":
                            return selectTodoListResult.get();
                        case "insert":
                            insertedTasks.add((ErpProjectRoleTaskDO) args[0]);
                            return 1;
                        case "updateById":
                            updatedTasks.add((ErpProjectRoleTaskDO) args[0]);
                            return 1;
                        default:
                            return null;
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private <T> T createNotifySendServiceProxy() {
        return (T) Proxy.newProxyInstance(NotifySendService.class.getClassLoader(),
                new Class<?>[]{NotifySendService.class},
                (proxy, method, args) -> {
                    if ("sendSingleNotifyToAdmin".equals(method.getName())) {
                        notifyUserId.set((Long) args[0]);
                        notifyTemplateCode.set((String) args[1]);
                        notifyTemplateParams.set((Map<String, Object>) args[2]);
                        return 10086L;
                    }
                    if ("sendSingleNotify".equals(method.getName())) {
                        notifyUserId.set((Long) args[0]);
                        notifyTemplateCode.set((String) args[2]);
                        notifyTemplateParams.set((Map<String, Object>) args[3]);
                        return 10086L;
                    }
                    return null;
                });
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(mapFieldName(fieldName));
        field.setAccessible(true);
        field.set(target, value);
    }

    private String mapFieldName(String fieldName) {
        return switch (fieldName) {
            case "projectRoleTaskMapper" -> "erpProjectRoleTaskMapper";
            default -> fieldName;
        };
    }

}
