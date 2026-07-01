package cn.weitee.erp.module.erp.service.purchase.approval;

import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseOrderService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PurchaseOrderResultHandlerTest {

    @Test
    void getSceneCode_shouldReturnCorrectCode() {
        PurchaseOrderResultHandler handler = new PurchaseOrderResultHandler();
        assertEquals("erp.purchase.order.submit", handler.getSceneCode());
    }

    @Test
    void onApprove_shouldUpdateStatusToApprove() throws Exception {
        PurchaseOrderResultHandler handler = new PurchaseOrderResultHandler();
        AtomicReference<Long> approvedId = new AtomicReference<>();
        AtomicReference<Integer> approvedStatus = new AtomicReference<>();

        injectField(handler, "purchaseOrderService", createServiceProxy(new ErpPurchaseOrderDO().setId(1L), approvedId, approvedStatus, null, null));

        handler.onApprove(1L, "PI-001", "approved");

        assertEquals(1L, approvedId.get());
        assertEquals(ErpAuditStatus.APPROVE.getStatus(), approvedStatus.get());
    }

    @Test
    void onApprove_shouldThrowWhenEntityNotExists() throws Exception {
        PurchaseOrderResultHandler handler = new PurchaseOrderResultHandler();
        injectField(handler, "purchaseOrderService", createServiceProxy(null, null, null, null, null));

        assertThrows(Exception.class, () -> handler.onApprove(999L, "PI-999", "not found"));
    }

    @Test
    void onReject_shouldUpdateStatusToReject() throws Exception {
        PurchaseOrderResultHandler handler = new PurchaseOrderResultHandler();
        AtomicReference<Long> rejectedId = new AtomicReference<>();
        AtomicReference<Integer> rejectedStatus = new AtomicReference<>();

        injectField(handler, "purchaseOrderService", createServiceProxy(new ErpPurchaseOrderDO().setId(2L), rejectedId, rejectedStatus, null, null));

        handler.onReject(2L, "PI-002", "rejected");

        assertEquals(2L, rejectedId.get());
        assertEquals(ErpAuditStatus.REJECT.getStatus(), rejectedStatus.get());
    }

    @Test
    void onCancel_shouldRollbackToDraftAndClearProcessInstanceId() throws Exception {
        PurchaseOrderResultHandler handler = new PurchaseOrderResultHandler();
        AtomicReference<Long> rollbackId = new AtomicReference<>();
        AtomicReference<String> bindingId = new AtomicReference<>();

        injectField(handler, "purchaseOrderService", createServiceProxy(new ErpPurchaseOrderDO().setId(3L), null, null, rollbackId, bindingId));

        handler.onCancel(3L, "PI-003", "cancel");

        assertEquals(3L, rollbackId.get());
        assertEquals("PI-003", bindingId.get());
    }

    private Object createServiceProxy(ErpPurchaseOrderDO entity, AtomicReference<Long> idRef,
                                      AtomicReference<Integer> statusRef, AtomicReference<Long> rollbackIdRef,
                                      AtomicReference<String> bindingIdRef) {
        return createProxy(ErpPurchaseOrderService.class, (methodName, args) -> {
            if ("getPurchaseOrder".equals(methodName)) {
                return entity;
            }
            if ("updatePurchaseOrderStatusByBpm".equals(methodName)) {
                if (idRef != null) idRef.set((Long) args[0]);
                if (statusRef != null) statusRef.set((Integer) args[2]);
            }
            if ("rollbackPurchaseOrderStatusToDraftByBpm".equals(methodName) && rollbackIdRef != null) {
                rollbackIdRef.set((Long) args[0]);
            }
            if ("rollbackPurchaseOrderStatusToDraftByBpm".equals(methodName) && bindingIdRef != null) {
                bindingIdRef.set((String) args[1]);
            }
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> type, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        if ("toString".equals(method.getName())) return type.getSimpleName() + "Proxy";
                        if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
                        if ("equals".equals(method.getName())) return proxy == args[0];
                    }
                    return handler.handle(method.getName(), args);
                });
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }
}
