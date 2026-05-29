package cn.iocoder.yudao.module.erp.controller.admin.finance;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.report.ErpFinanceReportReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.report.ErpFinanceStatementRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.report.ErpFinanceTrialBalanceRespVO;
import cn.iocoder.yudao.module.erp.service.finance.ErpFinanceReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 财务报表")
@RestController
@RequestMapping("/erp/finance-report")
@Validated
public class ErpFinanceReportController {

    @Resource
    private ErpFinanceReportService financeReportService;

    @GetMapping("/trial-balance")
    @Operation(summary = "获得试算平衡表")
    @PreAuthorize("@ss.hasAnyPermissions('erp:finance-report:query', 'erp:finance-voucher:query')")
    public CommonResult<ErpFinanceTrialBalanceRespVO> getTrialBalance(@Valid ErpFinanceReportReqVO reqVO) {
        return success(financeReportService.getTrialBalance(reqVO));
    }

    @GetMapping("/balance-sheet")
    @Operation(summary = "获得资产负债表")
    @PreAuthorize("@ss.hasAnyPermissions('erp:finance-report:query', 'erp:finance-voucher:query')")
    public CommonResult<ErpFinanceStatementRespVO> getBalanceSheet(@Valid ErpFinanceReportReqVO reqVO) {
        return success(financeReportService.getBalanceSheet(reqVO));
    }

    @GetMapping("/income-statement")
    @Operation(summary = "获得利润表")
    @PreAuthorize("@ss.hasAnyPermissions('erp:finance-report:query', 'erp:finance-voucher:query')")
    public CommonResult<ErpFinanceStatementRespVO> getIncomeStatement(@Valid ErpFinanceReportReqVO reqVO) {
        return success(financeReportService.getIncomeStatement(reqVO));
    }

    @GetMapping("/cash-flow-statement")
    @Operation(summary = "获得现金流量表")
    @PreAuthorize("@ss.hasAnyPermissions('erp:finance-report:query', 'erp:finance-voucher:query')")
    public CommonResult<ErpFinanceStatementRespVO> getCashFlowStatement(@Valid ErpFinanceReportReqVO reqVO) {
        return success(financeReportService.getCashFlowStatement(reqVO));
    }

}
