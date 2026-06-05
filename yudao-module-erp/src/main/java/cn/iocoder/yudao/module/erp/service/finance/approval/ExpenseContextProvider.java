package cn.iocoder.yudao.module.erp.service.finance.approval;

import cn.iocoder.yudao.module.bpm.service.approval.provider.ApprovalContext;
import cn.iocoder.yudao.module.bpm.service.approval.provider.ApprovalContextProvider;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceExpenseMapper;
import cn.iocoder.yudao.module.erp.enums.ErpFinanceExpenseBpmConstants;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsExpense.EXPENSE_NOT_EXISTS;

@Component
public class ExpenseContextProvider implements ApprovalContextProvider {

    @Resource
    private ErpFinanceExpenseMapper financeExpenseMapper;

    @Override
    public String getSceneCode() {
        return "erp.finance.expense.submit";
    }

    @Override
    public ApprovalContext getContext(Long bizId) {
        ErpFinanceExpenseDO expense = financeExpenseMapper.selectById(bizId);
        if (expense == null) {
            throw exception(EXPENSE_NOT_EXISTS);
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put(ErpFinanceExpenseBpmConstants.VARIABLE_EXPENSE_ID, expense.getId());
        variables.put(ErpFinanceExpenseBpmConstants.VARIABLE_EXPENSE_NO, expense.getNo());
        variables.put(ErpFinanceExpenseBpmConstants.VARIABLE_EXPENSE_PRICE, expense.getExpensePrice());
        variables.put(ErpFinanceExpenseBpmConstants.VARIABLE_DEPT_ID, expense.getDeptId());
        variables.put(ErpFinanceExpenseBpmConstants.VARIABLE_PROJECT_ID, expense.getProjectId());
        variables.put(ErpFinanceExpenseBpmConstants.VARIABLE_SUPPLIER_ID, expense.getSupplierId());
        variables.put(ErpFinanceExpenseBpmConstants.VARIABLE_FINANCE_USER_ID, expense.getFinanceUserId());
        variables.put(ErpFinanceExpenseBpmConstants.VARIABLE_ACCOUNT_ID, expense.getAccountId());
        Map<String, Object> notifyParams = new HashMap<>();
        notifyParams.put("bizTitle", "费用单 " + expense.getNo());
        notifyParams.put("bizNo", expense.getNo());
        notifyParams.put("amount", expense.getExpensePrice());
        return ApprovalContext.builder()
                .bizId(expense.getId())
                .bizNo(expense.getNo())
                .bizTitle("费用单 " + expense.getNo())
                .amount(expense.getExpensePrice())
                .startUserId(parseCreatorId(expense.getCreator()))
                .variables(variables)
                .notifyParams(notifyParams)
                .build();
    }

    private Long parseCreatorId(String creator) {
        return creator != null && creator.matches("\\d+") ? Long.valueOf(creator) : null;
    }
}
