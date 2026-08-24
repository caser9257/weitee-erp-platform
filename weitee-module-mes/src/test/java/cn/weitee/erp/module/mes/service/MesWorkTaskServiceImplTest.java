package cn.weitee.erp.module.mes.service;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderStepDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderStepMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.mes.controller.admin.vo.worktask.MesWorkTaskUpdatePlanTimeReqVO;
import cn.weitee.erp.module.mes.dal.dataobject.MesWorkTaskDO;
import cn.weitee.erp.module.mes.dal.mysql.MesWorkTaskMapper;
import cn.weitee.erp.module.mes.enums.MesWorkTaskStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MesWorkTaskServiceImplTest {

    private final AtomicReference<ErpProductionOrderDO> orderRef = new AtomicReference<>();
    private final AtomicReference<List<ErpProductionOrderStepDO>> stepsRef = new AtomicReference<>(List.of());
    private final List<MesWorkTaskDO> insertedTasks = new java.util.ArrayList<>();
    private final AtomicReference<MesWorkTaskDO> existedTaskRef = new AtomicReference<>();
    private final AtomicReference<MesWorkTaskDO> taskRef = new AtomicReference<>();
    private final AtomicReference<MesWorkTaskDO> updatedTaskRef = new AtomicReference<>();
    private final AtomicInteger noSeq = new AtomicInteger(1);

    private MesWorkTaskServiceImpl taskService;

    @BeforeEach
    void setUp() throws Exception {
        taskService = new MesWorkTaskServiceImpl();
        orderRef.set(null);
        stepsRef.set(List.of());
        insertedTasks.clear();
        existedTaskRef.set(null);
        taskRef.set(null);
        updatedTaskRef.set(null);

        setField(taskService, "erpProductionOrderMapper", createProxy(ErpProductionOrderMapper.class, (m, a) -> {
            if ("selectById".equals(m)) return orderRef.get();
            return null;
        }));
        setField(taskService, "erpProductionOrderStepMapper", createProxy(ErpProductionOrderStepMapper.class, (m, a) -> {
            if ("selectListByOrderId".equals(m)) return stepsRef.get();
            return null;
        }));
        setField(taskService, "mesWorkTaskMapper", createProxy(MesWorkTaskMapper.class, (m, a) -> {
            if ("selectListByOrderId".equals(m)) {
                // 已生成任务列表：由 existedTaskRef 提供
                return existedTaskRef.get() == null ? List.of() : List.of(existedTaskRef.get());
            }
            if ("insert".equals(m)) {
                insertedTasks.add((MesWorkTaskDO) a[0]);
                return 1;
            }
            if ("selectById".equals(m)) return taskRef.get();
            if ("updatePlanTimeByCas".equals(m) || "cancelTaskByCas".equals(m)) {
                // CAS 模拟：当前状态 0/1 可更新，否则返回 0
                MesWorkTaskDO current = taskRef.get();
                if (current != null && (current.getStatus() == 0 || current.getStatus() == 1)) {
                    return 1;
                }
                return 0;
            }
            return null;
        }));
        setField(taskService, "noRedisDAO", new ErpNoRedisDAO() {
            @Override
            public String generate(String prefix) {
                return "RW202608130000" + noSeq.getAndIncrement();
            }
        });
    }

    @Test
    void createTasks_shouldCreateOneTaskPerStep() {
        orderRef.set(new ErpProductionOrderDO().setId(10L).setOrderNo("SCGD-1"));
        stepsRef.set(List.of(
                new ErpProductionOrderStepDO().setId(101L).setStepNo(10).setStepCode("OP-10")
                        .setStepName("Cutting").setWorkCenterId(501L).setPlanQty(new BigDecimal("50")),
                new ErpProductionOrderStepDO().setId(102L).setStepNo(20).setStepCode("OP-20")
                        .setStepName("Assembly").setWorkCenterId(502L).setPlanQty(new BigDecimal("50"))
        ));

        taskService.createTasksByOrderReleased(10L);

        assertEquals(2, insertedTasks.size());
        MesWorkTaskDO t1 = insertedTasks.get(0);
        assertEquals("SCGD-1", t1.getProductionOrderNo());
        assertEquals(101L, t1.getOrderStepId());
        assertEquals("OP-10", t1.getStepCode());
        assertEquals(501L, t1.getWorkCenterId());
        assertEquals(MesWorkTaskStatusEnum.WAIT_SCHEDULE.getStatus(), t1.getStatus());
    }

    @Test
    void createTasks_shouldSkipExistingStep() {
        orderRef.set(new ErpProductionOrderDO().setId(10L).setOrderNo("SCGD-1"));
        stepsRef.set(List.of(
                new ErpProductionOrderStepDO().setId(101L).setStepNo(10).setStepCode("OP-10")
                        .setStepName("Cutting").setWorkCenterId(501L).setPlanQty(new BigDecimal("50"))
        ));
        existedTaskRef.set(new MesWorkTaskDO().setId(1L).setOrderStepId(101L));

        taskService.createTasksByOrderReleased(10L);

        assertEquals(0, insertedTasks.size(), "已存在任务应跳过，不重复生成");
    }

    @Test
    void createTasks_shouldSkipWhenOrderMissing() {
        taskService.createTasksByOrderReleased(99L);
        assertEquals(0, insertedTasks.size());
    }

    @Test
    void updatePlanTime_shouldRejectWhenStatusNotSchedulable() {
        taskRef.set(new MesWorkTaskDO().setId(1L).setStatus(MesWorkTaskStatusEnum.PROCESSING.getStatus()));
        MesWorkTaskUpdatePlanTimeReqVO reqVO = new MesWorkTaskUpdatePlanTimeReqVO();
        reqVO.setId(1L);
        reqVO.setPlanStartTime(LocalDateTime.of(2026, 8, 20, 8, 0));
        reqVO.setPlanEndTime(LocalDateTime.of(2026, 8, 20, 18, 0));

        assertThrows(ServiceException.class, () -> taskService.updatePlanTime(reqVO));
    }

    @Test
    void updatePlanTime_shouldRejectWhenTimeInvalid() {
        taskRef.set(new MesWorkTaskDO().setId(1L).setStatus(MesWorkTaskStatusEnum.WAIT_SCHEDULE.getStatus()));
        MesWorkTaskUpdatePlanTimeReqVO reqVO = new MesWorkTaskUpdatePlanTimeReqVO();
        reqVO.setId(1L);
        reqVO.setPlanStartTime(LocalDateTime.of(2026, 8, 20, 18, 0));
        reqVO.setPlanEndTime(LocalDateTime.of(2026, 8, 20, 8, 0));

        assertThrows(ServiceException.class, () -> taskService.updatePlanTime(reqVO));
    }

    @Test
    void updatePlanTime_shouldSucceedWhenSchedulable() {
        taskRef.set(new MesWorkTaskDO().setId(1L).setStatus(MesWorkTaskStatusEnum.WAIT_SCHEDULE.getStatus()));
        MesWorkTaskUpdatePlanTimeReqVO reqVO = new MesWorkTaskUpdatePlanTimeReqVO();
        reqVO.setId(1L);
        reqVO.setPlanStartTime(LocalDateTime.of(2026, 8, 20, 8, 0));
        reqVO.setPlanEndTime(LocalDateTime.of(2026, 8, 20, 18, 0));

        taskService.updatePlanTime(reqVO);
        // 不抛异常即 CAS 更新成功（模拟器中已标记状态变更）
    }

    @Test
    void cancelTask_shouldRejectWhenFinished() {
        taskRef.set(new MesWorkTaskDO().setId(1L).setStatus(MesWorkTaskStatusEnum.FINISHED.getStatus()));

        assertThrows(ServiceException.class, () -> taskService.cancelTask(1L));
    }

    @Test
    void cancelTask_shouldSucceedWhenScheduled() {
        taskRef.set(new MesWorkTaskDO().setId(1L).setStatus(MesWorkTaskStatusEnum.SCHEDULED.getStatus()));

        taskService.cancelTask(1L);
        // 不抛异常即 CAS 更新成功
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> type, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        if ("toString".equals(method.getName())) return type.getSimpleName() + "Proxy";
                        if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
                        if ("equals".equals(method.getName())) return proxy == args[0];
                    }
                    return handler.handle(method.getName(), args);
                });
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }

}
