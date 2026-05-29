package cn.iocoder.yudao.module.erp.service.finance.approval;

import cn.iocoder.yudao.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePaymentDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinancePaymentMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinancePaymentService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.FINANCE_PAYMENT_NOT_EXISTS;

/**
 * 付款单审批结果处理器
 */
@Component
public class FinancePaymentResultHandler implements ApprovalResultHandler {

    @Resource
    private ErpFinancePaymentMapper financePaymentMapper;
    @Resource
    private ErpFinancePaymentService financePaymentService;

    @Override
    public String getSceneCode() {
        return "erp.finance.payment.submit";
    }

    @Override
    public void onApprove(Long bizId, String processInstanceId, String reason) {
        financePaymentService.updateFinancePaymentStatusByBpm(bizId, processInstanceId,
                ErpAuditStatus.APPROVE.getStatus(), reason);
    }

    @Override
    public void onReject(Long bizId, String processInstanceId, String reason) {
        financePaymentService.updateFinancePaymentStatusByBpm(bizId, processInstanceId,
                ErpAuditStatus.REJECT.getStatus(), reason);
    }

    @Override
    public void onCancel(Long bizId, String processInstanceId, String reason) {
        financePaymentMapper.clearProcessInstanceId(bizId);
    }

}
