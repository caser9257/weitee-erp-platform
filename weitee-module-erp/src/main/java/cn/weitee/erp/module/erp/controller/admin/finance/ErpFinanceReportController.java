package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.report.ErpFinanceReportReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.report.ErpFinanceStatementRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.report.ErpFinanceTrialBalanceRespVO;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceReportService;
import cn.weitee.erp.module.erp.service.finance.FinanceDataPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 财务报表")
@RestController
@RequestMapping("/erp/finance-report")
@Validated
public class ErpFinanceReportController {

    @Resource
    private ErpFinanceReportService financeReportService;
    @Resource
    private FinanceDataPermissionService financeDataPermissionService;

    @GetMapping("/trial-balance")
    @Operation(summary = "获得试算平衡表")
    @PreAuthorize("@ss.hasAnyPermissions('erp:finance-report:query', 'erp:finance-voucher:query')")
    public CommonResult<ErpFinanceTrialBalanceRespVO> getTrialBalance(@Valid ErpFinanceReportReqVO reqVO) {
        if (!canAccessLedger(reqVO.getLedgerId())) {
            return success(null);
        }
        return success(financeReportService.getTrialBalance(reqVO));
    }

    @GetMapping("/balance-sheet")
    @Operation(summary = "获得资产负债表")
    @PreAuthorize("@ss.hasAnyPermissions('erp:finance-report:query', 'erp:finance-voucher:query')")
    public CommonResult<ErpFinanceStatementRespVO> getBalanceSheet(@Valid ErpFinanceReportReqVO reqVO) {
        if (!canAccessLedger(reqVO.getLedgerId())) {
            return success(null);
        }
        return success(financeReportService.getBalanceSheet(reqVO));
    }

    @GetMapping("/income-statement")
    @Operation(summary = "获得利润表")
    @PreAuthorize("@ss.hasAnyPermissions('erp:finance-report:query', 'erp:finance-voucher:query')")
    public CommonResult<ErpFinanceStatementRespVO> getIncomeStatement(@Valid ErpFinanceReportReqVO reqVO) {
        if (!canAccessLedger(reqVO.getLedgerId())) {
            return success(null);
        }
        return success(financeReportService.getIncomeStatement(reqVO));
    }

    @GetMapping("/cash-flow-statement")
    @Operation(summary = "获得现金流量表")
    @PreAuthorize("@ss.hasAnyPermissions('erp:finance-report:query', 'erp:finance-voucher:query')")
    public CommonResult<ErpFinanceStatementRespVO> getCashFlowStatement(@Valid ErpFinanceReportReqVO reqVO) {
        if (!canAccessLedger(reqVO.getLedgerId())) {
            return success(null);
        }
        return success(financeReportService.getCashFlowStatement(reqVO));
    }

    private boolean canAccessLedger(Long ledgerId) {
        return financeDataPermissionService.canAccessLedger(ledgerId);
    }

}
