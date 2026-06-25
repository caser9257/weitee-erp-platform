package cn.weitee.erp.module.erp.service.sale;

import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionFinishQualityMapper;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.weitee.erp.module.erp.enums.sale.ErpSaleOrderDeliveryReadyStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ErpSaleOrderDeliveryReadyServiceImplTest {

    private final AtomicReference<ErpSaleOrderDO> saleOrderRef = new AtomicReference<>();
    private final AtomicReference<BigDecimal> qualifiedQtyRef = new AtomicReference<>(BigDecimal.ZERO);
    private final AtomicReference<ErpSaleOrderDO> updatedOrderRef = new AtomicReference<>();
    private final AtomicInteger updateCallCount = new AtomicInteger();

    private ErpSaleOrderDeliveryReadyServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new ErpSaleOrderDeliveryReadyServiceImpl();
        saleOrderRef.set(null);
        qualifiedQtyRef.set(BigDecimal.ZERO);
        updatedOrderRef.set(null);
        updateCallCount.set(0);
        setField(service, "saleOrderMapper", createProxy(ErpSaleOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return saleOrderRef.get();
            }
            if ("updateById".equals(methodName)) {
                updatedOrderRef.set((ErpSaleOrderDO) args[0]);
                updateCallCount.incrementAndGet();
                return 1;
            }
            return null;
        }));
        setField(service, "productionFinishQualityMapper", createProxy(ErpProductionFinishQualityMapper.class, (methodName, args) -> {
            if ("sumQualifiedQtyBySourceOrderId".equals(methodName)) {
                return qualifiedQtyRef.get();
            }
            return null;
        }));
    }

    @Test
    void recalculate_shouldSetNotReadyWhenQualifiedQtyIsZero() {
        saleOrderRef.set(buildOrder().setDeliveryReadyStatus(ErpSaleOrderDeliveryReadyStatusEnum.PART_READY.getStatus()));
        qualifiedQtyRef.set(BigDecimal.ZERO);

        service.recalculate(1L);

        assertEquals(ErpSaleOrderDeliveryReadyStatusEnum.NOT_READY.getStatus(), updatedOrderRef.get().getDeliveryReadyStatus());
    }

    @Test
    void recalculate_shouldSetPartReadyWhenQualifiedQtyIsLessThanRemainingShipQty() {
        saleOrderRef.set(buildOrder());
        qualifiedQtyRef.set(new BigDecimal("6"));

        service.recalculate(1L);

        assertEquals(ErpSaleOrderDeliveryReadyStatusEnum.PART_READY.getStatus(), updatedOrderRef.get().getDeliveryReadyStatus());
    }

    @Test
    void recalculate_shouldSetReadyToShipWhenQualifiedQtyCoversRemainingShipQty() {
        saleOrderRef.set(buildOrder().setDeliveryReadyStatus(ErpSaleOrderDeliveryReadyStatusEnum.PART_READY.getStatus()));
        qualifiedQtyRef.set(new BigDecimal("10"));

        service.recalculate(1L);

        assertEquals(ErpSaleOrderDeliveryReadyStatusEnum.READY_TO_SHIP.getStatus(), updatedOrderRef.get().getDeliveryReadyStatus());
    }

    @Test
    void recalculate_shouldSetNotReadyWhenRemainingShipQtyIsZero() {
        saleOrderRef.set(buildOrder()
                .setOutCount(new BigDecimal("10"))
                .setDeliveryReadyStatus(ErpSaleOrderDeliveryReadyStatusEnum.READY_TO_SHIP.getStatus()));
        qualifiedQtyRef.set(new BigDecimal("10"));

        service.recalculate(1L);

        assertEquals(ErpSaleOrderDeliveryReadyStatusEnum.NOT_READY.getStatus(), updatedOrderRef.get().getDeliveryReadyStatus());
    }

    @Test
    void recalculate_shouldSkipUpdateWhenStatusUnchanged() {
        saleOrderRef.set(buildOrder());
        qualifiedQtyRef.set(BigDecimal.ZERO);

        service.recalculate(1L);

        assertEquals(0, updateCallCount.get());
        assertNull(updatedOrderRef.get());
    }

    private ErpSaleOrderDO buildOrder() {
        return new ErpSaleOrderDO()
                .setId(1L)
                .setTotalCount(new BigDecimal("10"))
                .setOutCount(BigDecimal.ZERO)
                .setReturnCount(BigDecimal.ZERO)
                .setDeliveryReadyStatus(ErpSaleOrderDeliveryReadyStatusEnum.NOT_READY.getStatus());
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
