package cn.iocoder.yudao.module.erp.service.sale;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderUpdateStatusReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderAuditLogDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderRejectLogDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpMrpStockReservationMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderAuditLogMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderRejectLogMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpSaleOrderAuditActionTypeConstants;
import cn.iocoder.yudao.module.erp.framework.event.ErpSaleOrderApprovedEvent;
import cn.iocoder.yudao.module.erp.service.mrp.ErpMrpStockReservationSummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SALE_ORDER_DELETE_FAIL_PROCESSING;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SALE_ORDER_REJECT_REASON_REQUIRED;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SALE_ORDER_STATUS_UPDATE_ILLEGAL;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.SALE_ORDER_UPDATE_FAIL_PROCESSING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErpSaleOrderServiceImplTest {

    private static final String REJECT_REASON = "资料不完整";

    private final AtomicReference<ErpSaleOrderDO> saleOrderRef = new AtomicReference<>();
    private final AtomicReference<Integer> updateCountRef = new AtomicReference<>(1);
    private final AtomicReference<ErpSaleOrderDO> lastUpdateObjRef = new AtomicReference<>();
    private final List<ErpSaleOrderRejectLogDO> rejectLogs = new ArrayList<>();
    private final List<ErpSaleOrderAuditLogDO> auditLogs = new ArrayList<>();
    private final List<Object> publishedEvents = new ArrayList<>();

    private ErpSaleOrderServiceImpl saleOrderService;

    @BeforeEach
    void setUp() throws Exception {
        saleOrderService = new ErpSaleOrderServiceImpl();
        saleOrderRef.set(null);
        updateCountRef.set(1);
        lastUpdateObjRef.set(null);
        rejectLogs.clear();
        auditLogs.clear();
        publishedEvents.clear();
        setField(saleOrderService, "saleOrderMapper", createSaleOrderMapperProxy());
        setField(saleOrderService, "saleOrderRejectLogMapper", createRejectLogMapperProxy());
        setField(saleOrderService, "saleOrderAuditLogMapper", createAuditLogMapperProxy());
        setField(saleOrderService, "mrpStockReservationMapper", createProxy(ErpMrpStockReservationMapper.class,
                (methodName, args) -> "selectListBySourceOrderIds".equals(methodName) ? Collections.emptyList() : 1));
        setField(saleOrderService, "mrpStockReservationSummaryService",
                createProxy(ErpMrpStockReservationSummaryService.class, (methodName, args) -> null));
        setField(saleOrderService, "eventPublisher", createEventPublisherProxy());
    }

    @Test
    void updateSaleOrderStatus_shouldWriteRejectSummaryAndLogWhenRejectSucceeds() {
        saleOrderRef.set(new ErpSaleOrderDO().setId(1L).setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setOutCount(BigDecimal.ZERO).setReturnCount(BigDecimal.ZERO));

        saleOrderService.updateSaleOrderStatus(buildReqVO(1L, ErpAuditStatus.REJECT.getStatus(), REJECT_REASON));

        assertEquals(0, publishedEvents.size());
        assertEquals(ErpAuditStatus.REJECT.getStatus(), lastUpdateObjRef.get().getStatus());
        assertEquals(REJECT_REASON, lastUpdateObjRef.get().getLastRejectReason());
        assertTrue(lastUpdateObjRef.get().getLastRejectTime() != null);
        assertEquals(1, rejectLogs.size());
        assertEquals(1L, rejectLogs.get(0).getOrderId());
        assertEquals(REJECT_REASON, rejectLogs.get(0).getReason());
        assertEquals(1, auditLogs.size());
        assertEquals(ErpSaleOrderAuditActionTypeConstants.REJECT, auditLogs.get(0).getActionType());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), auditLogs.get(0).getBeforeStatus());
        assertEquals(ErpAuditStatus.REJECT.getStatus(), auditLogs.get(0).getAfterStatus());
    }

    @Test
    void updateSaleOrderStatus_shouldFailWhenRejectReasonMissing() {
        saleOrderRef.set(new ErpSaleOrderDO().setId(1L).setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setOutCount(BigDecimal.ZERO).setReturnCount(BigDecimal.ZERO));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> saleOrderService.updateSaleOrderStatus(buildReqVO(1L, ErpAuditStatus.REJECT.getStatus(), " ")));

        assertEquals(SALE_ORDER_REJECT_REASON_REQUIRED.getCode(), ex.getCode());
        assertEquals(0, rejectLogs.size());
        assertEquals(0, auditLogs.size());
        assertEquals(0, publishedEvents.size());
    }

    @Test
    void updateSaleOrderStatus_shouldNotPublishEventWhenResubmitSucceeds() {
        saleOrderRef.set(new ErpSaleOrderDO().setId(1L).setStatus(ErpAuditStatus.REJECT.getStatus())
                .setOutCount(BigDecimal.ZERO).setReturnCount(BigDecimal.ZERO));

        saleOrderService.updateSaleOrderStatus(buildReqVO(1L, ErpAuditStatus.PROCESS.getStatus(), null));

        assertEquals(ErpAuditStatus.PROCESS.getStatus(), lastUpdateObjRef.get().getStatus());
        assertEquals(0, publishedEvents.size());
        assertEquals(0, rejectLogs.size());
        assertEquals(1, auditLogs.size());
        assertEquals(ErpSaleOrderAuditActionTypeConstants.RESUBMIT, auditLogs.get(0).getActionType());
    }

    @Test
    void updateSaleOrderStatus_shouldPublishEventWhenApproveSucceeds() {
        saleOrderRef.set(new ErpSaleOrderDO().setId(1L).setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setOutCount(BigDecimal.ZERO).setReturnCount(BigDecimal.ZERO));

        saleOrderService.updateSaleOrderStatus(buildReqVO(1L, ErpAuditStatus.APPROVE.getStatus(), null));

        assertEquals(1, publishedEvents.size());
        ErpSaleOrderApprovedEvent event = assertInstanceOf(ErpSaleOrderApprovedEvent.class, publishedEvents.get(0));
        assertEquals(1L, event.getSaleOrderId());
        assertEquals(1, auditLogs.size());
        assertEquals(ErpSaleOrderAuditActionTypeConstants.APPROVE, auditLogs.get(0).getActionType());
    }

    @Test
    void updateSaleOrderStatus_shouldWriteAuditLogWhenReverseApproveSucceeds() {
        saleOrderRef.set(new ErpSaleOrderDO().setId(1L).setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setOutCount(BigDecimal.ZERO).setReturnCount(BigDecimal.ZERO));

        saleOrderService.updateSaleOrderStatus(buildReqVO(1L, ErpAuditStatus.PROCESS.getStatus(), null));

        assertEquals(ErpAuditStatus.PROCESS.getStatus(), lastUpdateObjRef.get().getStatus());
        assertEquals(1, auditLogs.size());
        assertEquals(ErpSaleOrderAuditActionTypeConstants.REVERSE_APPROVE, auditLogs.get(0).getActionType());
        assertEquals(0, publishedEvents.size());
    }

    @Test
    void updateSaleOrderStatus_shouldRejectIllegalTransition() {
        saleOrderRef.set(new ErpSaleOrderDO().setId(1L).setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setOutCount(BigDecimal.ZERO).setReturnCount(BigDecimal.ZERO));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> saleOrderService.updateSaleOrderStatus(buildReqVO(1L, ErpAuditStatus.PROCESS.getStatus(), null)));

        assertEquals(SALE_ORDER_STATUS_UPDATE_ILLEGAL.getCode(), ex.getCode());
        assertEquals(0, publishedEvents.size());
        assertEquals(0, rejectLogs.size());
        assertEquals(0, auditLogs.size());
    }

    @Test
    void updateSaleOrderStatus_shouldNotPublishEventWhenUpdateFails() {
        saleOrderRef.set(new ErpSaleOrderDO().setId(1L).setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setOutCount(BigDecimal.ZERO).setReturnCount(BigDecimal.ZERO));
        updateCountRef.set(0);

        assertThrows(RuntimeException.class,
                () -> saleOrderService.updateSaleOrderStatus(buildReqVO(1L, ErpAuditStatus.APPROVE.getStatus(), null)));

        assertEquals(0, publishedEvents.size());
        assertEquals(0, auditLogs.size());
    }

    @Test
    void updateSaleOrder_shouldRejectWhenApprovalIsRunning() {
        saleOrderRef.set(new ErpSaleOrderDO().setId(1L).setNo("SO-001")
                .setStatus(ErpAuditStatus.PROCESS.getStatus()).setProcessInstanceId("PI-001"));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> saleOrderService.updateSaleOrder(new ErpSaleOrderSaveReqVO().setId(1L)));

        assertEquals(SALE_ORDER_UPDATE_FAIL_PROCESSING.getCode(), ex.getCode());
    }

    @Test
    void countUpdateMethods_shouldDeclareTransactionalBoundary() throws Exception {
        assertTransactional("updateSaleOrderOutCount", Long.class, java.util.Map.class);
        assertTransactional("updateSaleOrderReturnCount", Long.class, java.util.Map.class);
    }

    @Test
    void deleteSaleOrder_shouldRejectWhenApprovalIsRunning() {
        saleOrderRef.set(new ErpSaleOrderDO().setId(1L).setNo("SO-001")
                .setStatus(ErpAuditStatus.PROCESS.getStatus()).setProcessInstanceId("PI-001"));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> saleOrderService.deleteSaleOrder(Collections.singletonList(1L)));

        assertEquals(SALE_ORDER_DELETE_FAIL_PROCESSING.getCode(), ex.getCode());
    }

    @Test
    void updateSaleOrderStatus_shouldRejectManualTransitionForBpmManagedOrder() {
        saleOrderRef.set(new ErpSaleOrderDO().setId(1L).setStatus(ErpAuditStatus.REJECT.getStatus())
                .setProcessInstanceId("PI-OLD")
                .setOutCount(BigDecimal.ZERO).setReturnCount(BigDecimal.ZERO));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> saleOrderService.updateSaleOrderStatus(buildReqVO(1L, ErpAuditStatus.PROCESS.getStatus(), null)));

        assertEquals(SALE_ORDER_STATUS_UPDATE_ILLEGAL.getCode(), ex.getCode());
    }

    @Test
    void updateSaleOrderStatusByBpm_shouldWriteRejectSummaryAndRejectLog() throws Exception {
        saleOrderRef.set(new ErpSaleOrderDO().setId(1L).setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId("PI-OLD")
                .setOutCount(BigDecimal.ZERO).setReturnCount(BigDecimal.ZERO));

        invokeUpdateSaleOrderStatusByBpm(1L, "PI-NEW", ErpAuditStatus.REJECT.getStatus(), REJECT_REASON);

        assertEquals(ErpAuditStatus.REJECT.getStatus(), lastUpdateObjRef.get().getStatus());
        assertEquals("PI-NEW", lastUpdateObjRef.get().getProcessInstanceId());
        assertEquals(REJECT_REASON, lastUpdateObjRef.get().getLastRejectReason());
        assertTrue(lastUpdateObjRef.get().getLastRejectTime() != null);
        assertEquals(1, rejectLogs.size());
        assertEquals(REJECT_REASON, rejectLogs.get(0).getReason());
        assertEquals(1, auditLogs.size());
        assertEquals(ErpSaleOrderAuditActionTypeConstants.REJECT, auditLogs.get(0).getActionType());
        assertEquals(0, publishedEvents.size());
    }

    @Test
    void updateSaleOrderStatusByBpm_shouldPublishApprovedEvent() throws Exception {
        saleOrderRef.set(new ErpSaleOrderDO().setId(2L).setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId("PI-APPROVE")
                .setOutCount(BigDecimal.ZERO).setReturnCount(BigDecimal.ZERO));

        invokeUpdateSaleOrderStatusByBpm(2L, "PI-APPROVE", ErpAuditStatus.APPROVE.getStatus(), "通过");

        assertEquals(ErpAuditStatus.APPROVE.getStatus(), lastUpdateObjRef.get().getStatus());
        assertEquals(1, auditLogs.size());
        assertEquals(ErpSaleOrderAuditActionTypeConstants.APPROVE, auditLogs.get(0).getActionType());
        assertEquals(1, publishedEvents.size());
        ErpSaleOrderApprovedEvent event = assertInstanceOf(ErpSaleOrderApprovedEvent.class, publishedEvents.get(0));
        assertEquals(2L, event.getSaleOrderId());
    }

    private ErpSaleOrderMapper createSaleOrderMapperProxy() {
        return createProxy(ErpSaleOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return saleOrderRef.get();
            }
            if ("selectByIds".equals(methodName)) {
                return saleOrderRef.get() == null ? Collections.emptyList() : Collections.singletonList(saleOrderRef.get());
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                lastUpdateObjRef.set((ErpSaleOrderDO) args[2]);
                return updateCountRef.get();
            }
            return null;
        });
    }

    private ErpSaleOrderRejectLogMapper createRejectLogMapperProxy() {
        return createProxy(ErpSaleOrderRejectLogMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                rejectLogs.add((ErpSaleOrderRejectLogDO) args[0]);
                return 1;
            }
            if ("selectListByOrderId".equals(methodName)) {
                return rejectLogs;
            }
            return null;
        });
    }

    private ErpSaleOrderAuditLogMapper createAuditLogMapperProxy() {
        return createProxy(ErpSaleOrderAuditLogMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                auditLogs.add((ErpSaleOrderAuditLogDO) args[0]);
                return 1;
            }
            if ("selectListByOrderId".equals(methodName)) {
                return auditLogs;
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
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private ErpSaleOrderUpdateStatusReqVO buildReqVO(Long id, Integer status, String reason) {
        ErpSaleOrderUpdateStatusReqVO reqVO = new ErpSaleOrderUpdateStatusReqVO();
        reqVO.setId(id);
        reqVO.setStatus(status);
        reqVO.setReason(reason);
        return reqVO;
    }

    private void invokeUpdateSaleOrderStatusByBpm(Long id, String processInstanceId, Integer status, String reason)
            throws Exception {
        Method method = saleOrderService.getClass().getMethod("updateSaleOrderStatusByBpm",
                Long.class, String.class, Integer.class, String.class);
        method.invoke(saleOrderService, id, processInstanceId, status, reason);
    }

    private void assertTransactional(String methodName, Class<?>... parameterTypes) throws Exception {
        Method method = saleOrderService.getClass().getMethod(methodName, parameterTypes);
        assertTrue(method.isAnnotationPresent(Transactional.class),
                () -> methodName + " should declare @Transactional");
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }

}
