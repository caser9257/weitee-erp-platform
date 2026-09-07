package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpArStatementDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpArStatementItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceReceiptDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceReceiptItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleReturnDO;
import cn.weitee.erp.module.erp.enums.ErpArStatementItemTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.sale.ErpSaleOrderService;
import cn.weitee.erp.module.erp.service.sale.ErpSaleOutService;
import cn.weitee.erp.module.erp.service.sale.ErpSaleReturnService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.FINANCE_RECEIPT_APPROVE_FAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 收款审批 AR 事实闭环单测：审批分配、反审核退回、重复审批、并发 CAS、超额收款、事务回滚
 */
@ExtendWith(MockitoExtension.class)
class ErpFinanceReceiptServiceImplTest {

    private static final Long RECEIPT_ID = 3L;
    private static final String RECEIPT_NO = "SK-001";
    private static final Long SALE_OUT_ID = 21L;
    private static final Long SALE_RETURN_ID = 22L;

    @Mock
    private cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceReceiptMapper erpFinanceReceiptMapper;
    @Mock
    private cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceReceiptItemMapper erpFinanceReceiptItemMapper;
    @Mock
    private cn.weitee.erp.module.erp.dal.mysql.finance.ErpArStatementMapper arStatementMapper;
    @Mock
    private cn.weitee.erp.module.erp.dal.mysql.finance.ErpArStatementItemMapper arStatementItemMapper;
    @Mock
    private cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO noRedisDAO;
    @Mock
    private cn.weitee.erp.module.erp.service.sale.ErpCustomerService erpCustomerService;
    @Mock
    private cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceDualLedgerConfigMapper dualLedgerConfigMapper;
    @Mock
    private ErpSaleOutService saleOutService;
    @Mock
    private ErpSaleReturnService saleReturnService;
    @Mock
    private ErpSaleOrderService saleOrderService;
    @Mock
    private ErpArStatementService arStatementService;
    @Mock
    private PlatformTransactionManager transactionManager;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RLock bizLock;

    @InjectMocks
    private ErpFinanceReceiptServiceImpl service;

    // ==================== 成功路径 ====================

    @Test
    void approveReceipt_shouldCreateAllocateFactAndRefreshStatement() {
        ErpFinanceReceiptDO receipt = buildReceipt(ErpAuditStatus.PROCESS.getStatus());
        ErpFinanceReceiptItemDO item = buildSaleOutItem(new BigDecimal("80.00"));
        stubCommon(receipt, item);
        stubLockSuccess();
        stubTxExecuteRuns();

        service.updateFinanceReceiptStatus(RECEIPT_ID, ErpAuditStatus.APPROVE.getStatus());

        // 审批写收款分配明细（类型 2），事实指向收款单
        ArgumentCaptor<Long> refIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> refNoCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(arStatementService).createReceiptAllocatedItem(eq(ErpBizTypeEnum.SALE_OUT.getType()),
                eq(SALE_OUT_ID), refIdCaptor.capture(), refNoCaptor.capture(),
                amountCaptor.capture(), any());
        assertEquals(RECEIPT_ID, refIdCaptor.getValue());
        assertEquals(RECEIPT_NO, refNoCaptor.getValue());
        assertEquals(new BigDecimal("80.00"), amountCaptor.getValue());
        verify(arStatementService, never()).createReceiptReturnedItem(any(), any(), any(), any(), any(), any());
        // 主表按事实源幂等刷新（convertSet 返回 Set，用内容断言）
        ArgumentCaptor<java.util.Collection<Long>> refreshIdsCaptor = ArgumentCaptor.forClass(java.util.Collection.class);
        verify(arStatementService).refreshStatementAmountByBizIds(eq(ErpBizTypeEnum.SALE_OUT.getType()),
                refreshIdsCaptor.capture());
        assertEquals(java.util.Set.of(SALE_OUT_ID), java.util.Set.copyOf(refreshIdsCaptor.getValue()));
    }

    @Test
    void approveReceipt_shouldCreateNegativeAllocateForReturnStatement() {
        ErpFinanceReceiptDO receipt = buildReceipt(ErpAuditStatus.PROCESS.getStatus());
        ErpFinanceReceiptItemDO item = buildSaleReturnItem(new BigDecimal("30.00"));
        stubCommon(receipt, item);
        when(saleReturnService.validateSaleReturn(SALE_RETURN_ID)).thenReturn(new ErpSaleReturnDO()
                .setId(SALE_RETURN_ID).setTotalPrice(new BigDecimal("-40.00")));
        // 审批前本单未入已审批口径，汇总为 0；否则 updateSalePrice 退货分支 null.negate() 会 NPE
        when(erpFinanceReceiptItemMapper.selectReceiptPriceSumByBizIdAndBizType(SALE_RETURN_ID,
                ErpBizTypeEnum.SALE_RETURN.getType())).thenReturn(BigDecimal.ZERO);
        stubLockSuccess();
        stubTxExecuteRuns();

        service.updateFinanceReceiptStatus(RECEIPT_ID, ErpAuditStatus.APPROVE.getStatus());

        // 退货台账金额为负向，审批同样写分配明细，金额方向由台账事实方法内统一取号
        verify(arStatementService).createReceiptAllocatedItem(eq(ErpBizTypeEnum.SALE_RETURN.getType()),
                eq(SALE_RETURN_ID), eq(RECEIPT_ID), eq(RECEIPT_NO),
                eq(new BigDecimal("30.00")), any());
    }

    @Test
    void approveReceipt_shouldRefreshStatementPerBizTypeWhenMixedItems() {
        ErpFinanceReceiptDO receipt = buildReceipt(ErpAuditStatus.PROCESS.getStatus());
        ErpFinanceReceiptItemDO outItem = buildSaleOutItem(new BigDecimal("50.00"));
        ErpFinanceReceiptItemDO returnItem = buildSaleReturnItem(new BigDecimal("20.00"));
        when(erpFinanceReceiptMapper.selectById(RECEIPT_ID)).thenReturn(receipt);
        when(erpFinanceReceiptMapper.updateByIdAndStatus(eq(RECEIPT_ID), eq(ErpAuditStatus.PROCESS.getStatus()),
                any())).thenReturn(1);
        when(erpFinanceReceiptItemMapper.selectListByReceiptId(RECEIPT_ID)).thenReturn(List.of(outItem, returnItem));
        when(saleOutService.validateSaleOut(SALE_OUT_ID)).thenReturn(new ErpSaleOutDO()
                .setId(SALE_OUT_ID).setTotalPrice(new BigDecimal("100.00")));
        when(saleReturnService.validateSaleReturn(SALE_RETURN_ID)).thenReturn(new ErpSaleReturnDO()
                .setId(SALE_RETURN_ID).setTotalPrice(new BigDecimal("-40.00")));
        // 审批前本单未入已审批口径，汇总为 0；否则 updateSalePrice 退货分支 null.negate() 会 NPE
        when(erpFinanceReceiptItemMapper.selectReceiptPriceSumByBizIdAndBizType(SALE_OUT_ID,
                ErpBizTypeEnum.SALE_OUT.getType())).thenReturn(BigDecimal.ZERO);
        when(erpFinanceReceiptItemMapper.selectReceiptPriceSumByBizIdAndBizType(SALE_RETURN_ID,
                ErpBizTypeEnum.SALE_RETURN.getType())).thenReturn(BigDecimal.ZERO);
        stubLockSuccess();
        stubTxExecuteRuns();

        service.updateFinanceReceiptStatus(RECEIPT_ID, ErpAuditStatus.APPROVE.getStatus());

        verify(arStatementService).createReceiptAllocatedItem(eq(ErpBizTypeEnum.SALE_OUT.getType()),
                eq(SALE_OUT_ID), eq(RECEIPT_ID), eq(RECEIPT_NO), eq(new BigDecimal("50.00")), any());
        verify(arStatementService).createReceiptAllocatedItem(eq(ErpBizTypeEnum.SALE_RETURN.getType()),
                eq(SALE_RETURN_ID), eq(RECEIPT_ID), eq(RECEIPT_NO), eq(new BigDecimal("20.00")), any());
        // 混合出库/退货必须按业务类型分组刷新，不能只刷第一项的类型
        ArgumentCaptor<java.util.Collection<Long>> outIdsCaptor = ArgumentCaptor.forClass(java.util.Collection.class);
        ArgumentCaptor<java.util.Collection<Long>> returnIdsCaptor = ArgumentCaptor.forClass(java.util.Collection.class);
        verify(arStatementService).refreshStatementAmountByBizIds(eq(ErpBizTypeEnum.SALE_OUT.getType()),
                outIdsCaptor.capture());
        verify(arStatementService).refreshStatementAmountByBizIds(eq(ErpBizTypeEnum.SALE_RETURN.getType()),
                returnIdsCaptor.capture());
        assertEquals(java.util.Set.of(SALE_OUT_ID), java.util.Set.copyOf(outIdsCaptor.getValue()));
        assertEquals(java.util.Set.of(SALE_RETURN_ID), java.util.Set.copyOf(returnIdsCaptor.getValue()));
    }

    @Test
    void unapproveReceipt_shouldCreateReturnedFactAndRefreshStatement() {
        ErpFinanceReceiptDO receipt = buildReceipt(ErpAuditStatus.APPROVE.getStatus());
        ErpFinanceReceiptItemDO item = buildSaleOutItem(new BigDecimal("80.00"));
        stubCommon(receipt, item);
        stubTxExecuteRuns();

        service.updateFinanceReceiptStatus(RECEIPT_ID, ErpAuditStatus.PROCESS.getStatus());

        // 反审核写收款退回明细（类型 3），并幂等刷新主表，金额与状态可恢复
        verify(arStatementService).createReceiptReturnedItem(eq(ErpBizTypeEnum.SALE_OUT.getType()),
                eq(SALE_OUT_ID), eq(RECEIPT_ID), eq(RECEIPT_NO), eq(new BigDecimal("80.00")), any());
        verify(arStatementService, never()).createReceiptAllocatedItem(any(), any(), any(), any(), any(), any());
        ArgumentCaptor<java.util.Collection<Long>> refreshIdsCaptor = ArgumentCaptor.forClass(java.util.Collection.class);
        verify(arStatementService).refreshStatementAmountByBizIds(eq(ErpBizTypeEnum.SALE_OUT.getType()),
                refreshIdsCaptor.capture());
        assertEquals(java.util.Set.of(SALE_OUT_ID), java.util.Set.copyOf(refreshIdsCaptor.getValue()));
        // 反审核不加业务锁（保持现有口径）
        verify(redissonClient, never()).getLock(org.mockito.ArgumentMatchers.anyString());
    }

    // ==================== 重复审批 / 并发 CAS ====================

    @Test
    void approveReceipt_shouldRejectDuplicateApproval() {
        ErpFinanceReceiptDO receipt = buildReceipt(ErpAuditStatus.APPROVE.getStatus());
        when(erpFinanceReceiptMapper.selectById(RECEIPT_ID)).thenReturn(receipt);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.updateFinanceReceiptStatus(RECEIPT_ID, ErpAuditStatus.APPROVE.getStatus()));

        assertEquals(FINANCE_RECEIPT_APPROVE_FAIL.getCode(), ex.getCode());
        verify(arStatementService, never()).createReceiptAllocatedItem(any(), any(), any(), any(), any(), any());
        verify(erpFinanceReceiptMapper, never()).updateByIdAndStatus(anyLong(), any(), any());
    }

    @Test
    void approveReceipt_shouldRejectWhenCasUpdateMisses() {
        // 并发审批：预检状态仍为未审核，但 CAS 更新（WHERE status=旧值）命中 0 行，必须中断
        ErpFinanceReceiptDO receipt = buildReceipt(ErpAuditStatus.PROCESS.getStatus());
        ErpFinanceReceiptItemDO item = buildSaleOutItem(new BigDecimal("80.00"));
        when(erpFinanceReceiptMapper.selectById(RECEIPT_ID)).thenReturn(receipt);
        when(erpFinanceReceiptItemMapper.selectListByReceiptId(RECEIPT_ID)).thenReturn(List.of(item));
        when(saleOutService.validateSaleOut(SALE_OUT_ID)).thenReturn(new ErpSaleOutDO()
                .setId(SALE_OUT_ID).setTotalPrice(new BigDecimal("100.00")));
        stubLockSuccess();
        stubTxExecuteRuns();
        when(erpFinanceReceiptMapper.updateByIdAndStatus(eq(RECEIPT_ID), eq(ErpAuditStatus.PROCESS.getStatus()),
                any())).thenReturn(0);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.updateFinanceReceiptStatus(RECEIPT_ID, ErpAuditStatus.APPROVE.getStatus()));

        assertEquals(FINANCE_RECEIPT_APPROVE_FAIL.getCode(), ex.getCode());
        // CAS 未命中时禁止写任何事实，重复审批不会重复增加已收金额
        verify(arStatementService, never()).createReceiptAllocatedItem(any(), any(), any(), any(), any(), any());
        verify(arStatementService, never()).refreshStatementAmountByBizIds(any(), anyCollection());
    }

    // ==================== 超额收款 ====================

    @Test
    void approveReceipt_shouldRejectWhenReceiptPriceExceedsAvailable() {
        ErpFinanceReceiptDO receipt = buildReceipt(ErpAuditStatus.PROCESS.getStatus());
        ErpFinanceReceiptItemDO item = buildSaleOutItem(new BigDecimal("999.00"));
        when(erpFinanceReceiptMapper.selectById(RECEIPT_ID)).thenReturn(receipt);
        when(erpFinanceReceiptItemMapper.selectListByReceiptId(RECEIPT_ID)).thenReturn(List.of(item));
        stubLockSuccess();
        stubTxExecuteRuns();
        when(saleOutService.validateSaleOut(SALE_OUT_ID)).thenReturn(new ErpSaleOutDO()
                .setId(SALE_OUT_ID).setTotalPrice(new BigDecimal("100.00")));
        // 已审核口径下其它收款已收 80，剩余可收 20 < 999
        when(erpFinanceReceiptItemMapper.selectReceiptPriceSumByBizIdAndBizType(SALE_OUT_ID,
                ErpBizTypeEnum.SALE_OUT.getType())).thenReturn(new BigDecimal("80.00"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updateFinanceReceiptStatus(RECEIPT_ID, ErpAuditStatus.APPROVE.getStatus()));

        assertEquals("本次收款不能超过业务单剩余可收金额", ex.getMessage());
        verify(arStatementService, never()).createReceiptAllocatedItem(any(), any(), any(), any(), any(), any());
    }

    // ==================== 事务失败与回滚路径 ====================

    @Test
    void approveReceipt_shouldRollbackAllFactsWhenStatementRefreshFails() {
        // 台账刷新抛异常 → 事务整体回滚 → 无任何事实残留（单测内表现为：事实行已尝试写入但异常上抛）
        ErpFinanceReceiptDO receipt = buildReceipt(ErpAuditStatus.PROCESS.getStatus());
        ErpFinanceReceiptItemDO item = buildSaleOutItem(new BigDecimal("80.00"));
        stubCommon(receipt, item);
        stubLockSuccess();
        stubTxRollbackOnException();
        doThrow(new IllegalStateException("ar statement refresh failed"))
                .when(arStatementService).refreshStatementAmountByBizIds(any(), anyCollection());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.updateFinanceReceiptStatus(RECEIPT_ID, ErpAuditStatus.APPROVE.getStatus()));

        assertEquals("ar statement refresh failed", ex.getMessage());
        verify(transactionManager).rollback(any(TransactionStatus.class));
        // 回滚路径上事实写入尝试过一次（事务内），不产生二次写入
        verify(arStatementService, org.mockito.Mockito.times(1)).createReceiptAllocatedItem(
                any(), any(), any(), any(), any(), any());
    }

    @Test
    void approveReceipt_shouldNotWriteFactWhenItemMapperReturnsEmpty() {
        ErpFinanceReceiptDO receipt = buildReceipt(ErpAuditStatus.PROCESS.getStatus());
        when(erpFinanceReceiptMapper.selectById(RECEIPT_ID)).thenReturn(receipt);
        when(erpFinanceReceiptMapper.updateByIdAndStatus(eq(RECEIPT_ID), eq(ErpAuditStatus.PROCESS.getStatus()),
                any())).thenReturn(1);
        when(erpFinanceReceiptItemMapper.selectListByReceiptId(RECEIPT_ID)).thenReturn(List.of());
        stubLockSuccess();
        stubTxExecuteRuns();

        service.updateFinanceReceiptStatus(RECEIPT_ID, ErpAuditStatus.APPROVE.getStatus());

        verify(arStatementService, never()).createReceiptAllocatedItem(any(), any(), any(), any(), any(), any());
        verify(arStatementService, never()).refreshStatementAmountByBizIds(any(), anyCollection());
    }

    // ==================== AR 台账事实方法（真实实现） ====================

    @Test
    void refreshStatementAmountByBizIds_shouldDeriveReceivedAmountIdempotently() {
        // 真实实现（不 mock AR service）：验证事实推导的幂等语义
        ErpArStatementServiceImpl arService = new ErpArStatementServiceImpl();
        inject(arService, "erpArStatementMapper", arStatementMapper);
        inject(arService, "erpArStatementItemMapper", arStatementItemMapper);
        inject(arService, "erpFinanceReceiptItemMapper", erpFinanceReceiptItemMapper);
        inject(arService, "erpNoRedisDAO", noRedisDAO);
        inject(arService, "erpCustomerService", erpCustomerService);
        inject(arService, "dualLedgerConfigMapper", dualLedgerConfigMapper);

        ErpArStatementDO statement = new ErpArStatementDO()
                .setId(10L)
                .setBizType(ErpBizTypeEnum.SALE_OUT.getType())
                .setBizId(SALE_OUT_ID)
                .setAmount(new BigDecimal("100.00"))
                .setReceivedAmount(new BigDecimal("0"))
                .setRemainAmount(new BigDecimal("100.00"))
                .setStatus(0);
        when(arStatementMapper.selectListByBizTypeAndBizIds(eq(ErpBizTypeEnum.SALE_OUT.getType()), anyCollection()))
                .thenReturn(List.of(statement));
        // 事实源：已审核收款汇总 80（无论主表旧值是 0 还是 50，推导结果一致 → 幂等）
        when(erpFinanceReceiptItemMapper.selectReceiptPriceSumByBizIdAndBizType(SALE_OUT_ID,
                ErpBizTypeEnum.SALE_OUT.getType())).thenReturn(new BigDecimal("80.00"));

        arService.refreshStatementAmountByBizIds(ErpBizTypeEnum.SALE_OUT.getType(), List.of(SALE_OUT_ID));

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<BigDecimal> receivedCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        ArgumentCaptor<BigDecimal> remainCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        ArgumentCaptor<Integer> statusCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(arStatementMapper).updateReceivedAmountById(idCaptor.capture(), receivedCaptor.capture(),
                remainCaptor.capture(), statusCaptor.capture());
        assertEquals(10L, idCaptor.getValue());
        assertEquals(new BigDecimal("80.00"), receivedCaptor.getValue());
        assertEquals(new BigDecimal("20.00"), remainCaptor.getValue());
        assertEquals(1, statusCaptor.getValue()); // 部分收
    }

    @Test
    void refreshStatementAmountByBizIds_shouldDeriveNegativeDirectionForReturnStatement() {
        ErpArStatementServiceImpl arService = new ErpArStatementServiceImpl();
        inject(arService, "erpArStatementMapper", arStatementMapper);
        inject(arService, "erpArStatementItemMapper", arStatementItemMapper);
        inject(arService, "erpFinanceReceiptItemMapper", erpFinanceReceiptItemMapper);
        inject(arService, "erpNoRedisDAO", noRedisDAO);
        inject(arService, "erpCustomerService", erpCustomerService);
        inject(arService, "dualLedgerConfigMapper", dualLedgerConfigMapper);

        ErpArStatementDO statement = new ErpArStatementDO()
                .setId(11L)
                .setBizType(ErpBizTypeEnum.SALE_RETURN.getType())
                .setBizId(SALE_RETURN_ID)
                .setAmount(new BigDecimal("-100.00"))
                .setReceivedAmount(new BigDecimal("0"))
                .setRemainAmount(new BigDecimal("-100.00"))
                .setStatus(0);
        when(arStatementMapper.selectListByBizTypeAndBizIds(eq(ErpBizTypeEnum.SALE_RETURN.getType()), anyCollection()))
                .thenReturn(List.of(statement));
        when(erpFinanceReceiptItemMapper.selectReceiptPriceSumByBizIdAndBizType(SALE_RETURN_ID,
                ErpBizTypeEnum.SALE_RETURN.getType())).thenReturn(new BigDecimal("40.00"));

        arService.refreshStatementAmountByBizIds(ErpBizTypeEnum.SALE_RETURN.getType(), List.of(SALE_RETURN_ID));

        // 退货台账负向：已收取 -40，剩余 = -100 - (-40) = -60
        verify(arStatementMapper).updateReceivedAmountById(eq(11L),
                eq(new BigDecimal("-40.00")), eq(new BigDecimal("-60.00")), eq(1));
    }

    @Test
    void refreshStatementAmountByBizIds_shouldSkipClosedStatement() {
        ErpArStatementServiceImpl arService = new ErpArStatementServiceImpl();
        inject(arService, "erpArStatementMapper", arStatementMapper);
        inject(arService, "erpArStatementItemMapper", arStatementItemMapper);
        inject(arService, "erpFinanceReceiptItemMapper", erpFinanceReceiptItemMapper);
        inject(arService, "erpNoRedisDAO", noRedisDAO);
        inject(arService, "erpCustomerService", erpCustomerService);
        inject(arService, "dualLedgerConfigMapper", dualLedgerConfigMapper);

        ErpArStatementDO closedStatement = new ErpArStatementDO()
                .setId(12L)
                .setBizType(ErpBizTypeEnum.SALE_OUT.getType())
                .setBizId(SALE_OUT_ID)
                .setAmount(new BigDecimal("100.00"))
                .setStatus(3); // 已关闭（出库反审核关闭台账）
        when(arStatementMapper.selectListByBizTypeAndBizIds(eq(ErpBizTypeEnum.SALE_OUT.getType()), anyCollection()))
                .thenReturn(List.of(closedStatement));

        arService.refreshStatementAmountByBizIds(ErpBizTypeEnum.SALE_OUT.getType(), List.of(SALE_OUT_ID));

        verify(arStatementMapper, never()).updateReceivedAmountById(anyLong(), any(), any(), any());
    }

    @Test
    void createReceiptFacts_shouldDirectNegativeStatementAndRollbackNegate() {
        ErpArStatementServiceImpl arService = new ErpArStatementServiceImpl();
        inject(arService, "erpArStatementMapper", arStatementMapper);
        inject(arService, "erpArStatementItemMapper", arStatementItemMapper);
        inject(arService, "erpFinanceReceiptItemMapper", erpFinanceReceiptItemMapper);
        inject(arService, "erpNoRedisDAO", noRedisDAO);
        inject(arService, "erpCustomerService", erpCustomerService);
        inject(arService, "dualLedgerConfigMapper", dualLedgerConfigMapper);

        ErpArStatementDO returnStatement = new ErpArStatementDO()
                .setId(13L)
                .setBizType(ErpBizTypeEnum.SALE_RETURN.getType())
                .setBizId(SALE_RETURN_ID)
                .setAmount(new BigDecimal("-100.00"))
                .setReceivedAmount(BigDecimal.ZERO)
                .setRemainAmount(new BigDecimal("-100.00"));

        // 分配：负向台账跟随方向 -30
        when(arStatementMapper.selectByBizTypeAndBizId(ErpBizTypeEnum.SALE_RETURN.getType(), SALE_RETURN_ID))
                .thenReturn(returnStatement);
        arService.createReceiptAllocatedItem(ErpBizTypeEnum.SALE_RETURN.getType(), SALE_RETURN_ID,
                RECEIPT_ID, RECEIPT_NO, new BigDecimal("30.00"), "分配");
        ArgumentCaptor<ErpArStatementItemDO> allocCaptor = ArgumentCaptor.forClass(ErpArStatementItemDO.class);
        verify(arStatementItemMapper, org.mockito.Mockito.times(1)).insert(allocCaptor.capture());
        assertEquals(ErpArStatementItemTypeEnum.RECEIPT_ALLOCATED.getStatus(), allocCaptor.getValue().getItemType());
        assertEquals(new BigDecimal("-30.00"), allocCaptor.getValue().getAmount());
        assertEquals(new BigDecimal("-30.00"), allocCaptor.getValue().getAfterReceivedAmount());
        assertEquals(new BigDecimal("-70.00"), allocCaptor.getValue().getAfterRemainAmount());

        // 回滚：分配明细取反 +30
        arService.createReceiptReturnedItem(ErpBizTypeEnum.SALE_RETURN.getType(), SALE_RETURN_ID,
                RECEIPT_ID, RECEIPT_NO, new BigDecimal("30.00"), "退回");
        ArgumentCaptor<ErpArStatementItemDO> returnCaptor = ArgumentCaptor.forClass(ErpArStatementItemDO.class);
        verify(arStatementItemMapper, org.mockito.Mockito.times(2)).insert(returnCaptor.capture());
        assertEquals(ErpArStatementItemTypeEnum.RECEIPT_RETURNED.getStatus(), returnCaptor.getAllValues().get(1).getItemType());
        assertEquals(new BigDecimal("30.00"), returnCaptor.getAllValues().get(1).getAmount());
    }

    @Test
    void createReceiptFacts_shouldSkipWhenStatementMissing() {
        ErpArStatementServiceImpl arService = new ErpArStatementServiceImpl();
        inject(arService, "erpArStatementMapper", arStatementMapper);
        inject(arService, "erpArStatementItemMapper", arStatementItemMapper);
        inject(arService, "erpFinanceReceiptItemMapper", erpFinanceReceiptItemMapper);
        inject(arService, "erpNoRedisDAO", noRedisDAO);
        inject(arService, "erpCustomerService", erpCustomerService);
        inject(arService, "dualLedgerConfigMapper", dualLedgerConfigMapper);

        when(arStatementMapper.selectByBizTypeAndBizId(any(), anyLong())).thenReturn(null);

        // 无台账（时序差异/历史数据）只告警跳过，不产生孤儿明细
        arService.createReceiptAllocatedItem(ErpBizTypeEnum.SALE_OUT.getType(), SALE_OUT_ID,
                RECEIPT_ID, RECEIPT_NO, new BigDecimal("50.00"), "分配");

        verify(arStatementItemMapper, never()).insert(any(ErpArStatementItemDO.class));
    }

    // ==================== 状态机断言 ====================

    @Test
    void arStatementItemTypeEnum_shouldKeepHistoricalCodes() {
        // 明细类型编码与 erp_ar_statement_item.item_type 历史数据绑定，禁止漂移
        assertEquals(1, ErpArStatementItemTypeEnum.CREATED.getStatus());
        assertEquals(2, ErpArStatementItemTypeEnum.RECEIPT_ALLOCATED.getStatus());
        assertEquals(3, ErpArStatementItemTypeEnum.RECEIPT_RETURNED.getStatus());
        assertEquals(4, ErpArStatementItemTypeEnum.CLOSED.getStatus());
    }

    @Test
    void updateFinanceReceiptStatus_shouldKeepTransactionalEntrySemantics() throws Exception {
        Method method = ErpFinanceReceiptServiceImpl.class.getMethod("updateFinanceReceiptStatus", Long.class, Integer.class);
        assertTrue(!method.isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class),
                "状态入口不应重复声明 @Transactional，事务边界由 executeInRequiredTransaction 统一控制");
    }

    // ==================== 桩与工具 ====================

    private ErpFinanceReceiptDO buildReceipt(Integer status) {
        return new ErpFinanceReceiptDO().setId(RECEIPT_ID).setNo(RECEIPT_NO).setStatus(status);
    }

    private ErpFinanceReceiptItemDO buildSaleOutItem(BigDecimal receiptPrice) {
        return new ErpFinanceReceiptItemDO()
                .setReceiptId(RECEIPT_ID)
                .setBizType(ErpBizTypeEnum.SALE_OUT.getType())
                .setBizId(SALE_OUT_ID)
                .setBizNo("XC-001")
                .setReceiptPrice(receiptPrice);
    }

    private ErpFinanceReceiptItemDO buildSaleReturnItem(BigDecimal receiptPrice) {
        return new ErpFinanceReceiptItemDO()
                .setReceiptId(RECEIPT_ID)
                .setBizType(ErpBizTypeEnum.SALE_RETURN.getType())
                .setBizId(SALE_RETURN_ID)
                .setBizNo("TH-001")
                .setReceiptPrice(receiptPrice);
    }

    private void stubCommon(ErpFinanceReceiptDO receipt, ErpFinanceReceiptItemDO item) {
        when(erpFinanceReceiptMapper.selectById(RECEIPT_ID)).thenReturn(receipt);
        when(erpFinanceReceiptMapper.updateByIdAndStatus(eq(RECEIPT_ID), eq(receipt.getStatus()), any())).thenReturn(1);
        when(erpFinanceReceiptItemMapper.selectListByReceiptId(RECEIPT_ID)).thenReturn(List.of(item));
        // lenient：反审核路径不走 reValidate，退货路径不查销售出库，避免 STRICT_STUBS 误报
        lenient().when(saleOutService.validateSaleOut(SALE_OUT_ID)).thenReturn(new ErpSaleOutDO()
                .setId(SALE_OUT_ID).setOrderId(99L).setTotalPrice(new BigDecimal("100.00")));
    }

    private void stubLockSuccess() {
        lenient().when(redissonClient.getLock(org.mockito.ArgumentMatchers.anyString())).thenReturn(bizLock);
        lenient().when(bizLock.tryLock()).thenReturn(true);
        lenient().when(bizLock.isHeldByCurrentThread()).thenReturn(true);
    }

    /**
     * mock 事务管理器：getTransaction 返回活跃状态，execute 回调直接执行，commit 记录
     */
    private void stubTxExecuteRuns() {
        when(transactionManager.getTransaction(any(TransactionDefinition.class))).thenReturn(new SimpleTransactionStatus());
    }

    /**
     * mock 事务管理器：回调抛异常时执行 rollback（等价 TransactionTemplate.rollbackOnException）
     */
    private void stubTxRollbackOnException() {
        when(transactionManager.getTransaction(any(TransactionDefinition.class))).thenReturn(new SimpleTransactionStatus());
    }

    private void inject(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("inject field failed: " + fieldName, e);
        }
    }

}
