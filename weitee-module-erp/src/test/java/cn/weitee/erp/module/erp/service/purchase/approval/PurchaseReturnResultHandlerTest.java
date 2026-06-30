package cn.weitee.erp.module.erp.service.purchase.approval;

import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseReturnMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseReturnService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PurchaseReturnResultHandlerTest {

    @Test
    void getSceneCode_shouldReturnCorrectCode() throws Exception {
        PurchaseReturnResultHandler handler = new PurchaseReturnResultHandler();
        assertEquals("erp.purchase.return.submit", handler.getSceneCode());
    }

    @Test
    void onApprove_shouldUpdateStatusToApprove() throws Exception {
        PurchaseReturnResultHandler handler = new PurchaseReturnResultHandler();
        AtomicReference<Long> approvedId = new AtomicReference<>();
        AtomicReference<Integer> approvedStatus = new AtomicReference<>();

        injectField(handler, "purchaseReturnMapper", createMapperProxy(new ErpPurchaseReturnDO().setId(1L)));
        injectField(handler, "purchaseReturnService", createServiceProxy(approvedId, approvedStatus));

        handler.onApprove(1L, "PI-001", "approved");

        assertEquals(1L, approvedId.get());
        assertEquals(ErpAuditStatus.APPROVE.getStatus(), approvedStatus.get());
    }

    @Test
    void onApprove_shouldThrowWhenEntityNotExists() throws Exception {
        PurchaseReturnResultHandler handler = new PurchaseReturnResultHandler();
        injectField(handler, "purchaseReturnMapper", createMapperProxy(null)); // null = not found
        injectField(handler, "purchaseReturnService", createServiceProxy(null, null));

        assertThrows(Exception.class, () -> handler.onApprove(999L, "PI-999", "not found"));
    }

    @Test
    void onReject_shouldSetDraftAndClearProcessInstanceId() throws Exception {
        PurchaseReturnResultHandler handler = new PurchaseReturnResultHandler();
        AtomicReference<ErpPurchaseReturnDO> updatedEntity = new AtomicReference<>();
        AtomicReference<Long> clearedId = new AtomicReference<>();

        injectField(handler, "purchaseReturnMapper", createMapperProxyForCancel(updatedEntity, clearedId));
        injectField(handler, "purchaseReturnService", createServiceProxy(null, null));

        handler.onReject(2L, "PI-002", "rejected");

        // Verify status set to DRAFT (same as onCancel — no stock/finance side effects)
        assertEquals(ErpAuditStatus.DRAFT.getStatus(), updatedEntity.get().getStatus());
        assertEquals(2L, updatedEntity.get().getId());
        // Verify processInstanceId cleared
        assertEquals(2L, clearedId.get());
    }

    @Test
    void onCancel_shouldSetDraftAndClearProcessInstanceId() throws Exception {
        PurchaseReturnResultHandler handler = new PurchaseReturnResultHandler();
        AtomicReference<ErpPurchaseReturnDO> updatedEntity = new AtomicReference<>();
        AtomicReference<Long> clearedId = new AtomicReference<>();

        injectField(handler, "purchaseReturnMapper", createMapperProxyForCancel(updatedEntity, clearedId));
        injectField(handler, "purchaseReturnService", createServiceProxy(null, null));

        handler.onCancel(3L, "PI-003", "cancel reason");

        // Verify status set to DRAFT
        assertEquals(ErpAuditStatus.DRAFT.getStatus(), updatedEntity.get().getStatus());
        assertEquals(3L, updatedEntity.get().getId());
        // Verify processInstanceId cleared
        assertEquals(3L, clearedId.get());
    }

    // ========== helpers ==========

    private Object createMapperProxy(ErpPurchaseReturnDO entity) {
        return createProxy(ErpPurchaseReturnMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) return entity;
            return null;
        });
    }

    private Object createMapperProxyForCancel(AtomicReference<ErpPurchaseReturnDO> updatedRef,
                                               AtomicReference<Long> clearedIdRef) {
        return createProxy(ErpPurchaseReturnMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpPurchaseReturnDO().setId((Long) args[0]);
            }
            if ("updateById".equals(methodName)) {
                updatedRef.set((ErpPurchaseReturnDO) args[0]);
                return 1;
            }
            if ("clearProcessInstanceId".equals(methodName)) {
                clearedIdRef.set((Long) args[0]);
                return 1;
            }
            return null;
        });
    }

    private Object createServiceProxy(AtomicReference<Long> idRef, AtomicReference<Integer> statusRef) {
        return createProxy(ErpPurchaseReturnService.class, (methodName, args) -> {
            if ("updatePurchaseReturnStatusByBpm".equals(methodName)) {
                if (idRef != null) idRef.set((Long) args[0]);
                if (statusRef != null) statusRef.set((Integer) args[2]);
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
