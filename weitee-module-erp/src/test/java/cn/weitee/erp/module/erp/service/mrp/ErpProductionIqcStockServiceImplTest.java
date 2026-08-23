package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInQualityItemDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderMapper;
import cn.weitee.erp.module.erp.enums.ErpPurchaseInQualityResultEnum;
import cn.weitee.erp.module.erp.enums.ErpPurchaseInQualityStatusEnum;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseInQualityService;
import cn.weitee.erp.module.erp.service.stock.ErpStockRecordService;
import cn.weitee.erp.module.erp.service.stock.ErpStockService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ErpProductionIqcStockServiceImplTest {

    @Test
    void deductStockForProduction_shouldCallStockDeductWithNegativeQty() throws Exception {
        Object service = new ErpProductionIqcStockServiceImpl();
        ErpProductionOrderDO order = new ErpProductionOrderDO()
                .setId(100L).setOrderNo("PO-001").setWarehouseId(10L).setStatus(20);
        AtomicReference<BigDecimal> capturedAvailable = new AtomicReference<>();
        AtomicReference<BigDecimal> capturedCount = new AtomicReference<>();
        cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO stock = new cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO()
                .setId(1L).setProductId(200L).setWarehouseId(10L).setCount(new BigDecimal("100")).setAvailableCount(new BigDecimal("100")).setQualityHoldCount(BigDecimal.ZERO);
        setField(service, "productionOrderMapper", createProxy(ErpProductionOrderMapper.class, (m, a) -> {
            if ("selectById".equals(m)) return order;
            return null;
        }));
        setField(service, "stockService", createProxy(ErpStockService.class, (m, a) -> {
            if ("updateStockCountIncrement".equals(m)) {
                capturedCount.set((BigDecimal) a[2]);
                return new BigDecimal("90");
            }
            return null;
        }));
        setField(service, "stockMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockMapper.class, (m, a) -> {
            if ("selectByProductIdAndWarehouseId".equals(m)) return stock;
            if ("updateAvailableCountIncrement".equals(m)) {
                capturedAvailable.set((BigDecimal) a[1]);
                return 1;
            }
            if ("updateCountIncrement".equals(m)) {
                capturedCount.set((BigDecimal) a[1]);
                return 1;
            }
            return null;
        }));
        setField(service, "stockRecordService", createProxy(ErpStockRecordService.class, (m, a) -> 1));
        setField(service, "purchaseInQualityService", createProxy(ErpPurchaseInQualityService.class, (m, a) -> null));

        Method method = service.getClass().getMethod("deductStockForProduction", Long.class, Long.class, BigDecimal.class);
        method.invoke(service, 100L, 200L, new BigDecimal("10"));

        assertNotNull(capturedAvailable.get());
        assertEquals(0, new BigDecimal("-10").compareTo(capturedAvailable.get()));
        assertNotNull(capturedCount.get());
    }

    @Test
    void handleIqcPassed_shouldAddStockForPassedItems() throws Exception {
        Object service = new ErpProductionIqcStockServiceImpl();
        ErpPurchaseInQualityDO quality = new ErpPurchaseInQualityDO()
                .setId(1L).setPurchaseInId(10L).setStatus(ErpPurchaseInQualityStatusEnum.DONE.getStatus())
                .setResult(ErpPurchaseInQualityResultEnum.PASSED.getStatus())
                .setNo("QC-001").setPurchaseInNo("PI-001");
        ErpPurchaseInQualityItemDO item = new ErpPurchaseInQualityItemDO()
                .setId(11L).setProductId(200L).setWarehouseId(10L).setQaPassCount(new BigDecimal("5"))
                .setPurchaseInItemId(100L);
        AtomicReference<BigDecimal> capturedAdd = new AtomicReference<>();
        cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO stock = new cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO()
                .setId(1L).setProductId(200L).setWarehouseId(10L).setCount(new BigDecimal("100")).setAvailableCount(new BigDecimal("90")).setQualityHoldCount(new BigDecimal("10"));
        setField(service, "productionOrderMapper", createProxy(ErpProductionOrderMapper.class, (m, a) -> null));
        setField(service, "purchaseInQualityService", createProxy(ErpPurchaseInQualityService.class, (m, a) -> {
            if ("getPurchaseInQualityByPurchaseInId".equals(m)) return quality;
            if ("getPurchaseInQualityItemListByQualityId".equals(m)) return List.of(item);
            return null;
        }));
        setField(service, "stockService", createProxy(ErpStockService.class, (m, a) -> {
            if ("updateStockCountIncrement".equals(m)) {
                capturedAdd.set((BigDecimal) a[2]);
                return new BigDecimal("105");
            }
            return null;
        }));
        setField(service, "stockMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockMapper.class, (m, a) -> {
            if ("selectByProductIdAndWarehouseId".equals(m)) return stock;
            if ("updateQualityHoldCountIncrement".equals(m)) {
                capturedAdd.set((BigDecimal) a[1]);
                return 1;
            }
            if ("updateAvailableCountIncrement".equals(m)) return 1;
            if ("updateCountIncrement".equals(m)) return 1;
            return null;
        }));
        setField(service, "stockRecordService", createProxy(ErpStockRecordService.class, (m, a) -> 1));

        Method method = service.getClass().getMethod("handleIqcPassed", Long.class);
        method.invoke(service, 10L);

        assertNotNull(capturedAdd.get());
    }

    @SuppressWarnings("unchecked")
    private static <T> T createProxy(Class<T> iface, ProxyHandler handler) {
        return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[]{iface},
                (proxy, method, args) -> handler.handle(method.getName(), args));
    }

    @FunctionalInterface
    interface ProxyHandler {
        Object handle(String methodName, Object[] args) throws Throwable;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        Class<?> cur = clazz;
        while (cur != null) {
            try {
                return cur.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                cur = cur.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }
}
