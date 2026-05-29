package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceExpenseMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        AtomicReference<ErpFinanceExpenseDO> updatedExpenseRef = new AtomicReference<>();
        AtomicReference<Object> createReqRef = new AtomicReference<>();

        setField(service, "erpFinanceExpenseMapper", createProxy(ErpFinanceExpenseMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return expenseRef.get();
            }
            if ("updateById".equals(methodName)) {
                updatedExpenseRef.set((ErpFinanceExpenseDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "processInstanceApi", createProxyByName(
                "cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi",
                (methodName, args) -> {
                    if ("createProcessInstance".equals(methodName)) {
                        createReqRef.set(args[1]);
                        return "PI-FIN-EXP-001";
                    }
                    return null;
                }));

        Object reqVO = createSubmitReqVO(21L, Map.of("task_1", List.of(7L, 8L)));
        Method method = service.getClass().getMethod("submitFinanceExpense", Long.class, reqVO.getClass());

        Object result = method.invoke(service, 9527L, reqVO);

        assertEquals("PI-FIN-EXP-001", result);
        assertNotNull(createReqRef.get());
        assertEquals("21", readProperty(createReqRef.get(), "getBusinessKey"));
        Map<?, ?> variables = (Map<?, ?>) readProperty(createReqRef.get(), "getVariables");
        assertEquals("LSBX20260521000001", variables.get("expenseNo"));
        assertEquals(new BigDecimal("888.66"), variables.get("expensePrice"));
        assertEquals(11L, variables.get("deptId"));
        assertEquals(22L, variables.get("projectId"));
        assertEquals(33L, variables.get("supplierId"));
        assertEquals(44L, variables.get("financeUserId"));
        assertEquals(55L, variables.get("accountId"));
        assertEquals(21L, updatedExpenseRef.get().getId());
        assertEquals("PI-FIN-EXP-001", updatedExpenseRef.get().getProcessInstanceId());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), updatedExpenseRef.get().getStatus());
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
        AtomicReference<Long> clearedExpenseIdRef = new AtomicReference<>();
        AtomicReference<Object> cancelReqRef = new AtomicReference<>();

        setField(service, "erpFinanceExpenseMapper", createProxy(ErpFinanceExpenseMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return expenseRef.get();
            }
            if ("clearProcessInstanceId".equals(methodName)) {
                clearedExpenseIdRef.set((Long) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "processInstanceService", createProxyByName(
                "cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService",
                (methodName, args) -> {
                    if ("cancelProcessInstanceByStartUser".equals(methodName)) {
                        cancelReqRef.set(args[1]);
                    }
                    return null;
                }));

        Object reqVO = createCancelReqVO(22L, "资料待补充");
        Method method = service.getClass().getMethod("cancelFinanceExpenseApproval", Long.class, reqVO.getClass());
        method.invoke(service, 9527L, reqVO);

        assertNotNull(cancelReqRef.get());
        assertEquals("PI-FIN-EXP-CANCEL", readProperty(cancelReqRef.get(), "getId"));
        assertEquals("资料待补充", readProperty(cancelReqRef.get(), "getReason"));
        assertEquals(22L, clearedExpenseIdRef.get());
    }

    @Test
    void handleProcessInstanceResult_shouldTranslateApproveAndIgnoreStaleProcessInstance() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpFinanceExpenseDO> expenseRef = new AtomicReference<>(
                new ErpFinanceExpenseDO().setId(23L).setProcessInstanceId("PI-MATCH"));
        AtomicReference<List<Object>> callbackArgsRef = new AtomicReference<>();
        AtomicReference<Long> clearedExpenseIdRef = new AtomicReference<>();

        setField(service, "erpFinanceExpenseMapper", createProxy(ErpFinanceExpenseMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return expenseRef.get();
            }
            if ("clearProcessInstanceId".equals(methodName)) {
                clearedExpenseIdRef.set((Long) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "financeExpenseService", createProxy(ErpFinanceExpenseService.class, (methodName, args) -> {
            if ("updateFinanceExpenseStatusByBpm".equals(methodName)) {
                callbackArgsRef.set(List.of(args));
            }
            return null;
        }));

        Method method = service.getClass().getMethod("handleProcessInstanceResult",
                Long.class, String.class, Integer.class, String.class);
        method.invoke(service, 23L, "PI-MATCH", bpmStatus("APPROVE"), "approved");

        assertNotNull(callbackArgsRef.get());
        assertEquals(23L, callbackArgsRef.get().get(0));
        assertEquals("PI-MATCH", callbackArgsRef.get().get(1));
        assertEquals(ErpAuditStatus.APPROVE.getStatus(), callbackArgsRef.get().get(2));
        assertEquals("approved", callbackArgsRef.get().get(3));
        assertNull(clearedExpenseIdRef.get());

        callbackArgsRef.set(null);
        expenseRef.set(new ErpFinanceExpenseDO().setId(23L).setProcessInstanceId("PI-NEW"));
        method.invoke(service, 23L, "PI-OLD", bpmStatus("REJECT"), "stale");
        assertNull(callbackArgsRef.get());
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
