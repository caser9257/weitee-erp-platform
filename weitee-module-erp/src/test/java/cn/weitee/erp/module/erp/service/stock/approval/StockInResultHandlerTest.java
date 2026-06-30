package cn.weitee.erp.module.erp.service.stock.approval;

import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockInDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockInMapper;
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

        injectField(handler, "stockInMapper", createMapperProxy(new ErpStockInDO().setId(1L)));
        injectField(handler, "stockInService", createServiceProxy(approvedId, approvedStatus));

        handler.onApprove(1L, "PI-001", "approved");

        assertEquals(1L, approvedId.get());
        assertEquals(ErpAuditStatus.APPROVE.getStatus(), approvedStatus.get());
    }

    @Test
    void onApprove_shouldThrowWhenEntityNotExists() throws Exception {
        StockInResultHandler handler = new StockInResultHandler();
        injectField(handler, "stockInMapper", createMapperProxy(null));
        injectField(handler, "stockInService", createServiceProxy(null, null));

        assertThrows(Exception.class, () -> handler.onApprove(999L, "PI-999", "not found"));
    }

    @Test
    void onReject_shouldUpdateStatusToReject() throws Exception {
        StockInResultHandler handler = new StockInResultHandler();
        AtomicReference<Long> rejectedId = new AtomicReference<>();
        AtomicReference<Integer> rejectedStatus = new AtomicReference<>();

        injectField(handler, "stockInMapper", createMapperProxy(new ErpStockInDO().setId(2L)));
        injectField(handler, "stockInService", createServiceProxy(rejectedId, rejectedStatus));

        handler.onReject(2L, "PI-002", "rejected");

        assertEquals(2L, rejectedId.get());
        assertEquals(ErpAuditStatus.REJECT.getStatus(), rejectedStatus.get());
    }

    @Test
    void onCancel_shouldSetDraftAndClearProcessInstanceId() throws Exception {
        StockInResultHandler handler = new StockInResultHandler();
        AtomicReference<ErpStockInDO> updatedEntity = new AtomicReference<>();
        AtomicReference<Long> clearedId = new AtomicReference<>();

        injectField(handler, "stockInMapper", createMapperProxyForCancel(updatedEntity, clearedId));
        injectField(handler, "stockInService", createServiceProxy(null, null));

        handler.onCancel(3L, "PI-003", "cancel reason");

        assertEquals(ErpAuditStatus.DRAFT.getStatus(), updatedEntity.get().getStatus());
        assertEquals(3L, updatedEntity.get().getId());
        assertEquals(3L, clearedId.get());
    }

    // ========== helpers ==========

    private Object createMapperProxy(ErpStockInDO entity) {
        return createProxy(ErpStockInMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) return entity;
            return null;
        });
    }

    private Object createMapperProxyForCancel(AtomicReference<ErpStockInDO> updatedRef,
                                               AtomicReference<Long> clearedIdRef) {
        return createProxy(ErpStockInMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpStockInDO().setId((Long) args[0]);
            }
            if ("updateById".equals(methodName)) {
                updatedRef.set((ErpStockInDO) args[0]);
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
        return createProxy(ErpStockInService.class, (methodName, args) -> {
            if ("updateStockInStatusByBpm".equals(methodName)) {
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
