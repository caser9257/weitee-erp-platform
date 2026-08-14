package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.util.ObjectUtil;
import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherActionReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherGenerateReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher.ErpFinanceVoucherReverseReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceAssetDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceAssetDepreciationDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualLedgerConfigDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualLedgerAmountDiffLogMapper;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceLedgerDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePeriodDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherEntryDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherTemplateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherTemplateItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpOutsourceFeeDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionInboundDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleReturnDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockCheckDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherEntryMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockCheckMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockCheckMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpFinanceVoucherAmountSourceEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceVoucherEntryDirectionEnum;
import cn.weitee.erp.module.erp.enums.ErpFinanceVoucherStatusEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.mrp.ErpProductionInboundService;
import cn.weitee.erp.module.erp.service.mrp.ErpOutsourceOrderService;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseInService;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseReturnService;
import cn.weitee.erp.module.erp.service.sale.ErpSaleOutService;
import cn.weitee.erp.module.erp.service.sale.ErpSaleReturnService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsFinanceVoucher.FINANCE_VOUCHER_SOURCE_TIME_REQUIRED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErpFinanceVoucherServiceImplTest {

    @Test
    void isBizSourceExists_shouldSupportAssetDepreciation() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        setField(service, "financeAssetDepreciationService", createProxy(ErpFinanceAssetDepreciationService.class,
                (methodName, args) -> "getFinanceAssetDepreciation".equals(methodName)
                        ? new ErpFinanceAssetDepreciationDO().setId(66L) : null));
        Method method = ErpFinanceVoucherServiceImpl.class
                .getDeclaredMethod("isBizSourceExists", Integer.class, Long.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(service, ErpBizTypeEnum.ASSET_DEPRECIATION.getType(), 66L));
    }

    @Test
    void resolveVoucherSource_shouldSnapshotDeptFromAssetDepreciation() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        setField(service, "financeAssetDepreciationService", createProxy(ErpFinanceAssetDepreciationService.class,
                (methodName, args) -> "getFinanceAssetDepreciation".equals(methodName)
                        ? new ErpFinanceAssetDepreciationDO().setId(66L).setAssetId(88L).setAssetNo("FA-001")
                        .setPeriod("2026-06").setDepreciationAmount(new BigDecimal("174.17")) : null));
        setField(service, "financeAssetService", createProxy(ErpFinanceAssetService.class,
                (methodName, args) -> "getFinanceAsset".equals(methodName)
                        ? new ErpFinanceAssetDO().setId(88L).setDeptId(99L) : null));
        Method method = ErpFinanceVoucherServiceImpl.class
                .getDeclaredMethod("resolveVoucherSource", Integer.class, Long.class);
        method.setAccessible(true);

        Object voucherSource = method.invoke(service, ErpBizTypeEnum.ASSET_DEPRECIATION.getType(), 66L);
        Field deptIdField = voucherSource.getClass().getDeclaredField("deptId");
        deptIdField.setAccessible(true);

        assertEquals(99L, deptIdField.get(voucherSource));
    }

    @Test
    void recomputeAutoGeneratedVoucher_shouldBindAssetDepreciationToRebuiltVoucher() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<Long> boundVoucherIdRef = new AtomicReference<>();

        mockLedgerWithDefault(service, 1L);
        mockAssetDepreciation(service, 66L, "FA-001", "2026-06", new BigDecimal("174.17"), boundVoucherIdRef);
        setField(service, "voucherTemplateService", createProxy(ErpFinanceVoucherTemplateService.class, (methodName, args) -> {
            if ("validateVoucherTemplate".equals(methodName)) {
                return template(70L, 1L, ErpBizTypeEnum.ASSET_DEPRECIATION.getType(), true, "ASSET_DEPRECIATION");
            }
            if ("getVoucherTemplateItemListByTemplateId".equals(methodName)) {
                return amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType(), null);
            }
            return null;
        }));
        setField(service, "financeVoucherMapper", createProxy(ErpFinanceVoucherMapper.class, (methodName, args) -> {
            if ("selectList".equals(methodName)) {
                return List.of(new ErpFinanceVoucherDO().setId(101L).setLedgerId(1L)
                        .setTemplateId(70L).setBizType(ErpBizTypeEnum.ASSET_DEPRECIATION.getType())
                        .setBizId(66L).setVoucherNo("CWPZ-001")
                        .setStatus(ErpFinanceVoucherStatusEnum.GENERATED.getStatus()));
            }
            return 1;
        }));
        setField(service, "financeVoucherEntryMapper", createProxy(ErpFinanceVoucherEntryMapper.class, (methodName, args) ->
                "insertBatch".equals(methodName) ? true : 1));

        Long voucherId = service.recomputeAutoGeneratedVoucher(
                ErpBizTypeEnum.ASSET_DEPRECIATION.getType(), 66L, 9L, "重算");

        assertEquals(101L, voucherId);
        assertEquals(101L, boundVoucherIdRef.get());
    }

    @Test
    void generateVoucher_shouldCreateVoucherAndEntriesForFinanceExpense() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<ErpFinanceVoucherDO> insertedVoucherRef = new AtomicReference<>();
        AtomicReference<List<ErpFinanceVoucherEntryDO>> insertedEntriesRef = new AtomicReference<>();

        mockValidatedLedger(service, 1L);
        mockTemplate(service, template(10L, 1L, ErpBizTypeEnum.FINANCE_EXPENSE.getType(), true, "EXPENSE_SUMMARY"),
                amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType(), null));
        mockApprovedExpense(service, 100L, "LSBX20260429000001", LocalDateTime.of(2026, 4, 29, 10, 0),
                new BigDecimal("300.00"), "expense remark");
        mockOpenPeriod(service, 20L, 1L, LocalDate.of(2026, 4, 29));
        mockVoucherPersistence(service, "CWPZ20260429000001", 88L, insertedVoucherRef, insertedEntriesRef, null);
        mockOutsourceOrderService(service, null);

        Long id = service.generateVoucher(new ErpFinanceVoucherGenerateReqVO()
                .setLedgerId(1L)
                .setBizType(ErpBizTypeEnum.FINANCE_EXPENSE.getType())
                .setBizId(100L)
                .setTemplateId(10L));

        assertEquals(88L, id);
        assertEquals("CWPZ20260429000001", insertedVoucherRef.get().getVoucherNo());
        assertEquals(new BigDecimal("300.00"), insertedVoucherRef.get().getTotalDebitAmount());
        assertEquals(new BigDecimal("300.00"), insertedVoucherRef.get().getTotalCreditAmount());
        assertEquals(99L, insertedVoucherRef.get().getDeptId());
        assertEquals("expense remark", insertedVoucherRef.get().getRemark());
        assertEquals(2, insertedEntriesRef.get().size());
        assertEquals(new BigDecimal("300.00"), insertedEntriesRef.get().get(0).getDebitAmount());
        assertEquals(new BigDecimal("300.00"), insertedEntriesRef.get().get(1).getCreditAmount());
    }

    @Test
    void generateVoucher_shouldCreateVoucherForStockCheckUsingAbsoluteDifferenceAmount() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<ErpFinanceVoucherDO> insertedVoucherRef = new AtomicReference<>();
        AtomicReference<List<ErpFinanceVoucherEntryDO>> insertedEntriesRef = new AtomicReference<>();

        mockValidatedLedger(service, 1L);
        mockTemplate(service, template(60L, 1L, ErpBizTypeEnum.STOCK_CHECK.getType(), true, "STOCK_CHECK"),
                amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType(), null));
        setField(service, "stockCheckMapper", createProxy(ErpStockCheckMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpStockCheckDO()
                        .setId(1001L)
                        .setNo("PD202605200001")
                        .setSnapshotTime(LocalDateTime.of(2026, 5, 20, 10, 0))
                        .setStatus(30)
                        .setTotalPrice(new BigDecimal("-120.00"))
                        .setRemark("盘亏差异");
            }
            return null;
        }));
        mockOpenPeriod(service, 20L, 1L, LocalDate.of(2026, 5, 20));
        mockVoucherPersistence(service, "CWPZ20260520000001", 88L,
                insertedVoucherRef, insertedEntriesRef, null);
        setField(service, "voucherLogService", createProxy(ErpFinanceVoucherLogService.class,
                (methodName, args) -> null));

        Long id = service.generateVoucher(new ErpFinanceVoucherGenerateReqVO()
                .setLedgerId(1L)
                .setBizType(ErpBizTypeEnum.STOCK_CHECK.getType())
                .setBizId(1001L)
                .setTemplateId(60L));

        assertEquals(88L, id);
        assertEquals("PD202605200001", insertedVoucherRef.get().getBizNo());
        assertEquals(new BigDecimal("120.00"), insertedVoucherRef.get().getTotalDebitAmount());
        assertEquals(new BigDecimal("120.00"), insertedVoucherRef.get().getTotalCreditAmount());
        assertEquals("盘亏差异", insertedVoucherRef.get().getRemark());
        assertEquals(new BigDecimal("120.00"), insertedEntriesRef.get().get(0).getDebitAmount());
        assertEquals(new BigDecimal("120.00"), insertedEntriesRef.get().get(1).getCreditAmount());
    }

    @Test
    void generateVoucherWithoutDualWrite_shouldNotPublishVoucherCreatedEvent() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicInteger eventCount = new AtomicInteger();

        mockValidatedLedger(service, 1L);
        mockTemplate(service, template(10L, 1L, ErpBizTypeEnum.FINANCE_EXPENSE.getType(), true, "EXPENSE_SUMMARY"),
                amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType(), null));
        mockApprovedExpense(service, 100L, "LSBX20260429000001", LocalDateTime.of(2026, 4, 29, 10, 0),
                new BigDecimal("300.00"), "expense remark");
        mockOpenPeriod(service, 20L, 1L, LocalDate.of(2026, 4, 29));
        AtomicReference<ErpFinanceVoucherDO> insertedVoucherRef = new AtomicReference<>();
        AtomicReference<List<ErpFinanceVoucherEntryDO>> insertedEntriesRef = new AtomicReference<>();
        mockVoucherPersistence(service, "CWPZ20260429000001", 88L, insertedVoucherRef, insertedEntriesRef, null);
        mockOutsourceOrderService(service, null);
        setField(service, "applicationContext", createProxy(ApplicationContext.class, (methodName, args) -> {
            if ("publishEvent".equals(methodName)) {
                eventCount.incrementAndGet();
            }
            return null;
        }));

        Long id = service.generateVoucherWithoutDualWrite(new ErpFinanceVoucherGenerateReqVO()
                .setLedgerId(1L)
                .setBizType(ErpBizTypeEnum.FINANCE_EXPENSE.getType())
                .setBizId(100L)
                .setTemplateId(10L));

        assertEquals(88L, id);
        assertEquals(0, eventCount.get());
    }

    @Test
    void generateVoucher_shouldPropagateDuplicateVoucherNoConflict() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicInteger insertCount = new AtomicInteger();

        mockValidatedLedger(service, 1L);
        mockTemplate(service, template(10L, 1L, ErpBizTypeEnum.FINANCE_EXPENSE.getType(), true, "EXPENSE_SUMMARY"),
                amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType(), null));
        mockApprovedExpense(service, 100L, "LSBX20260429000001", LocalDateTime.of(2026, 4, 29, 10, 0),
                new BigDecimal("300.00"), "expense remark");
        mockOpenPeriod(service, 20L, 1L, LocalDate.of(2026, 4, 29));
        mockOutsourceOrderService(service, null);

        setField(service, "financeVoucherMapper", createProxy(ErpFinanceVoucherMapper.class, (methodName, args) -> {
            if ("selectByLedgerIdAndBiz".equals(methodName)) {
                return null;
            }
            if ("insert".equals(methodName)) {
                if (insertCount.incrementAndGet() == 1) {
                    throw new DuplicateKeyException("Duplicate entry '0-CWPZ20260526000001-\\x00' for key 'erp_finance_voucher.uk_finance_voucher_no'");
                }
                return 1;
            }
            return null;
        }));
        setField(service, "financeVoucherEntryMapper", createProxy(ErpFinanceVoucherEntryMapper.class, (methodName, args) -> {
            if ("insertBatch".equals(methodName)) {
                return true;
            }
            return null;
        }));
        setField(service, "noRedisDAO", new ErpNoRedisDAO() {
            private int seq = 0;

            @Override
            public String generate(String prefix) {
                return prefix + "2026052600000" + (++seq);
            }
        });

        assertThrows(DuplicateKeyException.class, () -> service.generateVoucher(new ErpFinanceVoucherGenerateReqVO()
                .setLedgerId(1L)
                .setBizType(ErpBizTypeEnum.FINANCE_EXPENSE.getType())
                .setBizId(100L)
                .setTemplateId(10L)));

        assertEquals(1, insertCount.get());
    }

    @Test
    void generateVoucher_shouldSupportFixedAmountTemplateItem() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<ErpFinanceVoucherDO> insertedVoucherRef = new AtomicReference<>();
        AtomicReference<List<ErpFinanceVoucherEntryDO>> insertedEntriesRef = new AtomicReference<>();

        mockValidatedLedger(service, 1L);
        mockTemplate(service, template(10L, 1L, ErpBizTypeEnum.FINANCE_EXPENSE.getType(), true, "FIXED_SUMMARY"),
                amountItems(ErpFinanceVoucherAmountSourceEnum.FIXED_AMOUNT.getType(), new BigDecimal("120.00")));
        mockApprovedExpense(service, 100L, "LSBX20260429000002", LocalDateTime.of(2026, 4, 29, 11, 0),
                new BigDecimal("300.00"), "fixed amount");
        mockOpenPeriod(service, 20L, 1L, LocalDate.of(2026, 4, 29));
        mockVoucherPersistence(service, "CWPZ20260429000002", 89L, insertedVoucherRef, insertedEntriesRef, null);
        mockOutsourceOrderService(service, null);

        Long id = service.generateVoucher(new ErpFinanceVoucherGenerateReqVO()
                .setLedgerId(1L)
                .setBizType(ErpBizTypeEnum.FINANCE_EXPENSE.getType())
                .setBizId(100L)
                .setTemplateId(10L));

        assertEquals(89L, id);
        assertEquals(new BigDecimal("120.000000"), insertedVoucherRef.get().getTotalDebitAmount());
        assertEquals(new BigDecimal("120.000000"), insertedVoucherRef.get().getTotalCreditAmount());
        assertEquals(new BigDecimal("120.000000"), insertedEntriesRef.get().get(0).getDebitAmount());
        assertEquals(new BigDecimal("120.000000"), insertedEntriesRef.get().get(1).getCreditAmount());
    }

    @Test
    void generateVoucher_shouldSupportBizAmountRateTemplateItem() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<ErpFinanceVoucherDO> insertedVoucherRef = new AtomicReference<>();
        AtomicReference<List<ErpFinanceVoucherEntryDO>> insertedEntriesRef = new AtomicReference<>();

        mockValidatedLedger(service, 1L);
        mockTemplate(service, template(10L, 1L, ErpBizTypeEnum.FINANCE_EXPENSE.getType(), true, "RATE_SUMMARY"),
                amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT_RATE.getType(), new BigDecimal("0.20")));
        mockApprovedExpense(service, 100L, "LSBX20260429000003", LocalDateTime.of(2026, 4, 29, 12, 0),
                new BigDecimal("300.00"), "rate amount");
        mockOpenPeriod(service, 20L, 1L, LocalDate.of(2026, 4, 29));
        mockVoucherPersistence(service, "CWPZ20260429000003", 90L, insertedVoucherRef, insertedEntriesRef, null);
        mockOutsourceOrderService(service, null);

        Long id = service.generateVoucher(new ErpFinanceVoucherGenerateReqVO()
                .setLedgerId(1L)
                .setBizType(ErpBizTypeEnum.FINANCE_EXPENSE.getType())
                .setBizId(100L)
                .setTemplateId(10L));

        assertEquals(90L, id);
        assertEquals(new BigDecimal("60.000000"), insertedVoucherRef.get().getTotalDebitAmount());
        assertEquals(new BigDecimal("60.000000"), insertedVoucherRef.get().getTotalCreditAmount());
        assertEquals(new BigDecimal("60.000000"), insertedEntriesRef.get().get(0).getDebitAmount());
        assertEquals(new BigDecimal("60.000000"), insertedEntriesRef.get().get(1).getCreditAmount());
    }

    @Test
    void generateVoucher_shouldCreateVoucherForPurchaseIn() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<ErpFinanceVoucherDO> insertedVoucherRef = new AtomicReference<>();

        mockValidatedLedger(service, 1L);
        mockTemplate(service, template(20L, 1L, ErpBizTypeEnum.PURCHASE_IN.getType(), true, "PURCHASE_IN_SUMMARY"),
                amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType(), null));
        mockPurchaseIn(service, 200L, "CGRK20260429000001", LocalDateTime.of(2026, 4, 29, 14, 0),
                new BigDecimal("500.00"), "purchase in");
        mockOpenPeriod(service, 21L, 1L, LocalDate.of(2026, 4, 29));
        mockVoucherPersistence(service, "CWPZ20260429000010", 91L, insertedVoucherRef, new AtomicReference<>(), null);
        mockOutsourceOrderService(service, null);

        Long id = service.generateVoucher(new ErpFinanceVoucherGenerateReqVO()
                .setLedgerId(1L)
                .setBizType(ErpBizTypeEnum.PURCHASE_IN.getType())
                .setBizId(200L)
                .setTemplateId(20L));

        assertEquals(91L, id);
        assertEquals("CGRK20260429000001", insertedVoucherRef.get().getBizNo());
        assertEquals(new BigDecimal("500.00"), insertedVoucherRef.get().getTotalDebitAmount());
        assertEquals(new BigDecimal("500.00"), insertedVoucherRef.get().getTotalCreditAmount());
    }

    @Test
    void generateVoucher_shouldCreateVoucherForPurchaseReturn() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<ErpFinanceVoucherDO> insertedVoucherRef = new AtomicReference<>();

        mockValidatedLedger(service, 1L);
        mockTemplate(service, template(21L, 1L, ErpBizTypeEnum.PURCHASE_RETURN.getType(), true, "PURCHASE_RETURN_SUMMARY"),
                amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType(), null));
        mockPurchaseReturn(service, 300L, "CGTH20260429000001", LocalDateTime.of(2026, 4, 29, 15, 0),
                new BigDecimal("180.00"), "purchase return");
        mockOpenPeriod(service, 22L, 1L, LocalDate.of(2026, 4, 29));
        mockVoucherPersistence(service, "CWPZ20260429000011", 92L, insertedVoucherRef, new AtomicReference<>(), null);
        mockOutsourceOrderService(service, null);

        Long id = service.generateVoucher(new ErpFinanceVoucherGenerateReqVO()
                .setLedgerId(1L)
                .setBizType(ErpBizTypeEnum.PURCHASE_RETURN.getType())
                .setBizId(300L)
                .setTemplateId(21L));

        assertEquals(92L, id);
        assertEquals("CGTH20260429000001", insertedVoucherRef.get().getBizNo());
        assertEquals(new BigDecimal("180.00"), insertedVoucherRef.get().getTotalDebitAmount());
        assertEquals(new BigDecimal("180.00"), insertedVoucherRef.get().getTotalCreditAmount());
    }

    @Test
    void generateVoucher_shouldUseAbsoluteAmountForStockCheckLoss() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<ErpFinanceVoucherDO> insertedVoucherRef = new AtomicReference<>();

        mockValidatedLedger(service, 1L);
        mockTemplate(service, template(22L, 1L, ErpBizTypeEnum.STOCK_CHECK.getType(), true, "STOCK_CHECK_SUMMARY"),
                amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType(), null));
        mockStockCheck(service, 301L, "PD202607150001", LocalDateTime.of(2026, 7, 15, 10, 0),
                new BigDecimal("-850.00"), "stock check loss");
        mockOpenPeriod(service, 23L, 1L, LocalDate.of(2026, 7, 15));
        mockVoucherPersistence(service, "CWPZ202607150001", 93L, insertedVoucherRef, new AtomicReference<>(), null);
        mockOutsourceOrderService(service, null);

        Long id = service.generateVoucher(new ErpFinanceVoucherGenerateReqVO()
                .setLedgerId(1L)
                .setBizType(ErpBizTypeEnum.STOCK_CHECK.getType())
                .setBizId(301L)
                .setTemplateId(22L));

        assertEquals(93L, id);
        assertEquals(new BigDecimal("850.00"), insertedVoucherRef.get().getTotalDebitAmount());
        assertEquals(new BigDecimal("850.00"), insertedVoucherRef.get().getTotalCreditAmount());
    }

    @Test
    void generateVoucher_shouldCreateVoucherForOutsourceFee() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<ErpFinanceVoucherDO> insertedVoucherRef = new AtomicReference<>();

        mockValidatedLedger(service, 1L);
        mockTemplate(service, template(30L, 1L, ErpBizTypeEnum.OUTSOURCE_FEE.getType(), true, "OUTSOURCE_SUMMARY"),
                amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType(), null));
        mockOutsourceOrderService(service, new ErpOutsourceFeeDO()
                .setId(400L)
                .setFeeNo("WWJG20260429000001")
                .setFeeTime(LocalDateTime.of(2026, 4, 29, 16, 0))
                .setFeeAmount(new BigDecimal("240.00"))
                .setRemark("outsource fee"));
        mockOpenPeriod(service, 23L, 1L, LocalDate.of(2026, 4, 29));
        mockVoucherPersistence(service, "CWPZ20260429000012", 93L, insertedVoucherRef, new AtomicReference<>(), null);

        Long id = service.generateVoucher(new ErpFinanceVoucherGenerateReqVO()
                .setLedgerId(1L)
                .setBizType(ErpBizTypeEnum.OUTSOURCE_FEE.getType())
                .setBizId(400L)
                .setTemplateId(30L));

        assertEquals(93L, id);
        assertEquals("WWJG20260429000001", insertedVoucherRef.get().getBizNo());
        assertEquals(new BigDecimal("240.00"), insertedVoucherRef.get().getTotalDebitAmount());
        assertEquals(new BigDecimal("240.00"), insertedVoucherRef.get().getTotalCreditAmount());
    }

    @Test
    void generateVoucher_shouldCreateVoucherForProductionInbound() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<ErpFinanceVoucherDO> insertedVoucherRef = new AtomicReference<>();

        mockValidatedLedger(service, 1L);
        mockTemplate(service, template(301L, 1L, ErpBizTypeEnum.PRODUCTION_INBOUND.getType(), true, "PRODUCTION_INBOUND_SUMMARY"),
                amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType(), null));
        mockProductionInbound(service, new ErpProductionInboundDO()
                .setId(700L)
                .setNo("ZZRK202605260001")
                .setInboundTime(LocalDateTime.of(2026, 5, 26, 16, 0))
                .setTotalCost(new BigDecimal("900.00"))
                .setRemark("production inbound"));
        mockOpenPeriod(service, 230L, 1L, LocalDate.of(2026, 5, 26));
        mockVoucherPersistence(service, "CWPZ20260526000012", 930L, insertedVoucherRef, new AtomicReference<>(), null);
        mockOutsourceOrderService(service, null);

        Long id = service.generateVoucher(new ErpFinanceVoucherGenerateReqVO()
                .setLedgerId(1L)
                .setBizType(ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                .setBizId(700L)
                .setTemplateId(301L));

        assertEquals(930L, id);
        assertEquals("ZZRK202605260001", insertedVoucherRef.get().getBizNo());
        assertEquals(new BigDecimal("900.00"), insertedVoucherRef.get().getTotalDebitAmount());
        assertEquals(new BigDecimal("900.00"), insertedVoucherRef.get().getTotalCreditAmount());
    }

    @Test
    void generateVoucher_shouldCreateVoucherForSaleOut() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<ErpFinanceVoucherDO> insertedVoucherRef = new AtomicReference<>();

        mockValidatedLedger(service, 1L);
        mockTemplate(service, template(31L, 1L, ErpBizTypeEnum.SALE_OUT.getType(), true, "SALE_OUT_SUMMARY"),
                amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType(), null));
        mockSaleOut(service, 500L, "XSCK20260429000001", LocalDateTime.of(2026, 4, 29, 17, 0),
                new BigDecimal("360.00"), "sale out");
        mockOpenPeriod(service, 24L, 1L, LocalDate.of(2026, 4, 29));
        mockVoucherPersistence(service, "CWPZ20260429000013", 94L, insertedVoucherRef, new AtomicReference<>(), null);
        mockOutsourceOrderService(service, null);

        Long id = service.generateVoucher(new ErpFinanceVoucherGenerateReqVO()
                .setLedgerId(1L)
                .setBizType(ErpBizTypeEnum.SALE_OUT.getType())
                .setBizId(500L)
                .setTemplateId(31L));

        assertEquals(94L, id);
        assertEquals("XSCK20260429000001", insertedVoucherRef.get().getBizNo());
        assertEquals(new BigDecimal("360.00"), insertedVoucherRef.get().getTotalDebitAmount());
        assertEquals(new BigDecimal("360.00"), insertedVoucherRef.get().getTotalCreditAmount());
    }

    @Test
    void generateVoucher_shouldCreateVoucherForSaleReturn() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<ErpFinanceVoucherDO> insertedVoucherRef = new AtomicReference<>();

        mockValidatedLedger(service, 1L);
        mockTemplate(service, template(32L, 1L, ErpBizTypeEnum.SALE_RETURN.getType(), true, "SALE_RETURN_SUMMARY"),
                amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType(), null));
        mockSaleReturn(service, 600L, "XSTH20260429000001", LocalDateTime.of(2026, 4, 29, 18, 0),
                new BigDecimal("210.00"), "sale return");
        mockOpenPeriod(service, 25L, 1L, LocalDate.of(2026, 4, 29));
        mockVoucherPersistence(service, "CWPZ20260429000014", 95L, insertedVoucherRef, new AtomicReference<>(), null);
        mockOutsourceOrderService(service, null);

        Long id = service.generateVoucher(new ErpFinanceVoucherGenerateReqVO()
                .setLedgerId(1L)
                .setBizType(ErpBizTypeEnum.SALE_RETURN.getType())
                .setBizId(600L)
                .setTemplateId(32L));

        assertEquals(95L, id);
        assertEquals("XSTH20260429000001", insertedVoucherRef.get().getBizNo());
        assertEquals(new BigDecimal("210.00"), insertedVoucherRef.get().getTotalDebitAmount());
        assertEquals(new BigDecimal("210.00"), insertedVoucherRef.get().getTotalCreditAmount());
    }

    @Test
    void generateVoucher_shouldFallbackToCreateTimeForSaleOut() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<ErpFinanceVoucherDO> insertedVoucherRef = new AtomicReference<>();

        mockValidatedLedger(service, 1L);
        mockTemplate(service, template(33L, 1L, ErpBizTypeEnum.SALE_OUT.getType(), true, "SALE_OUT_FALLBACK"),
                amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType(), null));
        mockSaleOut(service, saleOut(501L, "XSCK20260429000002", null,
                LocalDateTime.of(2026, 4, 28, 17, 0), LocalDateTime.of(2026, 4, 29, 8, 0),
                new BigDecimal("365.00"), "sale out fallback"));
        mockOpenPeriod(service, 26L, 1L, LocalDate.of(2026, 4, 28));
        mockVoucherPersistence(service, "CWPZ20260429000015", 96L, insertedVoucherRef, new AtomicReference<>(), null);
        mockOutsourceOrderService(service, null);

        Long id = service.generateVoucher(new ErpFinanceVoucherGenerateReqVO()
                .setLedgerId(1L)
                .setBizType(ErpBizTypeEnum.SALE_OUT.getType())
                .setBizId(501L)
                .setTemplateId(33L));

        assertEquals(96L, id);
        assertEquals(LocalDateTime.of(2026, 4, 28, 17, 0), insertedVoucherRef.get().getVoucherTime());
    }

    @Test
    void generateVoucher_shouldFallbackToUpdateTimeForSaleReturn() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<ErpFinanceVoucherDO> insertedVoucherRef = new AtomicReference<>();

        mockValidatedLedger(service, 1L);
        mockTemplate(service, template(34L, 1L, ErpBizTypeEnum.SALE_RETURN.getType(), true, "SALE_RETURN_FALLBACK"),
                amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType(), null));
        mockSaleReturn(service, saleReturn(601L, "XSTH20260429000002", null,
                null, LocalDateTime.of(2026, 4, 27, 18, 0),
                new BigDecimal("215.00"), "sale return fallback"));
        mockOpenPeriod(service, 27L, 1L, LocalDate.of(2026, 4, 27));
        mockVoucherPersistence(service, "CWPZ20260429000016", 97L, insertedVoucherRef, new AtomicReference<>(), null);
        mockOutsourceOrderService(service, null);

        Long id = service.generateVoucher(new ErpFinanceVoucherGenerateReqVO()
                .setLedgerId(1L)
                .setBizType(ErpBizTypeEnum.SALE_RETURN.getType())
                .setBizId(601L)
                .setTemplateId(34L));

        assertEquals(97L, id);
        assertEquals(LocalDateTime.of(2026, 4, 27, 18, 0), insertedVoucherRef.get().getVoucherTime());
    }

    @Test
    void generateVoucher_shouldRejectWhenSaleOutSourceTimeMissingAndVoucherTimeAbsent() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();

        mockValidatedLedger(service, 1L);
        mockTemplate(service, template(35L, 1L, ErpBizTypeEnum.SALE_OUT.getType(), true, "SALE_OUT_TIME_REQUIRED"),
                amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType(), null));
        mockSaleOut(service, saleOut(502L, "XSCK20260429000003", null,
                null, null, new BigDecimal("366.00"), "sale out missing time"));
        mockVoucherPersistence(service, "CWPZ20260429000017", 98L, new AtomicReference<>(), new AtomicReference<>(), null);
        mockOutsourceOrderService(service, null);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.generateVoucher(new ErpFinanceVoucherGenerateReqVO()
                .setLedgerId(1L)
                .setBizType(ErpBizTypeEnum.SALE_OUT.getType())
                .setBizId(502L)
                .setTemplateId(35L)));

        assertEquals(FINANCE_VOUCHER_SOURCE_TIME_REQUIRED.getCode(), ex.getCode());
    }

    @Test
    void generateVoucher_shouldAllowManualVoucherTimeWhenSaleReturnSourceTimeMissing() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<ErpFinanceVoucherDO> insertedVoucherRef = new AtomicReference<>();

        mockValidatedLedger(service, 1L);
        mockTemplate(service, template(36L, 1L, ErpBizTypeEnum.SALE_RETURN.getType(), true, "SALE_RETURN_MANUAL_TIME"),
                amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType(), null));
        mockSaleReturn(service, saleReturn(602L, "XSTH20260429000003", null,
                null, null, new BigDecimal("216.00"), "sale return manual time"));
        mockOpenPeriod(service, 28L, 1L, LocalDate.of(2026, 4, 26));
        mockVoucherPersistence(service, "CWPZ20260429000018", 99L, insertedVoucherRef, new AtomicReference<>(), null);
        mockOutsourceOrderService(service, null);

        Long id = service.generateVoucher(new ErpFinanceVoucherGenerateReqVO()
                .setLedgerId(1L)
                .setBizType(ErpBizTypeEnum.SALE_RETURN.getType())
                .setBizId(602L)
                .setTemplateId(36L)
                .setVoucherTime(LocalDateTime.of(2026, 4, 26, 9, 30)));

        assertEquals(99L, id);
        assertEquals(LocalDateTime.of(2026, 4, 26, 9, 30), insertedVoucherRef.get().getVoucherTime());
    }

    @Test
    void autoGenerateVoucher_shouldReturnNullWhenDefaultLedgerMissing() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("getDefaultFinanceLedger".equals(methodName)) {
                return null;
            }
            return null;
        }));

        Long id = service.autoGenerateVoucher(ErpBizTypeEnum.FINANCE_EXPENSE.getType(), 100L);

        assertNull(id);
    }

    @Test
    void autoGenerateVoucher_shouldUseDefaultLedgerAndAutoTemplate() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<ErpFinanceVoucherDO> insertedVoucherRef = new AtomicReference<>();

        mockLedgerWithDefault(service, 1L);
        mockTemplate(service, template(10L, 1L, ErpBizTypeEnum.FINANCE_EXPENSE.getType(), true, "AUTO_SUMMARY"),
                amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType(), null));
        mockApprovedExpense(service, 100L, "LSBX20260429000004", LocalDateTime.of(2026, 4, 29, 13, 0),
                new BigDecimal("300.00"), "auto expense");
        mockOpenPeriod(service, 20L, 1L, LocalDate.of(2026, 4, 29));
        mockVoucherPersistence(service, "CWPZ20260429000004", 77L, insertedVoucherRef, new AtomicReference<>(), null);
        mockOutsourceOrderService(service, null);

        Long id = service.autoGenerateVoucher(ErpBizTypeEnum.FINANCE_EXPENSE.getType(), 100L);

        assertEquals(77L, id);
        assertEquals(1L, insertedVoucherRef.get().getLedgerId());
        assertEquals(10L, insertedVoucherRef.get().getTemplateId());
        assertEquals(ErpBizTypeEnum.FINANCE_EXPENSE.getType(), insertedVoucherRef.get().getBizType());
        assertEquals(100L, insertedVoucherRef.get().getBizId());
    }

    @Test
    void autoGenerateVoucher_shouldUseFallbackTemplateLedgerWhenDefaultLedgerHasNoTemplate() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<ErpFinanceVoucherDO> insertedVoucherRef = new AtomicReference<>();
        ErpFinanceVoucherTemplateDO fallbackTemplate = template(60L, 1L,
                ErpBizTypeEnum.STOCK_CHECK.getType(), true, "STOCK_CHECK");

        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("getDefaultFinanceLedger".equals(methodName)) {
                return new ErpFinanceLedgerDO().setId(99601L).setName("DEFAULT_LEDGER")
                        .setStatus(CommonStatusEnum.ENABLE.getStatus()).setDefaultStatus(true);
            }
            if ("validateFinanceLedger".equals(methodName)) {
                Long ledgerId = (Long) args[0];
                return new ErpFinanceLedgerDO().setId(ledgerId).setName("LEDGER-" + ledgerId)
                        .setStatus(CommonStatusEnum.ENABLE.getStatus());
            }
            return null;
        }));
        setField(service, "voucherTemplateService", createProxy(ErpFinanceVoucherTemplateService.class,
                (methodName, args) -> {
                    if ("getVoucherTemplateListByLedgerAndBizType".equals(methodName)) {
                        return ObjectUtil.equal(args[0], 1L) ? List.of(fallbackTemplate) : List.of();
                    }
                    if ("validateVoucherTemplate".equals(methodName)) {
                        return fallbackTemplate;
                    }
                    if ("getVoucherTemplateItemListByTemplateId".equals(methodName)) {
                        return amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType(), null);
                    }
                    return null;
                }));
        setField(service, "stockCheckMapper", createProxy(ErpStockCheckMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpStockCheckDO().setId(1001L).setNo("PD202607200001")
                        .setSnapshotTime(LocalDateTime.of(2026, 7, 20, 18, 0))
                        .setStatus(40).setTotalPrice(new BigDecimal("-280.00"));
            }
            return null;
        }));
        mockOpenPeriod(service, 20L, 1L, LocalDate.of(2026, 7, 20));
        mockVoucherPersistence(service, "CWPZ20260720000001", 77L,
                insertedVoucherRef, new AtomicReference<>(), null);
        setField(service, "voucherLogService", createProxy(ErpFinanceVoucherLogService.class,
                (methodName, args) -> null));

        Long id = service.autoGenerateVoucher(ErpBizTypeEnum.STOCK_CHECK.getType(), 1001L);

        assertEquals(77L, id);
        assertEquals(1L, insertedVoucherRef.get().getLedgerId());
        assertEquals(60L, insertedVoucherRef.get().getTemplateId());
    }

    @Test
    void autoGenerateVoucher_shouldMapResearchExpenseToCapitalizeBizType() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<ErpFinanceVoucherDO> insertedVoucherRef = new AtomicReference<>();

        mockLedgerWithDefault(service, 1L);
        mockTemplate(service, template(10L, 1L, ErpBizTypeEnum.FINANCE_EXPENSE_CAPITALIZE.getType(), true, "AUTO_SUMMARY"),
                amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType(), null));
        mockApprovedExpense(service, new ErpFinanceExpenseDO().setId(100L).setNo("LSBX20260429000004")
                .setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setExpenseType(10)
                .setRdAccountingType(20)
                .setExpenseTime(LocalDateTime.of(2026, 4, 29, 13, 0))
                .setExpensePrice(new BigDecimal("300.00"))
                .setRemark("auto expense"));
        mockOpenPeriod(service, 20L, 1L, LocalDate.of(2026, 4, 29));
        mockVoucherPersistence(service, "CWPZ20260429000004", 77L, insertedVoucherRef, new AtomicReference<>(), null);
        mockOutsourceOrderService(service, null);

        Long id = service.autoGenerateVoucher(ErpBizTypeEnum.FINANCE_EXPENSE.getType(), 100L);

        assertEquals(77L, id);
        assertEquals(ErpBizTypeEnum.FINANCE_EXPENSE_CAPITALIZE.getType(), insertedVoucherRef.get().getBizType());
        assertEquals(100L, insertedVoucherRef.get().getBizId());
    }

    @Test
    void autoGenerateVoucher_shouldReuseLegacyFinanceExpenseVoucherForResearchExpense() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        ErpFinanceVoucherDO legacyVoucher = new ErpFinanceVoucherDO()
                .setId(66L)
                .setLedgerId(1L)
                .setBizType(ErpBizTypeEnum.FINANCE_EXPENSE.getType())
                .setBizId(100L)
                .setVoucherNo("CWPZ20260429000003");

        mockLedgerWithDefault(service, 1L);
        mockTemplate(service, template(10L, 1L, ErpBizTypeEnum.FINANCE_EXPENSE_CAPITALIZE.getType(), true, "AUTO_SUMMARY"),
                amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType(), null));
        mockApprovedExpense(service, new ErpFinanceExpenseDO().setId(100L).setNo("LSBX20260429000006")
                .setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setExpenseType(10)
                .setRdAccountingType(20)
                .setExpenseTime(LocalDateTime.of(2026, 4, 29, 13, 0))
                .setExpensePrice(new BigDecimal("300.00"))
                .setRemark("legacy auto expense"));
        mockOpenPeriod(service, 20L, 1L, LocalDate.of(2026, 4, 29));
        mockVoucherPersistence(service, "CWPZ20260429000006", 78L, new AtomicReference<>(),
                new AtomicReference<>(), null, legacyVoucher);
        mockOutsourceOrderService(service, null);

        Long id = service.autoGenerateVoucher(ErpBizTypeEnum.FINANCE_EXPENSE.getType(), 100L);

        assertEquals(66L, id);
    }

    @Test
    void autoGenerateVoucher_shouldGenerateBothMappedLedgersWhenDualLedgerConfigExists() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        List<ErpFinanceVoucherDO> insertedVouchers = new ArrayList<>();
        AtomicInteger eventCount = new AtomicInteger();

        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("getDefaultFinanceLedger".equals(methodName)) {
                return null;
            }
            if ("validateFinanceLedger".equals(methodName)) {
                Long ledgerId = (Long) args[0];
                return new ErpFinanceLedgerDO().setId(ledgerId).setName("LEDGER-" + ledgerId)
                        .setStatus(CommonStatusEnum.ENABLE.getStatus());
            }
            return null;
        }));
        setField(service, "dualLedgerConfigService", createProxy(ErpFinanceDualLedgerConfigService.class, (methodName, args) -> {
            if ("getEnabledDualLedgerConfig".equals(methodName)) {
                return new ErpFinanceDualLedgerConfigDO().setId(1L).setBizType(ErpBizTypeEnum.FINANCE_EXPENSE.getType())
                        .setExternalLedgerId(1L).setInternalLedgerId(2L).setStatus(CommonStatusEnum.ENABLE.getStatus());
            }
            return null;
        }));
        setField(service, "voucherTemplateService", createProxy(ErpFinanceVoucherTemplateService.class, (methodName, args) -> {
            if ("getVoucherTemplateListByLedgerAndBizType".equals(methodName)) {
                Long ledgerId = (Long) args[0];
                Integer bizType = (Integer) args[1];
                return List.of(template(ledgerId == 1L ? 10L : 20L, ledgerId, bizType, true, "AUTO_SUMMARY"));
            }
            if ("validateVoucherTemplate".equals(methodName)) {
                Long templateId = (Long) args[0];
                return templateId == 10L
                        ? template(10L, 1L, ErpBizTypeEnum.FINANCE_EXPENSE.getType(), true, "AUTO_SUMMARY")
                        : template(20L, 2L, ErpBizTypeEnum.FINANCE_EXPENSE.getType(), true, "AUTO_SUMMARY");
            }
            if ("getVoucherTemplateItemListByTemplateId".equals(methodName)) {
                return amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType(), null);
            }
            return null;
        }));
        mockApprovedExpense(service, 100L, "LSBX20260429000007", LocalDateTime.of(2026, 4, 29, 13, 0),
                new BigDecimal("300.00"), "dual ledger expense");
        mockOpenPeriodForLedgers(service, List.of(1L, 2L), LocalDate.of(2026, 4, 29));
        setField(service, "financeVoucherMapper", createProxy(ErpFinanceVoucherMapper.class, (methodName, args) -> {
            if ("selectByLedgerIdAndBiz".equals(methodName)) {
                return null;
            }
            if ("insert".equals(methodName)) {
                ErpFinanceVoucherDO voucher = (ErpFinanceVoucherDO) args[0];
                voucher.setId(voucher.getLedgerId() == 1L ? 101L : 102L);
                insertedVouchers.add(voucher);
                return 1;
            }
            return null;
        }));
        setField(service, "financeVoucherEntryMapper", createProxy(ErpFinanceVoucherEntryMapper.class, (methodName, args) -> true));
        setField(service, "noRedisDAO", new ErpNoRedisDAO() {
            private int seq = 0;
            @Override
            public String generate(String prefix) {
                return prefix + (++seq);
            }
        });
        setField(service, "applicationContext", createProxy(ApplicationContext.class, (methodName, args) -> {
            if ("publishEvent".equals(methodName)) {
                eventCount.incrementAndGet();
            }
            return null;
        }));
        mockOutsourceOrderService(service, null);

        Long id = service.autoGenerateVoucher(ErpBizTypeEnum.FINANCE_EXPENSE.getType(), 100L);

        assertEquals(101L, id);
        assertEquals(2, insertedVouchers.size());
        assertEquals(1L, insertedVouchers.get(0).getLedgerId());
        assertEquals(2L, insertedVouchers.get(1).getLedgerId());
        assertEquals(0, eventCount.get());
    }

    @Test
    void generateVoucher_shouldAllowResearchExpenseCapitalizeBizType() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<ErpFinanceVoucherDO> insertedVoucherRef = new AtomicReference<>();
        AtomicReference<List<ErpFinanceVoucherEntryDO>> insertedEntriesRef = new AtomicReference<>();

        mockValidatedLedger(service, 1L);
        mockTemplate(service, template(10L, 1L, ErpBizTypeEnum.FINANCE_EXPENSE_CAPITALIZE.getType(), true, "CAPITALIZE_SUMMARY"),
                amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType(), null));
        mockApprovedExpense(service, new ErpFinanceExpenseDO().setId(100L).setNo("LSBX20260429000005")
                .setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setExpenseType(10)
                .setRdAccountingType(20)
                .setExpenseTime(LocalDateTime.of(2026, 4, 29, 10, 0))
                .setExpensePrice(new BigDecimal("300.00"))
                .setRemark("capitalized expense"));
        mockOpenPeriod(service, 20L, 1L, LocalDate.of(2026, 4, 29));
        mockVoucherPersistence(service, "CWPZ20260429000005", 88L, insertedVoucherRef, insertedEntriesRef, null);
        mockOutsourceOrderService(service, null);

        Long id = service.generateVoucher(new ErpFinanceVoucherGenerateReqVO()
                .setLedgerId(1L)
                .setBizType(ErpBizTypeEnum.FINANCE_EXPENSE_CAPITALIZE.getType())
                .setBizId(100L)
                .setTemplateId(10L));

        assertEquals(88L, id);
        assertEquals(ErpBizTypeEnum.FINANCE_EXPENSE_CAPITALIZE.getType(), insertedVoucherRef.get().getBizType());
        assertEquals(new BigDecimal("300.00"), insertedEntriesRef.get().get(0).getDebitAmount());
        assertEquals(new BigDecimal("300.00"), insertedEntriesRef.get().get(1).getCreditAmount());
    }

    @Test
    void approveVoucher_shouldUpdateApprovedStatus() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<ErpFinanceVoucherDO> updatedVoucherRef = new AtomicReference<>();

        setField(service, "financeVoucherMapper", createProxy(ErpFinanceVoucherMapper.class, (methodName, args) -> {
            if ("selectBatchIds".equals(methodName)) {
                return List.of(new ErpFinanceVoucherDO().setId(1L).setVoucherNo("CWPZ1")
                        .setStatus(ErpFinanceVoucherStatusEnum.GENERATED.getStatus()));
            }
            if ("updateById".equals(methodName)) {
                updatedVoucherRef.set((ErpFinanceVoucherDO) args[0]);
                return 1;
            }
            return null;
        }));

        service.approveVoucher(9L, new ErpFinanceVoucherActionReqVO().setIds(List.of(1L)));

        assertEquals(1L, updatedVoucherRef.get().getId());
        assertEquals(ErpFinanceVoucherStatusEnum.APPROVED.getStatus(), updatedVoucherRef.get().getStatus());
        assertEquals(9L, updatedVoucherRef.get().getApproveUserId());
    }

    @Test
    void approveVoucher_shouldSkipAlreadyApprovedVoucher() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicInteger updateCount = new AtomicInteger();

        setField(service, "financeVoucherMapper", createProxy(ErpFinanceVoucherMapper.class, (methodName, args) -> {
            if ("selectBatchIds".equals(methodName)) {
                return List.of(new ErpFinanceVoucherDO().setId(1L).setVoucherNo("CWPZ1")
                        .setStatus(ErpFinanceVoucherStatusEnum.APPROVED.getStatus()));
            }
            if ("updateById".equals(methodName)) {
                updateCount.incrementAndGet();
                return 1;
            }
            return null;
        }));

        service.approveVoucher(9L, new ErpFinanceVoucherActionReqVO().setIds(List.of(1L)));

        assertEquals(0, updateCount.get());
    }

    @Test
    void cancelApproveVoucher_shouldUpdateGeneratedStatus() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<ErpFinanceVoucherDO> updatedVoucherRef = new AtomicReference<>();

        setField(service, "financeVoucherMapper", createProxy(ErpFinanceVoucherMapper.class, (methodName, args) -> {
            if ("selectBatchIds".equals(methodName)) {
                return List.of(new ErpFinanceVoucherDO().setId(1L).setVoucherNo("CWPZ2")
                        .setStatus(ErpFinanceVoucherStatusEnum.APPROVED.getStatus())
                        .setApproveUserId(9L)
                        .setApproveTime(LocalDateTime.of(2026, 4, 29, 10, 0)));
            }
            if ("updateById".equals(methodName)) {
                updatedVoucherRef.set((ErpFinanceVoucherDO) args[0]);
                return 1;
            }
            if ("clearApprovalMetadataById".equals(methodName)) {
                return 1;
            }
            return null;
        }));

        service.cancelApproveVoucher(9L, new ErpFinanceVoucherActionReqVO().setIds(List.of(1L)));

        assertEquals(1L, updatedVoucherRef.get().getId());
        assertEquals(ErpFinanceVoucherStatusEnum.GENERATED.getStatus(), updatedVoucherRef.get().getStatus());
        assertNull(updatedVoucherRef.get().getApproveUserId());
        assertNull(updatedVoucherRef.get().getApproveTime());
    }

    @Test
    void postVoucher_shouldUpdatePostedStatusWhenPeriodOpen() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<ErpFinanceVoucherDO> updatedVoucherRef = new AtomicReference<>();
        AtomicReference<ErpFinanceVoucherDO> appliedVoucherRef = new AtomicReference<>();
        AtomicReference<List<ErpFinanceVoucherEntryDO>> appliedEntriesRef = new AtomicReference<>();
        AtomicInteger batchQueryCount = new AtomicInteger();
        AtomicInteger singleQueryCount = new AtomicInteger();

        setField(service, "financeVoucherMapper", createProxy(ErpFinanceVoucherMapper.class, (methodName, args) -> {
            if ("selectBatchIds".equals(methodName)) {
                return List.of(new ErpFinanceVoucherDO().setId(1L).setVoucherNo("CWPZ3")
                        .setLedgerId(1L).setPeriodId(20L)
                        .setVoucherTime(LocalDateTime.of(2026, 4, 29, 10, 0))
                        .setStatus(ErpFinanceVoucherStatusEnum.APPROVED.getStatus()));
            }
            if ("updateById".equals(methodName)) {
                updatedVoucherRef.set((ErpFinanceVoucherDO) args[0]);
                return 1;
            }
            return null;
        }));
        mockValidatedLedger(service, 1L);
        mockOpenPeriod(service, 20L, 1L, LocalDate.of(2026, 4, 29));
        setField(service, "financeVoucherEntryMapper", createProxy(ErpFinanceVoucherEntryMapper.class, (methodName, args) -> {
            if ("selectListByVoucherIds".equals(methodName)) {
                batchQueryCount.incrementAndGet();
                return List.of(new ErpFinanceVoucherEntryDO().setVoucherId(1L).setEntryNo(1)
                        .setSubjectCode("660201").setSubjectName("MGMT_EXPENSE_RD")
                        .setDebitAmount(new BigDecimal("300.00")).setCreditAmount(BigDecimal.ZERO));
            }
            if ("selectListByVoucherId".equals(methodName)) {
                singleQueryCount.incrementAndGet();
                return List.of();
            }
            return null;
        }));
        setField(service, "financeGeneralLedgerService", createProxy(ErpFinanceGeneralLedgerService.class, (methodName, args) -> {
            if ("applyPostedVoucher".equals(methodName)) {
                appliedVoucherRef.set((ErpFinanceVoucherDO) args[0]);
                appliedEntriesRef.set((List<ErpFinanceVoucherEntryDO>) args[1]);
            }
            return null;
        }));

        service.postVoucher(9L, new ErpFinanceVoucherActionReqVO().setIds(List.of(1L)));

        assertEquals(1L, updatedVoucherRef.get().getId());
        assertEquals(ErpFinanceVoucherStatusEnum.POSTED.getStatus(), updatedVoucherRef.get().getStatus());
        assertEquals(9L, updatedVoucherRef.get().getPostUserId());
        assertEquals(1L, appliedVoucherRef.get().getId());
        assertEquals(1, appliedEntriesRef.get().size());
        assertEquals(1, batchQueryCount.get());
        assertEquals(0, singleQueryCount.get());
    }

    @Test
    void cancelPostVoucher_shouldRollbackPostedStatusWhenPeriodOpen() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<ErpFinanceVoucherDO> updatedVoucherRef = new AtomicReference<>();
        AtomicReference<ErpFinanceVoucherDO> rolledBackVoucherRef = new AtomicReference<>();
        AtomicReference<List<ErpFinanceVoucherEntryDO>> rolledBackEntriesRef = new AtomicReference<>();

        setField(service, "financeVoucherMapper", createProxy(ErpFinanceVoucherMapper.class, (methodName, args) -> {
            if ("selectBatchIds".equals(methodName)) {
                return List.of(new ErpFinanceVoucherDO().setId(1L).setVoucherNo("CWPZ4")
                        .setLedgerId(1L).setPeriodId(20L)
                        .setVoucherTime(LocalDateTime.of(2026, 4, 29, 10, 0))
                        .setStatus(ErpFinanceVoucherStatusEnum.POSTED.getStatus()));
            }
            if ("updateById".equals(methodName)) {
                updatedVoucherRef.set((ErpFinanceVoucherDO) args[0]);
                return 1;
            }
            if ("clearPostingMetadataById".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        mockValidatedLedger(service, 1L);
        mockOpenPeriod(service, 20L, 1L, LocalDate.of(2026, 4, 29));
        setField(service, "financeVoucherEntryMapper", createProxy(ErpFinanceVoucherEntryMapper.class, (methodName, args) -> {
            if ("selectListByVoucherId".equals(methodName)) {
                return List.of(new ErpFinanceVoucherEntryDO().setVoucherId(1L).setEntryNo(1)
                        .setSubjectCode("660201").setSubjectName("MGMT_EXPENSE_RD")
                        .setDebitAmount(new BigDecimal("300.00")).setCreditAmount(BigDecimal.ZERO));
            }
            return null;
        }));
        setField(service, "financeGeneralLedgerService", createProxy(ErpFinanceGeneralLedgerService.class, (methodName, args) -> {
            if ("rollbackPostedVoucher".equals(methodName)) {
                rolledBackVoucherRef.set((ErpFinanceVoucherDO) args[0]);
                rolledBackEntriesRef.set((List<ErpFinanceVoucherEntryDO>) args[1]);
            }
            return null;
        }));

        service.cancelPostVoucher(9L, new ErpFinanceVoucherActionReqVO().setIds(List.of(1L)));

        assertEquals(1L, updatedVoucherRef.get().getId());
        assertEquals(ErpFinanceVoucherStatusEnum.APPROVED.getStatus(), updatedVoucherRef.get().getStatus());
        assertNull(updatedVoucherRef.get().getPostUserId());
        assertEquals(1L, rolledBackVoucherRef.get().getId());
        assertEquals(1, rolledBackEntriesRef.get().size());
    }

    @Test
    void cancelPostVoucher_shouldRejectReverseVoucher() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();

        setField(service, "financeVoucherMapper", createProxy(ErpFinanceVoucherMapper.class, (methodName, args) -> {
            if ("selectBatchIds".equals(methodName)) {
                return List.of(new ErpFinanceVoucherDO().setId(1L).setVoucherNo("CWPZ5")
                        .setLedgerId(1L).setPeriodId(20L)
                        .setVoucherTime(LocalDateTime.of(2026, 4, 29, 10, 0))
                        .setStatus(ErpFinanceVoucherStatusEnum.POSTED.getStatus())
                        .setReverseFromVoucherId(99L));
            }
            return null;
        }));

        assertThrows(RuntimeException.class,
                () -> service.cancelPostVoucher(9L, new ErpFinanceVoucherActionReqVO().setIds(List.of(1L))));
    }

    @Test
    void reverseVoucher_shouldCreateReverseVoucherAndMarkSourceReversed() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<ErpFinanceVoucherDO> insertedVoucherRef = new AtomicReference<>();
        AtomicReference<List<ErpFinanceVoucherEntryDO>> insertedEntriesRef = new AtomicReference<>();
        AtomicReference<ErpFinanceVoucherDO> updatedSourceRef = new AtomicReference<>();
        AtomicReference<ErpFinanceVoucherDO> appliedVoucherRef = new AtomicReference<>();
        AtomicReference<List<ErpFinanceVoucherEntryDO>> appliedEntriesRef = new AtomicReference<>();

        setField(service, "financeVoucherMapper", createProxy(ErpFinanceVoucherMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpFinanceVoucherDO().setId(1L).setVoucherNo("CWPZ20260429000001")
                        .setLedgerId(1L).setPeriodId(20L).setTemplateId(10L)
                        .setBizType(ErpBizTypeEnum.FINANCE_EXPENSE.getType()).setBizId(100L)
                        .setBizNo("LSBX20260429000001")
                        .setVoucherTime(LocalDateTime.of(2026, 4, 29, 10, 0))
                        .setStatus(ErpFinanceVoucherStatusEnum.POSTED.getStatus())
                        .setTotalDebitAmount(new BigDecimal("300.00"))
                        .setTotalCreditAmount(new BigDecimal("300.00"));
            }
            if ("insert".equals(methodName)) {
                ErpFinanceVoucherDO voucher = (ErpFinanceVoucherDO) args[0];
                voucher.setId(99L);
                insertedVoucherRef.set(voucher);
                return 1;
            }
            if ("updateById".equals(methodName)) {
                updatedSourceRef.set((ErpFinanceVoucherDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "financeVoucherEntryMapper", createProxy(ErpFinanceVoucherEntryMapper.class, (methodName, args) -> {
            if ("selectListByVoucherId".equals(methodName)) {
                return List.of(
                        new ErpFinanceVoucherEntryDO().setId(11L).setEntryNo(1).setSummary("DEBIT")
                                .setSubjectCode("660201").setSubjectName("MGMT_EXPENSE_RD")
                                .setDebitAmount(new BigDecimal("300.00")).setCreditAmount(BigDecimal.ZERO),
                        new ErpFinanceVoucherEntryDO().setId(12L).setEntryNo(2).setSummary("CREDIT")
                                .setSubjectCode("220201").setSubjectName("OTHER_PAYABLE")
                                .setDebitAmount(BigDecimal.ZERO).setCreditAmount(new BigDecimal("300.00")));
            }
            if ("insertBatch".equals(methodName)) {
                insertedEntriesRef.set((List<ErpFinanceVoucherEntryDO>) args[0]);
                return true;
            }
            return null;
        }));
        mockOpenPeriod(service, 30L, 1L, LocalDate.of(2026, 4, 30));
        setField(service, "financeGeneralLedgerService", createProxy(ErpFinanceGeneralLedgerService.class, (methodName, args) -> {
            if ("applyPostedVoucher".equals(methodName)) {
                appliedVoucherRef.set((ErpFinanceVoucherDO) args[0]);
                appliedEntriesRef.set((List<ErpFinanceVoucherEntryDO>) args[1]);
            }
            return null;
        }));
        setField(service, "noRedisDAO", fixedNoRedisDao("CWPZ20260430000001"));

        Long reverseId = service.reverseVoucher(9L, new ErpFinanceVoucherReverseReqVO()
                .setId(1L)
                .setVoucherTime(LocalDateTime.of(2026, 4, 30, 9, 0))
                .setRemark("reverse"));

        assertEquals(99L, reverseId);
        assertEquals(99L, insertedVoucherRef.get().getId());
        assertEquals(ErpFinanceVoucherStatusEnum.POSTED.getStatus(), insertedVoucherRef.get().getStatus());
        assertEquals(1L, insertedVoucherRef.get().getReverseFromVoucherId());
        assertEquals(2, insertedEntriesRef.get().size());
        assertEquals(new BigDecimal("300.00"), insertedEntriesRef.get().get(0).getCreditAmount());
        assertEquals(new BigDecimal("300.00"), insertedEntriesRef.get().get(1).getDebitAmount());
        assertEquals(ErpFinanceVoucherStatusEnum.REVERSED.getStatus(), updatedSourceRef.get().getStatus());
        assertEquals(99L, updatedSourceRef.get().getReverseVoucherId());
        assertEquals(99L, appliedVoucherRef.get().getId());
        assertEquals(2, appliedEntriesRef.get().size());
    }

    @Test
    void rollbackAutoGeneratedVoucher_shouldVoidGeneratedDualLedgerVouchers() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<List<ErpFinanceVoucherDO>> updatedVoucherListRef = new AtomicReference<>(new ArrayList<>());
        AtomicReference<Boolean> entryDeletedRef = new AtomicReference<>(false);
        AtomicReference<String> deletedDiffLogKeyRef = new AtomicReference<>();

        setField(service, "dualLedgerConfigService", createProxy(ErpFinanceDualLedgerConfigService.class, (methodName, args) -> {
            if ("getEnabledDualLedgerConfig".equals(methodName)) {
                return new ErpFinanceDualLedgerConfigDO().setBizType(11)
                        .setExternalLedgerId(1L).setInternalLedgerId(2L);
            }
            return null;
        }));
        setField(service, "erpFinanceVoucherMapper", createProxy(ErpFinanceVoucherMapper.class, (methodName, args) -> {
            if ("selectList".equals(methodName)) {
                return List.of(
                        new ErpFinanceVoucherDO().setId(101L).setLedgerId(1L).setBizType(11).setBizId(88L)
                                .setVoucherNo("V-EXT").setStatus(ErpFinanceVoucherStatusEnum.GENERATED.getStatus()),
                        new ErpFinanceVoucherDO().setId(102L).setLedgerId(2L).setBizType(11).setBizId(88L)
                                .setVoucherNo("V-INT").setStatus(ErpFinanceVoucherStatusEnum.APPROVED.getStatus()));
            }
            if ("clearBizIdById".equals(methodName)) {
                return 1;
            }
            if ("updateById".equals(methodName)) {
                updatedVoucherListRef.get().add((ErpFinanceVoucherDO) args[0]);
                return 2;
            }
            return null;
        }));
        setField(service, "erpFinanceVoucherEntryMapper", createProxy(ErpFinanceVoucherEntryMapper.class, (methodName, args) -> {
            if ("deleteByVoucherIds".equals(methodName)) {
                entryDeletedRef.set(true);
                return 2;
            }
            return null;
        }));
        setField(service, "dualLedgerAmountDiffLogMapper", createProxy(ErpFinanceDualLedgerAmountDiffLogMapper.class, (methodName, args) -> {
            if ("deleteByBizTypeAndBizId".equals(methodName)) {
                deletedDiffLogKeyRef.set(args[0] + "-" + args[1]);
                return null;
            }
            return null;
        }));

        service.rollbackAutoGeneratedVoucher(11, 88L, 9L, "反审核回滚");

        assertEquals(2, updatedVoucherListRef.get().size());
        assertTrue(updatedVoucherListRef.get().stream().allMatch(item ->
                ErpFinanceVoucherStatusEnum.VOIDED.getStatus().equals(item.getStatus())));
        assertFalse(entryDeletedRef.get());
        assertEquals("11-88", deletedDiffLogKeyRef.get());
    }

    @Test
    void rollbackAutoGeneratedVoucher_shouldClearBizRelationBeforeVoidingGeneratedVoucher() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<List<Long>> clearedVoucherIdsRef = new AtomicReference<>(new ArrayList<>());
        AtomicReference<List<ErpFinanceVoucherDO>> updatedVoucherListRef = new AtomicReference<>(new ArrayList<>());

        setField(service, "dualLedgerConfigService", createProxy(ErpFinanceDualLedgerConfigService.class, (methodName, args) -> {
            if ("getEnabledDualLedgerConfig".equals(methodName)) {
                return new ErpFinanceDualLedgerConfigDO().setBizType(11)
                        .setExternalLedgerId(1L).setInternalLedgerId(2L);
            }
            return null;
        }));
        setField(service, "erpFinanceVoucherMapper", createProxy(ErpFinanceVoucherMapper.class, (methodName, args) -> {
            if ("selectList".equals(methodName)) {
                return List.of(
                        new ErpFinanceVoucherDO().setId(101L).setLedgerId(1L).setBizType(11).setBizId(88L)
                                .setVoucherNo("V-EXT").setStatus(ErpFinanceVoucherStatusEnum.GENERATED.getStatus()),
                        new ErpFinanceVoucherDO().setId(102L).setLedgerId(2L).setBizType(11).setBizId(88L)
                                .setVoucherNo("V-INT").setStatus(ErpFinanceVoucherStatusEnum.APPROVED.getStatus()));
            }
            if ("clearBizIdById".equals(methodName)) {
                clearedVoucherIdsRef.get().add((Long) args[0]);
                return 1;
            }
            if ("updateById".equals(methodName)) {
                updatedVoucherListRef.get().add((ErpFinanceVoucherDO) args[0]);
                return 2;
            }
            return null;
        }));
        setField(service, "erpFinanceVoucherEntryMapper", createProxy(ErpFinanceVoucherEntryMapper.class, (methodName, args) -> {
            if ("deleteByVoucherIds".equals(methodName)) {
                return 2;
            }
            return null;
        }));

        service.rollbackAutoGeneratedVoucher(11, 88L, 9L, "反审核回滚");

        assertEquals(List.of(101L, 102L), clearedVoucherIdsRef.get());
        assertEquals(List.of(101L, 102L),
                updatedVoucherListRef.get().stream().map(ErpFinanceVoucherDO::getId).toList());
    }

    @Test
    void rollbackAutoGeneratedVoucher_shouldReversePostedVoucherAndClearBizRelation() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<List<ErpFinanceVoucherDO>> updatedVoucherListRef = new AtomicReference<>(new ArrayList<>());

        setField(service, "dualLedgerConfigService", createProxy(ErpFinanceDualLedgerConfigService.class, (methodName, args) -> {
            if ("getEnabledDualLedgerConfig".equals(methodName)) {
                return new ErpFinanceDualLedgerConfigDO().setBizType(11)
                        .setExternalLedgerId(1L).setInternalLedgerId(2L);
            }
            return null;
        }));
        setField(service, "erpFinanceVoucherMapper", createProxy(ErpFinanceVoucherMapper.class, (methodName, args) -> {
            if ("selectList".equals(methodName)) {
                return List.of(new ErpFinanceVoucherDO().setId(101L).setLedgerId(1L).setBizType(11).setBizId(88L)
                        .setVoucherNo("V-EXT").setStatus(ErpFinanceVoucherStatusEnum.POSTED.getStatus()));
            }
            if ("selectById".equals(methodName)) {
                return new ErpFinanceVoucherDO().setId(101L).setLedgerId(1L).setBizType(11).setBizId(88L)
                        .setBizNo("CGRK202605240001").setVoucherNo("V-EXT")
                        .setVoucherTime(LocalDateTime.of(2026, 5, 24, 10, 0))
                        .setStatus(ErpFinanceVoucherStatusEnum.POSTED.getStatus())
                        .setTotalDebitAmount(new BigDecimal("100.00"))
                        .setTotalCreditAmount(new BigDecimal("100.00"));
            }
            if ("insert".equals(methodName)) {
                ErpFinanceVoucherDO voucher = (ErpFinanceVoucherDO) args[0];
                voucher.setId(201L);
                return 1;
            }
            if ("updateById".equals(methodName)) {
                updatedVoucherListRef.get().add((ErpFinanceVoucherDO) args[0]);
                return 1;
            }
            if ("clearBizIdById".equals(methodName)) {
                updatedVoucherListRef.get().add(new ErpFinanceVoucherDO().setId((Long) args[0]).setBizId(null));
                return 1;
            }
            return null;
        }));
        setField(service, "erpFinanceVoucherEntryMapper", createProxy(ErpFinanceVoucherEntryMapper.class, (methodName, args) -> {
            if ("selectListByVoucherId".equals(methodName)) {
                return List.of(
                        new ErpFinanceVoucherEntryDO().setId(11L).setEntryNo(1)
                                .setSubjectCode("1001").setSubjectName("现金")
                                .setDebitAmount(new BigDecimal("100.00")).setCreditAmount(BigDecimal.ZERO),
                        new ErpFinanceVoucherEntryDO().setId(12L).setEntryNo(2)
                                .setSubjectCode("2202").setSubjectName("应付")
                                .setDebitAmount(BigDecimal.ZERO).setCreditAmount(new BigDecimal("100.00")));
            }
            if ("insertBatch".equals(methodName)) {
                return true;
            }
            return null;
        }));
        mockOpenPeriod(service, 31L, 1L, LocalDate.of(2026, 5, 24));
        setField(service, "financeGeneralLedgerService", createProxy(ErpFinanceGeneralLedgerService.class, (methodName, args) -> null));
        setField(service, "noRedisDAO", fixedNoRedisDao("V-REV"));

        service.rollbackAutoGeneratedVoucher(11, 88L, 9L, "反审核回滚");

        assertEquals(3, updatedVoucherListRef.get().size());
        assertTrue(updatedVoucherListRef.get().stream().anyMatch(item -> ObjectUtil.equal(item.getId(), 101L) && item.getBizId() == null));
        assertTrue(updatedVoucherListRef.get().stream().anyMatch(item -> ObjectUtil.equal(item.getId(), 201L) && item.getBizId() == null));
    }

    @Test
    void recomputeAutoGeneratedVoucher_shouldRebuildExistingGeneratedVoucher() throws Exception {
        ErpFinanceVoucherServiceImpl service = newService();
        AtomicReference<List<ErpFinanceVoucherDO>> updatedVoucherListRef = new AtomicReference<>(new ArrayList<>());
        AtomicReference<Boolean> entryDeletedRef = new AtomicReference<>(false);
        AtomicReference<List<ErpFinanceVoucherEntryDO>> insertedEntriesRef = new AtomicReference<>();

        mockLedgerWithDefault(service, 1L);
        setField(service, "voucherTemplateService", createProxy(ErpFinanceVoucherTemplateService.class, (methodName, args) -> {
            if ("validateVoucherTemplate".equals(methodName)) {
                return template(10L, 1L, ErpBizTypeEnum.PURCHASE_IN.getType(), true, "PURCHASE_IN");
            }
            if ("getVoucherTemplateItemListByTemplateId".equals(methodName)) {
                return amountItems(ErpFinanceVoucherAmountSourceEnum.BIZ_AMOUNT.getType(), null);
            }
            if ("getVoucherTemplateListByLedgerAndBizType".equals(methodName)) {
                Long ledgerId = (Long) args[0];
                if (ObjectUtil.equal(ledgerId, 1L)) {
                    return List.of(template(10L, 1L, ErpBizTypeEnum.PURCHASE_IN.getType(), true, "PURCHASE_IN"));
                }
                return List.of();
            }
            return null;
        }));
        mockPurchaseIn(service, 88L, "CGRK202605240001", LocalDateTime.of(2026, 5, 24, 9, 0),
                new BigDecimal("500.00"), "purchase in");
        mockOpenPeriodForLedgers(service, List.of(1L, 2L), LocalDate.of(2026, 5, 24));
        setField(service, "erpFinanceVoucherMapper", createProxy(ErpFinanceVoucherMapper.class, (methodName, args) -> {
            if ("selectList".equals(methodName)) {
                return List.of(new ErpFinanceVoucherDO().setId(101L).setLedgerId(1L).setBizType(11).setBizId(88L)
                        .setVoucherNo("CWPZ20260526000001").setStatus(ErpFinanceVoucherStatusEnum.GENERATED.getStatus()));
            }
            if ("clearBizIdById".equals(methodName)) {
                return 1;
            }
            if ("updateById".equals(methodName)) {
                updatedVoucherListRef.get().add((ErpFinanceVoucherDO) args[0]);
                return 1;
            }
            if ("selectByLedgerIdAndBiz".equals(methodName)) {
                return null;
            }
            return null;
        }));
        setField(service, "erpFinanceVoucherEntryMapper", createProxy(ErpFinanceVoucherEntryMapper.class, (methodName, args) -> {
            if ("deleteByVoucherIds".equals(methodName)) {
                entryDeletedRef.set(true);
                return 1;
            }
            if ("delete".equals(methodName)) {
                entryDeletedRef.set(true);
                return 2;
            }
            if ("insertBatch".equals(methodName)) {
                insertedEntriesRef.set((List<ErpFinanceVoucherEntryDO>) args[0]);
                return true;
            }
            return null;
        }));
        setField(service, "noRedisDAO", new ErpNoRedisDAO() {
            private int seq = 1;

            @Override
            public String generate(String prefix) {
                return "CWPZ2026052600000" + (++seq);
            }
        });

        Long voucherId = service.recomputeAutoGeneratedVoucher(11, 88L, 9L, "重算");

        assertEquals(101L, voucherId);
        assertTrue(updatedVoucherListRef.get().stream().noneMatch(item ->
                ErpFinanceVoucherStatusEnum.VOIDED.getStatus().equals(item.getStatus())));
        assertTrue(entryDeletedRef.get());
        assertEquals(2, insertedEntriesRef.get().size());
    }

    private ErpFinanceVoucherServiceImpl newService() throws Exception {
        ErpFinanceVoucherServiceImpl service = new ErpFinanceVoucherServiceImpl();
        setField(service, "noRedisDAO", fixedNoRedisDao("CWPZ20260429000000"));
        setField(service, "dualLedgerConfigService", createProxy(ErpFinanceDualLedgerConfigService.class, (methodName, args) -> null));
        setField(service, "applicationContext", createProxy(ApplicationContext.class, (methodName, args) -> null));
        return service;
    }

    private void mockValidatedLedger(ErpFinanceVoucherServiceImpl service, Long ledgerId) throws Exception {
        mockLedger(service, ledgerId, false);
    }

    private void mockLedgerWithDefault(ErpFinanceVoucherServiceImpl service, Long ledgerId) throws Exception {
        mockLedger(service, ledgerId, true);
    }

    private void mockLedger(ErpFinanceVoucherServiceImpl service, Long ledgerId, boolean defaultStatus) throws Exception {
        setField(service, "financeLedgerService", createProxy(ErpFinanceLedgerService.class, (methodName, args) -> {
            if ("validateFinanceLedger".equals(methodName) || "getDefaultFinanceLedger".equals(methodName)) {
                return new ErpFinanceLedgerDO().setId(ledgerId).setName("LEDGER")
                        .setStatus(CommonStatusEnum.ENABLE.getStatus()).setDefaultStatus(defaultStatus);
            }
            return null;
        }));
    }

    private void mockTemplate(ErpFinanceVoucherServiceImpl service, ErpFinanceVoucherTemplateDO template,
                              List<ErpFinanceVoucherTemplateItemDO> items) throws Exception {
        setField(service, "voucherTemplateService", createProxy(ErpFinanceVoucherTemplateService.class, (methodName, args) -> {
            if ("validateVoucherTemplate".equals(methodName)) {
                return template;
            }
            if ("getVoucherTemplateItemListByTemplateId".equals(methodName)) {
                return items;
            }
            if ("getVoucherTemplateListByLedgerAndBizType".equals(methodName)) {
                return List.of(template);
            }
            return null;
        }));
    }

    private void mockApprovedExpense(ErpFinanceVoucherServiceImpl service, Long id, String no, LocalDateTime expenseTime,
                                     BigDecimal amount, String remark) throws Exception {
        setField(service, "financeExpenseService", createProxy(ErpFinanceExpenseService.class, (methodName, args) -> {
            if ("getFinanceExpense".equals(methodName)) {
                return new ErpFinanceExpenseDO().setId(id).setNo(no).setDeptId(99L)
                        .setStatus(ErpAuditStatus.APPROVE.getStatus())
                        .setExpenseTime(expenseTime)
                        .setExpensePrice(amount)
                        .setRemark(remark);
            }
            return null;
        }));
    }

    private void mockApprovedExpense(ErpFinanceVoucherServiceImpl service, ErpFinanceExpenseDO expense) throws Exception {
        setField(service, "financeExpenseService", createProxy(ErpFinanceExpenseService.class, (methodName, args) -> {
            if ("getFinanceExpense".equals(methodName)) {
                return expense;
            }
            return null;
        }));
    }

    private void mockAssetDepreciation(ErpFinanceVoucherServiceImpl service, Long id, String assetNo,
                                       String period, BigDecimal amount, AtomicReference<Long> boundVoucherIdRef) throws Exception {
        setField(service, "financeAssetDepreciationService", createProxy(ErpFinanceAssetDepreciationService.class,
                (methodName, args) -> {
                    if ("getFinanceAssetDepreciation".equals(methodName)) {
                        return new ErpFinanceAssetDepreciationDO().setId(id).setAssetNo(assetNo)
                                .setPeriod(period).setDepreciationAmount(amount);
                    }
                    if ("bindVoucher".equals(methodName)) {
                        boundVoucherIdRef.set((Long) args[1]);
                    }
                    return null;
                }));
    }

    private void mockPurchaseIn(ErpFinanceVoucherServiceImpl service, Long id, String no, LocalDateTime inTime,
                                BigDecimal amount, String remark) throws Exception {
        setField(service, "purchaseInService", createProxy(ErpPurchaseInService.class, (methodName, args) -> {
            if ("getPurchaseIn".equals(methodName)) {
                return new ErpPurchaseInDO().setId(id).setNo(no)
                        .setInTime(inTime)
                        .setTotalPrice(amount)
                        .setRemark(remark);
            }
            return null;
        }));
    }

    private void mockPurchaseReturn(ErpFinanceVoucherServiceImpl service, Long id, String no, LocalDateTime createTime,
                                    BigDecimal amount, String remark) throws Exception {
        setField(service, "purchaseReturnService", createProxy(ErpPurchaseReturnService.class, (methodName, args) -> {
            if ("getPurchaseReturn".equals(methodName)) {
                ErpPurchaseReturnDO purchaseReturn = new ErpPurchaseReturnDO()
                        .setId(id)
                        .setNo(no)
                        .setTotalPrice(amount)
                        .setRemark(remark);
                purchaseReturn.setCreateTime(createTime);
                return purchaseReturn;
            }
            return null;
        }));
    }

    private void mockStockCheck(ErpFinanceVoucherServiceImpl service, Long id, String no, LocalDateTime snapshotTime,
                                BigDecimal totalPrice, String remark) throws Exception {
        setField(service, "stockCheckMapper", createProxy(ErpStockCheckMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                ErpStockCheckDO stockCheck = new ErpStockCheckDO().setId(id).setNo(no)
                        .setSnapshotTime(snapshotTime)
                        .setTotalPrice(totalPrice)
                        .setRemark(remark);
                stockCheck.setCreateTime(snapshotTime);
                return stockCheck;
            }
            return null;
        }));
    }

    private void mockOutsourceOrderService(ErpFinanceVoucherServiceImpl service, ErpOutsourceFeeDO fee) throws Exception {
        setField(service, "outsourceOrderService", createProxy(ErpOutsourceOrderService.class, (methodName, args) -> {
            if ("getOutsourceFee".equals(methodName)) {
                return fee;
            }
            return null;
        }));
    }

    private void mockProductionInbound(ErpFinanceVoucherServiceImpl service, ErpProductionInboundDO inbound) throws Exception {
        setField(service, "productionInboundService", createProxy(ErpProductionInboundService.class, (methodName, args) -> {
            if ("getProductionInbound".equals(methodName)) {
                return inbound;
            }
            return null;
        }));
    }

    private void mockSaleOut(ErpFinanceVoucherServiceImpl service, Long id, String no, LocalDateTime outTime,
                             BigDecimal amount, String remark) throws Exception {
        setField(service, "saleOutService", createProxy(ErpSaleOutService.class, (methodName, args) -> {
            if ("getSaleOut".equals(methodName)) {
                return new ErpSaleOutDO().setId(id).setNo(no)
                        .setOutTime(outTime)
                        .setTotalPrice(amount)
                        .setRemark(remark);
            }
            return null;
        }));
    }

    private void mockSaleOut(ErpFinanceVoucherServiceImpl service, ErpSaleOutDO saleOut) throws Exception {
        setField(service, "saleOutService", createProxy(ErpSaleOutService.class, (methodName, args) -> {
            if ("getSaleOut".equals(methodName)) {
                return saleOut;
            }
            return null;
        }));
    }

    private void mockSaleReturn(ErpFinanceVoucherServiceImpl service, Long id, String no, LocalDateTime returnTime,
                                BigDecimal amount, String remark) throws Exception {
        setField(service, "saleReturnService", createProxy(ErpSaleReturnService.class, (methodName, args) -> {
            if ("getSaleReturn".equals(methodName)) {
                return new ErpSaleReturnDO().setId(id).setNo(no)
                        .setReturnTime(returnTime)
                        .setTotalPrice(amount)
                        .setRemark(remark);
            }
            return null;
        }));
    }

    private void mockSaleReturn(ErpFinanceVoucherServiceImpl service, ErpSaleReturnDO saleReturn) throws Exception {
        setField(service, "saleReturnService", createProxy(ErpSaleReturnService.class, (methodName, args) -> {
            if ("getSaleReturn".equals(methodName)) {
                return saleReturn;
            }
            return null;
        }));
    }

    private ErpSaleOutDO saleOut(Long id, String no, LocalDateTime outTime, LocalDateTime createTime,
                                 LocalDateTime updateTime, BigDecimal amount, String remark) {
        ErpSaleOutDO saleOut = new ErpSaleOutDO().setId(id).setNo(no)
                .setOutTime(outTime)
                .setTotalPrice(amount)
                .setRemark(remark);
        saleOut.setCreateTime(createTime);
        saleOut.setUpdateTime(updateTime);
        return saleOut;
    }

    private ErpSaleReturnDO saleReturn(Long id, String no, LocalDateTime returnTime, LocalDateTime createTime,
                                       LocalDateTime updateTime, BigDecimal amount, String remark) {
        ErpSaleReturnDO saleReturn = new ErpSaleReturnDO().setId(id).setNo(no)
                .setReturnTime(returnTime)
                .setTotalPrice(amount)
                .setRemark(remark);
        saleReturn.setCreateTime(createTime);
        saleReturn.setUpdateTime(updateTime);
        return saleReturn;
    }

    private void mockOpenPeriod(ErpFinanceVoucherServiceImpl service, Long periodId, Long ledgerId, LocalDate date) throws Exception {
        setField(service, "financePeriodService", createProxy(ErpFinancePeriodService.class, (methodName, args) -> {
            if ("getCurrentOpenPeriod".equals(methodName)) {
                return new ErpFinancePeriodDO().setId(periodId).setLedgerId(ledgerId)
                        .setPeriodCode(date.getYear() + "-" + String.format("%02d", date.getMonthValue()))
                        .setStartDate(date.withDayOfMonth(1))
                        .setEndDate(date.withDayOfMonth(date.lengthOfMonth()));
            }
            return null;
        }));
    }

    private void mockOpenPeriodForLedgers(ErpFinanceVoucherServiceImpl service, List<Long> ledgerIds, LocalDate date) throws Exception {
        setField(service, "financePeriodService", createProxy(ErpFinancePeriodService.class, (methodName, args) -> {
            if ("getCurrentOpenPeriod".equals(methodName)) {
                Long ledgerId = (Long) args[0];
                if (ledgerIds.contains(ledgerId)) {
                    return new ErpFinancePeriodDO().setId(ledgerId + 100).setLedgerId(ledgerId)
                            .setPeriodCode(date.getYear() + "-" + String.format("%02d", date.getMonthValue()))
                            .setStartDate(date.withDayOfMonth(1))
                            .setEndDate(date.withDayOfMonth(date.lengthOfMonth()));
                }
            }
            return null;
        }));
    }

    private void mockVoucherPersistence(ErpFinanceVoucherServiceImpl service, String voucherNo, Long insertId,
                                        AtomicReference<ErpFinanceVoucherDO> insertedVoucherRef,
                                        AtomicReference<List<ErpFinanceVoucherEntryDO>> insertedEntriesRef,
                                        ErpFinanceVoucherDO existedVoucher) throws Exception {
        mockVoucherPersistence(service, voucherNo, insertId, insertedVoucherRef, insertedEntriesRef, existedVoucher, null);
    }

    private void mockVoucherPersistence(ErpFinanceVoucherServiceImpl service, String voucherNo, Long insertId,
                                        AtomicReference<ErpFinanceVoucherDO> insertedVoucherRef,
                                        AtomicReference<List<ErpFinanceVoucherEntryDO>> insertedEntriesRef,
                                        ErpFinanceVoucherDO existedVoucher,
                                        ErpFinanceVoucherDO legacyExpenseVoucher) throws Exception {
        setField(service, "financeVoucherMapper", createProxy(ErpFinanceVoucherMapper.class, (methodName, args) -> {
            if ("selectByLedgerIdAndBiz".equals(methodName)) {
                Long ledgerId = (Long) args[0];
                Integer bizType = (Integer) args[1];
                Long bizId = (Long) args[2];
                if (legacyExpenseVoucher != null
                        && legacyExpenseVoucher.getLedgerId().equals(ledgerId)
                        && legacyExpenseVoucher.getBizType().equals(bizType)
                        && legacyExpenseVoucher.getBizId().equals(bizId)) {
                    return legacyExpenseVoucher;
                }
                return existedVoucher;
            }
            if ("insert".equals(methodName)) {
                ErpFinanceVoucherDO voucher = (ErpFinanceVoucherDO) args[0];
                voucher.setId(insertId);
                if (insertedVoucherRef != null) {
                    insertedVoucherRef.set(voucher);
                }
                return 1;
            }
            return null;
        }));
        setField(service, "financeVoucherEntryMapper", createProxy(ErpFinanceVoucherEntryMapper.class, (methodName, args) -> {
            if ("insertBatch".equals(methodName)) {
                if (insertedEntriesRef != null) {
                    insertedEntriesRef.set((List<ErpFinanceVoucherEntryDO>) args[0]);
                }
                return true;
            }
            return null;
        }));
        setField(service, "noRedisDAO", fixedNoRedisDao(voucherNo));
    }

    private ErpFinanceVoucherTemplateDO template(Long id, Long ledgerId, Integer bizType, boolean autoGenerate, String summary) {
        return new ErpFinanceVoucherTemplateDO()
                .setId(id)
                .setLedgerId(ledgerId)
                .setBizType(bizType)
                .setName("TEMPLATE-" + id)
                .setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setAutoGenerate(autoGenerate)
                .setDefaultSummary(summary);
    }

    private List<ErpFinanceVoucherTemplateItemDO> amountItems(Integer amountSource, BigDecimal amountSourceValue) {
        return List.of(
                new ErpFinanceVoucherTemplateItemDO().setId(1L).setEntryNo(1)
                        .setEntryDirection(ErpFinanceVoucherEntryDirectionEnum.DEBIT.getType())
                        .setSubjectCode("660201").setSubjectName("MGMT_EXPENSE_RD")
                        .setAmountSource(amountSource)
                        .setAmountSourceValue(amountSourceValue),
                new ErpFinanceVoucherTemplateItemDO().setId(2L).setEntryNo(2)
                        .setEntryDirection(ErpFinanceVoucherEntryDirectionEnum.CREDIT.getType())
                        .setSubjectCode("220201").setSubjectName("OTHER_PAYABLE")
                        .setAmountSource(amountSource)
                        .setAmountSourceValue(amountSourceValue));
    }

    private ErpNoRedisDAO fixedNoRedisDao(String voucherNo) {
        return new ErpNoRedisDAO() {
            @Override
            public String generate(String prefix) {
                return voucherNo;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> type, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
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
        Object handle(String methodName, Object[] args) throws Exception;
    }
}
