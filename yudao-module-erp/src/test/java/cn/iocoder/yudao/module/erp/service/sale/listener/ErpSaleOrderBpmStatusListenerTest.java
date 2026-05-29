package cn.iocoder.yudao.module.erp.service.sale.listener;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ErpSaleOrderBpmStatusListenerTest {

    @Test
    void onApplicationEvent_shouldRouteMatchingSaleOrderEvent() throws Exception {
        Object listener = instantiateListener();
        AtomicReference<List<Object>> argsRef = new AtomicReference<>();
        setField(listener, "saleOrderBpmService", createProxyByName("cn.iocoder.yudao.module.erp.service.sale.ErpSaleOrderBpmService",
                (methodName, args) -> {
            if ("handleProcessInstanceResult".equals(methodName)) {
                argsRef.set(List.of(args));
            }
            return null;
        }));

        Object event = createEvent("PI-20260407-101", "101", "审批通过", bpmStatus("APPROVE"), "erp_sale_order");

        listener.getClass().getMethod("onApplicationEvent", Class.forName("cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent"))
                .invoke(listener, event);

        assertEquals(101L, argsRef.get().get(0));
        assertEquals("PI-20260407-101", argsRef.get().get(1));
        assertEquals(bpmStatus("APPROVE"), argsRef.get().get(2));
        assertEquals("审批通过", argsRef.get().get(3));
    }

    @Test
    void onApplicationEvent_shouldIgnoreOtherProcessDefinition() throws Exception {
        Object listener = instantiateListener();
        AtomicReference<List<Object>> argsRef = new AtomicReference<>();
        setField(listener, "saleOrderBpmService", createProxyByName("cn.iocoder.yudao.module.erp.service.sale.ErpSaleOrderBpmService",
                (methodName, args) -> {
            if ("handleProcessInstanceResult".equals(methodName)) {
                argsRef.set(List.of(args));
            }
            return null;
        }));

        Object event = createEvent("PI-OTHER", "101", "ignored", bpmStatus("REJECT"), "oa_leave");

        listener.getClass().getMethod("onApplicationEvent", Class.forName("cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent"))
                .invoke(listener, event);

        assertNull(argsRef.get());
    }

    private Object instantiateListener() throws Exception {
        Class<?> clazz = Class.forName("cn.iocoder.yudao.module.erp.service.sale.listener.ErpSaleOrderBpmStatusListener");
        return clazz.getDeclaredConstructor().newInstance();
    }

    private Object createEvent(String id, String businessKey, String reason, Integer status, String processDefinitionKey)
            throws Exception {
        Class<?> clazz = Class.forName("cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent");
        Object event = clazz.getConstructor(Object.class).newInstance(this);
        clazz.getMethod("setId", String.class).invoke(event, id);
        clazz.getMethod("setBusinessKey", String.class).invoke(event, businessKey);
        clazz.getMethod("setReason", String.class).invoke(event, reason);
        clazz.getMethod("setStatus", Integer.class).invoke(event, status);
        clazz.getMethod("setProcessDefinitionKey", String.class).invoke(event, processDefinitionKey);
        return event;
    }

    private Object createProxyByName(String className, MethodHandler handler) throws Exception {
        return createProxy(Class.forName(className), handler);
    }

    private Integer bpmStatus(String enumName) throws Exception {
        Class<?> clazz = Class.forName("cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum");
        Object enumObj = Enum.valueOf((Class<Enum>) clazz.asSubclass(Enum.class), enumName);
        return (Integer) clazz.getMethod("getStatus").invoke(enumObj);
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
