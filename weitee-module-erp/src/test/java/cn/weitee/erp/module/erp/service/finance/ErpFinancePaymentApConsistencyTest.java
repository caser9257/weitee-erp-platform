package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePaymentAllocateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePaymentDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePaymentItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpApStatementItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePaymentAllocateMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePaymentItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePaymentMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.weitee.erp.module.erp.enums.ErpApStatementItemTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpApStatementStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpFinancePaymentAllocateStatusEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseOrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.AP_STATEMENT_ALLOCATE_AMOUNT_EXCEED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * P2 预备：付款审批/作废/BPM 撤回与 AP 台账一致性测试。
 * 覆盖：部分付款、超额核销、重复回调、作废释放核销、作废锁与事务回滚、撤回不触碰核销事实。
 */
@ExtendWith(MockitoExtension.class)
class ErpFinancePaymentApConsistencyTest {

    @InjectMocks
    private ErpFinancePaymentServiceImpl service;

    @Mock
    private ErpFinancePaymentMapper erpFinancePaymentMapper;
    @Mock
    private ErpFinancePaymentItemMapper erpFinancePaymentItemMapper;
    @Mock
    private ErpFinancePaymentAllocateMapper erpFinancePaymentAllocateMapper;
    @Mock
    private ErpApStatementItemMapper erpApStatementItemMapper;
    @Mock
    private ErpApStatementService apStatementService;
    @Mock
    private ErpPurchaseInMapper purchaseInMapper;
    @Mock
    private ErpPurchaseOrderService purchaseOrderService;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RLock rLock;
    @Mock
    private PlatformTransactionManager transactionManager;

    private ErpFinancePaymentDO payment(Long id, Integer status, String processInstanceId) {
        return new ErpFinancePaymentDO().setId(id).setNo("FP-" + id).setStatus(status)
                .setProcessInstanceId(processInstanceId).setSupplierId(201L);
    }

    private ErpFinancePaymentItemDO paymentItem(Long id, Long paymentId, Long statementId, String amount) {
        return new ErpFinancePaymentItemDO().setId(id).setPaymentId(paymentId)
                .setApStatementId(statementId).setBizType(ErpBizTypeEnum.PURCHASE_IN.getType())
                .setBizId(statementId).setBizNo("PI-" + statementId)
                .setPaymentPrice(new BigDecimal(amount));
    }

    private ErpApStatementDO statement(Long id, String amount, String remain) {
        return new ErpApStatementDO().setId(id).setStatementNo("AP-" + id)
                .setBizType(ErpBizTypeEnum.PURCHASE_IN.getType()).setBizId(id).setBizNo("PI-" + id)
                .setSupplierId(201L).setAmount(new BigDecimal(amount)).setPaidAmount(BigDecimal.ZERO)
                .setRemainAmount(new BigDecimal(remain))
                .setStatus(ErpApStatementStatusEnum.UNPAID.getStatus());
    }

    private void mockTransactionCommit() {
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    }

    private void mockLockAcquired() {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        try {
            when(rLock.tryLock(anyLong(), any(java.util.concurrent.TimeUnit.class))).thenReturn(true);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
    }

    @Test
    void updateFinancePaymentStatusByBpm_shouldRejectOverAllocateBeforeStatusCas() {
        ErpFinancePaymentDO payment = payment(11L, ErpAuditStatus.PROCESS.getStatus(), "PI-11");
        ErpFinancePaymentItemDO item = paymentItem(111L, 11L, 21L, "100.00");
        when(erpFinancePaymentMapper.selectById(11L)).thenReturn(payment);
        when(erpFinancePaymentItemMapper.selectListByPaymentId(11L)).thenReturn(List.of(item));
        mockLockAcquired();
        mockTransactionCommit();
        when(apStatementService.validateApStatement(21L)).thenReturn(statement(21L, "100.00", "60.00"));

        ServiceException ex = assertThrows(ServiceException.class, () ->
                service.updateFinancePaymentStatusByBpm(11L, "PI-11", ErpAuditStatus.APPROVE.getStatus(), "ok"));

        assertEquals(AP_STATEMENT_ALLOCATE_AMOUNT_EXCEED.getCode(), ex.getCode());
        // 超额核销必须在状态 CAS 之前阻断，不允许留下已审核但无核销事实的半条 AP 事实
        verify(erpFinancePaymentMapper, never()).updateByIdAndStatus(anyLong(), any(), any());
        verify(erpFinancePaymentAllocateMapper, never()).insert(any(ErpFinancePaymentAllocateDO.class));
        verify(transactionManager).rollback(any(TransactionStatus.class));
        verify(rLock).unlock();
    }

    @Test
    void updateFinancePaymentStatusByBpm_shouldCreatePartialAllocateAndRefreshChain() {
        ErpFinancePaymentDO payment = payment(12L, ErpAuditStatus.PROCESS.getStatus(), "PI-12");
        ErpFinancePaymentItemDO item = paymentItem(112L, 12L, 22L, "40.00");
        ErpApStatementDO statement = statement(22L, "100.00", "100.00");
        when(erpFinancePaymentMapper.selectById(12L)).thenReturn(payment);
        when(erpFinancePaymentItemMapper.selectListByPaymentId(12L)).thenReturn(List.of(item));
        mockLockAcquired();
        mockTransactionCommit();
        when(apStatementService.validateApStatement(22L)).thenReturn(statement);
        when(erpFinancePaymentMapper.updateByIdAndStatus(eq(12L), eq(ErpAuditStatus.PROCESS.getStatus()), any()))
                .thenReturn(1);
        when(apStatementService.getApStatementListByIds(anyCollection()))
                .thenReturn(List.of(statement.setPaidAmount(new BigDecimal("40.00")).setRemainAmount(new BigDecimal("60.00"))));
        when(purchaseInMapper.selectByIds(any())).thenReturn(List.of(new ErpPurchaseInDO().setId(22L).setOrderId(322L)));

        service.updateFinancePaymentStatusByBpm(12L, "PI-12", ErpAuditStatus.APPROVE.getStatus(), "ok");

        ArgumentCaptor<ErpFinancePaymentAllocateDO> allocateCaptor =
                ArgumentCaptor.forClass(ErpFinancePaymentAllocateDO.class);
        verify(erpFinancePaymentAllocateMapper).insert(allocateCaptor.capture());
        assertEquals(new BigDecimal("40.00"), allocateCaptor.getValue().getAllocateAmount());
        assertEquals(ErpFinancePaymentAllocateStatusEnum.APPROVED.getStatus(), allocateCaptor.getValue().getStatus());
        assertEquals(22L, allocateCaptor.getValue().getApStatementId());
        verify(apStatementService).refreshStatementAmountByIds(List.of(22L));
        verify(apStatementService).refreshBizSummaryByStatementIds(List.of(22L));
        ArgumentCaptor<ErpApStatementItemDO> logCaptor = ArgumentCaptor.forClass(ErpApStatementItemDO.class);
        verify(erpApStatementItemMapper).insert(logCaptor.capture());
        assertEquals(ErpApStatementItemTypeEnum.PAYMENT_ALLOCATED.getStatus(), logCaptor.getValue().getItemType());
        assertEquals(new BigDecimal("40.00"), logCaptor.getValue().getAmount());
        assertEquals(new BigDecimal("60.00"), logCaptor.getValue().getAfterRemainAmount());
        // 采购订单回写：批量查询一次 + 每个订单一次
        verify(purchaseInMapper, times(1)).selectByIds(any());
        verify(purchaseOrderService).updatePurchaseOrderPaymentPrice(322L);
        verify(transactionManager).commit(any(TransactionStatus.class));
        verify(rLock).unlock();
    }

    @Test
    void updateFinancePaymentStatusByBpm_shouldIgnoreDuplicateApproveCallback() {
        // 已审核的付款单收到重复 APPROVE 回调：只允许一次核销事实，重复回调必须被忽略
        when(erpFinancePaymentMapper.selectById(13L))
                .thenReturn(payment(13L, ErpAuditStatus.APPROVE.getStatus(), "PI-13"));

        service.updateFinancePaymentStatusByBpm(13L, "PI-13", ErpAuditStatus.APPROVE.getStatus(), "ok");

        verify(erpFinancePaymentMapper, never()).updateByIdAndStatus(anyLong(), any(), any());
        verify(erpFinancePaymentAllocateMapper, never()).insert(any(ErpFinancePaymentAllocateDO.class));
        verifyNoInteractions(redissonClient, transactionManager);
    }

    @Test
    void updateFinancePaymentStatusByBpm_shouldRejectCallbackWithMismatchedProcessInstance() {
        when(erpFinancePaymentMapper.selectById(14L))
                .thenReturn(payment(14L, ErpAuditStatus.PROCESS.getStatus(), "PI-BOUND"));

        assertThrows(ServiceException.class, () ->
                service.updateFinancePaymentStatusByBpm(14L, "PI-OTHER", ErpAuditStatus.APPROVE.getStatus(), "ok"));

        verify(erpFinancePaymentMapper, never()).updateByIdAndStatus(anyLong(), any(), any());
        verifyNoInteractions(redissonClient, transactionManager);
    }

    @Test
    void voidFinancePayment_shouldShareApStatementLockAndReleaseAllocate() {
        ErpFinancePaymentDO payment = payment(15L, ErpAuditStatus.APPROVE.getStatus(), "PI-15");
        ErpFinancePaymentItemDO item = paymentItem(115L, 15L, 25L, "100.00");
        ErpFinancePaymentAllocateDO approved = new ErpFinancePaymentAllocateDO()
                .setId(1501L).setPaymentId(15L).setApStatementId(25L)
                .setAllocateAmount(new BigDecimal("100.00"))
                .setStatus(ErpFinancePaymentAllocateStatusEnum.APPROVED.getStatus());
        when(erpFinancePaymentMapper.selectById(15L)).thenReturn(payment);
        when(erpFinancePaymentItemMapper.selectListByPaymentId(15L)).thenReturn(List.of(item));
        mockLockAcquired();
        mockTransactionCommit();
        when(erpFinancePaymentMapper.updateByIdAndStatus(eq(15L), eq(ErpAuditStatus.APPROVE.getStatus()), any()))
                .thenReturn(1);
        when(erpFinancePaymentAllocateMapper.selectListByPaymentId(15L)).thenReturn(List.of(approved));
        ErpApStatementDO refreshed = statement(25L, "100.00", "100.00")
                .setPaidAmount(BigDecimal.ZERO).setRemainAmount(new BigDecimal("100.00"));
        when(apStatementService.getApStatementListByIds(anyCollection())).thenReturn(List.of(refreshed));
        when(purchaseInMapper.selectByIds(any())).thenReturn(List.of(new ErpPurchaseInDO().setId(25L).setOrderId(325L)));

        service.voidFinancePayment(15L, "作废测试");

        // 作废必须使用与审批核销相同的台账锁 key，否则并发下互相看不到对方的写入
        verify(redissonClient).getLock(ErpApStatementService.allocateLockKey(25L));
        ArgumentCaptor<ErpFinancePaymentAllocateDO> cancelCaptor =
                ArgumentCaptor.forClass(ErpFinancePaymentAllocateDO.class);
        verify(erpFinancePaymentAllocateMapper).updateById(cancelCaptor.capture());
        assertEquals(ErpFinancePaymentAllocateStatusEnum.CANCELED.getStatus(), cancelCaptor.getValue().getStatus());
        verify(apStatementService).refreshStatementAmountByIds(List.of(25L));
        verify(apStatementService).refreshBizSummaryByStatementIds(List.of(25L));
        ArgumentCaptor<ErpApStatementItemDO> logCaptor = ArgumentCaptor.forClass(ErpApStatementItemDO.class);
        verify(erpApStatementItemMapper).insert(logCaptor.capture());
        assertEquals(ErpApStatementItemTypeEnum.PAYMENT_ALLOCATE_ROLLBACK.getStatus(), logCaptor.getValue().getItemType());
        assertEquals(new BigDecimal("-100.00"), logCaptor.getValue().getAmount());
        verify(purchaseOrderService).updatePurchaseOrderPaymentPrice(325L);
        verify(rLock).unlock();
    }

    @Test
    void voidFinancePayment_shouldRollbackTransactionAndReleaseLockWhenRefreshFails() {
        ErpFinancePaymentDO payment = payment(16L, ErpAuditStatus.APPROVE.getStatus(), "PI-16");
        ErpFinancePaymentItemDO item = paymentItem(116L, 16L, 26L, "100.00");
        when(erpFinancePaymentMapper.selectById(16L)).thenReturn(payment);
        when(erpFinancePaymentItemMapper.selectListByPaymentId(16L)).thenReturn(List.of(item));
        mockLockAcquired();
        mockTransactionCommit();
        when(erpFinancePaymentMapper.updateByIdAndStatus(eq(16L), eq(ErpAuditStatus.APPROVE.getStatus()), any()))
                .thenReturn(1);
        when(erpFinancePaymentAllocateMapper.selectListByPaymentId(16L)).thenReturn(List.of(
                new ErpFinancePaymentAllocateDO().setId(1601L).setPaymentId(16L).setApStatementId(26L)
                        .setAllocateAmount(new BigDecimal("100.00"))
                        .setStatus(ErpFinancePaymentAllocateStatusEnum.APPROVED.getStatus())));
        org.mockito.Mockito.doThrow(new IllegalStateException("refresh failed"))
                .when(apStatementService).refreshStatementAmountByIds(anyCollection());

        assertThrows(IllegalStateException.class, () -> service.voidFinancePayment(16L, "作废失败测试"));

        // 事务失败必须整体回滚（不留下已作废但未释放核销的半条 AP 事实），且锁必须释放
        verify(transactionManager).rollback(any(TransactionStatus.class));
        verify(transactionManager, never()).commit(any(TransactionStatus.class));
        verify(rLock).unlock();
    }

    @Test
    void voidFinancePayment_shouldRejectWhenLockBusy() {
        ErpFinancePaymentDO payment = payment(17L, ErpAuditStatus.APPROVE.getStatus(), "PI-17");
        when(erpFinancePaymentMapper.selectById(17L)).thenReturn(payment);
        when(erpFinancePaymentItemMapper.selectListByPaymentId(17L))
                .thenReturn(List.of(paymentItem(117L, 17L, 27L, "10.00")));
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        try {
            when(rLock.tryLock(anyLong(), any(java.util.concurrent.TimeUnit.class))).thenReturn(false);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        assertThrows(ServiceException.class, () -> service.voidFinancePayment(17L, "并发作废"));

        verify(erpFinancePaymentMapper, never()).updateByIdAndStatus(anyLong(), any(), any());
        verifyNoInteractions(transactionManager);
    }

    @Test
    void updateFinancePaymentStatus_unapproveShouldShareApStatementLock() {
        // 反审核会取消核销事实并刷新台账，必须与审批/作废共用同一台账锁（code review 固化用例）
        ErpFinancePaymentDO payment = payment(20L, ErpAuditStatus.APPROVE.getStatus(), null);
        ErpFinancePaymentItemDO item = paymentItem(120L, 20L, 28L, "60.00");
        ErpFinancePaymentAllocateDO approved = new ErpFinancePaymentAllocateDO()
                .setId(2001L).setPaymentId(20L).setApStatementId(28L)
                .setAllocateAmount(new BigDecimal("60.00"))
                .setStatus(ErpFinancePaymentAllocateStatusEnum.APPROVED.getStatus());
        when(erpFinancePaymentMapper.selectById(20L)).thenReturn(payment);
        when(erpFinancePaymentItemMapper.selectListByPaymentId(20L)).thenReturn(List.of(item));
        mockLockAcquired();
        mockTransactionCommit();
        when(erpFinancePaymentMapper.updateByIdAndStatus(eq(20L), eq(ErpAuditStatus.APPROVE.getStatus()), any()))
                .thenReturn(1);
        when(erpFinancePaymentAllocateMapper.selectListByPaymentId(20L)).thenReturn(List.of(approved));
        when(apStatementService.getApStatementListByIds(anyCollection()))
                .thenReturn(List.of(statement(28L, "100.00", "40.00")));
        when(purchaseInMapper.selectByIds(any())).thenReturn(List.of(new ErpPurchaseInDO().setId(28L).setOrderId(328L)));

        service.updateFinancePaymentStatus(20L, ErpAuditStatus.PROCESS.getStatus());

        verify(redissonClient).getLock(ErpApStatementService.allocateLockKey(28L));
        ArgumentCaptor<ErpFinancePaymentAllocateDO> cancelCaptor =
                ArgumentCaptor.forClass(ErpFinancePaymentAllocateDO.class);
        verify(erpFinancePaymentAllocateMapper).updateById(cancelCaptor.capture());
        assertEquals(ErpFinancePaymentAllocateStatusEnum.CANCELED.getStatus(), cancelCaptor.getValue().getStatus());
        verify(apStatementService).refreshStatementAmountByIds(List.of(28L));
        verify(rLock).unlock();
    }

    @Test
    void rollbackFinancePaymentStatusToDraftByBpm_shouldOnlyResetStatusWithoutTouchingAllocate() {
        when(erpFinancePaymentMapper.selectById(18L))
                .thenReturn(payment(18L, ErpAuditStatus.PROCESS.getStatus(), "PI-18"));
        when(erpFinancePaymentMapper.resetStatusToDraftByBpm(18L, "PI-18")).thenReturn(1);

        service.rollbackFinancePaymentStatusToDraftByBpm(18L, "PI-18", "撤回");

        verify(erpFinancePaymentMapper).resetStatusToDraftByBpm(18L, "PI-18");
        // 撤回发生在 PROCESS 阶段，核销事实尚未产生，不允许触碰核销与台账刷新
        verifyNoInteractions(erpFinancePaymentAllocateMapper, apStatementService, erpApStatementItemMapper);
    }

    @Test
    void rollbackFinancePaymentStatusToDraftByBpm_shouldRejectCallbackAfterProcessInstanceUnbound() {
        // 撤回成功后 processInstanceId 已清空：重复/迟到的撤回回调因流程实例不再匹配而被拒绝，
        // 不允许再次重置状态（可追踪、无副作用）
        when(erpFinancePaymentMapper.selectById(19L))
                .thenReturn(payment(19L, ErpAuditStatus.DRAFT.getStatus(), null));

        ServiceException ex = assertThrows(ServiceException.class, () ->
                service.rollbackFinancePaymentStatusToDraftByBpm(19L, "PI-19", "重复撤回"));

        assertEquals(cn.weitee.erp.module.erp.enums.ErrorCodeConstants.FINANCE_PAYMENT_STATUS_UPDATE_ILLEGAL.getCode(),
                ex.getCode());
        verify(erpFinancePaymentMapper, never()).resetStatusToDraftByBpm(anyLong(), anyString());
    }

    @Test
    void rollbackFinancePaymentStatusToDraftByBpm_shouldIgnoreCallbackWhenAlreadyDraftWithBoundProcess() {
        // 状态已回退但流程实例仍绑定的迟到回调：忽略且不重复重置
        when(erpFinancePaymentMapper.selectById(20L))
                .thenReturn(payment(20L, ErpAuditStatus.DRAFT.getStatus(), "PI-20"));

        service.rollbackFinancePaymentStatusToDraftByBpm(20L, "PI-20", "迟到撤回");

        verify(erpFinancePaymentMapper, never()).resetStatusToDraftByBpm(anyLong(), anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void refreshChain_shouldReceiveDeduplicatedStatementIds() {
        // 同一台账多行付款时，刷新触发点只传一次台账 id
        ErpFinancePaymentDO payment = payment(30L, ErpAuditStatus.PROCESS.getStatus(), "PI-30");
        when(erpFinancePaymentMapper.selectById(30L)).thenReturn(payment);
        when(erpFinancePaymentItemMapper.selectListByPaymentId(30L)).thenReturn(List.of(
                paymentItem(301L, 30L, 40L, "10.00"), paymentItem(302L, 30L, 40L, "20.00")));
        mockLockAcquired();
        mockTransactionCommit();
        when(apStatementService.validateApStatement(40L)).thenReturn(statement(40L, "100.00", "100.00"));
        when(erpFinancePaymentMapper.updateByIdAndStatus(eq(30L), eq(ErpAuditStatus.PROCESS.getStatus()), any()))
                .thenReturn(1);
        when(apStatementService.getApStatementListByIds(anyCollection())).thenReturn(List.of(statement(40L, "100.00", "70.00")));
        when(purchaseInMapper.selectByIds(any())).thenReturn(List.of(new ErpPurchaseInDO().setId(40L).setOrderId(940L)));

        service.updateFinancePaymentStatusByBpm(30L, "PI-30", ErpAuditStatus.APPROVE.getStatus(), "ok");

        ArgumentCaptor<Collection<Long>> refreshCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(apStatementService).refreshStatementAmountByIds(refreshCaptor.capture());
        assertEquals(List.of(40L), List.copyOf(refreshCaptor.getValue()));
        verify(erpFinancePaymentAllocateMapper, times(2)).insert(any(ErpFinancePaymentAllocateDO.class));
        verify(erpApStatementItemMapper, times(1)).insert(any(ErpApStatementItemDO.class));
    }

}
