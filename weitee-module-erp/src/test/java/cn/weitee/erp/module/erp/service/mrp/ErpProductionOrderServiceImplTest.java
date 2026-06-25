package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionMaterialDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionSuggestDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionMaterialMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionOrderStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpProductionOrderServiceImplTest {

    private final AtomicReference<ErpProductionOrderDO> insertedOrderRef = new AtomicReference<>();
    private final AtomicReference<ErpProductionOrderDO> selectedOrderRef = new AtomicReference<>();
    private final AtomicReference<ErpBomDO> bomRef = new AtomicReference<>();
    private final AtomicReference<List<ErpBomItemDO>> bomItemsRef = new AtomicReference<>(Collections.emptyList());
    private final List<ErpProductionMaterialDO> insertedMaterials = new ArrayList<>();
    private final AtomicReference<ErpProductionOrderDO> updatedOrderRef = new AtomicReference<>();
    private final AtomicReference<ErpProductionOrderDO> qualityOrderRef = new AtomicReference<>();
    private final AtomicReference<BigDecimal> qualityReportQtyRef = new AtomicReference<>();
    private final AtomicInteger stockIncrementCallCount = new AtomicInteger();

    private ErpProductionOrderServiceImpl productionOrderService;

    @BeforeEach
    void setUp() throws Exception {
        productionOrderService = new ErpProductionOrderServiceImpl();
        insertedOrderRef.set(null);
        selectedOrderRef.set(null);
        bomRef.set(null);
        bomItemsRef.set(Collections.emptyList());
        insertedMaterials.clear();
        updatedOrderRef.set(null);
        qualityOrderRef.set(null);
        qualityReportQtyRef.set(null);
        stockIncrementCallCount.set(0);

        setField(productionOrderService, "erpProductionOrderMapper", createProxy(ErpProductionOrderMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertedOrderRef.set((ErpProductionOrderDO) args[0]);
                return 1;
            }
            if ("selectById".equals(methodName)) {
                return selectedOrderRef.get();
            }
            if ("updateById".equals(methodName)) {
                updatedOrderRef.set((ErpProductionOrderDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(productionOrderService, "erpBomMapper", createProxy(ErpBomMapper.class, (methodName, args) -> {
            if ("selectEffectiveByProductId".equals(methodName)) {
                return bomRef.get();
            }
            return null;
        }));
        setField(productionOrderService, "erpBomItemMapper", createProxy(ErpBomItemMapper.class, (methodName, args) -> {
            if ("selectListByBomId".equals(methodName)) {
                return bomItemsRef.get();
            }
            return null;
        }));
        setField(productionOrderService, "erpProductionMaterialMapper", createProxy(ErpProductionMaterialMapper.class, (methodName, args) -> {
            if ("insertBatch".equals(methodName)) {
                insertedMaterials.addAll((List<ErpProductionMaterialDO>) args[0]);
                return true;
            }
            return null;
        }));
        setField(productionOrderService, "noRedisDAO", new ErpNoRedisDAO() {
            @Override
            public String generate(String prefix) {
                return "SC202604080001";
            }
        });
        setField(productionOrderService, "productionFinishQualityService",
                createProxy(ErpProductionFinishQualityService.class, (methodName, args) -> {
                    if ("createPendingQualityAfterFinish".equals(methodName)) {
                        qualityOrderRef.set((ErpProductionOrderDO) args[0]);
                        qualityReportQtyRef.set((BigDecimal) args[1]);
                    }
                    return null;
                }));
    }

    @Test
    void createProductionOrderBySuggest_shouldCarryProjectIdFromSuggest() {
        ErpProductionSuggestDO suggest = new ErpProductionSuggestDO()
                .setId(100L)
                .setProjectId(2000L)
                .setSourceOrderId(88L)
                .setSourceItemId(99L)
                .setProductId(3000L)
                .setSuggestQty(new BigDecimal("7"))
                .setSuggestStartDate(LocalDate.of(2026, 4, 8))
                .setSuggestEndDate(LocalDate.of(2026, 4, 12));

        productionOrderService.createProductionOrderBySuggest(suggest, "mrp convert");

        assertEquals(2000L, insertedOrderRef.get().getProjectId());
        assertEquals("MRP_SUGGEST", insertedOrderRef.get().getSourceType());
        assertEquals(100L, insertedOrderRef.get().getSourceId());
        assertEquals(88L, insertedOrderRef.get().getSourceOrderId());
        assertEquals(99L, insertedOrderRef.get().getSourceItemId());
    }

    @Test
    void releaseProductionOrder_shouldCreateProductionMaterialSnapshot() {
        selectedOrderRef.set(new ErpProductionOrderDO()
                .setId(11L)
                .setProductId(100L)
                .setPlanQty(new BigDecimal("10"))
                .setStatus(ErpProductionOrderStatusEnum.CREATED.getStatus()));
        bomRef.set(new ErpBomDO().setId(21L).setProductId(100L).setStatus(1));
        bomItemsRef.set(List.of(
                new ErpBomItemDO().setId(31L).setMaterialId(1001L).setUsageQty(new BigDecimal("2")).setLossRate(BigDecimal.ZERO),
                new ErpBomItemDO().setId(32L).setMaterialId(1002L).setUsageQty(new BigDecimal("1.5")).setLossRate(new BigDecimal("0.10"))
        ));

        productionOrderService.releaseProductionOrder(11L);

        assertEquals(2, insertedMaterials.size());
        assertEquals(new BigDecimal("20.000000"), insertedMaterials.get(0).getRequiredQty());
        assertEquals(new BigDecimal("16.500000"), insertedMaterials.get(1).getRequiredQty());
        assertEquals(ErpProductionOrderStatusEnum.RELEASED.getStatus(), updatedOrderRef.get().getStatus());
    }

    @Test
    void releaseProductionOrder_shouldRejectWhenEffectiveBomMissing() {
        selectedOrderRef.set(new ErpProductionOrderDO()
                .setId(12L)
                .setProductId(101L)
                .setPlanQty(new BigDecimal("5"))
                .setStatus(ErpProductionOrderStatusEnum.CREATED.getStatus()));
        bomRef.set(null);

        assertThrows(ServiceException.class, () -> productionOrderService.releaseProductionOrder(12L));
        assertEquals(0, insertedMaterials.size());
    }

    @Test
    void finishProductionOrder_shouldCreatePendingQualityWithoutIncreasingStock() {
        selectedOrderRef.set(new ErpProductionOrderDO()
                .setId(13L)
                .setProductId(501L)
                .setStatus(ErpProductionOrderStatusEnum.RELEASED.getStatus())
                .setSourceOrderId(600L)
                .setSourceItemId(700L));
        setFieldQuietly("warehouseService", createProxyByName(
                "cn.weitee.erp.module.erp.service.stock.ErpWarehouseService", (methodName, args) -> null));
        setFieldQuietly("stockService", createProxyByName(
                "cn.weitee.erp.module.erp.service.stock.ErpStockService", (methodName, args) -> {
                    if ("updateStockCountIncrement".equals(methodName)) {
                        stockIncrementCallCount.incrementAndGet();
                    }
                    return null;
                }));

        productionOrderService.finishProductionOrder(new cn.weitee.erp.module.erp.controller.admin.mrp.vo.production.ErpProductionOrderFinishReqVO()
                .setId(13L)
                .setWarehouseId(11L)
                .setFinishedQty(new BigDecimal("12")));

        assertEquals(0, stockIncrementCallCount.get());
        assertEquals(ErpProductionOrderStatusEnum.FINISHED.getStatus(), updatedOrderRef.get().getStatus());
        assertEquals(new BigDecimal("12"), updatedOrderRef.get().getFinishedQty());
        assertEquals(11L, updatedOrderRef.get().getWarehouseId());
        assertEquals(13L, qualityOrderRef.get().getId());
        assertEquals(new BigDecimal("12"), qualityReportQtyRef.get());
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

    @SuppressWarnings("unchecked")
    private <T> T createProxyByName(String className, MethodHandler handler) {
        try {
            Class<T> type = (Class<T>) Class.forName(className);
            return createProxy(type, handler);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void setFieldQuietly(String fieldName, Object value) {
        try {
            setField(productionOrderService, fieldName, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }

}
