package cn.weitee.erp.module.erp.service.stock.approval;

import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockInDO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.service.stock.ErpStockInService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StockInResultHandlerTest {

    @Test
    void getSceneCode_shouldReturnCorrectCode() {
        StockInResultHandler handler = new StockInResultHandler();
        assertEquals("erp.stock.in.submit", handler.getSceneCode());
    }

    @Test
    void onApprove_shouldUpdateStatusToApprove() throws Exception {
        StockInResultHandler handler = new StockInResultHandler();
        AtomicReference<Long> approvedId = new AtomicReference<>();
        AtomicReference<Integer> approvedStatus = new AtomicReference<>();

        injectField(handler, "stockInService", createServiceProxy(new ErpStockInDO().setId(1L), approvedId, approvedStatus, null, null));

        handler.onApprove(1L, "PI-001", "approved");

        assertEquals(1L, approvedId.get());
        assertEquals(ErpAuditStatus.APPROVE.getStatus(), approvedStatus.get());
    }

    @Test
    void onApprove_shouldThrowWhenEntityNotExists() throws Exception {
        StockInResultHandler handler = new StockInResultHandler();
        injectField(handler, "stockInService", createServiceProxy(null, null, null, null, null));

        assertThrows(Exception.class, () -> handler.onApprove(999L, "PI-999", "not found"));
    }

    @Test
    void onReject_shouldSetDraftAndClearProcessInstanceId() throws Exception {
        StockInResultHandler handler = new StockInResultHandler();
        AtomicReference<Long> rollbackId = new AtomicReference<>();

        injectField(handler, "stockInService", createServiceProxy(new ErpStockInDO().setId(2L), null, null, rollbackId, null));

        handler.onReject(2L, "PI-002", "rejected");

        assertEquals(2L, rollbackId.get());
    }

    @Test
    void onCancel_shouldSetDraftAndClearProcessInstanceId() throws Exception {
        StockInResultHandler handler = new StockInResultHandler();
        AtomicReference<Long> rollbackId = new AtomicReference<>();

        injectField(handler, "stockInService", createServiceProxy(new ErpStockInDO().setId(3L), null, null, rollbackId, null));

        handler.onCancel(3L, "PI-003", "cancel reason");

        assertEquals(3L, rollbackId.get());
    }

    // ========== helpers ==========

    @Test
    void onApproveWithSnapshot_shouldPassSnapshotId() throws Exception {
        StockInResultHandler handler = new StockInResultHandler();
        AtomicReference<String> bindingId = new AtomicReference<>();

        injectField(handler, "stockInService", createServiceProxy(new ErpStockInDO().setId(11L), null, null, null, bindingId));

        handler.onApproveWithSnapshot(11L, "PI-011", "SNAP-011", "approved");

        assertEquals("SNAP-011", bindingId.get());
    }

    @Test
    void onRejectWithSnapshot_shouldPassSnapshotId() throws Exception {
        StockInResultHandler handler = new StockInResultHandler();
        AtomicReference<String> bindingId = new AtomicReference<>();

        injectField(handler, "stockInService", createServiceProxy(new ErpStockInDO().setId(12L), null, null, null, bindingId));

        handler.onRejectWithSnapshot(12L, "PI-012", "SNAP-012", "rejected");

        assertEquals("SNAP-012", bindingId.get());
    }

    @Test
    void onCancelWithSnapshot_shouldPassSnapshotId() throws Exception {
        StockInResultHandler handler = new StockInResultHandler();
        AtomicReference<String> bindingId = new AtomicReference<>();

        injectField(handler, "stockInService", createServiceProxy(new ErpStockInDO().setId(13L), null, null, null, bindingId));

        handler.onCancelWithSnapshot(13L, "PI-013", "SNAP-013", "cancel");

        assertEquals("SNAP-013", bindingId.get());
    }

    private Object createServiceProxy(ErpStockInDO entity, AtomicReference<Long> idRef,
                                      AtomicReference<Integer> statusRef, AtomicReference<Long> rollbackIdRef,
                                      AtomicReference<String> bindingIdRef) {
        return createProxy(ErpStockInService.class, (methodName, args) -> {
            if ("getStockIn".equals(methodName)) {
                return entity;
            }
            if ("updateStockInStatusByBpm".equals(methodName)) {
                if (idRef != null) idRef.set((Long) args[0]);
                if (statusRef != null) statusRef.set((Integer) args[2]);
                if (bindingIdRef != null) bindingIdRef.set((String) args[1]);
            }
            if ("rollbackStockInStatusToDraftByBpm".equals(methodName) && rollbackIdRef != null) {
                rollbackIdRef.set((Long) args[0]);
            }
            if ("rollbackStockInStatusToDraftByBpm".equals(methodName) && bindingIdRef != null) {
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
