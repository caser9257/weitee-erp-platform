package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementAgingReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementAgingRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apstatement.ErpApStatementUpdateInvoiceReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApStatementItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePaymentAllocateDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePrepaymentAllocateDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpOutsourceFeeDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpOutsourceOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpApStatementItemMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpApStatementMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceExpenseMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinancePaymentAllocateMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinancePrepaymentAllocateMapper;
import cn.iocoder.yudao.module.erp.enums.ErpApEstimateReverseTypeEnum;
import cn.iocoder.yudao.module.erp.enums.ErpApInvoiceStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpApStatementItemTypeEnum;
import cn.iocoder.yudao.module.erp.enums.ErpApStatementStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpFinancePaymentAllocateStatusEnum;
import cn.iocoder.yudao.module.erp.enums.common.ErpBizTypeEnum;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ErpApStatementServiceImplTest {

    @Test
    void shouldExposeApStatementBaseModels() {
        assertEquals(10, ErpApStatementStatusEnum.UNPAID.getStatus());
        assertEquals(0, ErpApInvoiceStatusEnum.NONE.getStatus());
        assertEquals(20, ErpFinancePaymentAllocateStatusEnum.APPROVED.getStatus());

        ErpApStatementDO statement = new ErpApStatementDO()
                .setAmount(new BigDecimal("120.00"))
                .setPaidAmount(BigDecimal.ZERO)
                .setRemainAmount(new BigDecimal("120.00"));
        assertEquals(new BigDecimal("120.00"), statement.getRemainAmount());
    }

    @Test
    void createStatementForPurchaseIn_shouldCreatePositiveRemainAmount() throws Exception {
        ErpApStatementServiceImpl service = new ErpApStatementServiceImpl();
        ErpPurchaseInDO purchaseIn = new ErpPurchaseInDO()
                .setId(11L)
                .setNo("PI-001")
                .setOrderId(21L)
                .setOrderNo("PO-001")
                .setSupplierId(31L)
                .setAccountId(41L)
                .setInTime(LocalDateTime.of(2026, 4, 23, 10, 0))
                .setTotalPrice(new BigDecimal("120.00"))
                .setRemark("purchase in");
        AtomicReference<ErpApStatementDO> insertedStatementRef = new AtomicReference<>();
        AtomicReference<ErpApStatementItemDO> insertedItemRef = new AtomicReference<>();

        setField(service, "erpApStatementMapper", createProxy(ErpApStatementMapper.class, (methodName, args) -> {
            if ("selectByBizTypeAndBizId".equals(methodName)) {
                return null;
            }
            if ("insert".equals(methodName)) {
                ErpApStatementDO statement = (ErpApStatementDO) args[0];
                statement.setId(1001L);
                insertedStatementRef.set(statement);
                return 1;
            }
            return null;
        }));
        setField(service, "erpApStatementItemMapper", createProxy(ErpApStatementItemMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertedItemRef.set((ErpApStatementItemDO) args[0]);
                return 1;
            }
            return null;
        }));

        service.createStatementForPurchaseIn(purchaseIn);

        assertEquals(ErpBizTypeEnum.PURCHASE_IN.getType(), insertedStatementRef.get().getBizType());
        assertEquals(new BigDecimal("120.00"), insertedStatementRef.get().getAmount());
        assertEquals(BigDecimal.ZERO, insertedStatementRef.get().getPaidAmount());
        assertEquals(new BigDecimal("120.00"), insertedStatementRef.get().getRemainAmount());
        assertEquals(ErpApStatementStatusEnum.UNPAID.getStatus(), insertedStatementRef.get().getStatus());
        assertEquals(ErpApStatementItemTypeEnum.CREATED.getStatus(), insertedItemRef.get().getItemType());
        assertEquals(new BigDecimal("120.00"), insertedItemRef.get().getAmount());
    }

    @Test
    void createStatementForOutsourceFee_shouldCreatePositiveRemainAmount() throws Exception {
        ErpApStatementServiceImpl service = new ErpApStatementServiceImpl();
        ErpOutsourceFeeDO outsourceFee = new ErpOutsourceFeeDO()
                .setId(31L)
                .setFeeNo("WWFY-001")
                .setOrderId(21L)
                .setFeeTime(LocalDateTime.of(2026, 4, 28, 9, 30))
                .setFeeAmount(new BigDecimal("88.00"))
                .setRemark("outsource fee");
        ErpOutsourceOrderDO outsourceOrder = new ErpOutsourceOrderDO()
                .setId(21L)
                .setNo("WWDD-001")
                .setSupplierId(41L);
        AtomicReference<ErpApStatementDO> insertedStatementRef = new AtomicReference<>();
        AtomicReference<ErpApStatementItemDO> insertedItemRef = new AtomicReference<>();

        setField(service, "erpApStatementMapper", createProxy(ErpApStatementMapper.class, (methodName, args) -> {
            if ("selectByBizTypeAndBizId".equals(methodName)) {
                return null;
            }
            if ("insert".equals(methodName)) {
                ErpApStatementDO statement = (ErpApStatementDO) args[0];
                statement.setId(1002L);
                insertedStatementRef.set(statement);
                return 1;
            }
            return null;
        }));
        setField(service, "erpApStatementItemMapper", createProxy(ErpApStatementItemMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertedItemRef.set((ErpApStatementItemDO) args[0]);
                return 1;
            }
            return null;
        }));

        service.createStatementForOutsourceFee(outsourceFee, outsourceOrder);

        assertEquals(ErpBizTypeEnum.OUTSOURCE_FEE.getType(), insertedStatementRef.get().getBizType());
        assertEquals("WWFY-001", insertedStatementRef.get().getBizNo());
        assertEquals("WWDD-001", insertedStatementRef.get().getSourceOrderNo());
        assertEquals(41L, insertedStatementRef.get().getSupplierId());
        assertEquals(new BigDecimal("88.00"), insertedStatementRef.get().getAmount());
        assertEquals(BigDecimal.ZERO, insertedStatementRef.get().getPaidAmount());
        assertEquals(new BigDecimal("88.00"), insertedStatementRef.get().getRemainAmount());
        assertEquals(ErpApStatementStatusEnum.UNPAID.getStatus(), insertedStatementRef.get().getStatus());
        assertEquals(ErpApStatementItemTypeEnum.CREATED.getStatus(), insertedItemRef.get().getItemType());
        assertEquals(new BigDecimal("88.00"), insertedItemRef.get().getAmount());
    }

    @Test
    void createStatementForFinanceExpense_shouldCreatePositiveRemainAmount() throws Exception {
        ErpApStatementServiceImpl service = new ErpApStatementServiceImpl();
        ErpFinanceExpenseDO expense = new ErpFinanceExpenseDO()
                .setId(51L)
                .setNo("LSBX20260429000001")
                .setSupplierId(41L)
                .setAccountId(51L)
                .setExpenseTime(LocalDateTime.of(2026, 4, 29, 10, 0))
                .setExpensePrice(new BigDecimal("66.00"))
                .setRemark("expense");
        AtomicReference<ErpApStatementDO> insertedStatementRef = new AtomicReference<>();
        AtomicReference<ErpApStatementItemDO> insertedItemRef = new AtomicReference<>();

        setField(service, "erpApStatementMapper", createProxy(ErpApStatementMapper.class, (methodName, args) -> {
            if ("selectByBizTypeAndBizId".equals(methodName)) {
                return null;
            }
            if ("insert".equals(methodName)) {
                ErpApStatementDO statement = (ErpApStatementDO) args[0];
                statement.setId(1003L);
                insertedStatementRef.set(statement);
                return 1;
            }
            return null;
        }));
        setField(service, "erpApStatementItemMapper", createProxy(ErpApStatementItemMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertedItemRef.set((ErpApStatementItemDO) args[0]);
                return 1;
            }
            return null;
        }));

        service.createStatementForFinanceExpense(expense);

        assertEquals(ErpBizTypeEnum.FINANCE_EXPENSE.getType(), insertedStatementRef.get().getBizType());
        assertEquals("LSBX20260429000001", insertedStatementRef.get().getBizNo());
        assertEquals(41L, insertedStatementRef.get().getSupplierId());
        assertEquals(new BigDecimal("66.00"), insertedStatementRef.get().getAmount());
        assertEquals(BigDecimal.ZERO, insertedStatementRef.get().getPaidAmount());
        assertEquals(new BigDecimal("66.00"), insertedStatementRef.get().getRemainAmount());
        assertEquals(ErpApStatementItemTypeEnum.CREATED.getStatus(), insertedItemRef.get().getItemType());
    }

    @Test
    void refreshStatementAmountByIds_shouldRecalculateOutsourceFeeStatementAmounts() throws Exception {
        ErpApStatementServiceImpl service = new ErpApStatementServiceImpl();
        ErpApStatementDO storedStatement = new ErpApStatementDO()
                .setId(3002L)
                .setBizType(ErpBizTypeEnum.OUTSOURCE_FEE.getType())
                .setBizId(31L)
                .setAmount(new BigDecimal("88.00"))
                .setPaidAmount(BigDecimal.ZERO)
                .setRemainAmount(new BigDecimal("88.00"))
                .setStatus(ErpApStatementStatusEnum.UNPAID.getStatus());
        AtomicReference<ErpApStatementDO> updatedStatementRef = new AtomicReference<>();

        setField(service, "erpApStatementMapper", createProxy(ErpApStatementMapper.class, (methodName, args) -> {
            if ("selectBatchIds".equals(methodName)) {
                return List.of(storedStatement);
            }
            if ("updateById".equals(methodName)) {
                ErpApStatementDO updateObj = (ErpApStatementDO) args[0];
                updatedStatementRef.set(updateObj);
                storedStatement.setPaidAmount(updateObj.getPaidAmount());
                storedStatement.setRemainAmount(updateObj.getRemainAmount());
                storedStatement.setStatus(updateObj.getStatus());
                return 1;
            }
            return null;
        }));
        setField(service, "erpFinancePaymentAllocateMapper", createProxy(ErpFinancePaymentAllocateMapper.class, (methodName, args) -> {
            if ("selectApprovedListByStatementIds".equals(methodName)) {
                return List.of(
                        new ErpFinancePaymentAllocateDO().setApStatementId(3002L).setAllocateAmount(new BigDecimal("60.00")),
                        new ErpFinancePaymentAllocateDO().setApStatementId(3002L).setAllocateAmount(new BigDecimal("8.00"))
                );
            }
            return null;
        }));
        setField(service, "erpFinancePrepaymentAllocateMapper", createProxy(ErpFinancePrepaymentAllocateMapper.class, (methodName, args) -> {
            if ("selectApprovedListByStatementIds".equals(methodName)) {
                return List.of(new ErpFinancePrepaymentAllocateDO()
                        .setApStatementId(3002L)
                        .setAllocateAmount(new BigDecimal("10.00")));
            }
            return null;
        }));

        service.refreshStatementAmountByIds(List.of(3002L));

        assertEquals(new BigDecimal("78.00"), updatedStatementRef.get().getPaidAmount());
        assertEquals(new BigDecimal("10.00"), updatedStatementRef.get().getRemainAmount());
        assertEquals(ErpApStatementStatusEnum.PARTIAL_PAID.getStatus(), updatedStatementRef.get().getStatus());
        assertEquals(new BigDecimal("78.00"), storedStatement.getPaidAmount());
        assertEquals(new BigDecimal("10.00"), storedStatement.getRemainAmount());
    }

    @Test
    void refreshStatementAmountByIds_shouldRestoreOutsourceFeeStatementWhenAllocationsRollback() throws Exception {
        ErpApStatementServiceImpl service = new ErpApStatementServiceImpl();
        ErpApStatementDO storedStatement = new ErpApStatementDO()
                .setId(3003L)
                .setBizType(ErpBizTypeEnum.OUTSOURCE_FEE.getType())
                .setBizId(32L)
                .setAmount(new BigDecimal("88.00"))
                .setPaidAmount(new BigDecimal("78.00"))
                .setRemainAmount(new BigDecimal("10.00"))
                .setStatus(ErpApStatementStatusEnum.PARTIAL_PAID.getStatus());
        AtomicReference<ErpApStatementDO> updatedStatementRef = new AtomicReference<>();

        setField(service, "erpApStatementMapper", createProxy(ErpApStatementMapper.class, (methodName, args) -> {
            if ("selectBatchIds".equals(methodName)) {
                return List.of(storedStatement);
            }
            if ("updateById".equals(methodName)) {
                ErpApStatementDO updateObj = (ErpApStatementDO) args[0];
                updatedStatementRef.set(updateObj);
                storedStatement.setPaidAmount(updateObj.getPaidAmount());
                storedStatement.setRemainAmount(updateObj.getRemainAmount());
                storedStatement.setStatus(updateObj.getStatus());
                return 1;
            }
            return null;
        }));
        setField(service, "erpFinancePaymentAllocateMapper", createProxy(ErpFinancePaymentAllocateMapper.class, (methodName, args) -> {
            if ("selectApprovedListByStatementIds".equals(methodName)) {
                return List.of();
            }
            return null;
        }));
        setField(service, "erpFinancePrepaymentAllocateMapper", createProxy(ErpFinancePrepaymentAllocateMapper.class, (methodName, args) -> {
            if ("selectApprovedListByStatementIds".equals(methodName)) {
                return List.of();
            }
            return null;
        }));

        service.refreshStatementAmountByIds(List.of(3003L));

        assertEquals(BigDecimal.ZERO, updatedStatementRef.get().getPaidAmount());
        assertEquals(new BigDecimal("88.00"), updatedStatementRef.get().getRemainAmount());
        assertEquals(ErpApStatementStatusEnum.UNPAID.getStatus(), updatedStatementRef.get().getStatus());
        assertEquals(BigDecimal.ZERO, storedStatement.getPaidAmount());
        assertEquals(new BigDecimal("88.00"), storedStatement.getRemainAmount());
    }

    @Test
    void refreshBizSummaryByStatementIds_shouldSyncExpenseAmounts() throws Exception {
        ErpApStatementServiceImpl service = new ErpApStatementServiceImpl();
        ErpApStatementDO statement = new ErpApStatementDO()
                .setId(3001L)
                .setBizType(ErpBizTypeEnum.FINANCE_EXPENSE.getType())
                .setBizId(51L)
                .setPaidAmount(new BigDecimal("20.00"))
                .setRemainAmount(new BigDecimal("46.00"));
        AtomicReference<ErpFinanceExpenseDO> updatedExpenseRef = new AtomicReference<>();

        setField(service, "erpApStatementMapper", createProxy(ErpApStatementMapper.class, (methodName, args) -> {
            if ("selectBatchIds".equals(methodName)) {
                return List.of(statement);
            }
            return null;
        }));
        setField(service, "erpFinanceExpenseMapper", createProxy(ErpFinanceExpenseMapper.class, (methodName, args) -> {
            if ("updateById".equals(methodName)) {
                updatedExpenseRef.set((ErpFinanceExpenseDO) args[0]);
                return 1;
            }
            return null;
        }));

        service.refreshBizSummaryByStatementIds(List.of(3001L));

        assertEquals(51L, updatedExpenseRef.get().getId());
        assertEquals(new BigDecimal("20.00"), updatedExpenseRef.get().getPaidPrice());
        assertEquals(new BigDecimal("46.00"), updatedExpenseRef.get().getRemainPrice());
    }

    @Test
    void calculateAging_shouldBucketOnlyPositiveRemainAmount() throws Exception {
        ErpApStatementServiceImpl service = new ErpApStatementServiceImpl();
        ErpApStatementAgingReqVO reqVO = new ErpApStatementAgingReqVO()
                .setAsOfDate(LocalDate.of(2026, 4, 23));
        ErpApStatementAgingRespVO agingRespVO = new ErpApStatementAgingRespVO()
                .setSupplierId(9L)
                .setAmount0To30(new BigDecimal("100.00"))
                .setAmount31To60(BigDecimal.ZERO)
                .setAmount61To90(BigDecimal.ZERO)
                .setAmount91Plus(BigDecimal.ZERO)
                .setTotalRemainAmount(new BigDecimal("100.00"));

        setField(service, "erpApStatementMapper", createProxy(ErpApStatementMapper.class, (methodName, args) -> {
            if ("selectAgingList".equals(methodName)) {
                return List.of(agingRespVO);
            }
            return null;
        }));

        List<ErpApStatementAgingRespVO> result = service.getAgingList(reqVO);

        assertEquals(1, result.size());
        assertEquals(new BigDecimal("100.00"), result.get(0).getAmount0To30());
        assertEquals(new BigDecimal("100.00"), result.get(0).getTotalRemainAmount());
    }

    @Test
    void updateInvoice_shouldClearInvoiceFieldsWhenResetToNone() throws Exception {
        ErpApStatementServiceImpl service = new ErpApStatementServiceImpl();
        ErpApStatementDO storedStatement = new ErpApStatementDO()
                .setId(1L)
                .setBizType(ErpBizTypeEnum.PURCHASE_IN.getType())
                .setBizId(11L)
                .setBizNo("PI-001")
                .setInvoiceStatus(ErpApInvoiceStatusEnum.RECEIVED.getStatus())
                .setInvoiceNo("INV-001")
                .setInvoiceAmount(new BigDecimal("99.00"))
                .setPaidAmount(BigDecimal.ZERO)
                .setRemainAmount(new BigDecimal("99.00"));
        AtomicReference<ErpApStatementItemDO> insertedItemRef = new AtomicReference<>();

        setField(service, "erpApStatementMapper", createProxy(ErpApStatementMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return storedStatement;
            }
            if ("updateById".equals(methodName)) {
                ErpApStatementDO updateObj = (ErpApStatementDO) args[0];
                if (updateObj.getInvoiceStatus() != null) {
                    storedStatement.setInvoiceStatus(updateObj.getInvoiceStatus());
                }
                if (updateObj.getInvoiceNo() != null) {
                    storedStatement.setInvoiceNo(updateObj.getInvoiceNo());
                }
                if (updateObj.getInvoiceAmount() != null) {
                    storedStatement.setInvoiceAmount(updateObj.getInvoiceAmount());
                }
                return 1;
            }
            if ("updateInvoiceById".equals(methodName)) {
                storedStatement.setInvoiceStatus((Integer) args[1]);
                storedStatement.setInvoiceNo((String) args[2]);
                storedStatement.setInvoiceAmount((BigDecimal) args[3]);
                return 1;
            }
            return null;
        }));
        setField(service, "erpApStatementItemMapper", createProxy(ErpApStatementItemMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertedItemRef.set((ErpApStatementItemDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "apEstimateService", createProxy(ErpApEstimateService.class, (methodName, args) -> null));

        service.updateInvoice(new ErpApStatementUpdateInvoiceReqVO()
                .setId(1L)
                .setInvoiceStatus(ErpApInvoiceStatusEnum.NONE.getStatus())
                .setInvoiceNo("")
                .setInvoiceAmount(BigDecimal.ZERO)
                .setRemark("reset invoice"));

        assertEquals(ErpApInvoiceStatusEnum.NONE.getStatus(), storedStatement.getInvoiceStatus());
        assertNull(storedStatement.getInvoiceNo());
        assertNull(storedStatement.getInvoiceAmount());
        assertEquals(ErpApStatementItemTypeEnum.INVOICE_UPDATED.getStatus(), insertedItemRef.get().getItemType());
    }

    @Test
    void updateInvoice_shouldReverseEstimateWhenPurchaseInInvoiceReceived() throws Exception {
        ErpApStatementServiceImpl service = new ErpApStatementServiceImpl();
        ErpApStatementDO storedStatement = new ErpApStatementDO()
                .setId(1L)
                .setBizType(ErpBizTypeEnum.PURCHASE_IN.getType())
                .setBizId(11L)
                .setBizNo("PI-001")
                .setInvoiceStatus(ErpApInvoiceStatusEnum.NONE.getStatus())
                .setPaidAmount(BigDecimal.ZERO)
                .setRemainAmount(new BigDecimal("99.00"));
        AtomicReference<Object[]> syncArgsRef = new AtomicReference<>();

        setField(service, "erpApStatementMapper", createProxy(ErpApStatementMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return storedStatement;
            }
            if ("updateInvoiceById".equals(methodName)) {
                storedStatement.setInvoiceStatus((Integer) args[1]);
                storedStatement.setInvoiceNo((String) args[2]);
                storedStatement.setInvoiceAmount((BigDecimal) args[3]);
                return 1;
            }
            return null;
        }));
        setField(service, "erpApStatementItemMapper", createProxy(ErpApStatementItemMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(service, "apEstimateService", createProxy(ErpApEstimateService.class, (methodName, args) -> {
            if ("syncByStatementInvoiceChange".equals(methodName)) {
                syncArgsRef.set(args);
            }
            return null;
        }));

        service.updateInvoice(new ErpApStatementUpdateInvoiceReqVO()
                .setId(1L)
                .setInvoiceStatus(ErpApInvoiceStatusEnum.RECEIVED.getStatus())
                .setInvoiceNo("INV-001")
                .setInvoiceAmount(new BigDecimal("99.00"))
                .setRemark("receive invoice"));

        assertEquals(ErpApInvoiceStatusEnum.RECEIVED.getStatus(), storedStatement.getInvoiceStatus());
        assertEquals("INV-001", storedStatement.getInvoiceNo());
        assertEquals(new BigDecimal("99.00"), storedStatement.getInvoiceAmount());
        assertEquals(storedStatement, syncArgsRef.get()[0]);
        assertEquals(ErpApInvoiceStatusEnum.NONE.getStatus(), syncArgsRef.get()[1]);
        assertEquals(ErpApInvoiceStatusEnum.RECEIVED.getStatus(), syncArgsRef.get()[2]);
        assertEquals("INV-001", syncArgsRef.get()[5]);
    }

    @Test
    void closeStatementByBiz_shouldReverseEstimateWhenPurchaseInClosed() throws Exception {
        ErpApStatementServiceImpl service = new ErpApStatementServiceImpl();
        ErpApStatementDO storedStatement = new ErpApStatementDO()
                .setId(1L)
                .setBizType(ErpBizTypeEnum.PURCHASE_IN.getType())
                .setBizId(11L)
                .setBizNo("PI-001")
                .setStatus(ErpApStatementStatusEnum.UNPAID.getStatus())
                .setPaidAmount(BigDecimal.ZERO)
                .setRemainAmount(new BigDecimal("99.00"));
        AtomicReference<ErpApStatementDO> updatedStatementRef = new AtomicReference<>();
        AtomicReference<Object[]> reverseArgsRef = new AtomicReference<>();

        setField(service, "erpApStatementMapper", createProxy(ErpApStatementMapper.class, (methodName, args) -> {
            if ("selectByBizTypeAndBizId".equals(methodName)) {
                return storedStatement;
            }
            if ("updateById".equals(methodName)) {
                updatedStatementRef.set((ErpApStatementDO) args[0]);
                storedStatement.setStatus(((ErpApStatementDO) args[0]).getStatus());
                return 1;
            }
            return null;
        }));
        setField(service, "erpApStatementItemMapper", createProxy(ErpApStatementItemMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(service, "apEstimateService", createProxy(ErpApEstimateService.class, (methodName, args) -> {
            if ("reverseBySourceBiz".equals(methodName)) {
                reverseArgsRef.set(args);
            }
            return null;
        }));

        service.closeStatementByBiz(ErpBizTypeEnum.PURCHASE_IN.getType(), 11L, "close statement");

        assertNotNull(updatedStatementRef.get());
        assertEquals(ErpApStatementStatusEnum.CLOSED.getStatus(), storedStatement.getStatus());
        assertEquals(ErpBizTypeEnum.PURCHASE_IN.getType(), reverseArgsRef.get()[0]);
        assertEquals(11L, reverseArgsRef.get()[1]);
        assertNull(reverseArgsRef.get()[2]);
        assertEquals(ErpApEstimateReverseTypeEnum.STATEMENT_CLOSED.getStatus(), reverseArgsRef.get()[3]);
        assertEquals(1L, reverseArgsRef.get()[4]);
        assertNull(reverseArgsRef.get()[5]);
        assertEquals("close statement", reverseArgsRef.get()[6]);
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
