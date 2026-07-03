package cn.weitee.erp.module.erp.service.finance.approval;

import cn.weitee.erp.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.service.finance.ErpFinancePaymentService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.FINANCE_PAYMENT_NOT_EXISTS;

/**
 * 付款单审批结果处理器
 */
@Component
public class FinancePaymentResultHandler implements ApprovalResultHandler {

    @Resource
    private ErpFinancePaymentService financePaymentService;

    @Override
    public String getSceneCode() {
        return "erp.finance.payment.submit";
    }

    @Override
    public void onApprove(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        financePaymentService.updateFinancePaymentStatusByBpm(bizId, processInstanceId,
                ErpAuditStatus.APPROVE.getStatus(), reason);
    }

    @Override
    public void onReject(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        financePaymentService.updateFinancePaymentStatusByBpm(bizId, processInstanceId,
                ErpAuditStatus.REJECT.getStatus(), reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onCancel(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        financePaymentService.rollbackFinancePaymentStatusToDraftByBpm(bizId, processInstanceId, reason);
    }

    private void validateExists(Long paymentId) {
        if (financePaymentService.getFinancePayment(paymentId) == null) {
            throw exception(FINANCE_PAYMENT_NOT_EXISTS);
        }
    }

}
