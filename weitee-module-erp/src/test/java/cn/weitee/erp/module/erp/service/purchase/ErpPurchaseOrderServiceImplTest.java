package cn.weitee.erp.module.erp.service.purchase;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderBatchUpdateReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderBatchUpdateResultVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpPurchaseSuggestDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderAuditLogDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderRejectLogDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpPurchaseSuggestMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseOrderAuditLogMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseOrderRejectLogMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpPurchaseOrderAuditActionTypeConstants;
import cn.weitee.erp.module.erp.enums.mrp.ErpMrpSuggestStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErpPurchaseOrderServiceImplTest {

    private static final String REJECT_REASON = "供应商信息不完整";

    private final AtomicReference<ErpPurchaseOrderDO> purchaseOrderRef = new AtomicReference<>();
    private final AtomicReference<Integer> updateCountRef = new AtomicReference<>(1);
    private final AtomicReference<ErpPurchaseOrderDO> lastUpdateObjRef = new AtomicReference<>();
    private final List<ErpPurchaseOrderRejectLogDO> rejectLogs = new ArrayList<>();
    private final List<ErpPurchaseOrderAuditLogDO> auditLogs = new ArrayList<>();
    private final List<ErpPurchaseSuggestDO> purchaseSuggests = new ArrayList<>();
    private final List<ErpPurchaseSuggestDO> updatedSuggests = new ArrayList<>();

    private ErpPurchaseOrderServiceImpl purchaseOrderService;

    @BeforeEach
    void setUp() throws Exception {
        purchaseOrderService = new ErpPurchaseOrderServiceImpl();
        purchaseOrderRef.set(null);
        updateCountRef.set(1);
        lastUpdateObjRef.set(null);
        rejectLogs.clear();
        auditLogs.clear();
        purchaseSuggests.clear();
        updatedSuggests.clear();
        setField(purchaseOrderService, "purchaseOrderMapper", createPurchaseOrderMapperProxy());
        setField(purchaseOrderService, "purchaseOrderRejectLogMapper", createRejectLogMapperProxy());
        setField(purchaseOrderService, "purchaseOrderAuditLogMapper", createAuditLogMapperProxy());
        setField(purchaseOrderService, "purchaseOrderItemMapper", createProxy(ErpPurchaseOrderItemMapper.class, (methodName, args) -> 1));
        setField(purchaseOrderService, "purchaseSuggestMapper", createPurchaseSuggestMapperProxy());
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
    void deletePurchaseOrder_shouldRollbackMrpSuggestWhenNonApprovedOrderDeleted() {
        purchaseOrderRef.set(new ErpPurchaseOrderDO().setId(1L).setNo("PO-001")
                .setStatus(ErpAuditStatus.REJECT.getStatus())
                .setInCount(BigDecimal.ZERO)
                .setReturnCount(BigDecimal.ZERO));
        purchaseSuggests.add(new ErpPurchaseSuggestDO().setId(9001L)
                .setConvertPurchaseOrderId(1L)
                .setStatus(ErpMrpSuggestStatusEnum.CONVERTED.getStatus()));

        purchaseOrderService.deletePurchaseOrder(Collections.singletonList(1L));

        assertEquals(1, updatedSuggests.size());
        assertEquals(ErpMrpSuggestStatusEnum.TO_CONFIRM.getStatus(), updatedSuggests.get(0).getStatus());
        assertEquals(null, updatedSuggests.get(0).getConvertPurchaseOrderId());
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
