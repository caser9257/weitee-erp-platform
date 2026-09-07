package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderStepDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderStepMapper;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionOrderStepStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpProductionOrderStepServiceImplTest {

    private final AtomicReference<ErpProductionOrderStepDO> stepRef = new AtomicReference<>();
    private final AtomicReference<List<ErpProductionOrderStepDO>> orderStepsRef = new AtomicReference<>(List.of());
    private final AtomicReference<Integer> casExpectedStatusRef = new AtomicReference<>();
    private final AtomicReference<Long> pendingQualityCountRef = new AtomicReference<>(0L);

    private ErpProductionOrderStepServiceImpl stepService;

    @BeforeEach
    void setUp() throws Exception {
        stepService = new ErpProductionOrderStepServiceImpl();
        stepRef.set(null);
        orderStepsRef.set(List.of());
        casExpectedStatusRef.set(null);
        pendingQualityCountRef.set(0L);

        setField(stepService, "productionOrderStepMapper",
                createProxy(ErpProductionOrderStepMapper.class, (methodName, args) -> {
                    if ("selectById".equals(methodName)) {
                        return stepRef.get();
                    }
                    if ("selectListByOrderId".equals(methodName)) {
                        return orderStepsRef.get();
                    }
                    if ("updateStepStatusByCas".equals(methodName)) {
                        Integer expectStatus = (Integer) args[1];
                        ErpProductionOrderStepDO current = stepRef.get();
                        if (current != null && expectStatus.equals(current.getStepStatus())) {
                            current.setStepStatus((Integer) args[2]);
                            return 1;
                        }
                        return 0;
                    }
                    return null;
                }));
        setField(stepService, "productionStepQualityMapper",
                createProxy(cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionStepQualityMapper.class,
                        (methodName, args) -> {
                            if ("selectPendingCount".equals(methodName)) {
                                return pendingQualityCountRef.get();
                            }
                            return null;
                        }));
        // 工序完工会发布 ErpProductionOrderStepFinishedEvent，夹具注入 no-op 事件发布器
        setField(stepService, "eventPublisher",
                createProxy(org.springframework.context.ApplicationEventPublisher.class,
                        (methodName, args) -> null));
    }

    @Test
    void startStep_shouldTransitionToProcessing() {
        stepRef.set(new ErpProductionOrderStepDO()
                .setId(1L).setProductionOrderId(10L).setStepNo(10)
                .setStepStatus(ErpProductionOrderStepStatusEnum.WAIT.getStatus()));
        orderStepsRef.set(List.of(new ErpProductionOrderStepDO()
                .setId(1L).setProductionOrderId(10L).setStepNo(10)
                .setStepStatus(ErpProductionOrderStepStatusEnum.WAIT.getStatus())));

        stepService.startStep(1L);
    }

    @Test
    void startStep_shouldRejectWhenPrecedentUnfinished() {
        stepRef.set(new ErpProductionOrderStepDO()
                .setId(2L).setProductionOrderId(10L).setStepNo(20)
                .setStepStatus(ErpProductionOrderStepStatusEnum.WAIT.getStatus()));
        orderStepsRef.set(List.of(
                new ErpProductionOrderStepDO().setId(1L).setProductionOrderId(10L).setStepNo(10)
                        .setStepStatus(ErpProductionOrderStepStatusEnum.PROCESSING.getStatus()),
                new ErpProductionOrderStepDO().setId(2L).setProductionOrderId(10L).setStepNo(20)
                        .setStepStatus(ErpProductionOrderStepStatusEnum.WAIT.getStatus())
        ));

        assertThrows(ServiceException.class, () -> stepService.startStep(2L));
    }

    @Test
    void startStep_shouldRejectWhenAlreadyProcessing() {
        stepRef.set(new ErpProductionOrderStepDO()
                .setId(3L).setProductionOrderId(10L).setStepNo(10)
                .setStepStatus(ErpProductionOrderStepStatusEnum.PROCESSING.getStatus()));

        assertThrows(ServiceException.class, () -> stepService.startStep(3L));
    }

    @Test
    void pauseResumeFinish_shouldFollowStateMachine() {
        stepRef.set(new ErpProductionOrderStepDO()
                .setId(4L).setProductionOrderId(10L).setStepNo(10)
                .setStepStatus(ErpProductionOrderStepStatusEnum.PROCESSING.getStatus()));

        stepService.pauseStep(4L);
        stepService.resumeStep(4L);
        stepService.finishStep(4L);
    }

    @Test
    void finishStep_shouldRejectWhenNotProcessing() {
        stepRef.set(new ErpProductionOrderStepDO()
                .setId(5L).setProductionOrderId(10L).setStepNo(10)
                .setStepStatus(ErpProductionOrderStepStatusEnum.WAIT.getStatus()));

        assertThrows(ServiceException.class, () -> stepService.finishStep(5L));
    }

    @Test
    void finishStep_shouldRejectWhenPendingQualityExists() {
        stepRef.set(new ErpProductionOrderStepDO()
                .setId(6L).setProductionOrderId(10L).setStepNo(10)
                .setQcFlag(Boolean.TRUE)
                .setStepStatus(ErpProductionOrderStepStatusEnum.PROCESSING.getStatus()));
        pendingQualityCountRef.set(1L);

        assertThrows(ServiceException.class, () -> stepService.finishStep(6L));
    }

    @Test
    void finishStep_shouldAllowWhenNoPendingQuality() {
        stepRef.set(new ErpProductionOrderStepDO()
                .setId(7L).setProductionOrderId(10L).setStepNo(10)
                .setQcFlag(Boolean.TRUE)
                .setStepStatus(ErpProductionOrderStepStatusEnum.PROCESSING.getStatus()));
        pendingQualityCountRef.set(0L);

        stepService.finishStep(7L);
    }

    @Test
    void startStep_shouldRejectWhenStepMissing() {
        assertThrows(ServiceException.class, () -> stepService.startStep(99L));
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> type, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        if ("toString".equals(method.getName())) {
                            return type.getSimpleName() + "Proxy";
                        }
                        if ("hashCode".equals(method.getName())) {
                            return System.identityHashCode(proxy);
                        }
                        if ("equals".equals(method.getName())) {
                            return proxy == args[0];
                        }
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
