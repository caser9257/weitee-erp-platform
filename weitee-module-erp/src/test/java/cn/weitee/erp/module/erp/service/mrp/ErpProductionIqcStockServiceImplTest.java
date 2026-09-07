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
    void deductStockForProduction_shouldDeductAvailableOnlyWithNegativeQty() throws Exception {
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
        setField(service, "stockMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockMapper.class, (m, a) -> {
            if ("selectByProductIdAndWarehouseIdForUpdate".equals(m)) return stock;
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
        // 行锁新事务：桩事务管理器，直接放行
        setField(service, "transactionManager", createProxy(org.springframework.transaction.PlatformTransactionManager.class,
                (m, a) -> null));
        setField(service, "stockRecordService", createProxy(ErpStockRecordService.class, (m, a) -> 1));
        setField(service, "purchaseInQualityService", createProxy(ErpPurchaseInQualityService.class, (m, a) -> null));

        Method method = service.getClass().getMethod("deductStockForProduction", Long.class, Long.class, Long.class, BigDecimal.class);
        method.invoke(service, 100L, 200L, 10L, new BigDecimal("10"));

        assertNotNull(capturedAvailable.get());
        assertEquals(0, new BigDecimal("-10").compareTo(capturedAvailable.get()));
        // 分工约定：count 由发料主流程流水机制维护，本服务不得再扣 count
        assertNull(capturedCount.get());
    }

    @Test
    void handleIqcPassed_shouldNotWriteStockSinceConfirmIsTheSingleWriter() throws Exception {
        Object service = new ErpProductionIqcStockServiceImpl();
        AtomicReference<BigDecimal> capturedHold = new AtomicReference<>();
        AtomicReference<BigDecimal> capturedAvailable = new AtomicReference<>();
        AtomicReference<BigDecimal> capturedCount = new AtomicReference<>();
        setField(service, "purchaseInQualityService", createProxy(ErpPurchaseInQualityService.class, (m, a) -> null));
        setField(service, "stockMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockMapper.class, (m, a) -> {
            if ("updateQualityHoldCountIncrement".equals(m)) {
                capturedHold.set((BigDecimal) a[1]);
                return 1;
            }
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
        setField(service, "transactionManager", createProxy(org.springframework.transaction.PlatformTransactionManager.class,
                (m, a) -> null));
        setField(service, "stockRecordService", createProxy(ErpStockRecordService.class, (m, a) -> 1));

        Method method = service.getClass().getMethod("handleIqcPassed", Long.class);
        method.invoke(service, 10L);

        // 单写者契约：可用库存由确认入库统一入账，IQC 完成事件不得再写库存
        assertNull(capturedHold.get());
        assertNull(capturedAvailable.get());
        assertNull(capturedCount.get());
    }

    @Test
    void deductStockForProduction_shouldSaveFailureLogWhenWarehouseMissing() throws Exception {
        Object service = new ErpProductionIqcStockServiceImpl();
        ErpProductionOrderDO order = new ErpProductionOrderDO()
                .setId(100L).setOrderNo("PO-001").setWarehouseId(null).setStatus(20);
        AtomicReference<BigDecimal> capturedAvailable = new AtomicReference<>();
        AtomicReference<Object> capturedFailure = new AtomicReference<>();
        setField(service, "productionOrderMapper", createProxy(ErpProductionOrderMapper.class, (m, a) -> {
            if ("selectById".equals(m)) return order;
            return null;
        }));
        setField(service, "stockMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockMapper.class, (m, a) -> {
            if ("updateAvailableCountIncrement".equals(m)) {
                capturedAvailable.set((BigDecimal) a[1]);
                return 1;
            }
            return null;
        }));
        setField(service, "transactionManager", createProxy(org.springframework.transaction.PlatformTransactionManager.class,
                (m, a) -> null));
        setField(service, "stockTaskFailureLogMapper", createProxy(
                cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockTaskFailureLogMapper.class, (m, a) -> {
                    if ("insert".equals(m)) {
                        capturedFailure.set(a[0]);
                        return 1;
                    }
                    return null;
                }));

        Method method = service.getClass().getMethod("deductStockForProduction", Long.class, Long.class, Long.class, BigDecimal.class);
        // 校验型失败不得抛出穿透 afterCommit 调用链，必须落失败记录待重试
        assertDoesNotThrow(() -> method.invoke(service, 100L, 200L, null, new BigDecimal("10")));
        assertNull(capturedAvailable.get());
        assertNotNull(capturedFailure.get());
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
