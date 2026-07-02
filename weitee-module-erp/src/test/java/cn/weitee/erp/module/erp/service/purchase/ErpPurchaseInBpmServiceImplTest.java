package cn.weitee.erp.module.erp.service.purchase;

import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import org.junit.jupiter.api.Test;

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
import static org.junit.jupiter.api.Assertions.assertNull;

class ErpPurchaseInBpmServiceImplTest {

    @Test
    void submitPurchaseIn_shouldStartProcessAndBindProcessInstance() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpPurchaseInDO> purchaseInRef = new AtomicReference<>(
                new ErpPurchaseInDO()
                        .setId(31L)
                        .setNo("PI-2026-001")
                        .setStatus(ErpAuditStatus.DRAFT.getStatus())
                        .setSupplierId(301L)
                        .setOrderId(501L)
                        .setTotalPrice(new BigDecimal("1880.66"))
                        .setInTime(LocalDateTime.of(2026, 4, 9, 10, 0))
        );
        List<ErpPurchaseInDO> updatedPurchaseIns = new ArrayList<>();
        AtomicReference<Long> submitBizIdRef = new AtomicReference<>();

        setField(service, "erpPurchaseInMapper", createProxy(ErpPurchaseInMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return purchaseInRef.get();
            }
            if ("updateById".equals(methodName)) {
                updatedPurchaseIns.add((ErpPurchaseInDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "approvalRuntimeService", createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> {
            if ("submit".equals(methodName)) {
                submitBizIdRef.set((Long) args[1]);
                return "PI-IN-20260409-001";
            }
            return null;
        }));

        Object reqVO = createSubmitReqVO(31L, Map.of("task_1", List.of(7L, 8L)));
        Method method = service.getClass().getMethod("submitPurchaseIn", Long.class, reqVO.getClass());

        Object result = method.invoke(service, 9527L, reqVO);

        assertNull(result);
        assertEquals(31L, submitBizIdRef.get());
        assertEquals(31L, updatedPurchaseIns.get(0).getId());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), updatedPurchaseIns.get(0).getStatus());
        assertNull(updatedPurchaseIns.get(0).getProcessInstanceId());
        assertEquals(31L, updatedPurchaseIns.get(1).getId());
        assertEquals("PI-IN-20260409-001", updatedPurchaseIns.get(1).getProcessInstanceId());
    }

    @Test
    void submitPurchaseIn_shouldRejectProcessStatusWithoutProcessInstance() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpPurchaseInDO> purchaseInRef = new AtomicReference<>(
                new ErpPurchaseInDO()
                        .setId(34L)
                        .setNo("PI-2026-INVALID")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus())
        );
        setField(service, "erpPurchaseInMapper", createProxy(ErpPurchaseInMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return purchaseInRef.get();
            }
            return null;
        }));
        setField(service, "approvalRuntimeService", createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> null));

        Object reqVO = createSubmitReqVO(34L, Map.of());
        Method method = service.getClass().getMethod("submitPurchaseIn", Long.class, reqVO.getClass());

        assertSubmitFail(method, service, reqVO);
    }

    @Test
    void cancelPurchaseInApproval_shouldCancelProcessAndClearBinding() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpPurchaseInDO> purchaseInRef = new AtomicReference<>(
                new ErpPurchaseInDO()
                        .setId(32L)
                        .setNo("PI-2026-CANCEL")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus())
                        .setProcessInstanceId("PI-IN-TO-CANCEL")
        );
        AtomicReference<Long> clearedInIdRef = new AtomicReference<>();
        AtomicReference<Long> cancelBizIdRef = new AtomicReference<>();

        setField(service, "erpPurchaseInMapper", createProxy(ErpPurchaseInMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return purchaseInRef.get();
            }
            if ("clearProcessInstanceId".equals(methodName)) {
                clearedInIdRef.set((Long) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "approvalRuntimeService", createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> {
            if ("cancel".equals(methodName)) {
                cancelBizIdRef.set((Long) args[1]);
            }
            return null;
        }));

        Object reqVO = createCancelReqVO(32L, "cancel test");
        Method method = service.getClass().getMethod("cancelPurchaseInApproval", Long.class, reqVO.getClass());
        method.invoke(service, 9527L, reqVO);

        assertEquals(32L, cancelBizIdRef.get());
        assertEquals(32L, clearedInIdRef.get());
    }

    @Test
    void handleProcessInstanceResult_shouldDoNothingAsHandledByResultHandler() throws Exception {
        Object service = instantiateService();
        Method method = service.getClass().getMethod("handleProcessInstanceResult",
                Long.class, String.class, Integer.class, String.class);
        method.invoke(service, 33L, "PI-MATCH", 20, "approved");
    }

    private Object instantiateService() throws Exception {
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.service.purchase.ErpPurchaseInBpmServiceImpl");
        return clazz.getDeclaredConstructor().newInstance();
    }

    private Object createSubmitReqVO(Long id, Map<String, List<Long>> assignees) throws Exception {
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInSubmitReqVO");
        Object reqVO = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("setId", Long.class).invoke(reqVO, id);
        clazz.getMethod("setStartUserSelectAssignees", Map.class).invoke(reqVO, assignees);
        return reqVO;
    }

    private Object createCancelReqVO(Long id, String reason) throws Exception {
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInCancelApprovalReqVO");
        Object reqVO = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("setId", Long.class).invoke(reqVO, id);
        clazz.getMethod("setReason", String.class).invoke(reqVO, reason);
        return reqVO;
    }

    private void assertSubmitFail(Method method, Object target, Object reqVO) throws Exception {
        try {
            method.invoke(target, 9527L, reqVO);
        } catch (java.lang.reflect.InvocationTargetException ex) {
            if (ex.getTargetException() instanceof cn.weitee.erp.framework.common.exception.ServiceException serviceException) {
                assertEquals(cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_IN_BPM_SUBMIT_FAIL.getCode(),
                        serviceException.getCode());
                return;
            }
            throw ex;
        }
        throw new AssertionError("Expected ServiceException");
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
