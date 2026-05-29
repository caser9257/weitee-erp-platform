package cn.iocoder.yudao.module.erp.service.mrp.listener;

import cn.iocoder.yudao.module.erp.framework.event.ErpProductionQualityPassedEvent;
import cn.iocoder.yudao.module.erp.service.sale.ErpSaleOrderDeliveryReadyService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErpProductionQualityPassedListenerTest {

    @Test
    void onProductionQualityPassed_shouldRouteSourceOrderIdToRecalculate() throws Exception {
        ErpProductionQualityPassedListener listener = new ErpProductionQualityPassedListener();
        AtomicReference<Long> orderIdRef = new AtomicReference<>();
        setField(listener, "saleOrderDeliveryReadyService", createProxy(ErpSaleOrderDeliveryReadyService.class,
                (methodName, args) -> {
                    if ("recalculate".equals(methodName)) {
                        orderIdRef.set((Long) args[0]);
                    }
                    return null;
                }));

        listener.onProductionQualityPassed(new ErpProductionQualityPassedEvent(123L));

        assertEquals(123L, orderIdRef.get());
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
