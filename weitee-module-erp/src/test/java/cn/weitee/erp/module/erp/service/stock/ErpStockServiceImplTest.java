package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockMapper;
import cn.weitee.erp.module.erp.service.product.ErpProductQuantityPrecisionService;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpStockServiceImplTest {

    @Test
    void updateStockCountIncrement_shouldDeclareTransactionalBoundary() throws Exception {
        Method method = ErpStockServiceImpl.class.getMethod("updateStockCountIncrement",
                Long.class, Long.class, java.math.BigDecimal.class);
        assertTrue(method.isAnnotationPresent(Transactional.class),
                "updateStockCountIncrement should declare @Transactional");
    }

    @Test
    void updateStockCountIncrement_shouldRejectInvalidQuantityPrecisionBeforeWritingStock() throws Exception {
        ErpStockServiceImpl service = new ErpStockServiceImpl();
        AtomicBoolean insertCalled = new AtomicBoolean(false);
        AtomicBoolean updateCalled = new AtomicBoolean(false);

        setField(service, "productQuantityPrecisionService", (ErpProductQuantityPrecisionService) (productId, quantity) -> {
            throw new ServiceException(1_030_502_003, "产品【测试产品】数量最多允许 0 位小数");
        });
        setField(service, "erpStockMapper", createProxy(ErpStockMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertCalled.set(true);
            }
            if ("updateCountIncrement".equals(methodName)) {
                updateCalled.set(true);
            }
            if ("selectByProductIdAndWarehouseId".equals(methodName)) {
                return new ErpStockDO().setId(1L).setProductId(10L).setWarehouseId(20L)
                        .setCount(new BigDecimal("5.000"));
            }
            return null;
        }));

        assertThrows(ServiceException.class,
                () -> service.updateStockCountIncrement(10L, 20L, new BigDecimal("0.6")));
        assertTrue(!insertCalled.get(), "invalid precision should not insert stock");
        assertTrue(!updateCalled.get(), "invalid precision should not update stock count");
    }

    @Test
    void updateStockCountIncrement_shouldLockRowAndComputeWeightedAverageFromPreIncrementSnapshot() throws Exception {
        ErpStockServiceImpl service = new ErpStockServiceImpl();
        List<String> callOrder = new ArrayList<>();
        List<ErpStockDO> costUpdates = new ArrayList<>();

        setField(service, "productQuantityPrecisionService", (ErpProductQuantityPrecisionService) (productId, quantity) -> {
        });
        setField(service, "erpStockMapper", createProxy(ErpStockMapper.class, (methodName, args) -> {
            switch (methodName) {
                case "selectByProductIdAndWarehouseIdForUpdate" -> callOrder.add("lock");
                case "selectByProductIdAndWarehouseId" -> callOrder.add("plainRead");
                case "updateCountIncrement" -> callOrder.add("increment");
                case "updateById" -> {
                    callOrder.add("costUpdate");
                    costUpdates.add((ErpStockDO) args[0]);
                }
                default -> {
                }
            }
            if ("updateCountIncrement".equals(methodName) || "updateById".equals(methodName)) {
                return 1;
            }
            if ("selectByProductIdAndWarehouseIdForUpdate".equals(methodName)) {
                return new ErpStockDO().setId(1L).setProductId(10L).setWarehouseId(20L)
                        .setCount(new BigDecimal("100")).setAverageCost(new BigDecimal("10"))
                        .setTotalCost(new BigDecimal("1000"));
            }
            return null;
        }));

        BigDecimal newCount = service.updateStockCountIncrement(
                10L, 20L, new BigDecimal("100"), new BigDecimal("12"));

        assertEquals(0, new BigDecimal("200").compareTo(newCount));
        assertEquals(List.of("lock", "lock", "increment", "costUpdate"), callOrder,
                "必须先 FOR UPDATE 行锁读取（禁止无锁读），且加权平均成本必须在数量变更之前计算");
        assertEquals(1, costUpdates.size());
        // 加权平均 = (100*10 + 100*12) / 200 = 11；总金额 = 11 * 200 = 2200
        assertEquals(0, new BigDecimal("11").compareTo(costUpdates.get(0).getAverageCost()),
                "加权平均成本不得把本次入库量重复计入分母");
        assertEquals(0, new BigDecimal("2200.00").compareTo(costUpdates.get(0).getTotalCost()));
    }

    @Test
    void updateStockCountIncrement_shouldSyncTotalCostOnInboundWithoutPrice() throws Exception {
        ErpStockServiceImpl service = new ErpStockServiceImpl();
        List<ErpStockDO> costUpdates = new ArrayList<>();

        setField(service, "productQuantityPrecisionService", (ErpProductQuantityPrecisionService) (productId, quantity) -> {
        });
        setField(service, "erpStockMapper", createProxy(ErpStockMapper.class, (methodName, args) -> {
            if ("updateCountIncrement".equals(methodName) || "updateById".equals(methodName)) {
                if ("updateById".equals(methodName)) {
                    costUpdates.add((ErpStockDO) args[0]);
                }
                return 1;
            }
            if ("selectByProductIdAndWarehouseIdForUpdate".equals(methodName)) {
                return new ErpStockDO().setId(1L).setProductId(10L).setWarehouseId(20L)
                        .setCount(new BigDecimal("100")).setAverageCost(new BigDecimal("10"))
                        .setTotalCost(new BigDecimal("1000"));
            }
            return null;
        }));

        service.updateStockCountIncrement(10L, 20L, new BigDecimal("50"), null);

        assertEquals(1, costUpdates.size(), "无单价入库也必须同步总金额，维持 totalCost = averageCost * count 守恒");
        assertNull(costUpdates.get(0).getAverageCost(), "无单价入库不得改动均价");
        assertEquals(0, new BigDecimal("1500.00").compareTo(costUpdates.get(0).getTotalCost()));
    }

    @Test
    void updateStockCountIncrement_shouldRejectDecrementBeyondStockWithoutAnyWrite() throws Exception {
        ErpStockServiceImpl service = new ErpStockServiceImpl();
        AtomicBoolean incrementCalled = new AtomicBoolean(false);
        AtomicBoolean costUpdateCalled = new AtomicBoolean(false);

        setField(service, "productQuantityPrecisionService", (ErpProductQuantityPrecisionService) (productId, quantity) -> {
        });
        setField(service, "erpStockMapper", createProxy(ErpStockMapper.class, (methodName, args) -> {
            if ("updateCountIncrement".equals(methodName)) {
                incrementCalled.set(true);
                return 1;
            }
            if ("updateById".equals(methodName)) {
                costUpdateCalled.set(true);
                return 1;
            }
            if ("selectByProductIdAndWarehouseIdForUpdate".equals(methodName)) {
                return new ErpStockDO().setId(1L).setProductId(10L).setWarehouseId(20L)
                        .setCount(new BigDecimal("10")).setAverageCost(new BigDecimal("10"))
                        .setTotalCost(new BigDecimal("100"));
            }
            return null;
        }));
        setField(service, "productService", createProxy(ErpProductService.class, (methodName, args) ->
                "getProduct".equals(methodName) ? new ErpProductDO().setId(10L).setName("测试产品") : null));
        setField(service, "warehouseService", createProxy(ErpWarehouseService.class, (methodName, args) ->
                "getWarehouse".equals(methodName) ? new ErpWarehouseDO().setId(20L).setName("测试仓库") : null));

        assertThrows(ServiceException.class,
                () -> service.updateStockCountIncrement(10L, 20L, new BigDecimal("-20")));
        assertTrue(!incrementCalled.get(), "库存不足不得变更数量");
        assertTrue(!costUpdateCalled.get(), "库存不足不得变更成本");
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
