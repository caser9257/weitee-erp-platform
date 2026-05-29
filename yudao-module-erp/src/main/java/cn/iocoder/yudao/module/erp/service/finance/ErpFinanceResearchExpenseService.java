package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceResearchExpenseSummaryReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceResearchExpenseSummaryRespVO;

import java.util.List;

public interface ErpFinanceResearchExpenseService {
    
    List<ErpFinanceResearchExpenseSummaryRespVO> getResearchExpenseSummary(
            ErpFinanceResearchExpenseSummaryReqVO reqVO);
    
    /**
     * 研发费用月末结转
     */
    void monthlyCarryForward(Long ledgerId, Integer year, Integer month);
}