package cn.iocoder.yudao.module.erp.service.purchase;

import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
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

class ErpPurchaseInBpmServiceImplTest {

    @Test
    void submitPurchaseIn_shouldStartProcessAndBindProcessInstance() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpPurchaseInDO> purchaseInRef = new AtomicReference<>(
                new ErpPurchaseInDO()
                        .setId(31L)
                        .setNo("PI-2026-001")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus())
                        .setSupplierId(301L)
                        .setOrderId(501L)
                        .setTotalPrice(new BigDecimal("1880.66"))
                        .setInTime(LocalDateTime.of(2026, 4, 9, 10, 0))
        );
        AtomicReference<ErpPurchaseInDO> updatedPurchaseInRef = new AtomicReference<>();
        AtomicReference<Object> createReqRef = new AtomicReference<>();

        setField(service, "purchaseInMapper", createProxy(ErpPurchaseInMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return purchaseInRef.get();
            }
            if ("updateById".equals(methodName)) {
                updatedPurchaseInRef.set((ErpPurchaseInDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "processInstanceApi", createProxyByName(
                "cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi",
                (methodName, args) -> {
                    if ("createProcessInstance".equals(methodName)) {
                        createReqRef.set(args[1]);
                        return "PI-IN-20260409-001";
                    }
                    return null;
                }));

        Object reqVO = createSubmitReqVO(31L, Map.of("task_1", List.of(7L, 8L)));
        Method method = service.getClass().getMethod("submitPurchaseIn", Long.class, reqVO.getClass());

        Object result = method.invoke(service, 9527L, reqVO);

        assertEquals("PI-IN-20260409-001", result);
        assertNotNull(createReqRef.get());
        assertEquals("31", readProperty(createReqRef.get(), "getBusinessKey"));
        Map<?, ?> variables = (Map<?, ?>) readProperty(createReqRef.get(), "getVariables");
        assertEquals("PI-2026-001", variables.get("purchaseInNo"));
        assertEquals(new BigDecimal("1880.66"), variables.get("totalPrice"));
        assertEquals(301L, variables.get("supplierId"));
        assertEquals(501L, variables.get("purchaseOrderId"));
        assertEquals(31L, updatedPurchaseInRef.get().getId());
        assertEquals("PI-IN-20260409-001", invokeGetter(updatedPurchaseInRef.get(), "getProcessInstanceId"));
    }

    @Test
    void cancelPurchaseInApproval_shouldCancelProcessAndClearBinding() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpPurchaseInDO> purchaseInRef = new AtomicReference<>(
                withProcessInstance(new ErpPurchaseInDO()
                        .setId(32L)
                        .setNo("PI-2026-CANCEL")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus()), "PI-IN-TO-CANCEL")
        );
        AtomicReference<Long> clearedInIdRef = new AtomicReference<>();
        AtomicReference<Object> cancelReqRef = new AtomicReference<>();

        setField(service, "purchaseInMapper", createProxy(ErpPurchaseInMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return purchaseInRef.get();
            }
            if ("clearProcessInstanceId".equals(methodName)) {
                clearedInIdRef.set((Long) args[0]);
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

        Object reqVO = createCancelReqVO(32L, "cancel test");
        Method method = service.getClass().getMethod("cancelPurchaseInApproval", Long.class, reqVO.getClass());
        method.invoke(service, 9527L, reqVO);

        assertNotNull(cancelReqRef.get());
        assertEquals("PI-IN-TO-CANCEL", readProperty(cancelReqRef.get(), "getId"));
        assertEquals("cancel test", readProperty(cancelReqRef.get(), "getReason"));
        assertEquals(32L, clearedInIdRef.get());
    }

    @Test
    void handleProcessInstanceResult_shouldTranslateApproveAndIgnoreStaleProcessInstance() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpPurchaseInDO> purchaseInRef = new AtomicReference<>(
                withProcessInstance(new ErpPurchaseInDO().setId(33L), "PI-MATCH")
        );
        AtomicReference<List<Object>> callbackArgsRef = new AtomicReference<>();
        AtomicReference<Long> clearedInIdRef = new AtomicReference<>();

        setField(service, "purchaseInMapper", createProxy(ErpPurchaseInMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return purchaseInRef.get();
            }
            if ("clearProcessInstanceId".equals(methodName)) {
                clearedInIdRef.set((Long) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "purchaseInService", createProxy(ErpPurchaseInService.class, (methodName, args) -> {
            if ("updatePurchaseInStatusByBpm".equals(methodName)) {
                callbackArgsRef.set(List.of(args));
            }
            return null;
        }));

        Method method = service.getClass().getMethod("handleProcessInstanceResult",
                Long.class, String.class, Integer.class, String.class);
        method.invoke(service, 33L, "PI-MATCH", bpmStatus("APPROVE"), "approved");

        assertNotNull(callbackArgsRef.get());
        assertEquals(33L, callbackArgsRef.get().get(0));
        assertEquals("PI-MATCH", callbackArgsRef.get().get(1));
        assertEquals(ErpAuditStatus.APPROVE.getStatus(), callbackArgsRef.get().get(2));
        assertEquals("approved", callbackArgsRef.get().get(3));
        assertNull(clearedInIdRef.get());

        callbackArgsRef.set(null);
        purchaseInRef.set(withProcessInstance(new ErpPurchaseInDO().setId(33L), "PI-NEW"));
        method.invoke(service, 33L, "PI-OLD", bpmStatus("REJECT"), "stale");
        assertNull(callbackArgsRef.get());
    }

    private Object instantiateService() throws Exception {
        Class<?> clazz = Class.forName("cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseInBpmServiceImpl");
        return clazz.getDeclaredConstructor().newInstance();
    }

    private Object createSubmitReqVO(Long id, Map<String, List<Long>> assignees) throws Exception {
        Class<?> clazz = Class.forName("cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInSubmitReqVO");
        Object reqVO = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("setId", Long.class).invoke(reqVO, id);
        clazz.getMethod("setStartUserSelectAssignees", Map.class).invoke(reqVO, assignees);
        return reqVO;
    }

    private Object createCancelReqVO(Long id, String reason) throws Exception {
        Class<?> clazz = Class.forName("cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInCancelApprovalReqVO");
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

    private Object invokeGetter(Object target, String methodName) throws Exception {
        return target.getClass().getMethod(methodName).invoke(target);
    }

    private ErpPurchaseInDO withProcessInstance(ErpPurchaseInDO purchaseIn, String processInstanceId) throws Exception {
        purchaseIn.getClass().getMethod("setProcessInstanceId", String.class).invoke(purchaseIn, processInstanceId);
        return purchaseIn;
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
