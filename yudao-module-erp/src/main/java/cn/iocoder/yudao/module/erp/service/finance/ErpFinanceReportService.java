package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.report.ErpFinanceReportReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.report.ErpFinanceStatementRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.report.ErpFinanceTrialBalanceRespVO;

import javax.validation.Valid;

public interface ErpFinanceReportService {

    ErpFinanceTrialBalanceRespVO getTrialBalance(@Valid ErpFinanceReportReqVO reqVO);

    ErpFinanceStatementRespVO getBalanceSheet(@Valid ErpFinanceReportReqVO reqVO);

    ErpFinanceStatementRespVO getIncomeStatement(@Valid ErpFinanceReportReqVO reqVO);

    ErpFinanceStatementRespVO getCashFlowStatement(@Valid ErpFinanceReportReqVO reqVO);

}
