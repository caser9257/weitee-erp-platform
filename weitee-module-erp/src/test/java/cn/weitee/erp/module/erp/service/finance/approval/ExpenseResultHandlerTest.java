package cn.weitee.erp.module.erp.service.finance.approval;

import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceExpenseService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExpenseResultHandlerTest {

    @Test
    void onApprove_shouldUpdateStatusToApprove() throws Exception {
        ExpenseResultHandler handler = new ExpenseResultHandler();
        AtomicReference<Long> approvedId = new AtomicReference<>();
        AtomicReference<Integer> approvedStatus = new AtomicReference<>();

        injectField(handler, "financeExpenseService",
                createServiceProxy(1L, approvedId, approvedStatus, null, null));

        handler.onApprove(1L, "PI-001", "approved");

        assertEquals(1L, approvedId.get());
        assertEquals(ErpAuditStatus.APPROVE.getStatus(), approvedStatus.get());
    }

    @Test
    void onCancel_shouldRollbackToDraftByMatchedProcessInstanceId() throws Exception {
        ExpenseResultHandler handler = new ExpenseResultHandler();
        AtomicReference<Long> rollbackIdRef = new AtomicReference<>();
        AtomicReference<String> rollbackBindingRef = new AtomicReference<>();

        injectField(handler, "financeExpenseService",
                createServiceProxy(2L, null, null, rollbackIdRef, rollbackBindingRef));

        handler.onCancel(2L, "PI-CANCEL", "cancel");

        assertEquals(2L, rollbackIdRef.get());
        assertEquals("PI-CANCEL", rollbackBindingRef.get());
    }

    @Test
    void onApprove_shouldThrowWhenEntityNotExists() throws Exception {
        ExpenseResultHandler handler = new ExpenseResultHandler();
        injectField(handler, "financeExpenseService",
                createServiceProxy(null, null, null, null, null));

        assertThrows(Exception.class, () -> handler.onApprove(999L, "PI-999", "not found"));
    }

    private Object createServiceProxy(Long entityId, AtomicReference<Long> idRef,
                                      AtomicReference<Integer> statusRef, AtomicReference<Long> rollbackIdRef,
                                      AtomicReference<String> rollbackBindingRef) {
        return createProxy(ErpFinanceExpenseService.class, (methodName, args) -> {
            if ("getFinanceExpense".equals(methodName)) {
                return entityId == null ? null : new cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO().setId(entityId);
            }
            if ("updateFinanceExpenseStatusByBpm".equals(methodName)) {
                if (idRef != null) {
                    idRef.set((Long) args[0]);
                }
                if (statusRef != null) {
                    statusRef.set((Integer) args[2]);
                }
            }
            if ("rollbackFinanceExpenseStatusToDraftByBpm".equals(methodName)) {
                if (rollbackIdRef != null) {
                    rollbackIdRef.set((Long) args[0]);
                }
                if (rollbackBindingRef != null) {
                    rollbackBindingRef.set((String) args[1]);
                }
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
