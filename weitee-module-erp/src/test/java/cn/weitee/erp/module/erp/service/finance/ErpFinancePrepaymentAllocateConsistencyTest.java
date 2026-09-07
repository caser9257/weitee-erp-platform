package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.prepayment.ErpFinancePrepaymentAllocateReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.prepayment.ErpFinancePrepaymentRollbackReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePrepaymentAllocateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePrepaymentDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpApStatementItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePrepaymentAllocateMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinancePrepaymentMapper;
import cn.weitee.erp.module.erp.enums.ErpApStatementItemTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpApStatementStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpFinancePrepaymentAllocateStatusEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
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
import java.util.ArrayList;
import java.util.List;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.AP_STATEMENT_ALLOCATE_AMOUNT_EXCEED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PREPAYMENT_ALLOCATE_AMOUNT_EXCEED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PREPAYMENT_ALLOCATE_FAIL_APPROVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * P2 预备：预付款核销/回滚与 AP 台账一致性测试。
 * 覆盖：核销写入链、台账超额、预付余额超额、未审核阻断、回滚幂等（重复执行）、锁共用与事务回滚。
 */
@ExtendWith(MockitoExtension.class)
class ErpFinancePrepaymentAllocateConsistencyTest {

    @InjectMocks
    private ErpFinancePrepaymentServiceImpl service;

    @Mock
    private ErpFinancePrepaymentMapper erpFinancePrepaymentMapper;
    @Mock
    private ErpFinancePrepaymentAllocateMapper erpFinancePrepaymentAllocateMapper;
    @Mock
    private ErpApStatementItemMapper erpApStatementItemMapper;
    @Mock
    private ErpApStatementService apStatementService;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RLock rLock;
    @Mock
    private PlatformTransactionManager transactionManager;

    private ErpFinancePrepaymentDO prepayment(Long id, Integer status, String remainPrice) {
        return new ErpFinancePrepaymentDO().setId(id).setNo("YF-" + id).setSupplierId(9L)
                .setStatus(status)
                .setPrepaymentPrice(new BigDecimal("100.00"))
                .setAllocatedPrice(BigDecimal.ZERO)
                .setRemainPrice(new BigDecimal(remainPrice));
    }

    private ErpApStatementDO statement(Long id, String amount, String remain) {
        return new ErpApStatementDO().setId(id).setStatementNo("AP-" + id)
                .setBizType(ErpBizTypeEnum.PURCHASE_IN.getType()).setBizId(id).setBizNo("PI-" + id)
                .setSupplierId(9L).setAmount(new BigDecimal(amount)).setPaidAmount(BigDecimal.ZERO)
                .setRemainAmount(new BigDecimal(remain))
                .setStatus(ErpApStatementStatusEnum.UNPAID.getStatus());
    }

    private ErpFinancePrepaymentAllocateReqVO allocateReqVO(Long prepaymentId, Long statementId, String amount) {
        return new ErpFinancePrepaymentAllocateReqVO().setPrepaymentId(prepaymentId)
                .setItems(List.of(new ErpFinancePrepaymentAllocateReqVO.Item()
                        .setApStatementId(statementId).setAllocateAmount(new BigDecimal(amount)).setRemark("核销")));
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

    private void mockTransaction() {
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    }

    @Test
    void allocateFinancePrepayment_shouldWriteAllocateRefreshStatementAndLog() {
        ErpFinancePrepaymentDO prepayment = prepayment(7L, ErpAuditStatus.APPROVE.getStatus(), "100.00");
        ErpApStatementDO statement = statement(11L, "80.00", "80.00");
        mockLockAcquired();
        mockTransaction();
        when(erpFinancePrepaymentMapper.selectById(7L)).thenReturn(prepayment);
        when(apStatementService.validateApStatement(11L)).thenReturn(statement);
        when(apStatementService.getApStatementListByIds(anyCollection()))
                .thenReturn(List.of(statement.setPaidAmount(new BigDecimal("20.00")).setRemainAmount(new BigDecimal("60.00"))));

        service.allocateFinancePrepayment(allocateReqVO(7L, 11L, "20.00"));

        // 锁：预付款单锁 + 与付款共用的台账锁
        verify(redissonClient).getLock("erp:finance-prepayment:allocate:7");
        verify(redissonClient).getLock(ErpApStatementService.allocateLockKey(11L));
        ArgumentCaptor<List<ErpFinancePrepaymentAllocateDO>> insertCaptor = ArgumentCaptor.forClass(List.class);
        verify(erpFinancePrepaymentAllocateMapper).insertBatch(insertCaptor.capture());
        ErpFinancePrepaymentAllocateDO allocate = insertCaptor.getValue().getFirst();
        assertEquals(7L, allocate.getPrepaymentId());
        assertEquals(11L, allocate.getApStatementId());
        assertEquals(new BigDecimal("20.00"), allocate.getAllocateAmount());
        assertEquals(ErpFinancePrepaymentAllocateStatusEnum.APPROVED.getStatus(), allocate.getStatus());
        // 刷新链：预付余额、台账金额、业务汇总、台账流水
        verify(erpFinancePrepaymentMapper).updateById(any(ErpFinancePrepaymentDO.class));
        verify(apStatementService).refreshStatementAmountByIds(List.of(11L));
        verify(apStatementService).refreshBizSummaryByStatementIds(List.of(11L));
        ArgumentCaptor<ErpApStatementItemDO> logCaptor = ArgumentCaptor.forClass(ErpApStatementItemDO.class);
        verify(erpApStatementItemMapper).insert(logCaptor.capture());
        assertEquals(ErpApStatementItemTypeEnum.PREPAYMENT_ALLOCATED.getStatus(), logCaptor.getValue().getItemType());
        assertEquals(new BigDecimal("20.00"), logCaptor.getValue().getAmount());
        assertEquals(new BigDecimal("60.00"), logCaptor.getValue().getAfterRemainAmount());
        verify(transactionManager).commit(any(TransactionStatus.class));
        verify(rLock, org.mockito.Mockito.times(2)).unlock();
    }

    @Test
    void allocateFinancePrepayment_shouldRejectWhenExceedStatementRemainAmount() {
        mockLockAcquired();
        mockTransaction();
        when(erpFinancePrepaymentMapper.selectById(7L))
                .thenReturn(prepayment(7L, ErpAuditStatus.APPROVE.getStatus(), "100.00"));
        when(apStatementService.validateApStatement(11L)).thenReturn(statement(11L, "80.00", "10.00"));

        ServiceException ex = assertThrows(ServiceException.class, () ->
                service.allocateFinancePrepayment(allocateReqVO(7L, 11L, "20.00")));

        assertEquals(AP_STATEMENT_ALLOCATE_AMOUNT_EXCEED.getCode(), ex.getCode());
        verify(erpFinancePrepaymentAllocateMapper, never()).insertBatch(any());
        verify(transactionManager).rollback(any(TransactionStatus.class));
        verify(rLock, org.mockito.Mockito.times(2)).unlock();
    }

    @Test
    void allocateFinancePrepayment_shouldRejectWhenExceedPrepaymentRemainPrice() {
        mockLockAcquired();
        mockTransaction();
        when(erpFinancePrepaymentMapper.selectById(7L))
                .thenReturn(prepayment(7L, ErpAuditStatus.APPROVE.getStatus(), "10.00"));
        when(apStatementService.validateApStatement(11L)).thenReturn(statement(11L, "100.00", "100.00"));

        ServiceException ex = assertThrows(ServiceException.class, () ->
                service.allocateFinancePrepayment(allocateReqVO(7L, 11L, "20.00")));

        assertEquals(PREPAYMENT_ALLOCATE_AMOUNT_EXCEED.getCode(), ex.getCode());
        verify(erpFinancePrepaymentAllocateMapper, never()).insertBatch(any());
        verify(apStatementService, never()).refreshStatementAmountByIds(anyCollection());
        verify(rLock, org.mockito.Mockito.times(2)).unlock();
    }

    @Test
    void allocateFinancePrepayment_shouldRejectWhenPrepaymentNotApproved() {
        mockLockAcquired();
        mockTransaction();
        when(erpFinancePrepaymentMapper.selectById(7L))
                .thenReturn(prepayment(7L, ErpAuditStatus.PROCESS.getStatus(), "100.00"));

        ServiceException ex = assertThrows(ServiceException.class, () ->
                service.allocateFinancePrepayment(allocateReqVO(7L, 11L, "20.00")));

        assertEquals(PREPAYMENT_ALLOCATE_FAIL_APPROVE.getCode(), ex.getCode());
        verify(erpFinancePrepaymentAllocateMapper, never()).insertBatch(any());
        verify(rLock, org.mockito.Mockito.times(2)).unlock();
    }

    @Test
    void rollbackFinancePrepaymentAllocate_shouldCancelRefreshAndLogNegative() {
        ErpFinancePrepaymentDO prepayment = prepayment(7L, ErpAuditStatus.APPROVE.getStatus(), "80.00");
        List<ErpFinancePrepaymentAllocateDO> rows = new ArrayList<>(List.of(
                new ErpFinancePrepaymentAllocateDO().setId(701L).setPrepaymentId(7L).setApStatementId(11L)
                        .setAllocateAmount(new BigDecimal("20.00"))
                        .setStatus(ErpFinancePrepaymentAllocateStatusEnum.APPROVED.getStatus())));
        mockLockAcquired();
        mockTransaction();
        when(erpFinancePrepaymentAllocateMapper.selectByIds(any())).thenReturn(rows);
        when(erpFinancePrepaymentMapper.selectById(7L)).thenReturn(prepayment);
        when(erpFinancePrepaymentAllocateMapper.updateById(any(ErpFinancePrepaymentAllocateDO.class)))
                .thenAnswer(invocation -> {
                    ErpFinancePrepaymentAllocateDO update = invocation.getArgument(0);
                    rows.stream().filter(row -> row.getId().equals(update.getId()))
                            .forEach(row -> row.setStatus(update.getStatus()));
                    return 1;
                });
        when(erpFinancePrepaymentAllocateMapper.selectApprovedListByPrepaymentId(7L)).thenReturn(List.of());
        when(apStatementService.getApStatementListByIds(anyCollection()))
                .thenReturn(List.of(statement(11L, "80.00", "80.00")));

        service.rollbackFinancePrepaymentAllocate(new ErpFinancePrepaymentRollbackReqVO().setIds(List.of(701L)));

        verify(redissonClient).getLock(ErpApStatementService.allocateLockKey(11L));
        assertEquals(ErpFinancePrepaymentAllocateStatusEnum.CANCELED.getStatus(), rows.getFirst().getStatus());
        verify(apStatementService).refreshStatementAmountByIds(List.of(11L));
        verify(apStatementService).refreshBizSummaryByStatementIds(List.of(11L));
        ArgumentCaptor<ErpApStatementItemDO> logCaptor = ArgumentCaptor.forClass(ErpApStatementItemDO.class);
        verify(erpApStatementItemMapper).insert(logCaptor.capture());
        assertEquals(ErpApStatementItemTypeEnum.PREPAYMENT_ALLOCATE_ROLLBACK.getStatus(), logCaptor.getValue().getItemType());
        assertEquals(new BigDecimal("-20.00"), logCaptor.getValue().getAmount());
        verify(rLock, org.mockito.Mockito.times(2)).unlock();
    }

    @Test
    void rollbackFinancePrepaymentAllocate_shouldBeIdempotentOnDuplicateExecution() {
        ErpFinancePrepaymentDO prepayment = prepayment(7L, ErpAuditStatus.APPROVE.getStatus(), "80.00");
        List<ErpFinancePrepaymentAllocateDO> rows = new ArrayList<>(List.of(
                new ErpFinancePrepaymentAllocateDO().setId(702L).setPrepaymentId(7L).setApStatementId(12L)
                        .setAllocateAmount(new BigDecimal("20.00"))
                        .setStatus(ErpFinancePrepaymentAllocateStatusEnum.APPROVED.getStatus())));
        mockLockAcquired();
        mockTransaction();
        when(erpFinancePrepaymentAllocateMapper.selectByIds(any())).thenReturn(rows);
        when(erpFinancePrepaymentMapper.selectById(7L)).thenReturn(prepayment);
        when(erpFinancePrepaymentAllocateMapper.updateById(any(ErpFinancePrepaymentAllocateDO.class)))
                .thenAnswer(invocation -> {
                    ErpFinancePrepaymentAllocateDO update = invocation.getArgument(0);
                    rows.stream().filter(row -> row.getId().equals(update.getId()))
                            .forEach(row -> row.setStatus(update.getStatus()));
                    return 1;
                });
        when(erpFinancePrepaymentAllocateMapper.selectApprovedListByPrepaymentId(7L)).thenReturn(List.of());
        when(apStatementService.getApStatementListByIds(anyCollection()))
                .thenReturn(List.of(statement(12L, "80.00", "80.00")));
        ErpFinancePrepaymentRollbackReqVO reqVO = new ErpFinancePrepaymentRollbackReqVO().setIds(List.of(702L));

        service.rollbackFinancePrepaymentAllocate(reqVO);
        // 第二次重复回滚：核销事实已是 CANCELED，不允许再次取消、再次刷新或再次写流水
        service.rollbackFinancePrepaymentAllocate(reqVO);

        verify(erpFinancePrepaymentAllocateMapper, org.mockito.Mockito.times(1)).updateById(any(ErpFinancePrepaymentAllocateDO.class));
        verify(apStatementService, org.mockito.Mockito.times(1)).refreshStatementAmountByIds(anyCollection());
        verify(erpApStatementItemMapper, org.mockito.Mockito.times(1)).insert(any(ErpApStatementItemDO.class));
    }

    @Test
    void rollbackFinancePrepaymentAllocate_shouldSkipWhenAllocateNotFound() {
        when(erpFinancePrepaymentAllocateMapper.selectByIds(any())).thenReturn(List.of());

        service.rollbackFinancePrepaymentAllocate(new ErpFinancePrepaymentRollbackReqVO().setIds(List.of(999L)));

        verifyNoInteractions(redissonClient, transactionManager, apStatementService);
    }

    @Test
    void allocateFinancePrepayment_shouldRollbackTransactionAndReleaseLockWhenRefreshFails() {
        ErpFinancePrepaymentDO prepayment = prepayment(7L, ErpAuditStatus.APPROVE.getStatus(), "100.00");
        mockLockAcquired();
        mockTransaction();
        when(erpFinancePrepaymentMapper.selectById(7L)).thenReturn(prepayment);
        when(apStatementService.validateApStatement(11L)).thenReturn(statement(11L, "80.00", "80.00"));
        when(erpFinancePrepaymentAllocateMapper.selectApprovedListByPrepaymentId(7L)).thenReturn(List.of());
        org.mockito.Mockito.doThrow(new IllegalStateException("refresh failed"))
                .when(apStatementService).refreshStatementAmountByIds(anyCollection());

        assertThrows(IllegalStateException.class, () ->
                service.allocateFinancePrepayment(allocateReqVO(7L, 11L, "20.00")));

        // 事务失败必须整体回滚：核销事实、预付余额、台账流水都不允许单独生效
        verify(transactionManager).rollback(any(TransactionStatus.class));
        verify(transactionManager, never()).commit(any(TransactionStatus.class));
        verify(erpApStatementItemMapper, never()).insert(any(ErpApStatementItemDO.class));
        verify(rLock, org.mockito.Mockito.times(2)).unlock();
    }

}
