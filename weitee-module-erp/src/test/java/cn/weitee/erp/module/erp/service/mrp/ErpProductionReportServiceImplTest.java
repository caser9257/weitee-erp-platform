package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.report.ErpProductionReportCreateReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderStepDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionReportDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionReportItemDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderStepMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionReportItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionReportMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionOrderStatusEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionOrderStepStatusEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionReportTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpProductionReportServiceImplTest {

    private final AtomicReference<ErpProductionOrderDO> orderRef = new AtomicReference<>();
    private final AtomicReference<List<ErpProductionOrderStepDO>> stepsRef = new AtomicReference<>(List.of());
    private final AtomicInteger casCallCount = new AtomicInteger();
    private final AtomicInteger casSuccessCount = new AtomicInteger();
    private final AtomicReference<ErpProductionReportDO> insertedReportRef = new AtomicReference<>();
    private final List<ErpProductionReportItemDO> insertedItems = new java.util.ArrayList<>();

    private ErpProductionReportServiceImpl reportService;

    @BeforeEach
    void setUp() throws Exception {
        reportService = new ErpProductionReportServiceImpl();
        orderRef.set(null);
        stepsRef.set(List.of());
        casCallCount.set(0);
        casSuccessCount.set(0);
        insertedReportRef.set(null);
        insertedItems.clear();

        setField(reportService, "productionOrderMapper", createProxy(ErpProductionOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return orderRef.get();
            }
            return null;
        }));
        setField(reportService, "productionOrderStepMapper", createProxy(ErpProductionOrderStepMapper.class, (methodName, args) -> {
            if ("selectListByIds".equals(methodName)) {
                return stepsRef.get();
            }
            if ("updateStepQtyByCas".equals(methodName)) {
                casCallCount.incrementAndGet();
                return casSuccessCount.getAndIncrement() < casCallCount.get() ? 1 : 0;
            }
            return null;
        }));
        setField(reportService, "productionReportMapper", createProxy(ErpProductionReportMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertedReportRef.set((ErpProductionReportDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(reportService, "productionReportItemMapper", createProxy(ErpProductionReportItemMapper.class, (methodName, args) -> {
            if ("insertBatch".equals(methodName)) {
                insertedItems.addAll((List<ErpProductionReportItemDO>) args[0]);
                return true;
            }
            return null;
        }));
        setField(reportService, "noRedisDAO", new ErpNoRedisDAO() {
            @Override
            public String generate(String prefix) {
                return "BGDG202608120001";
            }
        });
        setField(reportService, "productionStepQualityService",
                createProxy(ErpProductionStepQualityService.class, (methodName, args) -> null));
        // 报工创建会发布 ErpProductionReportCreatedEvent，夹具注入 no-op 事件发布器
        setField(reportService, "eventPublisher",
                createProxy(org.springframework.context.ApplicationEventPublisher.class,
                        (methodName, args) -> null));
    }

    private ErpProductionReportCreateReqVO buildReqVO() {
        ErpProductionReportCreateReqVO reqVO = new ErpProductionReportCreateReqVO();
        reqVO.setProductionOrderId(10L);
        reqVO.setReportType(ErpProductionReportTypeEnum.STEP.getType());
        ErpProductionReportCreateReqVO.Item item = new ErpProductionReportCreateReqVO.Item();
        item.setProductionOrderStepId(101L);
        item.setReportedQty(new BigDecimal("8"));
        item.setQualifiedQty(new BigDecimal("7"));
        item.setScrapQty(new BigDecimal("1"));
        reqVO.setItems(List.of(item));
        return reqVO;
    }

    @Test
    void createReport_shouldInsertReportAndItems() {
        orderRef.set(new ErpProductionOrderDO().setId(10L).setStatus(ErpProductionOrderStatusEnum.RELEASED.getStatus()));
        stepsRef.set(List.of(new ErpProductionOrderStepDO()
                .setId(101L).setProductionOrderId(10L).setStepNo(10)
                .setStepStatus(ErpProductionOrderStepStatusEnum.PROCESSING.getStatus())));

        Long reportId = reportService.createReport(buildReqVO());

        assertEquals(1, casCallCount.get());
        assertEquals("BGDG202608120001", insertedReportRef.get().getReportNo());
        assertEquals(ErpProductionReportTypeEnum.STEP.getType(), insertedReportRef.get().getReportType());
        assertEquals(1, insertedItems.size());
        assertEquals(101L, insertedItems.get(0).getProductionOrderStepId());
        assertEquals(new BigDecimal("8"), insertedItems.get(0).getReportedQty());
        assertEquals(reportId, insertedReportRef.get().getId());
    }

    @Test
    void createReport_shouldRejectWhenStepNotProcessing() {
        orderRef.set(new ErpProductionOrderDO().setId(10L).setStatus(ErpProductionOrderStatusEnum.RELEASED.getStatus()));
        stepsRef.set(List.of(new ErpProductionOrderStepDO()
                .setId(101L).setProductionOrderId(10L).setStepNo(10)
                .setStepStatus(ErpProductionOrderStepStatusEnum.WAIT.getStatus())));

        assertThrows(ServiceException.class, () -> reportService.createReport(buildReqVO()));
        assertEquals(0, casCallCount.get());
    }

    @Test
    void createReport_shouldRejectWhenStepMismatch() {
        orderRef.set(new ErpProductionOrderDO().setId(10L).setStatus(ErpProductionOrderStatusEnum.RELEASED.getStatus()));
        stepsRef.set(List.of(new ErpProductionOrderStepDO()
                .setId(999L).setProductionOrderId(10L).setStepNo(20)
                .setStepStatus(ErpProductionOrderStepStatusEnum.PROCESSING.getStatus())));

        assertThrows(ServiceException.class, () -> reportService.createReport(buildReqVO()));
        assertEquals(0, casCallCount.get());
    }

    @Test
    void createReport_shouldRejectWhenOrderNotReleased() {
        orderRef.set(new ErpProductionOrderDO().setId(10L).setStatus(ErpProductionOrderStatusEnum.CREATED.getStatus()));
        stepsRef.set(List.of(new ErpProductionOrderStepDO()
                .setId(101L).setProductionOrderId(10L).setStepNo(10)
                .setStepStatus(ErpProductionOrderStepStatusEnum.PROCESSING.getStatus())));

        assertThrows(ServiceException.class, () -> reportService.createReport(buildReqVO()));
        assertEquals(0, casCallCount.get());
    }

    @Test
    void createReport_shouldRejectWhenQtyMismatch() {
        orderRef.set(new ErpProductionOrderDO().setId(10L).setStatus(ErpProductionOrderStatusEnum.RELEASED.getStatus()));
        stepsRef.set(List.of(new ErpProductionOrderStepDO()
                .setId(101L).setProductionOrderId(10L).setStepNo(10)
                .setStepStatus(ErpProductionOrderStepStatusEnum.PROCESSING.getStatus())));
        ErpProductionReportCreateReqVO reqVO = buildReqVO();
        reqVO.getItems().get(0).setScrapQty(new BigDecimal("2"));

        assertThrows(ServiceException.class, () -> reportService.createReport(reqVO));
        assertEquals(0, casCallCount.get());
    }

    @Test
    void createReport_shouldRejectWhenReportTypeUnsupported() {
        orderRef.set(new ErpProductionOrderDO().setId(10L).setStatus(ErpProductionOrderStatusEnum.RELEASED.getStatus()));
        stepsRef.set(List.of(new ErpProductionOrderStepDO()
                .setId(101L).setProductionOrderId(10L).setStepNo(10)
                .setStepStatus(ErpProductionOrderStepStatusEnum.PROCESSING.getStatus())));
        ErpProductionReportCreateReqVO reqVO = buildReqVO();
        reqVO.setReportType(ErpProductionReportTypeEnum.FINISH.getType());

        assertThrows(ServiceException.class, () -> reportService.createReport(reqVO));
        assertEquals(0, casCallCount.get());
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
