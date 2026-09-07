package cn.weitee.erp.module.erp.service.sale;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleReturnDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleReturnItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleReturnItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleReturnMapper;
import cn.weitee.erp.module.erp.service.finance.ErpArStatementService;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceBizHookService;
import cn.weitee.erp.module.erp.service.stock.ErpStockRecordService;
import cn.weitee.erp.module.erp.service.stock.ErpStockService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.SALE_RETURN_PROCESS_FAIL_EXISTS_REFUND;

class ErpSaleReturnServiceImplTest {

    @Test
    void updateSaleReturnStatus_shouldInvokeFinanceHookWhenApprove() throws Exception {
        ErpSaleReturnServiceImpl service = new ErpSaleReturnServiceImpl();
        AtomicReference<Integer> hookBizTypeRef = new AtomicReference<>();
        AtomicReference<Long> hookBizIdRef = new AtomicReference<>();
        AtomicReference<LocalDate> hookBizDateRef = new AtomicReference<>();

        setField(service, "saleReturnMapper", createProxy(ErpSaleReturnMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpSaleReturnDO().setId(200L).setNo("XSTH202604290001")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus())
                        .setReturnTime(LocalDateTime.of(2026, 4, 29, 11, 0));
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(service, "saleReturnItemMapper", createProxy(ErpSaleReturnItemMapper.class, (methodName, args) -> {
            if ("selectListByReturnId".equals(methodName)) {
                return List.of(new ErpSaleReturnItemDO().setId(201L).setReturnId(200L)
                        .setProductId(1L).setWarehouseId(2L).setCount(new BigDecimal("3.000")));
            }
            return null;
        }));
        setField(service, "stockRecordService", createProxy(ErpStockRecordService.class, (methodName, args) -> null));
        setField(service, "stockService", createProxy(ErpStockService.class, (methodName, args) -> {
            if ("getStockMapByProductAndWarehouseIds".equals(methodName)) {
                return java.util.Collections.singletonMap(
                        ErpStockService.buildProductWarehouseKey(1L, 2L),
                        new ErpStockDO().setAverageCost(new BigDecimal("10.00")));
            }
            return null;
        }));
        setField(service, "financeBizHookService", createProxy(ErpFinanceBizHookService.class, (methodName, args) -> {
            if ("handleApprovedBiz".equals(methodName)) {
                hookBizTypeRef.set((Integer) args[0]);
                hookBizIdRef.set((Long) args[1]);
                hookBizDateRef.set((LocalDate) args[2]);
                return 1L;
            }
            return null;
        }));
        setField(service, "arStatementService", createProxy(ErpArStatementService.class, (methodName, args) -> null));

        service.updateSaleReturnStatus(200L, ErpAuditStatus.APPROVE.getStatus());

        assertEquals(ErpBizTypeEnum.SALE_RETURN.getType(), hookBizTypeRef.get());
        assertEquals(200L, hookBizIdRef.get());
        assertEquals(LocalDate.of(2026, 4, 29), hookBizDateRef.get());
    }

    @Test
    void updateSaleReturnStatus_shouldFallbackToCreateTimeWhenReturnTimeMissing() throws Exception {
        ErpSaleReturnServiceImpl service = new ErpSaleReturnServiceImpl();
        AtomicReference<LocalDate> hookBizDateRef = new AtomicReference<>();

        setField(service, "saleReturnMapper", createProxy(ErpSaleReturnMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                ErpSaleReturnDO saleReturn = new ErpSaleReturnDO().setId(200L).setNo("XSTH202604290001")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus());
                saleReturn.setCreateTime(LocalDateTime.of(2026, 4, 27, 10, 0));
                saleReturn.setUpdateTime(LocalDateTime.of(2026, 4, 29, 11, 0));
                return saleReturn;
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(service, "saleReturnItemMapper", createProxy(ErpSaleReturnItemMapper.class, (methodName, args) -> {
            if ("selectListByReturnId".equals(methodName)) {
                return List.of(new ErpSaleReturnItemDO().setId(201L).setReturnId(200L)
                        .setProductId(1L).setWarehouseId(2L).setCount(new BigDecimal("3.000")));
            }
            return null;
        }));
        setField(service, "stockRecordService", createProxy(ErpStockRecordService.class, (methodName, args) -> null));
        setField(service, "stockService", createProxy(ErpStockService.class, (methodName, args) -> {
            if ("getStockMapByProductAndWarehouseIds".equals(methodName)) {
                return java.util.Collections.singletonMap(
                        ErpStockService.buildProductWarehouseKey(1L, 2L),
                        new ErpStockDO().setAverageCost(new BigDecimal("10.00")));
            }
            return null;
        }));
        setField(service, "financeBizHookService", createProxy(ErpFinanceBizHookService.class, (methodName, args) -> {
            if ("handleApprovedBiz".equals(methodName)) {
                hookBizDateRef.set((LocalDate) args[2]);
                return 1L;
            }
            return null;
        }));
        setField(service, "arStatementService", createProxy(ErpArStatementService.class, (methodName, args) -> null));

        service.updateSaleReturnStatus(200L, ErpAuditStatus.APPROVE.getStatus());

        assertEquals(LocalDate.of(2026, 4, 27), hookBizDateRef.get());
    }

    @Test
    void updateSaleReturnStatus_shouldBlockReverseWhenAlreadyRefunded() throws Exception {
        ErpSaleReturnServiceImpl service = new ErpSaleReturnServiceImpl();
        java.util.concurrent.atomic.AtomicBoolean stockTouched = new java.util.concurrent.atomic.AtomicBoolean(false);

        // 已退款：refundPrice 为负数（finance 回写口径），修复前 `> 0` 恒不成立导致漏判
        setField(service, "saleReturnMapper", createProxy(ErpSaleReturnMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpSaleReturnDO().setId(200L).setNo("XSTH202604290001")
                        .setStatus(ErpAuditStatus.APPROVE.getStatus())
                        .setRefundPrice(new BigDecimal("-100.00"));
            }
            return null;
        }));
        setField(service, "saleReturnItemMapper", createProxy(ErpSaleReturnItemMapper.class, (methodName, args) -> null));
        setField(service, "stockRecordService", createProxy(ErpStockRecordService.class, (methodName, args) -> {
            if ("createStockRecord".equals(methodName)) {
                stockTouched.set(true);
            }
            return null;
        }));
        setField(service, "stockService", createProxy(ErpStockService.class, (methodName, args) -> null));
        setField(service, "financeBizHookService", createProxy(ErpFinanceBizHookService.class, (methodName, args) -> null));
        setField(service, "arStatementService", createProxy(ErpArStatementService.class, (methodName, args) -> null));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.updateSaleReturnStatus(200L, ErpAuditStatus.PROCESS.getStatus()));

        assertEquals(SALE_RETURN_PROCESS_FAIL_EXISTS_REFUND.getCode(), ex.getCode());
        assertFalse(stockTouched.get(), "前置校验应在库存变更前拦截");
    }

    @Test
    void updateSaleReturnStatus_shouldAbortReverseWhenConcurrentRefundLanded() throws Exception {
        ErpSaleReturnServiceImpl service = new ErpSaleReturnServiceImpl();
        java.util.concurrent.atomic.AtomicInteger selectCount = new java.util.concurrent.atomic.AtomicInteger();

        // 首次读未退款（前置校验通过），失败重读时并发退款已落地（refundPrice≠0）
        setField(service, "saleReturnMapper", createProxy(ErpSaleReturnMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                BigDecimal refund = selectCount.getAndIncrement() == 0
                        ? BigDecimal.ZERO : new BigDecimal("-100.00");
                return new ErpSaleReturnDO().setId(200L).setNo("XSTH202604290001")
                        .setStatus(ErpAuditStatus.APPROVE.getStatus()).setRefundPrice(refund);
            }
            if ("updateByIdAndStatusAndNoRefund".equals(methodName)) {
                return 0; // 并发退款先落地，CAS 命中 0 行
            }
            return null;
        }));
        setField(service, "saleReturnItemMapper", createProxy(ErpSaleReturnItemMapper.class, (methodName, args) -> {
            if ("selectListByReturnId".equals(methodName)) {
                return java.util.Collections.emptyList();
            }
            return null;
        }));
        setField(service, "stockRecordService", createProxy(ErpStockRecordService.class, (methodName, args) -> null));
        setField(service, "stockService", createProxy(ErpStockService.class, (methodName, args) -> null));
        setField(service, "financeBizHookService", createProxy(ErpFinanceBizHookService.class, (methodName, args) -> null));
        setField(service, "arStatementService", createProxy(ErpArStatementService.class, (methodName, args) -> null));

        // 退货「先业务后 CAS」：CAS 失败抛异常后，@Transactional 回滚已执行的业务操作
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.updateSaleReturnStatus(200L, ErpAuditStatus.PROCESS.getStatus()));
        assertEquals(SALE_RETURN_PROCESS_FAIL_EXISTS_REFUND.getCode(), ex.getCode());
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
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> type, String fieldName) throws NoSuchFieldException {
        for (String candidate : resolveFieldCandidates(fieldName)) {
            try {
                return type.getDeclaredField(candidate);
            } catch (NoSuchFieldException ignored) {
                // try next candidate
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private String[] resolveFieldCandidates(String fieldName) {
        return fieldName.startsWith("erp")
                ? new String[]{fieldName}
                : new String[]{fieldName, "erp" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1)};
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }
}
