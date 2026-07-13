package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionFinishQualityDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionFinishQualityMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.ErpQaStatusEnum;
import cn.weitee.erp.module.erp.framework.event.ErpProductionQualityPassedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpProductionFinishQualityServiceImplTest {

    private final AtomicReference<ErpProductionFinishQualityDO> insertedQualityRef = new AtomicReference<>();
    private final AtomicReference<ErpProductionFinishQualityDO> selectedQualityRef = new AtomicReference<>();
    private final AtomicReference<ErpProductionFinishQualityDO> updatedQualityRef = new AtomicReference<>();
    private final List<Object> publishedEvents = new ArrayList<>();
    private final AtomicInteger productionInboundCreateCallCount = new AtomicInteger();

    private ErpProductionFinishQualityServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new ErpProductionFinishQualityServiceImpl();
        insertedQualityRef.set(null);
        selectedQualityRef.set(null);
        updatedQualityRef.set(null);
        publishedEvents.clear();
        productionInboundCreateCallCount.set(0);

        setField(service, "erpProductionFinishQualityMapper", createProxy(ErpProductionFinishQualityMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertedQualityRef.set((ErpProductionFinishQualityDO) args[0]);
                return 1;
            }
            if ("selectById".equals(methodName)) {
                return selectedQualityRef.get();
            }
            if ("selectByProductionOrderId".equals(methodName)) {
                return null;
            }
            if ("updateById".equals(methodName)) {
                updatedQualityRef.set((ErpProductionFinishQualityDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "noRedisDAO", new ErpNoRedisDAO() {
            @Override
            public String generate(String prefix) {
                return "CPZJ202604140001";
            }
        });
        setField(service, "eventPublisher", createProxy(ApplicationEventPublisher.class, (methodName, args) -> {
            if ("publishEvent".equals(methodName)) {
                publishedEvents.add(args[0]);
            }
            return null;
        }));
        injectProductionInboundServiceProxy();
    }

    @Test
    void createPendingQualityAfterFinish_shouldInsertPendingRecord() {
        ErpProductionOrderDO order = new ErpProductionOrderDO()
                .setId(10L)
                .setOrderNo("SCGD001")
                .setProductId(20L)
                .setSourceOrderId(30L)
                .setSourceItemId(40L);

        service.createPendingQualityAfterFinish(order, new BigDecimal("15"));

        assertEquals(10L, insertedQualityRef.get().getProductionOrderId());
        assertEquals(30L, insertedQualityRef.get().getSourceOrderId());
        assertEquals(40L, insertedQualityRef.get().getSourceItemId());
        assertEquals(ErpQaStatusEnum.TO_INSPECT.getStatus(), insertedQualityRef.get().getStatus());
    }

    @Test
    void submitQuality_shouldRejectWhenQualifiedAndUnqualifiedDoNotMatchReportQty() {
        selectedQualityRef.set(new ErpProductionFinishQualityDO()
                .setId(1L)
                .setReportQty(new BigDecimal("10"))
                .setSourceOrderId(30L)
                .setStatus(ErpQaStatusEnum.TO_INSPECT.getStatus()));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.submitQuality(1L, 2L, new BigDecimal("7"), new BigDecimal("1"), "mismatch"));

        assertEquals(1_030_700_016, ex.getCode());
    }

    @Test
    void submitQuality_shouldRejectNegativeQuantityEvenWhenTotalMatchesReportQty() {
        selectedQualityRef.set(new ErpProductionFinishQualityDO()
                .setId(4L)
                .setReportQty(new BigDecimal("10"))
                .setStatus(ErpQaStatusEnum.TO_INSPECT.getStatus()));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.submitQuality(4L, 2L, new BigDecimal("11"), new BigDecimal("-1"), "invalid"));

        assertEquals(1_030_700_016, ex.getCode());
        assertEquals(null, updatedQualityRef.get());
    }

    @Test
    void submitQuality_shouldMarkPartialAndPublishEvent() {
        selectedQualityRef.set(new ErpProductionFinishQualityDO()
                .setId(1L)
                .setProductionOrderId(11L)
                .setReportQty(new BigDecimal("10"))
                .setSourceOrderId(30L)
                .setStatus(ErpQaStatusEnum.TO_INSPECT.getStatus()));

        service.submitQuality(1L, 2L, new BigDecimal("6"), new BigDecimal("4"), "partial release");

        assertEquals(ErpQaStatusEnum.PARTIAL.getStatus(), updatedQualityRef.get().getStatus());
        assertEquals(1, productionInboundCreateCallCount.get());
        assertEquals(1, publishedEvents.size());
        ErpProductionQualityPassedEvent event = assertInstanceOf(ErpProductionQualityPassedEvent.class, publishedEvents.get(0));
        assertEquals(30L, event.getSourceOrderId());
    }

    @Test
    void submitQuality_shouldCreateProductionInboundWhenPassed() {
        selectedQualityRef.set(new ErpProductionFinishQualityDO()
                .setId(2L)
                .setProductionOrderId(12L)
                .setReportQty(new BigDecimal("8"))
                .setSourceOrderId(31L)
                .setStatus(ErpQaStatusEnum.TO_INSPECT.getStatus()));

        service.submitQuality(2L, 3L, new BigDecimal("8"), BigDecimal.ZERO, "all passed");

        assertEquals(ErpQaStatusEnum.PASSED.getStatus(), updatedQualityRef.get().getStatus());
        assertEquals(1, productionInboundCreateCallCount.get());
    }

    @Test
    void submitQuality_shouldNotCreateProductionInboundWhenRejected() {
        selectedQualityRef.set(new ErpProductionFinishQualityDO()
                .setId(3L)
                .setProductionOrderId(13L)
                .setReportQty(new BigDecimal("5"))
                .setSourceOrderId(32L)
                .setStatus(ErpQaStatusEnum.TO_INSPECT.getStatus()));

        service.submitQuality(3L, 4L, BigDecimal.ZERO, new BigDecimal("5"), "rejected");

        assertEquals(ErpQaStatusEnum.REJECTED.getStatus(), updatedQualityRef.get().getStatus());
        assertEquals(0, productionInboundCreateCallCount.get());
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

    private void injectProductionInboundServiceProxy() {
        try {
            setField(service, "productionInboundService", createProxyByName(
                    "cn.weitee.erp.module.erp.service.mrp.ErpProductionInboundService", (methodName, args) -> {
                        if ("createProductionInboundFromQuality".equals(methodName)) {
                            productionInboundCreateCallCount.incrementAndGet();
                            return 100L;
                        }
                        return null;
                    }));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxyByName(String className, MethodHandler handler) {
        try {
            Class<T> type = (Class<T>) Class.forName(className);
            return createProxy(type, handler);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }

}
