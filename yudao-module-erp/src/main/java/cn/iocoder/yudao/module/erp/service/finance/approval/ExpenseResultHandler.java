package cn.iocoder.yudao.module.erp.service.finance.approval;

import cn.iocoder.yudao.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceExpenseMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinanceExpenseService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsExpense.EXPENSE_NOT_EXISTS;

@Component
public class ExpenseResultHandler implements ApprovalResultHandler {

    @Resource
    private ErpFinanceExpenseMapper financeExpenseMapper;
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
                ErpAuditStatus.APPROVE.getStatus(), reason);
    }

    @Override
    public void onReject(Long bizId, String processInstanceId, String reason) {
        validateExists(bizId);
        financeExpenseService.updateFinanceExpenseStatusByBpm(bizId, processInstanceId,
                ErpAuditStatus.REJECT.getStatus(), reason);
    }

    @Override
    public void onCancel(Long bizId, String processInstanceId, String reason) {
        financeExpenseMapper.clearProcessInstanceId(bizId);
    }

    private void validateExists(Long expenseId) {
        if (financeExpenseMapper.selectById(expenseId) == null) {
            throw exception(EXPENSE_NOT_EXISTS);
        }
    }
}
