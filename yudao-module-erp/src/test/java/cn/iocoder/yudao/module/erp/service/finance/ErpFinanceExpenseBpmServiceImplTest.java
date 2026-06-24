package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceExpenseMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ErpFinanceExpenseBpmServiceImplTest {

    @Test
    void submitFinanceExpense_shouldStartProcessAndBindProcessInstance() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpFinanceExpenseDO> expenseRef = new AtomicReference<>(
                new ErpFinanceExpenseDO()
                        .setId(21L)
                        .setNo("LSBX20260521000001")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus())
                        .setExpenseTime(LocalDateTime.of(2026, 5, 21, 10, 0))
                        .setExpensePrice(new BigDecimal("888.66"))
                        .setDeptId(11L)
                        .setProjectId(22L)
                        .setSupplierId(33L)
                        .setFinanceUserId(44L)
                        .setAccountId(55L));
        List<ErpFinanceExpenseDO> updatedExpenses = new ArrayList<>();
        AtomicReference<Long> submitBizIdRef = new AtomicReference<>();

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

        // afterCommit 模式：submit 返回 null
        assertNull(result);
        assertEquals(21L, submitBizIdRef.get());
        // 第一次 updateById：设置 PROCESS 状态，processInstanceId=null
        assertEquals(21L, updatedExpenses.get(0).getId());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), updatedExpenses.get(0).getStatus());
        assertNull(updatedExpenses.get(0).getProcessInstanceId());
        // 第二次 updateById（afterCommit）：设置 processInstanceId
        assertEquals(21L, updatedExpenses.get(1).getId());
        assertEquals("PI-FIN-EXP-001", updatedExpenses.get(1).getProcessInstanceId());
    }

    @Test
    void cancelFinanceExpenseApproval_shouldCancelProcessAndClearBinding() throws Exception {
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

        // afterCommit 模式：BPM 撤回在 afterCommit 中执行
        assertEquals(22L, cancelBizIdRef.get());
        assertEquals("资料待补充", cancelReasonRef.get());
    }

    @Test
    void handleProcessInstanceResult_shouldDoNothingAsHandledByResultHandler() throws Exception {
        Object service = instantiateService();

        Method method = service.getClass().getMethod("handleProcessInstanceResult",
                Long.class, String.class, Integer.class, String.class);
        // 结果回写已收敛到 ExpenseResultHandler，此方法为空实现
        method.invoke(service, 23L, "PI-MATCH", bpmStatus("APPROVE"), "approved");
        // 无异常即通过
    }

    private Object instantiateService() throws Exception {
        Class<?> clazz = Class.forName("cn.iocoder.yudao.module.erp.service.finance.ErpFinanceExpenseBpmServiceImpl");
        return clazz.getDeclaredConstructor().newInstance();
    }

    private Object createSubmitReqVO(Long id, Map<String, List<Long>> assignees) throws Exception {
        Class<?> clazz = Class.forName("cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseSubmitReqVO");
        Object reqVO = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("setId", Long.class).invoke(reqVO, id);
        clazz.getMethod("setStartUserSelectAssignees", Map.class).invoke(reqVO, assignees);
        return reqVO;
    }

    private Object createCancelReqVO(Long id, String reason) throws Exception {
        Class<?> clazz = Class.forName("cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseCancelApprovalReqVO");
        Object reqVO = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("setId", Long.class).invoke(reqVO, id);
        clazz.getMethod("setReason", String.class).invoke(reqVO, reason);
        return reqVO;
    }

    private Object createProxyByName(String className, MethodHandler handler) throws Exception {
        return createProxy(Class.forName(className), handler);
    }

    private Object readProperty(Object target, String methodName) throws Exception {
        return target.getClass().getMethod(methodName).invoke(target);
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
        Object handle(String methodName, Object[] args) throws Exception;
    }
}
