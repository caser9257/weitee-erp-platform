package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.module.erp.controller.admin.finance.vo.report.ErpFinanceReportReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.report.ErpFinanceStatementRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.report.ErpFinanceTrialBalanceRespVO;

import jakarta.validation.Valid;

public interface ErpFinanceReportService {

    ErpFinanceTrialBalanceRespVO getTrialBalance(@Valid ErpFinanceReportReqVO reqVO);

    ErpFinanceStatementRespVO getBalanceSheet(@Valid ErpFinanceReportReqVO reqVO);

    ErpFinanceStatementRespVO getIncomeStatement(@Valid ErpFinanceReportReqVO reqVO);

    ErpFinanceStatementRespVO getCashFlowStatement(@Valid ErpFinanceReportReqVO reqVO);

}
