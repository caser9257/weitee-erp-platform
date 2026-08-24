package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderStepDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionReportItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionStepQualityDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionStepQualityMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.ErpQaStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpProductionStepQualityServiceImplTest {

    private final AtomicReference<ErpProductionStepQualityDO> qualityRef = new AtomicReference<>();
    private final List<ErpProductionStepQualityDO> insertedQualities = new java.util.ArrayList<>();
    private final AtomicReference<ErpProductionStepQualityDO> updatedQualityRef = new AtomicReference<>();

    private ErpProductionStepQualityServiceImpl qualityService;

    @BeforeEach
    void setUp() throws Exception {
        qualityService = new ErpProductionStepQualityServiceImpl();
        qualityRef.set(null);
        insertedQualities.clear();
        updatedQualityRef.set(null);

        setField(qualityService, "productionStepQualityMapper",
                createProxy(ErpProductionStepQualityMapper.class, (methodName, args) -> {
                    if ("selectById".equals(methodName)) {
                        return qualityRef.get();
                    }
                    if ("insert".equals(methodName)) {
                        insertedQualities.add((ErpProductionStepQualityDO) args[0]);
                        return 1;
                    }
                    if ("updateById".equals(methodName)) {
                        updatedQualityRef.set((ErpProductionStepQualityDO) args[0]);
                        return 1;
                    }
                    return null;
                }));
        setField(qualityService, "noRedisDAO", new ErpNoRedisDAO() {
            @Override
            public String generate(String prefix) {
                return "GXZJ202608120001";
            }
        });
    }

    @Test
    void createPendingFromReport_shouldCreateQualityOnlyWhenQcFlag() {
        List<ErpProductionOrderStepDO> steps = List.of(
                new ErpProductionOrderStepDO().setId(1L).setProductionOrderId(10L)
                        .setStepNo(10).setStepCode("OP-10").setStepName("下料").setQcFlag(Boolean.TRUE),
                new ErpProductionOrderStepDO().setId(2L).setProductionOrderId(10L)
                        .setStepNo(20).setStepCode("OP-20").setStepName("装配").setQcFlag(Boolean.FALSE)
        );
        List<ErpProductionReportItemDO> items = List.of(
                new ErpProductionReportItemDO().setReportId(100L).setProductionOrderStepId(1L)
                        .setReportedQty(new BigDecimal("8")),
                new ErpProductionReportItemDO().setReportId(100L).setProductionOrderStepId(2L)
                        .setReportedQty(new BigDecimal("6"))
        );

        qualityService.createPendingFromReport(100L, items, steps);

        assertEquals(1, insertedQualities.size());
        ErpProductionStepQualityDO q = insertedQualities.get(0);
        assertEquals(1L, q.getProductionOrderStepId());
        assertEquals(100L, q.getReportId());
        assertEquals("GXZJ202608120001", q.getNo());
        assertEquals(new BigDecimal("8"), q.getReportQty());
        assertEquals(ErpQaStatusEnum.TO_INSPECT.getStatus(), q.getStatus());
    }

    @Test
    void submitQuality_shouldPassWhenQualifiedEqualsReportQty() {
        qualityRef.set(new ErpProductionStepQualityDO()
                .setId(1L).setReportQty(new BigDecimal("8"))
                .setStatus(ErpQaStatusEnum.TO_INSPECT.getStatus()));

        qualityService.submitQuality(1L, 9L, new BigDecimal("8"), BigDecimal.ZERO, null);

        assertEquals(ErpQaStatusEnum.PASSED.getStatus(), updatedQualityRef.get().getStatus());
        assertEquals(9L, updatedQualityRef.get().getCheckerUserId());
    }

    @Test
    void submitQuality_shouldRejectWhenStatusNotPending() {
        qualityRef.set(new ErpProductionStepQualityDO()
                .setId(2L).setReportQty(new BigDecimal("8"))
                .setStatus(ErpQaStatusEnum.PASSED.getStatus()));

        assertThrows(ServiceException.class,
                () -> qualityService.submitQuality(2L, 9L, new BigDecimal("8"), BigDecimal.ZERO, null));
    }

    @Test
    void submitQuality_shouldRejectWhenCountMismatch() {
        qualityRef.set(new ErpProductionStepQualityDO()
                .setId(3L).setReportQty(new BigDecimal("8"))
                .setStatus(ErpQaStatusEnum.TO_INSPECT.getStatus()));

        assertThrows(ServiceException.class,
                () -> qualityService.submitQuality(3L, 9L, new BigDecimal("7"), BigDecimal.ZERO, null));
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
