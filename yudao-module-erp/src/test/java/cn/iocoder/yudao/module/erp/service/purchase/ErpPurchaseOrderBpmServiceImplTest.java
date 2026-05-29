package cn.iocoder.yudao.module.erp.service.purchase;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderAuditLogDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderAuditLogMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpPurchaseOrderAuditActionTypeConstants;
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

class ErpPurchaseOrderBpmServiceImplTest {

    @Test
    void submitPurchaseOrder_shouldStartProcessAndBindProcessInstance() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpPurchaseOrderDO> purchaseOrderRef = new AtomicReference<>(
                new ErpPurchaseOrderDO()
                        .setId(21L)
                        .setNo("PO-2026-001")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus())
                        .setSupplierId(301L)
                        .setTotalPrice(new BigDecimal("2350.80"))
                        .setOrderTime(LocalDateTime.of(2026, 4, 8, 15, 0))
        );
        AtomicReference<ErpPurchaseOrderDO> updatedOrderRef = new AtomicReference<>();
        AtomicReference<Object> createReqRef = new AtomicReference<>();

        setField(service, "purchaseOrderMapper", createProxy(ErpPurchaseOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return purchaseOrderRef.get();
            }
            if ("updateById".equals(methodName)) {
                updatedOrderRef.set((ErpPurchaseOrderDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "processInstanceApi", createProxyByName(
                "cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi",
                (methodName, args) -> {
                    if ("createProcessInstance".equals(methodName)) {
                        createReqRef.set(args[1]);
                        return "PI-PO-20260408-001";
                    }
                    return null;
                }));
        setField(service, "purchaseOrderAuditLogMapper", createProxy(ErpPurchaseOrderAuditLogMapper.class, (methodName, args) -> 1));

        Object reqVO = createSubmitReqVO(21L, Map.of("task_1", List.of(7L, 8L)));
        Method method = service.getClass().getMethod("submitPurchaseOrder", Long.class, reqVO.getClass());

        Object result = method.invoke(service, 9527L, reqVO);

        assertEquals("PI-PO-20260408-001", result);
        assertNotNull(createReqRef.get());
        assertEquals("21", readProperty(createReqRef.get(), "getBusinessKey"));
        Map<?, ?> variables = (Map<?, ?>) readProperty(createReqRef.get(), "getVariables");
        assertEquals("PO-2026-001", variables.get("purchaseOrderNo"));
        assertEquals(new BigDecimal("2350.80"), variables.get("totalPrice"));
        assertEquals(301L, variables.get("supplierId"));
        assertEquals("MANUAL", variables.get("sourceType"));
        assertEquals(21L, updatedOrderRef.get().getId());
        assertEquals("PI-PO-20260408-001", updatedOrderRef.get().getProcessInstanceId());
    }

    @Test
    void submitPurchaseOrder_shouldWriteResubmitAuditLogWhenOrderWasRejected() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpPurchaseOrderDO> purchaseOrderRef = new AtomicReference<>(
                new ErpPurchaseOrderDO()
                        .setId(22L)
                        .setNo("PO-2026-REJECT")
                        .setStatus(ErpAuditStatus.REJECT.getStatus())
                        .setSupplierId(302L)
                        .setTotalPrice(new BigDecimal("200"))
        );
        List<ErpPurchaseOrderAuditLogDO> auditLogs = new ArrayList<>();

        setField(service, "purchaseOrderMapper", createProxy(ErpPurchaseOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return purchaseOrderRef.get();
            }
            return 1;
        }));
        setField(service, "processInstanceApi", createProxyByName(
                "cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi",
                (methodName, args) -> "PI-PO-NEW"));
        setField(service, "purchaseOrderAuditLogMapper", createProxy(ErpPurchaseOrderAuditLogMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                auditLogs.add((ErpPurchaseOrderAuditLogDO) args[0]);
                return 1;
            }
            return null;
        }));

        Object reqVO = createSubmitReqVO(22L, new HashMap<>());
        Method method = service.getClass().getMethod("submitPurchaseOrder", Long.class, reqVO.getClass());
        method.invoke(service, 9527L, reqVO);

        assertEquals(1, auditLogs.size());
        assertEquals(ErpPurchaseOrderAuditActionTypeConstants.RESUBMIT, auditLogs.get(0).getActionType());
        assertEquals(ErpAuditStatus.REJECT.getStatus(), auditLogs.get(0).getBeforeStatus());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), auditLogs.get(0).getAfterStatus());
    }

    @Test
    void cancelPurchaseOrderApproval_shouldCancelProcessAndClearBinding() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpPurchaseOrderDO> purchaseOrderRef = new AtomicReference<>(
                new ErpPurchaseOrderDO()
                        .setId(23L)
                        .setNo("PO-2026-CANCEL")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus())
                        .setProcessInstanceId("PI-PO-TO-CANCEL")
        );
        AtomicReference<Long> clearedOrderIdRef = new AtomicReference<>();
        AtomicReference<Object> cancelReqRef = new AtomicReference<>();

        setField(service, "purchaseOrderMapper", createProxy(ErpPurchaseOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return purchaseOrderRef.get();
            }
            if ("clearProcessInstanceId".equals(methodName)) {
                clearedOrderIdRef.set((Long) args[0]);
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
        setField(service, "purchaseOrderAuditLogMapper", createProxy(ErpPurchaseOrderAuditLogMapper.class, (methodName, args) -> 1));

        Object reqVO = createCancelReqVO(23L, "cancel test");
        Method method = service.getClass().getMethod("cancelPurchaseOrderApproval", Long.class, reqVO.getClass());
        method.invoke(service, 9527L, reqVO);

        assertNotNull(cancelReqRef.get());
        assertEquals("PI-PO-TO-CANCEL", readProperty(cancelReqRef.get(), "getId"));
        assertEquals("cancel test", readProperty(cancelReqRef.get(), "getReason"));
        assertEquals(23L, clearedOrderIdRef.get());
    }

    @Test
    void handleProcessInstanceResult_shouldTranslateApproveAndIgnoreStaleProcessInstance() throws Exception {
        Object service = instantiateService();
        AtomicReference<ErpPurchaseOrderDO> purchaseOrderRef = new AtomicReference<>(
                new ErpPurchaseOrderDO().setId(24L).setProcessInstanceId("PI-MATCH")
        );
        AtomicReference<List<Object>> callbackArgsRef = new AtomicReference<>();
        AtomicReference<Long> clearedOrderIdRef = new AtomicReference<>();

        setField(service, "purchaseOrderMapper", createProxy(ErpPurchaseOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return purchaseOrderRef.get();
            }
            if ("clearProcessInstanceId".equals(methodName)) {
                clearedOrderIdRef.set((Long) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "purchaseOrderService", createProxy(ErpPurchaseOrderService.class, (methodName, args) -> {
            if ("updatePurchaseOrderStatusByBpm".equals(methodName)) {
                callbackArgsRef.set(List.of(args));
            }
            return null;
        }));
        setField(service, "purchaseOrderAuditLogMapper", createProxy(ErpPurchaseOrderAuditLogMapper.class, (methodName, args) -> 1));

        Method method = service.getClass().getMethod("handleProcessInstanceResult",
                Long.class, String.class, Integer.class, String.class);
        method.invoke(service, 24L, "PI-MATCH", bpmStatus("APPROVE"), "approved");

        assertNotNull(callbackArgsRef.get());
        assertEquals(24L, callbackArgsRef.get().get(0));
        assertEquals("PI-MATCH", callbackArgsRef.get().get(1));
        assertEquals(ErpAuditStatus.APPROVE.getStatus(), callbackArgsRef.get().get(2));
        assertEquals("approved", callbackArgsRef.get().get(3));
        assertNull(clearedOrderIdRef.get());

        callbackArgsRef.set(null);
        purchaseOrderRef.set(new ErpPurchaseOrderDO().setId(24L).setProcessInstanceId("PI-NEW"));
        method.invoke(service, 24L, "PI-OLD", bpmStatus("REJECT"), "stale");
        assertNull(callbackArgsRef.get());
    }

    private Object instantiateService() throws Exception {
        Class<?> clazz = Class.forName("cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseOrderBpmServiceImpl");
        return clazz.getDeclaredConstructor().newInstance();
    }

    private Object createSubmitReqVO(Long id, Map<String, List<Long>> assignees) throws Exception {
        Class<?> clazz = Class.forName("cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderSubmitReqVO");
        Object reqVO = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("setId", Long.class).invoke(reqVO, id);
        clazz.getMethod("setStartUserSelectAssignees", Map.class).invoke(reqVO, assignees);
        return reqVO;
    }

    private Object createCancelReqVO(Long id, String reason) throws Exception {
        Class<?> clazz = Class.forName("cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderCancelApprovalReqVO");
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
