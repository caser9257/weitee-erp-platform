package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceExpenseMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpFinanceExpenseBpmServiceImplTest {

    @Test
    void submitFinanceExpense_shouldReturnProcessInstanceIdAndBindProcessInstance() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpFinanceExpenseDO> expenseRef = new AtomicReference<>(
                new ErpFinanceExpenseDO()
                        .setId(21L)
                        .setNo("LSBX20260521000001")
                        .setStatus(ErpAuditStatus.REJECT.getStatus())
                        .setExpenseTime(LocalDateTime.of(2026, 5, 21, 10, 0))
                        .setExpensePrice(new BigDecimal("888.66"))
                        .setDeptId(11L)
                        .setProjectId(22L)
                        .setSupplierId(33L)
                        .setFinanceUserId(44L)
                        .setAccountId(55L));
        List<ErpFinanceExpenseDO> updatedExpenses = new ArrayList<>();
        AtomicReference<Long> submitBizIdRef = new AtomicReference<>();

        setField(service, "transactionManager", createTransactionManagerProxy());
        setField(service, "erpFinanceExpenseMapper", createProxy(ErpFinanceExpenseMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return expenseRef.get();
            }
            if ("updateById".equals(methodName)) {
                updatedExpenses.add((ErpFinanceExpenseDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "approvalRuntimeService", createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> {
            if ("submit".equals(methodName)) {
                submitBizIdRef.set((Long) args[1]);
                return "PI-FIN-EXP-001";
            }
            return null;
        }));

        Object reqVO = createSubmitReqVO(21L, Map.of("task_1", List.of(7L, 8L)));
        Method method = service.getClass().getMethod("submitFinanceExpense", Long.class, reqVO.getClass());

        Object result = method.invoke(service, 9527L, reqVO);

        assertEquals("PI-FIN-EXP-001", result);
        assertEquals(21L, submitBizIdRef.get());
        assertEquals(2, updatedExpenses.size());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), updatedExpenses.get(0).getStatus());
        assertEquals("PI-FIN-EXP-001", updatedExpenses.get(1).getProcessInstanceId());
    }

    @Test
    void submitFinanceExpense_shouldMarkFailedAndThrowWhenBpmSubmitThrows() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpFinanceExpenseDO> expenseRef = new AtomicReference<>(
                new ErpFinanceExpenseDO()
                        .setId(24L)
                        .setNo("LSBX20260521000024")
                        .setStatus(ErpAuditStatus.DRAFT.getStatus()));
        List<ErpFinanceExpenseDO> updatedExpenses = new ArrayList<>();

        setField(service, "transactionManager", createTransactionManagerProxy());
        setField(service, "erpFinanceExpenseMapper", createProxy(ErpFinanceExpenseMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return expenseRef.get();
            }
            if ("updateById".equals(methodName)) {
                updatedExpenses.add((ErpFinanceExpenseDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "approvalRuntimeService", createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> {
            if ("submit".equals(methodName)) {
                throw new IllegalStateException("flowable unavailable");
            }
            return null;
        }));

        Object reqVO = createSubmitReqVO(24L, Map.of());
        Method method = service.getClass().getMethod("submitFinanceExpense", Long.class, reqVO.getClass());

        java.lang.reflect.InvocationTargetException ex = assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> method.invoke(service, 9527L, reqVO));

        assertEquals(IllegalStateException.class, ex.getCause().getClass());
        assertEquals("flowable unavailable", ex.getCause().getMessage());
        assertEquals(2, updatedExpenses.size());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), updatedExpenses.get(0).getStatus());
        assertEquals(ErpAuditStatus.FAILED.getStatus(), updatedExpenses.get(1).getStatus());
    }

    @Test
    void submitFinanceExpense_shouldNotMarkFailedWhenBindingProcessInstanceThrows() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpFinanceExpenseDO> expenseRef = new AtomicReference<>(
                new ErpFinanceExpenseDO()
                        .setId(26L)
                        .setNo("LSBX20260521000026")
                        .setStatus(ErpAuditStatus.DRAFT.getStatus()));
        List<ErpFinanceExpenseDO> updatedExpenses = new ArrayList<>();

        setField(service, "transactionManager", createTransactionManagerProxy());
        setField(service, "erpFinanceExpenseMapper", createProxy(ErpFinanceExpenseMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return expenseRef.get();
            }
            if ("updateById".equals(methodName)) {
                ErpFinanceExpenseDO update = (ErpFinanceExpenseDO) args[0];
                if ("PI-FIN-EXP-BIND-FAIL".equals(update.getProcessInstanceId())) {
                    throw new IllegalStateException("bind failed");
                }
                updatedExpenses.add(update);
                return 1;
            }
            return null;
        }));
        setField(service, "approvalRuntimeService", createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> {
            if ("submit".equals(methodName)) {
                return "PI-FIN-EXP-BIND-FAIL";
            }
            return null;
        }));

        Object reqVO = createSubmitReqVO(26L, Map.of());
        Method method = service.getClass().getMethod("submitFinanceExpense", Long.class, reqVO.getClass());

        java.lang.reflect.InvocationTargetException ex = assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> method.invoke(service, 9527L, reqVO));

        assertEquals(IllegalStateException.class, ex.getCause().getClass());
        assertEquals("bind failed", ex.getCause().getMessage());
        assertEquals(1, updatedExpenses.size());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), updatedExpenses.get(0).getStatus());
    }

    @Test
    void cancelFinanceExpenseApproval_shouldCancelProcessSynchronously() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpFinanceExpenseDO> expenseRef = new AtomicReference<>(
                new ErpFinanceExpenseDO()
                        .setId(22L)
                        .setNo("LSBX20260521000002")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus())
                        .setProcessInstanceId("PI-FIN-EXP-CANCEL"));
        AtomicReference<Long> cancelBizIdRef = new AtomicReference<>();
        AtomicReference<String> cancelReasonRef = new AtomicReference<>();

        setField(service, "erpFinanceExpenseMapper", createProxy(ErpFinanceExpenseMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return expenseRef.get();
            }
            return null;
        }));
        setField(service, "approvalRuntimeService", createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> {
            if ("cancel".equals(methodName)) {
                cancelBizIdRef.set((Long) args[1]);
                cancelReasonRef.set((String) args[3]);
            }
            return null;
        }));

        Object reqVO = createCancelReqVO(22L, "资料待补充");
        Method method = service.getClass().getMethod("cancelFinanceExpenseApproval", Long.class, reqVO.getClass());
        method.invoke(service, 9527L, reqVO);

        assertEquals(22L, cancelBizIdRef.get());
        assertEquals("资料待补充", cancelReasonRef.get());
    }

    @Test
    void cancelFinanceExpenseApproval_shouldThrowWhenNoRunningProcess() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpFinanceExpenseDO> expenseRef = new AtomicReference<>(
                new ErpFinanceExpenseDO()
                        .setId(25L)
                        .setNo("LSBX20260521000025")
                        .setStatus(ErpAuditStatus.DRAFT.getStatus()));

        setField(service, "erpFinanceExpenseMapper", createProxy(ErpFinanceExpenseMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return expenseRef.get();
            }
            return null;
        }));
        setField(service, "approvalRuntimeService", createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> null));

        Object reqVO = createCancelReqVO(25L, "资料待补充");
        Method method = service.getClass().getMethod("cancelFinanceExpenseApproval", Long.class, reqVO.getClass());

        java.lang.reflect.InvocationTargetException ex = assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> method.invoke(service, 9527L, reqVO));
        assertNotNull(ex.getCause());
        assertEquals(ServiceException.class, ex.getCause().getClass());
    }

    @Test
    void handleProcessInstanceResult_shouldDoNothingAsHandledByResultHandler() throws Exception {
        Object service = instantiateService();

        Method method = service.getClass().getMethod("handleProcessInstanceResult",
                Long.class, String.class, Integer.class, String.class);
        method.invoke(service, 23L, "PI-MATCH", 2, "approved");
    }

    private Object instantiateService() throws Exception {
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.service.finance.ErpFinanceExpenseBpmServiceImpl");
        return clazz.getDeclaredConstructor().newInstance();
    }

    private Object createSubmitReqVO(Long id, Map<String, List<Long>> assignees) throws Exception {
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseSubmitReqVO");
        Object reqVO = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("setId", Long.class).invoke(reqVO, id);
        clazz.getMethod("setStartUserSelectAssignees", Map.class).invoke(reqVO, assignees);
        return reqVO;
    }

    private Object createCancelReqVO(Long id, String reason) throws Exception {
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseCancelApprovalReqVO");
        Object reqVO = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("setId", Long.class).invoke(reqVO, id);
        clazz.getMethod("setReason", String.class).invoke(reqVO, reason);
        return reqVO;
    }

    private PlatformTransactionManager createTransactionManagerProxy() {
        return createProxy(PlatformTransactionManager.class, (methodName, args) -> {
            if ("getTransaction".equals(methodName)) {
                return new SimpleTransactionStatus();
            }
            if ("commit".equals(methodName) || "rollback".equals(methodName)) {
                return null;
            }
            return null;
        });
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
        Object handle(String methodName, Object[] args) throws Exception;
    }
}
