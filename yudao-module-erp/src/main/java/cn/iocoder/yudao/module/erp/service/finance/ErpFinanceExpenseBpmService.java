package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseCancelApprovalReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseSubmitReqVO;

public interface ErpFinanceExpenseBpmService {

    String submitFinanceExpense(Long userId, ErpFinanceExpenseSubmitReqVO reqVO);

    void cancelFinanceExpenseApproval(Long userId, ErpFinanceExpenseCancelApprovalReqVO reqVO);

    void handleProcessInstanceResult(Long expenseId, String processInstanceId, Integer status, String reason);

}
