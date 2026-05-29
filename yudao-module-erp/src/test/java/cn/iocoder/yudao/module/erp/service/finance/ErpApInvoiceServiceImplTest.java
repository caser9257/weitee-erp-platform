package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoiceCancelMatchReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoiceMatchReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoicePendingItemPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apinvoice.ErpApInvoicePendingItemRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApInvoiceDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApInvoiceMatchItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApStatementItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpApInvoiceMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpApInvoiceMatchItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpApStatementItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpApStatementMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInItemMapper;
import cn.iocoder.yudao.module.erp.enums.ErpApInvoiceMatchItemStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpApInvoiceMatchStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpApInvoiceStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpApStatementItemTypeEnum;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseInService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.AP_INVOICE_MATCH_AMOUNT_EXCEED;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.AP_INVOICE_MATCH_COUNT_EXCEED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpApInvoiceServiceImplTest {

    @Test
    void getPendingItemPage_shouldDelegateToSqlPageQuery() throws Exception {
        ErpApInvoiceServiceImpl service = new ErpApInvoiceServiceImpl();
        ErpApInvoiceDO invoice = new ErpApInvoiceDO()
                .setId(10L)
                .setSupplierId(201L);
        ErpApInvoicePendingItemRespVO pendingItem = new ErpApInvoicePendingItemRespVO()
                .setSourcePurchaseInItemId(1001L)
                .setSourcePurchaseInNo("PI-001");
        AtomicReference<Object[]> sqlArgsRef = new AtomicReference<>();

        setField(service, "invoiceMapper", createProxy(ErpApInvoiceMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return invoice;
            }
            return null;
        }));
        setField(service, "invoiceMatchItemMapper", createProxy(ErpApInvoiceMatchItemMapper.class, (methodName, args) -> {
            if ("selectPendingItemPage".equals(methodName)) {
                sqlArgsRef.set(args);
                Page<ErpApInvoicePendingItemRespVO> page = (Page<ErpApInvoicePendingItemRespVO>) args[0];
                page.setRecords(List.of(pendingItem));
                page.setTotal(1L);
                return page;
            }
            return null;
        }));

        ErpApInvoicePendingItemPageReqVO reqVO = new ErpApInvoicePendingItemPageReqVO();
        reqVO.setInvoiceId(10L);
        reqVO.setPageNo(1);
        reqVO.setPageSize(20);
        reqVO.setPurchaseInNo("PI");
        reqVO.setSourceOrderNo("PO");

        PageResult<ErpApInvoicePendingItemRespVO> result = service.getPendingItemPage(reqVO);

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals(1001L, result.getList().get(0).getSourcePurchaseInItemId());
        assertEquals("PI-001", result.getList().get(0).getSourcePurchaseInNo());
        assertEquals(201L, sqlArgsRef.get()[2]);
    }

    @Test
    void confirmMatch_shouldMarkMatchedWhenDifferenceWithinTolerance() throws Exception {
        ErpApInvoiceServiceImpl service = new ErpApInvoiceServiceImpl();
        ErpApInvoiceDO invoice = new ErpApInvoiceDO()
                .setId(1L)
                .setSupplierId(201L)
                .setInvoiceNo("FP-20260428-001")
                .setInvoiceDate(LocalDateTime.of(2026, 4, 28, 10, 0))
                .setInvoiceType(10)
                .setTotalAmount(new BigDecimal("100.40"))
                .setMatchedAmount(BigDecimal.ZERO)
                .setUnmatchedAmount(new BigDecimal("100.40"))
                .setToleranceAmount(new BigDecimal("1.00"))
                .setMatchStatus(ErpApInvoiceMatchStatusEnum.UNMATCHED.getStatus());
        ErpPurchaseInItemDO purchaseInItem = new ErpPurchaseInItemDO()
                .setId(1001L)
                .setInId(11L)
                .setOrderItemId(21L)
                .setProductId(301L)
                .setCount(new BigDecimal("10"))
                .setTotalPrice(new BigDecimal("100.00"))
                .setTaxPrice(new BigDecimal("13.00"));
        ErpPurchaseInDO purchaseIn = new ErpPurchaseInDO()
                .setId(11L)
                .setNo("PI-001")
                .setOrderId(21L)
                .setOrderNo("PO-001")
                .setSupplierId(201L);
        ErpApStatementDO statement = new ErpApStatementDO()
                .setId(31L)
                .setBizType(ErpBizTypeEnum.PURCHASE_IN.getType())
                .setBizId(11L)
                .setBizNo("PI-001")
                .setSourceOrderId(21L)
                .setSourceOrderNo("PO-001")
                .setSupplierId(201L)
                .setAmount(new BigDecimal("100.00"))
                .setInvoiceStatus(ErpApInvoiceStatusEnum.NONE.getStatus());
        List<ErpApInvoiceMatchItemDO> insertedItems = new ArrayList<>();
        AtomicReference<ErpApInvoiceDO> updatedInvoiceRef = new AtomicReference<>();
        AtomicReference<Object[]> updatedStatementInvoiceRef = new AtomicReference<>();
        AtomicReference<ErpApStatementItemDO> insertedStatementItemRef = new AtomicReference<>();
        AtomicReference<Object[]> estimateSyncArgsRef = new AtomicReference<>();

        setField(service, "invoiceMapper", createProxy(ErpApInvoiceMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return invoice;
            }
            if ("selectBatchIds".equals(methodName)) {
                return List.of(invoice);
            }
            if ("updateById".equals(methodName)) {
                updatedInvoiceRef.set((ErpApInvoiceDO) args[0]);
                invoice.setMatchedAmount(updatedInvoiceRef.get().getMatchedAmount());
                invoice.setUnmatchedAmount(updatedInvoiceRef.get().getUnmatchedAmount());
                invoice.setDifferenceAmount(updatedInvoiceRef.get().getDifferenceAmount());
                invoice.setMatchStatus(updatedInvoiceRef.get().getMatchStatus());
                return 1;
            }
            return null;
        }));
        setField(service, "invoiceMatchItemMapper", createProxy(ErpApInvoiceMatchItemMapper.class, (methodName, args) -> {
            if ("selectActiveListByPurchaseInItemIds".equals(methodName)) {
                return List.of();
            }
            if ("selectActiveListByInvoiceId".equals(methodName)) {
                return insertedItems;
            }
            if ("selectActiveListByStatementIds".equals(methodName)) {
                return insertedItems;
            }
            if ("insertBatch".equals(methodName)) {
                insertedItems.addAll((List<ErpApInvoiceMatchItemDO>) args[0]);
                return true;
            }
            return null;
        }));
        setField(service, "purchaseInItemMapper", createProxy(ErpPurchaseInItemMapper.class, (methodName, args) -> {
            if ("selectListByIds".equals(methodName)) {
                return List.of(purchaseInItem);
            }
            return null;
        }));
        setField(service, "purchaseInService", createProxy(ErpPurchaseInService.class, (methodName, args) -> {
            if ("getPurchaseInListByIds".equals(methodName)) {
                return List.of(purchaseIn);
            }
            return null;
        }));
        setField(service, "apStatementMapper", createProxy(ErpApStatementMapper.class, (methodName, args) -> {
            if ("selectListByBizTypeAndBizIds".equals(methodName)) {
                return List.of(statement);
            }
            if ("selectBatchIds".equals(methodName)) {
                return List.of(statement);
            }
            if ("updateInvoiceById".equals(methodName)) {
                updatedStatementInvoiceRef.set(args);
                statement.setInvoiceStatus((Integer) args[1]);
                statement.setInvoiceNo((String) args[2]);
                statement.setInvoiceAmount((BigDecimal) args[3]);
                return 1;
            }
            return null;
        }));
        setField(service, "apStatementItemMapper", createProxy(ErpApStatementItemMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertedStatementItemRef.set((ErpApStatementItemDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "apEstimateService", createProxy(ErpApEstimateService.class, (methodName, args) -> {
            if ("syncByStatementInvoiceChange".equals(methodName)) {
                estimateSyncArgsRef.set(args);
            }
            return null;
        }));

        service.confirmMatch(new ErpApInvoiceMatchReqVO()
                .setInvoiceId(1L)
                .setItems(List.of(new ErpApInvoiceMatchReqVO.Item()
                        .setPurchaseInItemId(1001L)
                        .setMatchCount(new BigDecimal("10"))
                        .setMatchAmount(new BigDecimal("100.00"))
                        .setRemark("整单收票"))));

        assertEquals(1, insertedItems.size());
        assertEquals(1L, insertedItems.get(0).getInvoiceId());
        assertEquals(31L, insertedItems.get(0).getApStatementId());
        assertEquals(11L, insertedItems.get(0).getSourcePurchaseInId());
        assertEquals(1001L, insertedItems.get(0).getSourcePurchaseInItemId());
        assertEquals(new BigDecimal("100.00"), insertedItems.get(0).getMatchAmount());
        assertEquals(ErpApInvoiceMatchItemStatusEnum.ACTIVE.getStatus(), insertedItems.get(0).getStatus());
        assertEquals(ErpApInvoiceMatchStatusEnum.MATCHED.getStatus(), updatedInvoiceRef.get().getMatchStatus());
        assertEquals(new BigDecimal("100.00"), updatedInvoiceRef.get().getMatchedAmount());
        assertEquals(new BigDecimal("0.40"), updatedInvoiceRef.get().getDifferenceAmount());
        assertEquals(31L, updatedStatementInvoiceRef.get()[0]);
        assertEquals(ErpApInvoiceStatusEnum.RECEIVED.getStatus(), updatedStatementInvoiceRef.get()[1]);
        assertEquals("FP-20260428-001", updatedStatementInvoiceRef.get()[2]);
        assertEquals(new BigDecimal("100.00"), updatedStatementInvoiceRef.get()[3]);
        assertEquals(ErpApStatementItemTypeEnum.INVOICE_MATCHED.getStatus(), insertedStatementItemRef.get().getItemType());
        assertEquals(statement, estimateSyncArgsRef.get()[0]);
        assertEquals(ErpApInvoiceStatusEnum.NONE.getStatus(), estimateSyncArgsRef.get()[1]);
        assertEquals(ErpApInvoiceStatusEnum.RECEIVED.getStatus(), estimateSyncArgsRef.get()[2]);
        assertEquals(1L, estimateSyncArgsRef.get()[4]);
        assertEquals("FP-20260428-001", estimateSyncArgsRef.get()[5]);
    }

    @Test
    void confirmMatch_shouldRejectWhenAmountExceedsRemainingAmount() throws Exception {
        ErpApInvoiceServiceImpl service = new ErpApInvoiceServiceImpl();
        ErpApInvoiceDO invoice = new ErpApInvoiceDO()
                .setId(2L)
                .setSupplierId(201L)
                .setInvoiceNo("FP-20260428-002")
                .setTotalAmount(new BigDecimal("120.00"))
                .setToleranceAmount(new BigDecimal("1.00"))
                .setMatchStatus(ErpApInvoiceMatchStatusEnum.UNMATCHED.getStatus());
        ErpPurchaseInItemDO purchaseInItem = new ErpPurchaseInItemDO()
                .setId(1002L)
                .setInId(12L)
                .setProductId(302L)
                .setCount(new BigDecimal("10"))
                .setTotalPrice(new BigDecimal("100.00"));
        ErpPurchaseInDO purchaseIn = new ErpPurchaseInDO()
                .setId(12L)
                .setNo("PI-002")
                .setSupplierId(201L);
        ErpApStatementDO statement = new ErpApStatementDO()
                .setId(32L)
                .setBizType(ErpBizTypeEnum.PURCHASE_IN.getType())
                .setBizId(12L)
                .setBizNo("PI-002")
                .setSupplierId(201L)
                .setAmount(new BigDecimal("100.00"));
        ErpApInvoiceMatchItemDO existingItem = new ErpApInvoiceMatchItemDO()
                .setId(5001L)
                .setInvoiceId(99L)
                .setSourcePurchaseInItemId(1002L)
                .setMatchCount(new BigDecimal("8"))
                .setMatchAmount(new BigDecimal("80.00"))
                .setStatus(ErpApInvoiceMatchItemStatusEnum.ACTIVE.getStatus());

        setField(service, "invoiceMapper", createProxy(ErpApInvoiceMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return invoice;
            }
            return null;
        }));
        setField(service, "invoiceMatchItemMapper", createProxy(ErpApInvoiceMatchItemMapper.class, (methodName, args) -> {
            if ("selectActiveListByPurchaseInItemIds".equals(methodName)) {
                return List.of(existingItem);
            }
            return List.of();
        }));
        setField(service, "purchaseInItemMapper", createProxy(ErpPurchaseInItemMapper.class, (methodName, args) -> {
            if ("selectListByIds".equals(methodName)) {
                return List.of(purchaseInItem);
            }
            return null;
        }));
        setField(service, "purchaseInService", createProxy(ErpPurchaseInService.class, (methodName, args) -> {
            if ("getPurchaseInListByIds".equals(methodName)) {
                return List.of(purchaseIn);
            }
            return null;
        }));
        setField(service, "apStatementMapper", createProxy(ErpApStatementMapper.class, (methodName, args) -> {
            if ("selectListByBizTypeAndBizIds".equals(methodName)) {
                return List.of(statement);
            }
            return List.of();
        }));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.confirmMatch(new ErpApInvoiceMatchReqVO()
                .setInvoiceId(2L)
                .setItems(List.of(new ErpApInvoiceMatchReqVO.Item()
                        .setPurchaseInItemId(1002L)
                        .setMatchCount(new BigDecimal("2"))
                        .setMatchAmount(new BigDecimal("30.00"))))));

        assertEquals(AP_INVOICE_MATCH_AMOUNT_EXCEED.getCode(), ex.getCode());
    }

    @Test
    void confirmMatch_shouldRejectWhenDuplicateItemsInSameRequestExceedRemainingCount() throws Exception {
        ErpApInvoiceServiceImpl service = new ErpApInvoiceServiceImpl();
        ErpApInvoiceDO invoice = new ErpApInvoiceDO()
                .setId(22L)
                .setSupplierId(201L)
                .setInvoiceNo("FP-20260428-022")
                .setTotalAmount(new BigDecimal("200.00"))
                .setToleranceAmount(new BigDecimal("1.00"))
                .setMatchStatus(ErpApInvoiceMatchStatusEnum.UNMATCHED.getStatus());
        ErpPurchaseInItemDO purchaseInItem = new ErpPurchaseInItemDO()
                .setId(1022L)
                .setInId(122L)
                .setProductId(302L)
                .setCount(new BigDecimal("10"))
                .setTotalPrice(new BigDecimal("100.00"));
        ErpPurchaseInDO purchaseIn = new ErpPurchaseInDO()
                .setId(122L)
                .setNo("PI-022")
                .setSupplierId(201L);
        ErpApStatementDO statement = new ErpApStatementDO()
                .setId(322L)
                .setBizType(ErpBizTypeEnum.PURCHASE_IN.getType())
                .setBizId(122L)
                .setBizNo("PI-022")
                .setSupplierId(201L)
                .setAmount(new BigDecimal("100.00"));

        setField(service, "invoiceMapper", createProxy(ErpApInvoiceMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return invoice;
            }
            return null;
        }));
        setField(service, "invoiceMatchItemMapper", createProxy(ErpApInvoiceMatchItemMapper.class, (methodName, args) -> {
            if ("selectActiveListByPurchaseInItemIds".equals(methodName)) {
                return List.of();
            }
            return List.of();
        }));
        setField(service, "purchaseInItemMapper", createProxy(ErpPurchaseInItemMapper.class, (methodName, args) -> {
            if ("selectListByIds".equals(methodName)) {
                return List.of(purchaseInItem);
            }
            return null;
        }));
        setField(service, "purchaseInService", createProxy(ErpPurchaseInService.class, (methodName, args) -> {
            if ("getPurchaseInListByIds".equals(methodName)) {
                return List.of(purchaseIn);
            }
            return null;
        }));
        setField(service, "apStatementMapper", createProxy(ErpApStatementMapper.class, (methodName, args) -> {
            if ("selectListByBizTypeAndBizIds".equals(methodName)) {
                return List.of(statement);
            }
            return List.of();
        }));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.confirmMatch(new ErpApInvoiceMatchReqVO()
                .setInvoiceId(22L)
                .setItems(List.of(
                        new ErpApInvoiceMatchReqVO.Item()
                                .setPurchaseInItemId(1022L)
                                .setMatchCount(new BigDecimal("6"))
                                .setMatchAmount(new BigDecimal("30.00")),
                        new ErpApInvoiceMatchReqVO.Item()
                                .setPurchaseInItemId(1022L)
                                .setMatchCount(new BigDecimal("5"))
                                .setMatchAmount(new BigDecimal("25.00"))))));

        assertEquals(AP_INVOICE_MATCH_COUNT_EXCEED.getCode(), ex.getCode());
    }

    @Test
    void cancelMatch_shouldRollbackStatementInvoiceSummary() throws Exception {
        ErpApInvoiceServiceImpl service = new ErpApInvoiceServiceImpl();
        ErpApInvoiceDO invoice = new ErpApInvoiceDO()
                .setId(3L)
                .setSupplierId(201L)
                .setInvoiceNo("FP-20260428-003")
                .setTotalAmount(new BigDecimal("100.00"))
                .setMatchedAmount(new BigDecimal("100.00"))
                .setUnmatchedAmount(BigDecimal.ZERO)
                .setToleranceAmount(new BigDecimal("1.00"))
                .setMatchStatus(ErpApInvoiceMatchStatusEnum.MATCHED.getStatus());
        ErpApInvoiceMatchItemDO activeItem = new ErpApInvoiceMatchItemDO()
                .setId(6001L)
                .setInvoiceId(3L)
                .setApStatementId(33L)
                .setSourcePurchaseInId(13L)
                .setSourcePurchaseInItemId(1003L)
                .setMatchCount(new BigDecimal("10"))
                .setMatchAmount(new BigDecimal("100.00"))
                .setStatus(ErpApInvoiceMatchItemStatusEnum.ACTIVE.getStatus());
        ErpApStatementDO statement = new ErpApStatementDO()
                .setId(33L)
                .setAmount(new BigDecimal("100.00"))
                .setPaidAmount(BigDecimal.ZERO)
                .setRemainAmount(new BigDecimal("100.00"));
        AtomicReference<ErpApInvoiceMatchItemDO> updatedItemRef = new AtomicReference<>();
        AtomicReference<ErpApInvoiceDO> updatedInvoiceRef = new AtomicReference<>();
        AtomicReference<Object[]> updatedStatementInvoiceRef = new AtomicReference<>();
        AtomicReference<ErpApStatementItemDO> insertedStatementItemRef = new AtomicReference<>();

        setField(service, "invoiceMapper", createProxy(ErpApInvoiceMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return invoice;
            }
            if ("updateById".equals(methodName)) {
                updatedInvoiceRef.set((ErpApInvoiceDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "invoiceMatchItemMapper", createProxy(ErpApInvoiceMatchItemMapper.class, (methodName, args) -> {
            if ("selectListByIds".equals(methodName)) {
                return List.of(activeItem);
            }
            if ("selectActiveListByInvoiceId".equals(methodName)) {
                return List.of();
            }
            if ("updateById".equals(methodName)) {
                updatedItemRef.set((ErpApInvoiceMatchItemDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "apStatementMapper", createProxy(ErpApStatementMapper.class, (methodName, args) -> {
            if ("selectBatchIds".equals(methodName)) {
                return List.of(statement);
            }
            if ("updateInvoiceById".equals(methodName)) {
                updatedStatementInvoiceRef.set(args);
                return 1;
            }
            return null;
        }));
        setField(service, "apStatementItemMapper", createProxy(ErpApStatementItemMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertedStatementItemRef.set((ErpApStatementItemDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "apEstimateService", createProxy(ErpApEstimateService.class, (methodName, args) -> null));

        service.cancelMatch(new ErpApInvoiceCancelMatchReqVO().setIds(List.of(6001L)));

        assertEquals(6001L, updatedItemRef.get().getId());
        assertEquals(ErpApInvoiceMatchItemStatusEnum.CANCELED.getStatus(), updatedItemRef.get().getStatus());
        assertEquals(ErpApInvoiceMatchStatusEnum.UNMATCHED.getStatus(), updatedInvoiceRef.get().getMatchStatus());
        assertEquals(BigDecimal.ZERO, updatedInvoiceRef.get().getMatchedAmount());
        assertEquals(new BigDecimal("100.00"), updatedInvoiceRef.get().getUnmatchedAmount());
        assertEquals(33L, updatedStatementInvoiceRef.get()[0]);
        assertEquals(ErpApInvoiceStatusEnum.NONE.getStatus(), updatedStatementInvoiceRef.get()[1]);
        assertNull(updatedStatementInvoiceRef.get()[2]);
        assertNull(updatedStatementInvoiceRef.get()[3]);
        assertEquals(ErpApStatementItemTypeEnum.INVOICE_MATCH_CANCELED.getStatus(), insertedStatementItemRef.get().getItemType());
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
