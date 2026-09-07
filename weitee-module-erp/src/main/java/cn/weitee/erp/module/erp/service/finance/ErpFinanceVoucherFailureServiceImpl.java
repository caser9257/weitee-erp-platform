package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucherfailure.ErpFinanceVoucherFailurePageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherFailureDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceVoucherFailureMapper;
import cn.weitee.erp.module.erp.enums.ErpFinanceVoucherFailureStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.VOUCHER_FAILURE_CONFIRM_REASON_REQUIRED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.VOUCHER_FAILURE_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.VOUCHER_FAILURE_RETRY_FAIL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.VOUCHER_FAILURE_RETRY_LOCK_BUSY;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.VOUCHER_FAILURE_STATUS_INVALID;

/**
 * ERP 凭证生成失败记录 Service 实现类
 *
 * @author WeTai
 */
@Service
@Validated
@Slf4j
public class ErpFinanceVoucherFailureServiceImpl implements ErpFinanceVoucherFailureService {

    private static final int ERROR_MESSAGE_MAX_LENGTH = 1000;
    private static final int ERROR_STACK_MAX_LENGTH = 2000;

    @Resource
    private ErpFinanceVoucherFailureMapper erpFinanceVoucherFailureMapper;
    @Resource
    private ErpFinanceVoucherService financeVoucherService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private PlatformTransactionManager transactionManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordFailure(Integer bizType, Long bizId, String errorMessage, String errorStack) {
        // 加入调用方（业务审核）事务：业务回滚则失败记录一并回滚，保证记录只对应已生效的审核
        ErpFinanceVoucherFailureDO pending = erpFinanceVoucherFailureMapper.selectPendingByBiz(bizType, bizId);
        String truncatedMessage = StrUtil.maxLength(errorMessage, ERROR_MESSAGE_MAX_LENGTH);
        String truncatedStack = StrUtil.maxLength(errorStack, ERROR_STACK_MAX_LENGTH);
        if (pending != null) {
            // 同一业务单据至多一条待重试记录：重复失败只更新错误信息，不重复插入
            erpFinanceVoucherFailureMapper.updateById(new ErpFinanceVoucherFailureDO()
                    .setId(pending.getId())
                    .setErrorMessage(truncatedMessage)
                    .setErrorStack(truncatedStack));
            return;
        }
        erpFinanceVoucherFailureMapper.insert(new ErpFinanceVoucherFailureDO()
                .setBizType(bizType)
                .setBizId(bizId)
                .setErrorMessage(truncatedMessage)
                .setErrorStack(truncatedStack)
                .setRetryCount(0)
                .setStatus(ErpFinanceVoucherFailureStatusEnum.PENDING.getStatus()));
    }

    @Override
    public Long retryVoucherFailure(Long id) {
        ErpFinanceVoucherFailureDO failure = validateFailureExists(id);
        // 锁在事务外获取：串行化同一业务单据的凭证生成，防止并发重试重复制证
        RLock lock = redissonClient.getLock(buildRetryLockKey(failure.getBizType(), failure.getBizId()));
        if (!lock.tryLock()) {
            throw exception(VOUCHER_FAILURE_RETRY_LOCK_BUSY);
        }
        try {
            RetryOutcome outcome = executeInRequiredTransaction(() -> doRetryVoucherFailure(id));
            if (outcome.errorMessage != null) {
                throw exception(VOUCHER_FAILURE_RETRY_FAIL, outcome.errorMessage);
            }
            return outcome.voucherId;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 事务内执行重试。autoGenerateVoucher 为 NESTED（savepoint）传播：
     * 制证失败只回滚 savepoint 内的凭证写入，本事务中 markRetryFailed 的记录更新不受影响
     */
    RetryOutcome doRetryVoucherFailure(Long id) {
        ErpFinanceVoucherFailureDO failure = validateFailureExists(id);
        if (!ErpFinanceVoucherFailureStatusEnum.PENDING.getStatus().equals(failure.getStatus())) {
            throw exception(VOUCHER_FAILURE_STATUS_INVALID);
        }
        try {
            Long voucherId = financeVoucherService.autoGenerateVoucher(failure.getBizType(), failure.getBizId());
            if (voucherId == null) {
                markRetryFailed(failure, "未配置可用凭证模板");
                return RetryOutcome.failed(StrUtil.format("业务单据({})未配置可用凭证模板", failure.getBizId()));
            }
            int updated = erpFinanceVoucherFailureMapper.updateStatusByIdAndStatus(id,
                    ErpFinanceVoucherFailureStatusEnum.PENDING.getStatus(),
                    new ErpFinanceVoucherFailureDO()
                            .setStatus(ErpFinanceVoucherFailureStatusEnum.SUCCESS.getStatus())
                            .setLastRetryTime(LocalDateTime.now()));
            if (updated == 0) {
                // 凭证已存在（幂等），但记录被并发确认/重试：不覆盖终态，仅告警
                log.warn("[retryVoucherFailure] 失败记录状态已被并发处理，id={}, voucherId={}", id, voucherId);
            }
            return RetryOutcome.success(voucherId);
        } catch (Exception e) {
            // 含 ServiceException（如期间关闭）：savepoint 已回滚凭证写入，此处记录重试失败并保持 PENDING
            log.error("[retryVoucherFailure] 重试生成凭证失败，id={}, bizType={}, bizId={}",
                    id, failure.getBizType(), failure.getBizId(), e);
            markRetryFailed(failure, e.getMessage());
            return RetryOutcome.failed(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmVoucherFailure(Long id, String reason) {
        if (StrUtil.isBlank(reason)) {
            throw exception(VOUCHER_FAILURE_CONFIRM_REASON_REQUIRED);
        }
        validateFailureExists(id);
        int updated = erpFinanceVoucherFailureMapper.updateStatusByIdAndStatus(id,
                ErpFinanceVoucherFailureStatusEnum.PENDING.getStatus(),
                new ErpFinanceVoucherFailureDO()
                        .setStatus(ErpFinanceVoucherFailureStatusEnum.CONFIRMED.getStatus())
                        .setConfirmReason(reason));
        if (updated == 0) {
            throw exception(VOUCHER_FAILURE_STATUS_INVALID);
        }
    }

    @Override
    public PageResult<ErpFinanceVoucherFailureDO> getVoucherFailurePage(ErpFinanceVoucherFailurePageReqVO pageReqVO) {
        return erpFinanceVoucherFailureMapper.selectPage(pageReqVO);
    }

    @Override
    public ErpFinanceVoucherFailureDO getVoucherFailure(Long id) {
        return erpFinanceVoucherFailureMapper.selectById(id);
    }

    private ErpFinanceVoucherFailureDO validateFailureExists(Long id) {
        ErpFinanceVoucherFailureDO failure = erpFinanceVoucherFailureMapper.selectById(id);
        if (failure == null) {
            throw exception(VOUCHER_FAILURE_NOT_EXISTS);
        }
        return failure;
    }

    private void markRetryFailed(ErpFinanceVoucherFailureDO failure, String errorMessage) {
        erpFinanceVoucherFailureMapper.updateById(new ErpFinanceVoucherFailureDO()
                .setId(failure.getId())
                .setRetryCount((failure.getRetryCount() == null ? 0 : failure.getRetryCount()) + 1)
                .setErrorMessage(StrUtil.maxLength(errorMessage, ERROR_MESSAGE_MAX_LENGTH))
                .setLastRetryTime(LocalDateTime.now()));
    }

    private String buildRetryLockKey(Integer bizType, Long bizId) {
        return "erp:finance-voucher-failure:retry:" + bizType + ":" + bizId;
    }

    private <T> T executeInRequiredTransaction(java.util.function.Supplier<T> supplier) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return transactionTemplate.execute(status -> supplier.get());
    }

    /**
     * 重试结果：voucherId 非空为成功；errorMessage 非空为失败（记录已保持 PENDING 并累计次数）
     */
    static class RetryOutcome {

        final Long voucherId;
        final String errorMessage;

        private RetryOutcome(Long voucherId, String errorMessage) {
            this.voucherId = voucherId;
            this.errorMessage = errorMessage;
        }

        static RetryOutcome success(Long voucherId) {
            return new RetryOutcome(voucherId, null);
        }

        static RetryOutcome failed(String errorMessage) {
            return new RetryOutcome(null, errorMessage);
        }

    }

}
