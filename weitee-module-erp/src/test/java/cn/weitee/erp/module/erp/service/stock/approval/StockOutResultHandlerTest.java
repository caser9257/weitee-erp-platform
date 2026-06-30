package cn.weitee.erp.module.erp.service.stock.approval;

import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockOutDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockOutMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.service.stock.ErpStockOutService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StockOutResultHandlerTest {

    @Test
    void getSceneCode_shouldReturnCorrectCode() {
        StockOutResultHandler handler = new StockOutResultHandler();
        assertEquals("erp.stock.out.submit", handler.getSceneCode());
    }

    @Test
    void onApprove_shouldUpdateStatusToApprove() throws Exception {
        StockOutResultHandler handler = new StockOutResultHandler();
        AtomicReference<Long> approvedId = new AtomicReference<>();
        AtomicReference<Integer> approvedStatus = new AtomicReference<>();

        injectField(handler, "stockOutMapper", createMapperProxy(new ErpStockOutDO().setId(1L)));
        injectField(handler, "stockOutService", createServiceProxy(approvedId, approvedStatus));

        handler.onApprove(1L, "PI-001", "approved");

        assertEquals(1L, approvedId.get());
        assertEquals(ErpAuditStatus.APPROVE.getStatus(), approvedStatus.get());
    }

    @Test
    void onApprove_shouldThrowWhenEntityNotExists() throws Exception {
        StockOutResultHandler handler = new StockOutResultHandler();
        injectField(handler, "stockOutMapper", createMapperProxy(null));
        injectField(handler, "stockOutService", createServiceProxy(null, null));

        assertThrows(Exception.class, () -> handler.onApprove(999L, "PI-999", "not found"));
    }

    @Test
    void onReject_shouldSetDraftAndClearProcessInstanceId() throws Exception {
        StockOutResultHandler handler = new StockOutResultHandler();
        AtomicReference<ErpStockOutDO> updatedEntity = new AtomicReference<>();
        AtomicReference<Long> clearedId = new AtomicReference<>();

        injectField(handler, "stockOutMapper", createMapperProxyForCancel(updatedEntity, clearedId));
        injectField(handler, "stockOutService", createServiceProxy(null, null));

        handler.onReject(2L, "PI-002", "rejected");

        // Verify status set to DRAFT (same as onCancel — no stock side effects)
        assertEquals(ErpAuditStatus.DRAFT.getStatus(), updatedEntity.get().getStatus());
        assertEquals(2L, updatedEntity.get().getId());
        // Verify processInstanceId cleared
        assertEquals(2L, clearedId.get());
    }

    @Test
    void onCancel_shouldSetDraftAndClearProcessInstanceId() throws Exception {
        StockOutResultHandler handler = new StockOutResultHandler();
        AtomicReference<ErpStockOutDO> updatedEntity = new AtomicReference<>();
        AtomicReference<Long> clearedId = new AtomicReference<>();

        injectField(handler, "stockOutMapper", createMapperProxyForCancel(updatedEntity, clearedId));
        injectField(handler, "stockOutService", createServiceProxy(null, null));

        handler.onCancel(3L, "PI-003", "cancel reason");

        assertEquals(ErpAuditStatus.DRAFT.getStatus(), updatedEntity.get().getStatus());
        assertEquals(3L, updatedEntity.get().getId());
        assertEquals(3L, clearedId.get());
    }

    // ========== helpers ==========

    private Object createMapperProxy(ErpStockOutDO entity) {
        return createProxy(ErpStockOutMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) return entity;
            return null;
        });
    }

    private Object createMapperProxyForCancel(AtomicReference<ErpStockOutDO> updatedRef,
                                               AtomicReference<Long> clearedIdRef) {
        return createProxy(ErpStockOutMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpStockOutDO().setId((Long) args[0]);
            }
            if ("updateById".equals(methodName)) {
                updatedRef.set((ErpStockOutDO) args[0]);
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
        return createProxy(ErpStockOutService.class, (methodName, args) -> {
            if ("updateStockOutStatusByBpm".equals(methodName)) {
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
