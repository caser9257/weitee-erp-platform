package cn.weitee.erp.module.erp.service.sale;

import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOutItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOutItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOutMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.ErpArStatementService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceBizHookService;
import cn.weitee.erp.module.erp.service.stock.ErpStockService;
import cn.weitee.erp.module.erp.service.stock.ErpStockBatchAllocationService;
import cn.weitee.erp.module.erp.service.stock.ErpStockRecordService;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchAllocateOutboundReqBO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErpSaleOutServiceImplTest {

    @Test
    void updateSaleOutStatus_shouldAllocateBatchWhenApprove() throws Exception {
        ErpSaleOutServiceImpl service = new ErpSaleOutServiceImpl();
        AtomicReference<ErpStockBatchAllocateOutboundReqBO> allocateReqRef = new AtomicReference<>();

        setField(service, "saleOutMapper", createProxy(ErpSaleOutMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpSaleOutDO().setId(100L).setNo("XSCK202604290001")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus());
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(service, "saleOutItemMapper", createProxy(ErpSaleOutItemMapper.class, (methodName, args) -> {
            if ("selectListByOutId".equals(methodName)) {
                return List.of(new ErpSaleOutItemDO().setId(101L).setOutId(100L)
                        .setProductId(1L).setWarehouseId(2L).setCount(new BigDecimal("7.000")));
            }
            return null;
        }));
        setField(service, "stockBatchAllocationService", createProxy(ErpStockBatchAllocationService.class, (methodName, args) -> {
            if ("allocateOutbound".equals(methodName)) {
                allocateReqRef.set((ErpStockBatchAllocateOutboundReqBO) args[0]);
                return List.of();
            }
            return null;
        }));
        setField(service, "stockRecordService", createProxy(ErpStockRecordService.class, (methodName, args) -> null));
        setDefaultStockService(service);
        setField(service, "financeBizHookService", createProxy(ErpFinanceBizHookService.class, (methodName, args) -> {
            if ("handleApprovedBiz".equals(methodName)) {
                return 1L;
            }
            return null;
        }));
        setDefaultArStatementService(service);

        service.updateSaleOutStatus(100L, ErpAuditStatus.APPROVE.getStatus());

        assertEquals(ErpStockRecordBizTypeEnum.SALE_OUT.getType(), allocateReqRef.get().getBizType());
        assertEquals(100L, allocateReqRef.get().getBizId());
        assertEquals(101L, allocateReqRef.get().getBizItemId());
        assertEquals(new BigDecimal("7.000"), allocateReqRef.get().getCount());
    }

    @Test
    void updateSaleOutStatus_shouldRollbackBatchWhenProcess() throws Exception {
        ErpSaleOutServiceImpl service = new ErpSaleOutServiceImpl();
        AtomicReference<Object[]> rollbackArgsRef = new AtomicReference<>();

        setField(service, "saleOutMapper", createProxy(ErpSaleOutMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpSaleOutDO().setId(100L).setNo("XSCK202604290001")
                        .setStatus(ErpAuditStatus.APPROVE.getStatus()).setReceiptPrice(BigDecimal.ZERO);
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(service, "saleOutItemMapper", createProxy(ErpSaleOutItemMapper.class, (methodName, args) -> {
            if ("selectListByOutId".equals(methodName)) {
                return List.of(new ErpSaleOutItemDO().setId(101L).setOutId(100L)
                        .setProductId(1L).setWarehouseId(2L).setCount(new BigDecimal("7.000")));
            }
            return null;
        }));
        setField(service, "stockBatchAllocationService", createProxy(ErpStockBatchAllocationService.class, (methodName, args) -> {
            if ("rollbackOutbound".equals(methodName)) {
                rollbackArgsRef.set(args);
            }
            return null;
        }));
        setField(service, "stockRecordService", createProxy(ErpStockRecordService.class, (methodName, args) -> null));
        setDefaultStockService(service);
        setDefaultArStatementService(service);
        setField(service, "financeBizHookService", createProxy(ErpFinanceBizHookService.class, (methodName, args) -> null));

        service.updateSaleOutStatus(100L, ErpAuditStatus.PROCESS.getStatus());

        assertEquals(ErpStockRecordBizTypeEnum.SALE_OUT.getType(), rollbackArgsRef.get()[0]);
        assertEquals(100L, rollbackArgsRef.get()[1]);
        assertEquals(ErpStockRecordBizTypeEnum.SALE_OUT_CANCEL.getType(), rollbackArgsRef.get()[2]);
    }

    @Test
    void updateSaleOutStatus_shouldInvokeFinanceHookWhenApprove() throws Exception {
        ErpSaleOutServiceImpl service = new ErpSaleOutServiceImpl();
        AtomicReference<Integer> hookBizTypeRef = new AtomicReference<>();
        AtomicReference<Long> hookBizIdRef = new AtomicReference<>();
        AtomicReference<LocalDate> hookBizDateRef = new AtomicReference<>();

        setField(service, "saleOutMapper", createProxy(ErpSaleOutMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpSaleOutDO().setId(100L).setNo("XSCK202604290001")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus())
                        .setOutTime(LocalDateTime.of(2026, 4, 29, 9, 0));
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(service, "saleOutItemMapper", createProxy(ErpSaleOutItemMapper.class, (methodName, args) -> {
            if ("selectListByOutId".equals(methodName)) {
                return List.of(new ErpSaleOutItemDO().setId(101L).setOutId(100L)
                        .setProductId(1L).setWarehouseId(2L).setCount(new BigDecimal("7.000")));
            }
            return null;
        }));
        setField(service, "stockBatchAllocationService", createProxy(ErpStockBatchAllocationService.class, (methodName, args) -> {
            if ("allocateOutbound".equals(methodName)) {
                return List.of();
            }
            return null;
        }));
        setField(service, "stockRecordService", createProxy(ErpStockRecordService.class, (methodName, args) -> null));
        setDefaultStockService(service);
        setField(service, "financeBizHookService", createProxy(ErpFinanceBizHookService.class, (methodName, args) -> {
            if ("handleApprovedBiz".equals(methodName)) {
                hookBizTypeRef.set((Integer) args[0]);
                hookBizIdRef.set((Long) args[1]);
                hookBizDateRef.set((LocalDate) args[2]);
                return 1L;
            }
            return null;
        }));
        setDefaultArStatementService(service);

        service.updateSaleOutStatus(100L, ErpAuditStatus.APPROVE.getStatus());

        assertEquals(ErpBizTypeEnum.SALE_OUT.getType(), hookBizTypeRef.get());
        assertEquals(100L, hookBizIdRef.get());
        assertEquals(LocalDate.of(2026, 4, 29), hookBizDateRef.get());
    }

    @Test
    void updateSaleOutStatus_shouldFallbackToCreateTimeWhenOutTimeMissing() throws Exception {
        ErpSaleOutServiceImpl service = new ErpSaleOutServiceImpl();
        AtomicReference<LocalDate> hookBizDateRef = new AtomicReference<>();

        setField(service, "saleOutMapper", createProxy(ErpSaleOutMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                ErpSaleOutDO saleOut = new ErpSaleOutDO().setId(100L).setNo("XSCK202604290001")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus());
                saleOut.setCreateTime(LocalDateTime.of(2026, 4, 28, 8, 0));
                saleOut.setUpdateTime(LocalDateTime.of(2026, 4, 29, 9, 0));
                return saleOut;
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(service, "saleOutItemMapper", createProxy(ErpSaleOutItemMapper.class, (methodName, args) -> {
            if ("selectListByOutId".equals(methodName)) {
                return List.of(new ErpSaleOutItemDO().setId(101L).setOutId(100L)
                        .setProductId(1L).setWarehouseId(2L).setCount(new BigDecimal("7.000")));
            }
            return null;
        }));
        setField(service, "stockBatchAllocationService", createProxy(ErpStockBatchAllocationService.class, (methodName, args) -> {
            if ("allocateOutbound".equals(methodName)) {
                return List.of();
            }
            return null;
        }));
        setField(service, "stockRecordService", createProxy(ErpStockRecordService.class, (methodName, args) -> null));
        setDefaultStockService(service);
        setField(service, "financeBizHookService", createProxy(ErpFinanceBizHookService.class, (methodName, args) -> {
            if ("handleApprovedBiz".equals(methodName)) {
                hookBizDateRef.set((LocalDate) args[2]);
                return 1L;
            }
            return null;
        }));
        setDefaultArStatementService(service);

        service.updateSaleOutStatus(100L, ErpAuditStatus.APPROVE.getStatus());

        assertEquals(LocalDate.of(2026, 4, 28), hookBizDateRef.get());
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
        Field field = target.getClass().getDeclaredField(mapFieldName(fieldName));
        field.setAccessible(true);
        field.set(target, value);
    }

    private void setDefaultStockService(ErpSaleOutServiceImpl service) throws Exception {
        setField(service, "stockService", createProxy(ErpStockService.class, (methodName, args) -> {
            if ("getStock".equals(methodName)) {
                return new ErpStockDO().setAverageCost(new BigDecimal("10.00"));
            }
            return null;
        }));
    }

    private void setDefaultArStatementService(ErpSaleOutServiceImpl service) throws Exception {
        setField(service, "arStatementService", createProxy(ErpArStatementService.class, (methodName, args) -> null));
    }

    private String mapFieldName(String fieldName) {
        return switch (fieldName) {
            case "saleOutMapper" -> "erpSaleOutMapper";
            case "saleOutItemMapper" -> "erpSaleOutItemMapper";
            default -> fieldName;
        };
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }
}
