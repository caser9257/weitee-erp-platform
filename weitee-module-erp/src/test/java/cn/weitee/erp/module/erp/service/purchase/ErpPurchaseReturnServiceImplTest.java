package cn.weitee.erp.module.erp.service.purchase;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseReturnItemDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePaymentAllocateMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseReturnItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseReturnMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpFinancePaymentAllocateStatusEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.ErpApStatementService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceBizHookService;
import cn.weitee.erp.module.erp.service.stock.ErpStockRecordService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PURCHASE_RETURN_PROCESS_FAIL_EXISTS_REFUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpPurchaseReturnServiceImplTest {

    @Test
    void updatePurchaseReturnStatus_shouldRefreshOrderReturnCountAndCreateApStatementWhenApproved() throws Exception {
        ErpPurchaseReturnServiceImpl service = new ErpPurchaseReturnServiceImpl();
        ErpPurchaseReturnDO purchaseReturn = new ErpPurchaseReturnDO()
                .setId(2L)
                .setOrderId(66L)
                .setNo("PR-001")
                .setReturnTime(LocalDateTime.of(2026, 4, 28, 14, 0))
                .setStatus(ErpAuditStatus.PROCESS.getStatus());
        List<ErpPurchaseReturnDO> approvedReturns = List.of(
                new ErpPurchaseReturnDO().setId(2L),
                new ErpPurchaseReturnDO().setId(8L));
        Map<Long, BigDecimal> returnCountMap = Map.of(2002L, new BigDecimal("4"));
        AtomicReference<List<Long>> approvedReturnIdsRef = new AtomicReference<>();
        AtomicReference<Long> orderIdRef = new AtomicReference<>();
        AtomicReference<Map<Long, BigDecimal>> orderReturnCountMapRef = new AtomicReference<>();
        AtomicReference<Long> createdStatementBizIdRef = new AtomicReference<>();
        AtomicReference<Integer> autoGenerateVoucherBizTypeRef = new AtomicReference<>();
        AtomicReference<Long> autoGenerateVoucherBizIdRef = new AtomicReference<>();
        AtomicReference<LocalDate> financeHookBizDateRef = new AtomicReference<>();

        setField(service, "purchaseReturnMapper", createProxy(ErpPurchaseReturnMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return purchaseReturn;
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                return 1;
            }
            if ("selectApprovedListByOrderId".equals(methodName)) {
                return approvedReturns;
            }
            return null;
        }));
        setField(service, "purchaseReturnItemMapper", createProxy(ErpPurchaseReturnItemMapper.class, (methodName, args) -> {
            if ("selectListByReturnId".equals(methodName)) {
                return List.of(new ErpPurchaseReturnItemDO()
                        .setReturnId(2L)
                        .setProductId(33L)
                        .setWarehouseId(44L)
                        .setCount(new BigDecimal("2")));
            }
            if ("selectOrderItemCountSumMapByReturnIds".equals(methodName)) {
                approvedReturnIdsRef.set(new ArrayList<>((Collection<Long>) args[0]));
                return returnCountMap;
            }
            return null;
        }));
        setField(service, "purchaseOrderService", createProxy(ErpPurchaseOrderService.class, (methodName, args) -> {
            if ("updatePurchaseOrderReturnCount".equals(methodName)) {
                orderIdRef.set((Long) args[0]);
                orderReturnCountMapRef.set((Map<Long, BigDecimal>) args[1]);
            }
            return null;
        }));
        setField(service, "stockRecordService", createProxy(ErpStockRecordService.class, (methodName, args) -> null));
        setField(service, "apStatementService", createProxy(ErpApStatementService.class, (methodName, args) -> {
            if ("createStatementForPurchaseReturn".equals(methodName)) {
                createdStatementBizIdRef.set(((ErpPurchaseReturnDO) args[0]).getId());
            }
            return null;
        }));
        setField(service, "financeBizHookService", createProxy(ErpFinanceBizHookService.class, (methodName, args) -> {
            if ("handleApprovedBiz".equals(methodName)) {
                autoGenerateVoucherBizTypeRef.set((Integer) args[0]);
                autoGenerateVoucherBizIdRef.set((Long) args[1]);
                financeHookBizDateRef.set((LocalDate) args[2]);
                return 1L;
            }
            return null;
        }));
        setField(service, "paymentAllocateMapper", createProxy(ErpFinancePaymentAllocateMapper.class, (methodName, args) -> 0L));

        service.updatePurchaseReturnStatus(2L, ErpAuditStatus.APPROVE.getStatus());

        assertEquals(List.of(2L, 8L), approvedReturnIdsRef.get());
        assertEquals(66L, orderIdRef.get());
        assertEquals(returnCountMap, orderReturnCountMapRef.get());
        assertEquals(2L, createdStatementBizIdRef.get());
        assertEquals(ErpBizTypeEnum.PURCHASE_RETURN.getType(), autoGenerateVoucherBizTypeRef.get());
        assertEquals(2L, autoGenerateVoucherBizIdRef.get());
        assertEquals(LocalDate.of(2026, 4, 28), financeHookBizDateRef.get());
    }

    @Test
    void updatePurchaseReturnStatus_shouldRejectWhenApprovedAllocateExists() throws Exception {
        ErpPurchaseReturnServiceImpl service = new ErpPurchaseReturnServiceImpl();
        ErpPurchaseReturnDO purchaseReturn = new ErpPurchaseReturnDO()
                .setId(3L)
                .setOrderId(77L)
                .setNo("PR-002")
                .setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setRefundPrice(BigDecimal.ZERO);

        setField(service, "purchaseReturnMapper", createProxy(ErpPurchaseReturnMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return purchaseReturn;
            }
            return null;
        }));
        setField(service, "paymentAllocateMapper", createProxy(ErpFinancePaymentAllocateMapper.class, (methodName, args) -> {
            if ("selectCountByBizTypeAndBizIdAndStatus".equals(methodName)) {
                assertEquals(ErpFinancePaymentAllocateStatusEnum.APPROVED.getStatus(), args[2]);
                return 1L;
            }
            return null;
        }));

        ServiceException ex = assertThrows(ServiceException.class, () ->
                service.updatePurchaseReturnStatus(3L, ErpAuditStatus.PROCESS.getStatus()));

        assertEquals(PURCHASE_RETURN_PROCESS_FAIL_EXISTS_REFUND.getCode(), ex.getCode());
    }

    @Test
    void updatePurchaseReturnStatus_shouldCloseApStatementWhenProcessWithoutApprovedAllocate() throws Exception {
        ErpPurchaseReturnServiceImpl service = new ErpPurchaseReturnServiceImpl();
        ErpPurchaseReturnDO purchaseReturn = new ErpPurchaseReturnDO()
                .setId(4L)
                .setOrderId(88L)
                .setNo("PR-003")
                .setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setRefundPrice(BigDecimal.ZERO);
        AtomicReference<Integer> closedBizTypeRef = new AtomicReference<>();
        AtomicReference<Long> closedBizIdRef = new AtomicReference<>();

        setField(service, "purchaseReturnMapper", createProxy(ErpPurchaseReturnMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return purchaseReturn;
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                return 1;
            }
            if ("selectApprovedListByOrderId".equals(methodName)) {
                return List.of();
            }
            return null;
        }));
        setField(service, "purchaseReturnItemMapper", createProxy(ErpPurchaseReturnItemMapper.class, (methodName, args) -> {
            if ("selectListByReturnId".equals(methodName)) {
                return List.of(new ErpPurchaseReturnItemDO()
                        .setReturnId(4L)
                        .setProductId(33L)
                        .setWarehouseId(44L)
                        .setCount(new BigDecimal("2")));
            }
            if ("selectOrderItemCountSumMapByReturnIds".equals(methodName)) {
                return Map.of();
            }
            return null;
        }));
        setField(service, "purchaseOrderService", createProxy(ErpPurchaseOrderService.class, (methodName, args) -> null));
        setField(service, "stockRecordService", createProxy(ErpStockRecordService.class, (methodName, args) -> null));
        setField(service, "paymentAllocateMapper", createProxy(ErpFinancePaymentAllocateMapper.class, (methodName, args) -> {
            if ("selectCountByBizTypeAndBizIdAndStatus".equals(methodName)) {
                return 0L;
            }
            return null;
        }));
        setField(service, "apStatementService", createProxy(ErpApStatementService.class, (methodName, args) -> {
            if ("closeStatementByBiz".equals(methodName)) {
                closedBizTypeRef.set((Integer) args[0]);
                closedBizIdRef.set((Long) args[1]);
            }
            return null;
        }));

        service.updatePurchaseReturnStatus(4L, ErpAuditStatus.PROCESS.getStatus());

        assertEquals(ErpBizTypeEnum.PURCHASE_RETURN.getType(), closedBizTypeRef.get());
        assertEquals(4L, closedBizIdRef.get());
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
        Object handle(String methodName, Object[] args);
    }

}
