package cn.weitee.erp.module.erp.service.sale;

import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderUpdateStatusReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderAuditLogDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderRejectLogDO;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOrderAuditLogMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpMrpStockReservationMapper;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOrderRejectLogMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpSaleOrderAuditActionTypeConstants;
import cn.weitee.erp.module.erp.service.mrp.ErpMrpStockReservationSummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErpSaleOrderAuditLogServiceImplTest {

    private final AtomicReference<ErpSaleOrderDO> saleOrderRef = new AtomicReference<>();
    private final AtomicReference<Integer> updateCountRef = new AtomicReference<>(1);
    private final List<ErpSaleOrderAuditLogDO> auditLogs = new ArrayList<>();
    private final List<ErpSaleOrderRejectLogDO> rejectLogs = new ArrayList<>();

    private ErpSaleOrderServiceImpl saleOrderService;

    @BeforeEach
    void setUp() throws Exception {
        saleOrderService = new ErpSaleOrderServiceImpl();
        saleOrderRef.set(null);
        updateCountRef.set(1);
        auditLogs.clear();
        rejectLogs.clear();
        setField(saleOrderService, "saleOrderMapper", createSaleOrderMapperProxy());
        setField(saleOrderService, "saleOrderAuditLogMapper", createAuditLogMapperProxy());
        setField(saleOrderService, "saleOrderRejectLogMapper", createRejectLogMapperProxy());
        setField(saleOrderService, "mrpStockReservationMapper",
                createProxy(ErpMrpStockReservationMapper.class, (methodName, args) -> "selectListBySourceOrderIds".equals(methodName)
                        ? java.util.Collections.emptyList() : 1));
        setField(saleOrderService, "mrpStockReservationSummaryService",
                createProxy(ErpMrpStockReservationSummaryService.class, (methodName, args) -> null));
        setField(saleOrderService, "eventPublisher", createEventPublisherProxy());
    }

    @Test
    void updateSaleOrderStatus_shouldWriteApproveAuditLog() {
        saleOrderRef.set(new ErpSaleOrderDO().setId(1L).setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setOutCount(BigDecimal.ZERO).setReturnCount(BigDecimal.ZERO));

        saleOrderService.updateSaleOrderStatus(buildReqVO(1L, ErpAuditStatus.APPROVE.getStatus(), null));

        assertEquals(1, auditLogs.size());
        assertEquals(ErpSaleOrderAuditActionTypeConstants.APPROVE, auditLogs.get(0).getActionType());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), auditLogs.get(0).getBeforeStatus());
        assertEquals(ErpAuditStatus.APPROVE.getStatus(), auditLogs.get(0).getAfterStatus());
    }

    @Test
    void updateSaleOrderStatus_shouldWriteRejectAuditLog() {
        saleOrderRef.set(new ErpSaleOrderDO().setId(1L).setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setOutCount(BigDecimal.ZERO).setReturnCount(BigDecimal.ZERO));

        saleOrderService.updateSaleOrderStatus(buildReqVO(1L, ErpAuditStatus.REJECT.getStatus(), "资料不完整"));

        assertEquals(1, auditLogs.size());
        assertEquals(ErpSaleOrderAuditActionTypeConstants.REJECT, auditLogs.get(0).getActionType());
        assertEquals("资料不完整", auditLogs.get(0).getReason());
        assertEquals(1, rejectLogs.size());
    }

    @Test
    void updateSaleOrderStatus_shouldWriteResubmitAuditLog() {
        saleOrderRef.set(new ErpSaleOrderDO().setId(1L).setStatus(ErpAuditStatus.REJECT.getStatus())
                .setOutCount(BigDecimal.ZERO).setReturnCount(BigDecimal.ZERO));

        saleOrderService.updateSaleOrderStatus(buildReqVO(1L, ErpAuditStatus.PROCESS.getStatus(), null));

        assertEquals(1, auditLogs.size());
        assertEquals(ErpSaleOrderAuditActionTypeConstants.RESUBMIT, auditLogs.get(0).getActionType());
        assertEquals(ErpAuditStatus.REJECT.getStatus(), auditLogs.get(0).getBeforeStatus());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), auditLogs.get(0).getAfterStatus());
    }

    @Test
    void updateSaleOrderStatus_shouldWriteReverseApproveAuditLog() {
        saleOrderRef.set(new ErpSaleOrderDO().setId(1L).setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setOutCount(BigDecimal.ZERO).setReturnCount(BigDecimal.ZERO));

        saleOrderService.updateSaleOrderStatus(buildReqVO(1L, ErpAuditStatus.PROCESS.getStatus(), null));

        assertEquals(1, auditLogs.size());
        assertEquals(ErpSaleOrderAuditActionTypeConstants.REVERSE_APPROVE, auditLogs.get(0).getActionType());
        assertEquals(ErpAuditStatus.APPROVE.getStatus(), auditLogs.get(0).getBeforeStatus());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), auditLogs.get(0).getAfterStatus());
    }

    private ErpSaleOrderMapper createSaleOrderMapperProxy() {
        return createProxy(ErpSaleOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return saleOrderRef.get();
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                return updateCountRef.get();
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

    private ErpSaleOrderRejectLogMapper createRejectLogMapperProxy() {
        return createProxy(ErpSaleOrderRejectLogMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                rejectLogs.add((ErpSaleOrderRejectLogDO) args[0]);
                return 1;
            }
            return null;
        });
    }

    private ApplicationEventPublisher createEventPublisherProxy() {
        return createProxy(ApplicationEventPublisher.class, (methodName, args) -> null);
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

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }

}
