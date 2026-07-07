package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePaymentDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePaymentMapper;
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

class ErpFinancePaymentBpmServiceImplTest {

    @Test
    void submitFinancePayment_shouldReturnProcessInstanceIdAndBindProcessInstance() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpFinancePaymentDO> paymentRef = new AtomicReference<>(
                new ErpFinancePaymentDO()
                        .setId(31L)
                        .setNo("FKD202607070001")
                        .setStatus(ErpAuditStatus.REJECT.getStatus())
                        .setPaymentTime(LocalDateTime.of(2026, 7, 7, 9, 30))
                        .setTotalPrice(new BigDecimal("1200.00"))
                        .setDiscountPrice(BigDecimal.ZERO)
                        .setPaymentPrice(new BigDecimal("1200.00"))
                        .setSupplierId(101L)
                        .setAccountId(202L)
                        .setFinanceUserId(303L));
        List<ErpFinancePaymentDO> updatedPayments = new ArrayList<>();
        AtomicReference<Long> submitBizIdRef = new AtomicReference<>();

        setField(service, "transactionManager", createTransactionManagerProxy());
        setField(service, "erpFinancePaymentMapper", createProxy(ErpFinancePaymentMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return paymentRef.get();
            }
            if ("updateById".equals(methodName)) {
                updatedPayments.add((ErpFinancePaymentDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "approvalRuntimeService", createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> {
            if ("submit".equals(methodName)) {
                submitBizIdRef.set((Long) args[1]);
                return "PI-FIN-PAY-001";
            }
            return null;
        }));

        Object reqVO = createSubmitReqVO(31L, Map.of("task_1", List.of(9L)));
        Method method = service.getClass().getMethod("submitFinancePayment", Long.class, reqVO.getClass());

        Object result = method.invoke(service, 9527L, reqVO);

        assertEquals("PI-FIN-PAY-001", result);
        assertEquals(31L, submitBizIdRef.get());
        assertEquals(2, updatedPayments.size());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), updatedPayments.get(0).getStatus());
        assertEquals("PI-FIN-PAY-001", updatedPayments.get(1).getProcessInstanceId());
    }

    @Test
    void submitFinancePayment_shouldMarkFailedAndThrowWhenBpmSubmitThrows() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpFinancePaymentDO> paymentRef = new AtomicReference<>(
                new ErpFinancePaymentDO()
                        .setId(32L)
                        .setNo("FKD202607070002")
                        .setStatus(ErpAuditStatus.DRAFT.getStatus()));
        List<ErpFinancePaymentDO> updatedPayments = new ArrayList<>();

        setField(service, "transactionManager", createTransactionManagerProxy());
        setField(service, "erpFinancePaymentMapper", createProxy(ErpFinancePaymentMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return paymentRef.get();
            }
            if ("updateById".equals(methodName)) {
                updatedPayments.add((ErpFinancePaymentDO) args[0]);
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

        Object reqVO = createSubmitReqVO(32L, Map.of());
        Method method = service.getClass().getMethod("submitFinancePayment", Long.class, reqVO.getClass());

        java.lang.reflect.InvocationTargetException ex = assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> method.invoke(service, 9527L, reqVO));

        assertEquals(IllegalStateException.class, ex.getCause().getClass());
        assertEquals("flowable unavailable", ex.getCause().getMessage());
        assertEquals(2, updatedPayments.size());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), updatedPayments.get(0).getStatus());
        assertEquals(ErpAuditStatus.FAILED.getStatus(), updatedPayments.get(1).getStatus());
    }

    @Test
    void submitFinancePayment_shouldNotMarkFailedWhenBindingProcessInstanceThrows() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpFinancePaymentDO> paymentRef = new AtomicReference<>(
                new ErpFinancePaymentDO()
                        .setId(35L)
                        .setNo("FKD202607070035")
                        .setStatus(ErpAuditStatus.DRAFT.getStatus()));
        List<ErpFinancePaymentDO> updatedPayments = new ArrayList<>();

        setField(service, "transactionManager", createTransactionManagerProxy());
        setField(service, "erpFinancePaymentMapper", createProxy(ErpFinancePaymentMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return paymentRef.get();
            }
            if ("updateById".equals(methodName)) {
                ErpFinancePaymentDO update = (ErpFinancePaymentDO) args[0];
                if ("PI-FIN-PAY-BIND-FAIL".equals(update.getProcessInstanceId())) {
                    throw new IllegalStateException("bind failed");
                }
                updatedPayments.add(update);
                return 1;
            }
            return null;
        }));
        setField(service, "approvalRuntimeService", createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> {
            if ("submit".equals(methodName)) {
                return "PI-FIN-PAY-BIND-FAIL";
            }
            return null;
        }));

        Object reqVO = createSubmitReqVO(35L, Map.of());
        Method method = service.getClass().getMethod("submitFinancePayment", Long.class, reqVO.getClass());

        java.lang.reflect.InvocationTargetException ex = assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> method.invoke(service, 9527L, reqVO));

        assertEquals(IllegalStateException.class, ex.getCause().getClass());
        assertEquals("bind failed", ex.getCause().getMessage());
        assertEquals(1, updatedPayments.size());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), updatedPayments.get(0).getStatus());
    }

    @Test
    void cancelFinancePaymentApproval_shouldCancelProcessSynchronously() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpFinancePaymentDO> paymentRef = new AtomicReference<>(
                new ErpFinancePaymentDO()
                        .setId(33L)
                        .setNo("FKD202607070003")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus())
                        .setProcessInstanceId("PI-FIN-PAY-CANCEL"));
        AtomicReference<Long> cancelBizIdRef = new AtomicReference<>();
        AtomicReference<String> cancelReasonRef = new AtomicReference<>();

        setField(service, "erpFinancePaymentMapper", createProxy(ErpFinancePaymentMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return paymentRef.get();
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

        Object reqVO = createCancelReqVO(33L, "回退补资料");
        Method method = service.getClass().getMethod("cancelFinancePaymentApproval", Long.class, reqVO.getClass());
        method.invoke(service, 9527L, reqVO);

        assertEquals(33L, cancelBizIdRef.get());
        assertEquals("回退补资料", cancelReasonRef.get());
    }

    @Test
    void cancelFinancePaymentApproval_shouldThrowWhenNoRunningProcess() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpFinancePaymentDO> paymentRef = new AtomicReference<>(
                new ErpFinancePaymentDO()
                        .setId(34L)
                        .setNo("FKD202607070004")
                        .setStatus(ErpAuditStatus.DRAFT.getStatus()));

        setField(service, "erpFinancePaymentMapper", createProxy(ErpFinancePaymentMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return paymentRef.get();
            }
            return null;
        }));
        setField(service, "approvalRuntimeService", createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> null));

        Object reqVO = createCancelReqVO(34L, "回退补资料");
        Method method = service.getClass().getMethod("cancelFinancePaymentApproval", Long.class, reqVO.getClass());

        java.lang.reflect.InvocationTargetException ex = assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> method.invoke(service, 9527L, reqVO));
        assertNotNull(ex.getCause());
        assertEquals(ServiceException.class, ex.getCause().getClass());
    }

    private Object instantiateService() throws Exception {
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.service.finance.ErpFinancePaymentBpmServiceImpl");
        return clazz.getDeclaredConstructor().newInstance();
    }

    private Object createSubmitReqVO(Long id, Map<String, List<Long>> assignees) throws Exception {
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.controller.admin.finance.vo.payment.ErpFinancePaymentSubmitReqVO");
        Object reqVO = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("setId", Long.class).invoke(reqVO, id);
        clazz.getMethod("setStartUserSelectAssignees", Map.class).invoke(reqVO, assignees);
        return reqVO;
    }

    private Object createCancelReqVO(Long id, String reason) throws Exception {
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.controller.admin.finance.vo.payment.ErpFinancePaymentCancelApprovalReqVO");
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
