package cn.weitee.erp.module.erp.service.finance.approval;

import cn.weitee.erp.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceExpenseService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErpAuditStatus.APPROVE;
import static cn.weitee.erp.module.erp.enums.ErpAuditStatus.REJECT;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsExpense.EXPENSE_NOT_EXISTS;

@Component
public class ExpenseResultHandler implements ApprovalResultHandler {

    @Resource
    private ErpFinanceExpenseService financeExpenseService;

    @Override
    public String getSceneCode() {
        return "erp.finance.expense.submit";
    }

    @Override
    public void onApprove(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        financeExpenseService.updateFinanceExpenseStatusByBpm(bizId, processInstanceId,
                APPROVE.getStatus(), reason);
    }

    @Override
    public void onReject(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        financeExpenseService.updateFinanceExpenseStatusByBpm(bizId, processInstanceId,
                REJECT.getStatus(), reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onCancel(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        financeExpenseService.rollbackFinanceExpenseStatusToDraftByBpm(bizId, processInstanceId, reason);
    }

    private void validateExists(Long expenseId) {
        if (financeExpenseService.getFinanceExpense(expenseId) == null) {
            throw exception(EXPENSE_NOT_EXISTS);
        }
    }
}
