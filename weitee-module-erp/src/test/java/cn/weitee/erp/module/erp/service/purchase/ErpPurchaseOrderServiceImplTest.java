package cn.weitee.erp.module.erp.service.purchase;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderBatchUpdateReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderBatchUpdateResultVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpPurchaseSuggestDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderAuditLogDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderRejectLogDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpPurchaseSuggestMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseOrderAuditLogMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseOrderRejectLogMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpPurchaseOrderAuditActionTypeConstants;
import cn.weitee.erp.module.erp.enums.mrp.ErpMrpSuggestStatusEnum;
import cn.weitee.erp.module.erp.framework.event.PurchaseOrderChangedEvent;
import cn.weitee.erp.module.erp.service.finance.ErpAccountService;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.system.api.user.AdminUserApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_ORDER_DELETE_FAIL_APPROVE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_ORDER_UPDATE_FAIL_APPROVE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_ORDER_UPDATE_FAIL_PROCESSING;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_ORDER_NOT_APPROVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErpPurchaseOrderServiceImplTest {

    private static final String REJECT_REASON = "供应商信息不完整";

    private final AtomicReference<ErpPurchaseOrderDO> purchaseOrderRef = new AtomicReference<>();
    private final AtomicReference<Integer> updateCountRef = new AtomicReference<>(1);
    private final AtomicReference<ErpPurchaseOrderDO> lastUpdateObjRef = new AtomicReference<>();
    private final AtomicReference<ErpPurchaseOrderDO> insertedPurchaseOrderRef = new AtomicReference<>();
    private final List<ErpPurchaseOrderItemDO> insertedPurchaseOrderItems = new ArrayList<>();
    private final List<ErpPurchaseOrderRejectLogDO> rejectLogs = new ArrayList<>();
    private final List<ErpPurchaseOrderAuditLogDO> auditLogs = new ArrayList<>();
    private final List<ErpPurchaseSuggestDO> purchaseSuggests = new ArrayList<>();
    private final List<ErpPurchaseSuggestDO> updatedSuggests = new ArrayList<>();
    private final List<Object> publishedEvents = new ArrayList<>();

    private ErpPurchaseOrderServiceImpl purchaseOrderService;

    @BeforeEach
    void setUp() throws Exception {
        purchaseOrderService = new ErpPurchaseOrderServiceImpl();
        purchaseOrderRef.set(null);
        updateCountRef.set(1);
        lastUpdateObjRef.set(null);
        insertedPurchaseOrderRef.set(null);
        insertedPurchaseOrderItems.clear();
        rejectLogs.clear();
        auditLogs.clear();
        purchaseSuggests.clear();
        updatedSuggests.clear();
        publishedEvents.clear();
        setField(purchaseOrderService, "purchaseOrderMapper", createPurchaseOrderMapperProxy());
        setField(purchaseOrderService, "purchaseOrderRejectLogMapper", createRejectLogMapperProxy());
        setField(purchaseOrderService, "purchaseOrderAuditLogMapper", createAuditLogMapperProxy());
        setField(purchaseOrderService, "purchaseOrderItemMapper", createPurchaseOrderItemMapperProxy());
        setField(purchaseOrderService, "purchaseSuggestMapper", createPurchaseSuggestMapperProxy());
        setField(purchaseOrderService, "noRedisDAO", new ErpNoRedisDAO() {
            @Override
            public String generate(String prefix) {
                return "PO-TEST-001";
            }
        });
        setField(purchaseOrderService, "productService", createProxy(ErpProductService.class, (methodName, args) -> {
            if ("validProductList".equals(methodName)) {
                return List.of(new cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO()
                        .setId(201L)
                        .setUnitId(2001L));
            }
            return null;
        }));
        setField(purchaseOrderService, "supplierService", createProxy(ErpSupplierService.class, (methodName, args) -> null));
        setField(purchaseOrderService, "accountService", createProxy(ErpAccountService.class, (methodName, args) -> null));
        setField(purchaseOrderService, "eventPublisher", createEventPublisherProxy());
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
    void deletePurchaseOrder_shouldPublishCancelEventWhenNonApprovedOrderDeleted() {
        purchaseOrderRef.set(new ErpPurchaseOrderDO().setId(1L).setNo("PO-001")
                .setStatus(ErpAuditStatus.REJECT.getStatus())
                .setInCount(BigDecimal.ZERO)
                .setReturnCount(BigDecimal.ZERO));
        purchaseSuggests.add(new ErpPurchaseSuggestDO().setId(9001L)
                .setConvertPurchaseOrderId(1L)
                .setStatus(ErpMrpSuggestStatusEnum.CONVERTED.getStatus()));

        purchaseOrderService.deletePurchaseOrder(Collections.singletonList(1L));

        assertEquals(1, publishedEvents.size());
        PurchaseOrderChangedEvent event = assertInstanceOf(PurchaseOrderChangedEvent.class, publishedEvents.get(0));
        assertEquals(1L, event.getPurchaseOrderId());
        assertEquals(PurchaseOrderChangedEvent.ChangeType.ORDER_CANCELLED, event.getChangeType());
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
                createBatchReqVO("remark", "补充备注"));

        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertEquals(Collections.singletonList(1L), result.getUpdatedIds());
        assertEquals("补充备注", lastUpdateObjRef.get().getRemark());
    }

    @Test
    void updatePurchaseOrderBatch_shouldRejectApprovedOrder() {
        purchaseOrderRef.set(new ErpPurchaseOrderDO().setId(1L).setNo("PO-001")
                .setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setInCount(BigDecimal.ZERO)
                .setReturnCount(BigDecimal.ZERO));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> purchaseOrderService.updatePurchaseOrderBatch(createBatchReqVO("remark", "补充备注")));

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
                () -> purchaseOrderService.updatePurchaseOrderBatch(createBatchReqVO("remark", "补充备注")));

        assertEquals(PURCHASE_ORDER_UPDATE_FAIL_PROCESSING.getCode(), ex.getCode());
    }

    @Test
    void rollbackPurchaseOrderStatusToDraftByBpm_shouldWriteDraftAuditLog() throws Exception {
        purchaseOrderRef.set(new ErpPurchaseOrderDO().setId(2L).setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId("PI-CANCEL"));

        invokeRollbackPurchaseOrderStatusToDraftByBpm(2L, "PI-CANCEL", "撤回审批");

        assertEquals(ErpAuditStatus.DRAFT.getStatus(), lastUpdateObjRef.get().getStatus());
        assertEquals(null, lastUpdateObjRef.get().getProcessInstanceId());
        assertEquals(1, auditLogs.size());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), auditLogs.get(0).getBeforeStatus());
        assertEquals(ErpAuditStatus.DRAFT.getStatus(), auditLogs.get(0).getAfterStatus());
        assertEquals("撤回审批", auditLogs.get(0).getReason());
    }

    @Test
    void rollbackPurchaseOrderStatusToDraftByBpm_shouldThrowWhenNotProcessing() {
        purchaseOrderRef.set(new ErpPurchaseOrderDO().setId(3L).setStatus(ErpAuditStatus.DRAFT.getStatus())
                .setProcessInstanceId("PI-DRAFT"));
        updateCountRef.set(0);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> {
                    try {
                        invokeRollbackPurchaseOrderStatusToDraftByBpm(3L, "PI-DRAFT", "撤回失败");
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
            if ("selectByNo".equals(methodName)) {
                return null;
            }
            if ("selectByIds".equals(methodName)) {
                return purchaseOrderRef.get() == null ? Collections.emptyList() : Collections.singletonList(purchaseOrderRef.get());
            }
            if ("insert".equals(methodName)) {
                ErpPurchaseOrderDO purchaseOrder = (ErpPurchaseOrderDO) args[0];
                purchaseOrder.setId(88L);
                insertedPurchaseOrderRef.set(purchaseOrder);
                return 1;
            }
            if ("updateById".equals(methodName) || "updateByIdAndStatus".equals(methodName)) {
                lastUpdateObjRef.set((ErpPurchaseOrderDO) ("updateById".equals(methodName) ? args[0] : args[2]));
                return updateCountRef.get();
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

    private ErpPurchaseOrderItemMapper createPurchaseOrderItemMapperProxy() {
        return createProxy(ErpPurchaseOrderItemMapper.class, (methodName, args) -> {
            if ("insertBatch".equals(methodName)) {
                insertedPurchaseOrderItems.clear();
                insertedPurchaseOrderItems.addAll((List<ErpPurchaseOrderItemDO>) args[0]);
                return 1;
            }
            if ("selectListByOrderId".equals(methodName)) {
                return Collections.emptyList();
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

    private ErpPurchaseSuggestMapper createPurchaseSuggestMapperProxy() {
        return createProxy(ErpPurchaseSuggestMapper.class, (methodName, args) -> {
            if ("selectListByConvertPurchaseOrderIds".equals(methodName)) {
                return purchaseSuggests;
            }
            if ("updateById".equals(methodName)) {
                updatedSuggests.add((ErpPurchaseSuggestDO) args[0]);
                return 1;
            }
            return null;
        });
    }

    private ApplicationEventPublisher createEventPublisherProxy() {
        return createProxy(ApplicationEventPublisher.class, (methodName, args) -> {
            if ("publishEvent".equals(methodName)) {
                publishedEvents.add(args[0]);
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
        String actualFieldName = switch (fieldName) {
            case "purchaseOrderMapper" -> "erpPurchaseOrderMapper";
            case "purchaseOrderRejectLogMapper" -> "erpPurchaseOrderRejectLogMapper";
            case "purchaseOrderAuditLogMapper" -> "erpPurchaseOrderAuditLogMapper";
            case "purchaseOrderItemMapper" -> "erpPurchaseOrderItemMapper";
            case "purchaseSuggestMapper" -> "erpPurchaseSuggestMapper";
            default -> fieldName;
        };
        Field field = target.getClass().getDeclaredField(actualFieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private ErpPurchaseOrderSaveReqVO buildCreateReqVO() {
        ErpPurchaseOrderSaveReqVO reqVO = new ErpPurchaseOrderSaveReqVO();
        reqVO.setSupplierId(2L);
        reqVO.setOrderTime(LocalDateTime.of(2026, 7, 1, 12, 30));
        ErpPurchaseOrderSaveReqVO.Item item = new ErpPurchaseOrderSaveReqVO.Item();
        item.setProductId(201L);
        item.setProductUnitId(2001L);
        item.setCount(BigDecimal.ONE);
        item.setProductPrice(new BigDecimal("20.00"));
        reqVO.setItems(List.of(item));
        return reqVO;
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
