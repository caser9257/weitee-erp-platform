package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpensePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseProjectSummaryReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseProjectSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpenseSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceExpenseItemDO;

import jakarta.validation.Valid;
import java.time.YearMonth;
import java.util.Collection;
import java.util.List;

/**
 * ERP 费用报销 Service 接口
 */
public interface ErpFinanceExpenseService {

    Long createFinanceExpense(@Valid ErpFinanceExpenseSaveReqVO createReqVO);

    void updateFinanceExpense(@Valid ErpFinanceExpenseSaveReqVO updateReqVO);

    void updateFinanceExpenseStatus(Long id, Integer status);

    void updateFinanceExpenseStatusByBpm(Long id, String processInstanceId, Integer status, String reason);

    void deleteFinanceExpense(List<Long> ids);

    ErpFinanceExpenseDO getFinanceExpense(Long id);

    PageResult<ErpFinanceExpenseDO> getFinanceExpensePage(ErpFinanceExpensePageReqVO pageReqVO);

    List<ErpFinanceExpenseItemDO> getFinanceExpenseItemListByExpenseId(Long expenseId);

    List<ErpFinanceExpenseItemDO> getFinanceExpenseItemListByExpenseIds(Collection<Long> expenseIds);

    List<ErpFinanceExpenseProjectSummaryRespVO> getFinanceExpenseProjectSummary(ErpFinanceExpenseProjectSummaryReqVO reqVO);

    List<ErpFinanceExpenseDO> getApprovedResearchExpenseListByMonth(YearMonth month);

}
