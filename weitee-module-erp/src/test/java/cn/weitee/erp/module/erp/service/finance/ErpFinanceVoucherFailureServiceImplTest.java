package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherFailureDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherFailureMapper;
import cn.weitee.erp.module.erp.enums.ErpFinanceVoucherFailureStatusEnum;
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
import org.springframework.transaction.support.SimpleTransactionStatus;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.VOUCHER_FAILURE_CONFIRM_REASON_REQUIRED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.VOUCHER_FAILURE_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.VOUCHER_FAILURE_RETRY_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.VOUCHER_FAILURE_RETRY_LOCK_BUSY;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.VOUCHER_FAILURE_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 凭证生成失败记录 Service 单测：落库去重、重试成功/失败、重复重试、并发锁、人工确认
 */
@ExtendWith(MockitoExtension.class)
class ErpFinanceVoucherFailureServiceImplTest {

    private static final Long FAILURE_ID = 1L;
    private static final Integer BIZ_TYPE = 21;
    private static final Long BIZ_ID = 100L;

    @Mock
    private ErpFinanceVoucherFailureMapper erpFinanceVoucherFailureMapper;
    @Mock
    private ErpFinanceVoucherService financeVoucherService;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RLock retryLock;
    @Mock
    private PlatformTransactionManager transactionManager;

    @InjectMocks
    private ErpFinanceVoucherFailureServiceImpl service;

    // ==================== recordFailure ====================

    @Test
    void recordFailure_shouldInsertPendingRecordWhenNoneExists() {
        when(erpFinanceVoucherFailureMapper.selectPendingByBiz(BIZ_TYPE, BIZ_ID)).thenReturn(null);

        service.recordFailure(BIZ_TYPE, BIZ_ID, "模板缺失", "stack...");

        ArgumentCaptor<ErpFinanceVoucherFailureDO> captor = ArgumentCaptor.forClass(ErpFinanceVoucherFailureDO.class);
        verify(erpFinanceVoucherFailureMapper).insert(captor.capture());
        assertEquals(BIZ_TYPE, captor.getValue().getBizType());
        assertEquals(BIZ_ID, captor.getValue().getBizId());
        assertEquals(ErpFinanceVoucherFailureStatusEnum.PENDING.getStatus(), captor.getValue().getStatus());
        assertEquals(0, captor.getValue().getRetryCount().intValue());
        assertEquals("模板缺失", captor.getValue().getErrorMessage());
    }

    @Test
    void recordFailure_shouldUpdateExistingPendingInsteadOfDuplicateInsert() {
        // 同一业务单据重复失败：只更新错误信息，不产生第二条待重试记录
        ErpFinanceVoucherFailureDO pending = buildFailure(ErpFinanceVoucherFailureStatusEnum.PENDING, 2);
        when(erpFinanceVoucherFailureMapper.selectPendingByBiz(BIZ_TYPE, BIZ_ID)).thenReturn(pending);

        service.recordFailure(BIZ_TYPE, BIZ_ID, "期间已关闭", "stack2...");

        verify(erpFinanceVoucherFailureMapper, never()).insert(any(ErpFinanceVoucherFailureDO.class));
        ArgumentCaptor<ErpFinanceVoucherFailureDO> captor = ArgumentCaptor.forClass(ErpFinanceVoucherFailureDO.class);
        verify(erpFinanceVoucherFailureMapper).updateById(captor.capture());
        assertEquals(FAILURE_ID, captor.getValue().getId());
        assertEquals("期间已关闭", captor.getValue().getErrorMessage());
    }

    // ==================== retry ====================

    @Test
    void retry_shouldCasSuccessWhenVoucherGenerated() {
        stubLockAcquired();
        stubTx();
        when(erpFinanceVoucherFailureMapper.selectById(FAILURE_ID))
                .thenReturn(buildFailure(ErpFinanceVoucherFailureStatusEnum.PENDING, 0));
        when(financeVoucherService.autoGenerateVoucher(BIZ_TYPE, BIZ_ID)).thenReturn(77L);
        when(erpFinanceVoucherFailureMapper.updateStatusByIdAndStatus(eq(FAILURE_ID),
                eq(ErpFinanceVoucherFailureStatusEnum.PENDING.getStatus()), any())).thenReturn(1);

        Long voucherId = service.retryVoucherFailure(FAILURE_ID);

        assertEquals(77L, voucherId);
        verify(erpFinanceVoucherFailureMapper, never()).updateById(any(ErpFinanceVoucherFailureDO.class));
    }

    @Test
    void retry_shouldMarkFailedAndKeepPendingWhenVoucherGenerationThrows() {
        // 制证抛异常（NESTED savepoint 已回滚凭证写入）：记录保持 PENDING、retry_count+1、对外抛业务异常
        stubLockAcquired();
        stubTx();
        when(erpFinanceVoucherFailureMapper.selectById(FAILURE_ID))
                .thenReturn(buildFailure(ErpFinanceVoucherFailureStatusEnum.PENDING, 1));
        when(financeVoucherService.autoGenerateVoucher(BIZ_TYPE, BIZ_ID))
                .thenThrow(new IllegalStateException("科目映射缺失"));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.retryVoucherFailure(FAILURE_ID));

        assertEquals(VOUCHER_FAILURE_RETRY_FAIL.getCode(), ex.getCode());
        ArgumentCaptor<ErpFinanceVoucherFailureDO> captor = ArgumentCaptor.forClass(ErpFinanceVoucherFailureDO.class);
        verify(erpFinanceVoucherFailureMapper).updateById(captor.capture());
        assertEquals(FAILURE_ID, captor.getValue().getId());
        assertEquals(2, captor.getValue().getRetryCount().intValue());
        assertEquals("科目映射缺失", captor.getValue().getErrorMessage());
        // 未置终态
        verify(erpFinanceVoucherFailureMapper, never()).updateStatusByIdAndStatus(anyLong(), anyInt(), any());
    }

    @Test
    void retry_shouldMarkFailedWhenNoTemplateGeneratedNull() {
        // autoGenerateVoucher 返回 null（无模板）：视为重试失败，保持 PENDING 等待配置修复
        stubLockAcquired();
        stubTx();
        when(erpFinanceVoucherFailureMapper.selectById(FAILURE_ID))
                .thenReturn(buildFailure(ErpFinanceVoucherFailureStatusEnum.PENDING, 0));
        when(financeVoucherService.autoGenerateVoucher(BIZ_TYPE, BIZ_ID)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.retryVoucherFailure(FAILURE_ID));

        assertEquals(VOUCHER_FAILURE_RETRY_FAIL.getCode(), ex.getCode());
        verify(erpFinanceVoucherFailureMapper).updateById(any(ErpFinanceVoucherFailureDO.class));
    }

    @Test
    void retry_shouldRejectNonPendingRecord() {
        // 重复重试：已成功的记录不允许再次重试，不触碰制证（锁内事务中做权威状态校验）
        stubLockAcquired();
        stubTx();
        when(erpFinanceVoucherFailureMapper.selectById(FAILURE_ID))
                .thenReturn(buildFailure(ErpFinanceVoucherFailureStatusEnum.SUCCESS, 1));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.retryVoucherFailure(FAILURE_ID));

        assertEquals(VOUCHER_FAILURE_STATUS_INVALID.getCode(), ex.getCode());
        verify(financeVoucherService, never()).autoGenerateVoucher(any(), any());
    }

    @Test
    void retry_shouldFailFastWhenLockBusy() {
        // 并发重试同一业务单据：tryLock 失败快速拒绝，不进入事务
        when(erpFinanceVoucherFailureMapper.selectById(FAILURE_ID))
                .thenReturn(buildFailure(ErpFinanceVoucherFailureStatusEnum.PENDING, 0));
        when(redissonClient.getLock(anyString())).thenReturn(retryLock);
        when(retryLock.tryLock()).thenReturn(false);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.retryVoucherFailure(FAILURE_ID));

        assertEquals(VOUCHER_FAILURE_RETRY_LOCK_BUSY.getCode(), ex.getCode());
        verify(financeVoucherService, never()).autoGenerateVoucher(any(), any());
    }

    @Test
    void retry_shouldTolerateCasMissFromConcurrentConfirm() {
        // 凭证已生成（幂等），但记录被并发确认：CAS 0 行只告警，不覆盖终态、不报错
        stubLockAcquired();
        stubTx();
        when(erpFinanceVoucherFailureMapper.selectById(FAILURE_ID))
                .thenReturn(buildFailure(ErpFinanceVoucherFailureStatusEnum.PENDING, 0));
        when(financeVoucherService.autoGenerateVoucher(BIZ_TYPE, BIZ_ID)).thenReturn(88L);
        when(erpFinanceVoucherFailureMapper.updateStatusByIdAndStatus(eq(FAILURE_ID),
                eq(ErpFinanceVoucherFailureStatusEnum.PENDING.getStatus()), any())).thenReturn(0);

        assertEquals(88L, service.retryVoucherFailure(FAILURE_ID));
    }

    @Test
    void retry_shouldThrowNotExistsForUnknownRecord() {
        when(erpFinanceVoucherFailureMapper.selectById(FAILURE_ID)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.retryVoucherFailure(FAILURE_ID));

        assertEquals(VOUCHER_FAILURE_NOT_EXISTS.getCode(), ex.getCode());
    }

    // ==================== confirm ====================

    @Test
    void confirm_shouldCasPendingToConfirmed() {
        when(erpFinanceVoucherFailureMapper.selectById(FAILURE_ID))
                .thenReturn(buildFailure(ErpFinanceVoucherFailureStatusEnum.PENDING, 3));
        when(erpFinanceVoucherFailureMapper.updateStatusByIdAndStatus(eq(FAILURE_ID),
                eq(ErpFinanceVoucherFailureStatusEnum.PENDING.getStatus()), any())).thenReturn(1);

        service.confirmVoucherFailure(FAILURE_ID, "单据已反审核，无需补凭证");

        verify(erpFinanceVoucherFailureMapper).updateStatusByIdAndStatus(eq(FAILURE_ID),
                eq(ErpFinanceVoucherFailureStatusEnum.PENDING.getStatus()), any());
    }

    @Test
    void confirm_shouldRequireReason() {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.confirmVoucherFailure(FAILURE_ID, "  "));

        assertEquals(VOUCHER_FAILURE_CONFIRM_REASON_REQUIRED.getCode(), ex.getCode());
        verify(erpFinanceVoucherFailureMapper, never()).updateStatusByIdAndStatus(anyLong(), anyInt(), any());
    }

    @Test
    void confirm_shouldRejectWhenCasMiss() {
        // 并发场景：确认与重试同时到达，CAS 失败方收到明确状态错误
        when(erpFinanceVoucherFailureMapper.selectById(FAILURE_ID))
                .thenReturn(buildFailure(ErpFinanceVoucherFailureStatusEnum.PENDING, 0));
        when(erpFinanceVoucherFailureMapper.updateStatusByIdAndStatus(eq(FAILURE_ID),
                eq(ErpFinanceVoucherFailureStatusEnum.PENDING.getStatus()), any())).thenReturn(0);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.confirmVoucherFailure(FAILURE_ID, "关闭"));

        assertEquals(VOUCHER_FAILURE_STATUS_INVALID.getCode(), ex.getCode());
    }

    // ==================== 状态机定义断言 ====================

    @Test
    void failureStatusEnum_shouldKeepContractCodes() {
        // 状态编码与 erp_finance_voucher_failure.status 列注释绑定，禁止漂移
        assertEquals(0, ErpFinanceVoucherFailureStatusEnum.PENDING.getStatus());
        assertEquals(1, ErpFinanceVoucherFailureStatusEnum.SUCCESS.getStatus());
        assertEquals(2, ErpFinanceVoucherFailureStatusEnum.CONFIRMED.getStatus());
    }

    // ==================== 桩 ====================

    private ErpFinanceVoucherFailureDO buildFailure(ErpFinanceVoucherFailureStatusEnum status, int retryCount) {
        return new ErpFinanceVoucherFailureDO()
                .setId(FAILURE_ID)
                .setBizType(BIZ_TYPE)
                .setBizId(BIZ_ID)
                .setStatus(status.getStatus())
                .setRetryCount(retryCount);
    }

    private void stubLockAcquired() {
        when(redissonClient.getLock(anyString())).thenReturn(retryLock);
        when(retryLock.tryLock()).thenReturn(true);
        lenient().when(retryLock.isHeldByCurrentThread()).thenReturn(true);
    }

    private void stubTx() {
        when(transactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(new SimpleTransactionStatus());
    }

}
