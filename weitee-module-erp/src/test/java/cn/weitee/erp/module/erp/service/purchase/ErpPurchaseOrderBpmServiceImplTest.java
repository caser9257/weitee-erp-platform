package cn.weitee.erp.module.erp.service.purchase;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderAuditLogDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseOrderAuditLogMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpPurchaseOrderAuditActionTypeConstants;
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
        List<ErpPurchaseOrderDO> updatedOrders = new ArrayList<>();
        AtomicReference<Long> submitBizIdRef = new AtomicReference<>();

        setField(service, "purchaseOrderMapper", createProxy(ErpPurchaseOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return purchaseOrderRef.get();
            }
            if ("updateById".equals(methodName)) {
                updatedOrders.add((ErpPurchaseOrderDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "approvalRuntimeService", createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> {
            if ("submit".equals(methodName)) {
                submitBizIdRef.set((Long) args[1]);
                return "PI-PO-20260408-001";
            }
            return null;
        }));
        setField(service, "purchaseOrderAuditLogMapper", createProxy(ErpPurchaseOrderAuditLogMapper.class, (methodName, args) -> 1));

        Object reqVO = createSubmitReqVO(21L, Map.of("task_1", List.of(7L, 8L)));
        Method method = service.getClass().getMethod("submitPurchaseOrder", Long.class, reqVO.getClass());

        Object result = method.invoke(service, 9527L, reqVO);

        // afterCommit 模式：submit 返回 null，processInstanceId 由 afterCommit 异步写入
        assertNull(result);
        assertEquals(21L, submitBizIdRef.get());
        // 第一次 updateById：设置 PROCESS 状态，processInstanceId=null
        assertEquals(21L, updatedOrders.get(0).getId());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), updatedOrders.get(0).getStatus());
        assertNull(updatedOrders.get(0).getProcessInstanceId());
        // 第二次 updateById（afterCommit）：设置 processInstanceId
        assertEquals(21L, updatedOrders.get(1).getId());
        assertEquals("PI-PO-20260408-001", updatedOrders.get(1).getProcessInstanceId());
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
        setField(service, "approvalRuntimeService", createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> "PI-PO-NEW"));
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
        AtomicReference<Long> cancelBizIdRef = new AtomicReference<>();
        AtomicReference<String> cancelReasonRef = new AtomicReference<>();

        setField(service, "purchaseOrderMapper", createProxy(ErpPurchaseOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return purchaseOrderRef.get();
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
        setField(service, "purchaseOrderAuditLogMapper", createProxy(ErpPurchaseOrderAuditLogMapper.class, (methodName, args) -> 1));

        Object reqVO = createCancelReqVO(23L, "cancel test");
        Method method = service.getClass().getMethod("cancelPurchaseOrderApproval", Long.class, reqVO.getClass());
        method.invoke(service, 9527L, reqVO);

        // afterCommit 模式：BPM 撤回在 afterCommit 中执行
        assertEquals(23L, cancelBizIdRef.get());
        assertEquals("cancel test", cancelReasonRef.get());
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
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.service.purchase.ErpPurchaseOrderBpmServiceImpl");
        return clazz.getDeclaredConstructor().newInstance();
    }

    private Object createSubmitReqVO(Long id, Map<String, List<Long>> assignees) throws Exception {
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderSubmitReqVO");
        Object reqVO = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("setId", Long.class).invoke(reqVO, id);
        clazz.getMethod("setStartUserSelectAssignees", Map.class).invoke(reqVO, assignees);
        return reqVO;
    }

    private Object createCancelReqVO(Long id, String reason) throws Exception {
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderCancelApprovalReqVO");
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
        Class<?> clazz = Class.forName("cn.weitee.erp.module.bpm.enums.task.BpmProcessInstanceStatusEnum");
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
