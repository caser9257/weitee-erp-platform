package cn.weitee.erp.module.erp.service.purchase;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderBatchUpdateReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderBatchUpdateResultVO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderAuditLogDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderRejectLogDO;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseOrderAuditLogMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseOrderRejectLogMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpPurchaseOrderAuditActionTypeConstants;
import cn.weitee.erp.module.erp.framework.event.PurchaseOrderChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_ORDER_DELETE_FAIL_APPROVE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_ORDER_NOT_APPROVE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_ORDER_UPDATE_FAIL_APPROVE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_ORDER_UPDATE_FAIL_PROCESSING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErpPurchaseOrderServiceImplTest {

    private static final String REJECT_REASON = "supplier info missing";

    private final AtomicReference<ErpPurchaseOrderDO> purchaseOrderRef = new AtomicReference<>();
    private final AtomicReference<Integer> updateCountRef = new AtomicReference<>(1);
    private final AtomicReference<ErpPurchaseOrderDO> lastUpdateObjRef = new AtomicReference<>();
    private final List<ErpPurchaseOrderRejectLogDO> rejectLogs = new ArrayList<>();
    private final List<ErpPurchaseOrderAuditLogDO> auditLogs = new ArrayList<>();
    private final List<PurchaseOrderChangedEvent> publishedEvents = new ArrayList<>();
    private final List<Long> deletedOrderIds = new ArrayList<>();
    private final List<Long> deletedOrderItemIds = new ArrayList<>();

    private ErpPurchaseOrderServiceImpl purchaseOrderService;

    @BeforeEach
    void setUp() throws Exception {
        purchaseOrderService = new ErpPurchaseOrderServiceImpl();
        purchaseOrderRef.set(null);
        updateCountRef.set(1);
        lastUpdateObjRef.set(null);
        rejectLogs.clear();
        auditLogs.clear();
        publishedEvents.clear();
        deletedOrderIds.clear();
        deletedOrderItemIds.clear();

        setField(purchaseOrderService, "erpPurchaseOrderMapper", createPurchaseOrderMapperProxy());
        setField(purchaseOrderService, "erpPurchaseOrderRejectLogMapper", createRejectLogMapperProxy());
        setField(purchaseOrderService, "erpPurchaseOrderAuditLogMapper", createAuditLogMapperProxy());
        setField(purchaseOrderService, "erpPurchaseOrderItemMapper", createProxy(ErpPurchaseOrderItemMapper.class, (methodName, args) -> {
            if ("deleteByOrderId".equals(methodName)) {
                deletedOrderItemIds.add((Long) args[0]);
                return 1;
            }
            return 1;
        }));
        setField(purchaseOrderService, "eventPublisher", createProxy(ApplicationEventPublisher.class, (methodName, args) -> {
            if ("publishEvent".equals(methodName)) {
                publishedEvents.add((PurchaseOrderChangedEvent) args[0]);
            }
            return null;
        }));
    }

    @Test
    void updatePurchaseOrderStatusByBpm_shouldWriteRejectSummaryAndRejectLog() throws Exception {
        purchaseOrderRef.set(new ErpPurchaseOrderDO().setId(1L).setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId("PI-OLD"));

        invokeUpdatePurchaseOrderStatusByBpm(1L, "PI-NEW", ErpAuditStatus.REJECT.getStatus(), REJECT_REASON);

        assertEquals(ErpAuditStatus.REJECT.getStatus(), lastUpdateObjRef.get().getStatus());
        assertEquals("PI-NEW", lastUpdateObjRef.get().getProcessInstanceId());
        assertEquals(REJECT_REASON, lastUpdateObjRef.get().getLastRejectReason());
        assertTrue(lastUpdateObjRef.get().getLastRejectTime() != null);
        assertEquals(1, rejectLogs.size());
        assertEquals(REJECT_REASON, rejectLogs.get(0).getReason());
        assertEquals(1, auditLogs.size());
        assertEquals(ErpPurchaseOrderAuditActionTypeConstants.REJECT, auditLogs.get(0).getActionType());
    }

    @Test
    void validatePurchaseOrder_shouldStillRequireApprovedStatus() {
        purchaseOrderRef.set(new ErpPurchaseOrderDO().setId(1L).setStatus(ErpAuditStatus.PROCESS.getStatus()));

        ServiceException ex = assertThrows(ServiceException.class, () -> purchaseOrderService.validatePurchaseOrder(1L));

        assertEquals(PURCHASE_ORDER_NOT_APPROVE.getCode(), ex.getCode());
    }

    @Test
    void deletePurchaseOrder_shouldDeleteOrderAndPublishCancelledEvent() {
        purchaseOrderRef.set(new ErpPurchaseOrderDO().setId(1L).setNo("PO-001")
                .setStatus(ErpAuditStatus.REJECT.getStatus())
                .setInCount(BigDecimal.ZERO)
                .setReturnCount(BigDecimal.ZERO));

        purchaseOrderService.deletePurchaseOrder(Collections.singletonList(1L));

        assertEquals(Collections.singletonList(1L), deletedOrderIds);
        assertEquals(Collections.singletonList(1L), deletedOrderItemIds);
        assertEquals(1, publishedEvents.size());
        assertEquals(1L, publishedEvents.get(0).getPurchaseOrderId());
        assertEquals(PurchaseOrderChangedEvent.ChangeType.ORDER_CANCELLED, publishedEvents.get(0).getChangeType());
    }

    @Test
    void deletePurchaseOrder_shouldRejectApprovedOrder() {
        purchaseOrderRef.set(new ErpPurchaseOrderDO().setId(1L).setNo("PO-001")
                .setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setInCount(BigDecimal.ZERO)
                .setReturnCount(BigDecimal.ZERO));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> purchaseOrderService.deletePurchaseOrder(Collections.singletonList(1L)));

        assertEquals(PURCHASE_ORDER_DELETE_FAIL_APPROVE.getCode(), ex.getCode());
    }

    @Test
    void updatePurchaseOrderBatch_shouldUpdateRemarkForEditableOrders() {
        purchaseOrderRef.set(new ErpPurchaseOrderDO().setId(1L).setNo("PO-001")
                .setStatus(ErpAuditStatus.REJECT.getStatus())
                .setInCount(BigDecimal.ZERO)
                .setReturnCount(BigDecimal.ZERO));

        ErpPurchaseOrderBatchUpdateResultVO result = purchaseOrderService.updatePurchaseOrderBatch(
                createBatchReqVO("remark", "extra remark"));

        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertEquals(Collections.singletonList(1L), result.getUpdatedIds());
        assertEquals("extra remark", lastUpdateObjRef.get().getRemark());
    }

    @Test
    void updatePurchaseOrderBatch_shouldRejectApprovedOrder() {
        purchaseOrderRef.set(new ErpPurchaseOrderDO().setId(1L).setNo("PO-001")
                .setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setInCount(BigDecimal.ZERO)
                .setReturnCount(BigDecimal.ZERO));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> purchaseOrderService.updatePurchaseOrderBatch(createBatchReqVO("remark", "extra remark")));

        assertEquals(PURCHASE_ORDER_UPDATE_FAIL_APPROVE.getCode(), ex.getCode());
    }

    @Test
    void updatePurchaseOrderBatch_shouldRejectProcessingOrder() {
        purchaseOrderRef.set(new ErpPurchaseOrderDO().setId(1L).setNo("PO-001")
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId("PI-001")
                .setInCount(BigDecimal.ZERO)
                .setReturnCount(BigDecimal.ZERO));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> purchaseOrderService.updatePurchaseOrderBatch(createBatchReqVO("remark", "extra remark")));

        assertEquals(PURCHASE_ORDER_UPDATE_FAIL_PROCESSING.getCode(), ex.getCode());
    }

    @Test
    void rollbackPurchaseOrderStatusToDraftByBpm_shouldWriteDraftAuditLog() throws Exception {
        purchaseOrderRef.set(new ErpPurchaseOrderDO().setId(2L).setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId("PI-CANCEL"));

        invokeRollbackPurchaseOrderStatusToDraftByBpm(2L, "PI-CANCEL", "cancel approval");

        assertEquals(ErpAuditStatus.DRAFT.getStatus(), lastUpdateObjRef.get().getStatus());
        assertEquals(null, lastUpdateObjRef.get().getProcessInstanceId());
        assertEquals(1, auditLogs.size());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), auditLogs.get(0).getBeforeStatus());
        assertEquals(ErpAuditStatus.DRAFT.getStatus(), auditLogs.get(0).getAfterStatus());
        assertEquals("cancel approval", auditLogs.get(0).getReason());
    }

    @Test
    void rollbackPurchaseOrderStatusToDraftByBpm_shouldThrowWhenNotProcessing() {
        purchaseOrderRef.set(new ErpPurchaseOrderDO().setId(3L).setStatus(ErpAuditStatus.DRAFT.getStatus())
                .setProcessInstanceId("PI-DRAFT"));
        updateCountRef.set(0);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> {
                    try {
                        invokeRollbackPurchaseOrderStatusToDraftByBpm(3L, "PI-DRAFT", "cancel failed");
                    } catch (Exception e) {
                        throw unwrap(e);
                    }
                });

        ServiceException serviceException = assertInstanceOf(ServiceException.class, ex);
        assertEquals(PURCHASE_ORDER_UPDATE_FAIL_PROCESSING.getCode(), serviceException.getCode());
    }

    @Test
    void countUpdateMethods_shouldDeclareTransactionalBoundary() throws Exception {
        assertTransactional("updatePurchaseOrderInCount", Long.class, java.util.Map.class);
        assertTransactional("updatePurchaseOrderReturnCount", Long.class, java.util.Map.class);
    }

    private ErpPurchaseOrderBatchUpdateReqVO createBatchReqVO(String fieldKey, String value) {
        ErpPurchaseOrderBatchUpdateReqVO reqVO = new ErpPurchaseOrderBatchUpdateReqVO();
        reqVO.setIds(Collections.singletonList(1L));
        reqVO.setFieldKey(fieldKey);
        reqVO.setMode("overwrite");
        reqVO.setValue(value);
        return reqVO;
    }

    private ErpPurchaseOrderMapper createPurchaseOrderMapperProxy() {
        return createProxy(ErpPurchaseOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return purchaseOrderRef.get();
            }
            if ("selectByIds".equals(methodName)) {
                return purchaseOrderRef.get() == null ? Collections.emptyList() : Collections.singletonList(purchaseOrderRef.get());
            }
            if ("updateById".equals(methodName) || "updateByIdAndStatus".equals(methodName)) {
                lastUpdateObjRef.set((ErpPurchaseOrderDO) ("updateById".equals(methodName) ? args[0] : args[2]));
                return updateCountRef.get();
            }
            if ("deleteById".equals(methodName)) {
                deletedOrderIds.add((Long) args[0]);
                return 1;
            }
            if ("resetStatusToDraftByBpm".equals(methodName)) {
                lastUpdateObjRef.set(new ErpPurchaseOrderDO()
                        .setId((Long) args[0])
                        .setStatus(ErpAuditStatus.DRAFT.getStatus())
                        .setProcessInstanceId(null));
                return updateCountRef.get();
            }
            return 1;
        });
    }

    private ErpPurchaseOrderRejectLogMapper createRejectLogMapperProxy() {
        return createProxy(ErpPurchaseOrderRejectLogMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                rejectLogs.add((ErpPurchaseOrderRejectLogDO) args[0]);
                return 1;
            }
            if ("selectListByOrderId".equals(methodName)) {
                return rejectLogs;
            }
            return null;
        });
    }

    private ErpPurchaseOrderAuditLogMapper createAuditLogMapperProxy() {
        return createProxy(ErpPurchaseOrderAuditLogMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                auditLogs.add((ErpPurchaseOrderAuditLogDO) args[0]);
                return 1;
            }
            if ("selectListByOrderId".equals(methodName)) {
                return auditLogs;
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

    private void invokeUpdatePurchaseOrderStatusByBpm(Long id, String processInstanceId, Integer status, String reason)
            throws Exception {
        Method method = purchaseOrderService.getClass().getMethod("updatePurchaseOrderStatusByBpm",
                Long.class, String.class, Integer.class, String.class);
        method.invoke(purchaseOrderService, id, processInstanceId, status, reason);
    }

    private void invokeRollbackPurchaseOrderStatusToDraftByBpm(Long id, String processInstanceId, String reason)
            throws Exception {
        Method method = purchaseOrderService.getClass().getMethod("rollbackPurchaseOrderStatusToDraftByBpm",
                Long.class, String.class, String.class);
        method.invoke(purchaseOrderService, id, processInstanceId, reason);
    }

    private RuntimeException unwrap(Exception exception) {
        if (exception instanceof java.lang.reflect.InvocationTargetException invocationTargetException
                && invocationTargetException.getTargetException() instanceof RuntimeException runtimeException) {
            return runtimeException;
        }
        return new RuntimeException(exception);
    }

    private void assertTransactional(String methodName, Class<?>... parameterTypes) throws Exception {
        Method method = purchaseOrderService.getClass().getMethod(methodName, parameterTypes);
        assertTrue(method.isAnnotationPresent(Transactional.class),
                () -> methodName + " should declare @Transactional");
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }

}
