package cn.weitee.erp.module.erp.service.finance.approval;

import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContext;
import cn.weitee.erp.module.bpm.service.approval.provider.ApprovalContextProvider;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpFinanceExpenseMapper;
import cn.weitee.erp.module.erp.enums.ErpFinanceExpenseBpmConstants;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsExpense.EXPENSE_NOT_EXISTS;

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
                .detailUrl("/finance/expense/detail?id=" + expense.getId())
                .variables(variables)
                .notifyParams(notifyParams)
                .build();
    }

    private Long parseCreatorId(String creator) {
        return creator != null && StrUtil.isNumeric(creator) ? Long.valueOf(creator) : null;
    }
}
