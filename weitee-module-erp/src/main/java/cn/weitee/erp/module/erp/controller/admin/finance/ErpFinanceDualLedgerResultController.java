package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerRecomputeReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerResultPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualledger.ErpFinanceDualLedgerResultRespVO;
import cn.weitee.erp.module.erp.service.finance.FinanceDataPermissionService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceDualLedgerResultService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceDualWriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;

import static cn.weitee.erp.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_FOUND;
import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - ERP 双账套结果")
@RestController
@RequestMapping("/erp/finance-dual-ledger-result")
@Validated
public class ErpFinanceDualLedgerResultController {

    @Resource
    private ErpFinanceDualLedgerResultService dualLedgerResultService;

    @Resource
    private ErpFinanceDualWriteService dualWriteService;
    @Resource
    private FinanceDataPermissionService financeDataPermissionService;

    @GetMapping("/page")
    @Operation(summary = "获得双账套结果分页")
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-ledger-result:query')")
    public CommonResult<PageResult<ErpFinanceDualLedgerResultRespVO>> getDualLedgerResultPage(
            @Valid ErpFinanceDualLedgerResultPageReqVO pageReqVO) {
        if (!canAccessDualLedger(pageReqVO.getBizType())) {
            return success(PageResult.empty(0L));
        }
        return success(dualLedgerResultService.getDualLedgerResultPage(pageReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得单条双账套结果")
    @Parameter(name = "bizType", required = true, description = "业务类型")
    @Parameter(name = "bizId", required = true, description = "业务主键")
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-ledger-result:query')")
    public CommonResult<ErpFinanceDualLedgerResultRespVO> getDualLedgerResult(@RequestParam("bizType") Integer bizType,
                                                                               @RequestParam("bizId") Long bizId) {
        if (!canAccessDualLedger(bizType)) {
            return success(null);
        }
        return success(dualLedgerResultService.getDualLedgerResult(bizType, bizId));
    }

    @PostMapping("/recompute")
    @Operation(summary = "重算双账套结果")
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-ledger-result:recompute')")
    public CommonResult<ErpFinanceDualLedgerResultRespVO> recomputeDualLedgerResult(
            @Valid @RequestBody ErpFinanceDualLedgerRecomputeReqVO reqVO) {
        requireDualLedgerAccessForWrite(reqVO.getBizType());
        return success(dualLedgerResultService.recomputeDualLedgerResult(getLoginUserId(), reqVO));
    }

    @PostMapping("/recompute-batch")
    @Operation(summary = "按业务类型批量重算双账套凭证")
    @Parameter(name = "bizType", required = true, description = "业务类型")
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-ledger-result:recompute')")
    public CommonResult<Integer> recomputeDualLedgerVouchers(@RequestParam("bizType") Integer bizType) {
        requireDualLedgerAccessForWrite(bizType);
        return success(dualWriteService.recomputeDualLedgerVouchers(bizType));
    }

    @PostMapping("/recompute-by-biz")
    @Operation(summary = "按业务单据重算双账套凭证")
    @Parameter(name = "bizType", required = true, description = "业务类型")
    @Parameter(name = "bizId", required = true, description = "业务单据ID")
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-ledger-result:recompute')")
    public CommonResult<Boolean> recomputeByBizId(@RequestParam("bizType") Integer bizType,
                                                  @RequestParam("bizId") Long bizId) {
        requireDualLedgerAccessForWrite(bizType);
        return success(dualWriteService.recomputeByBizId(bizType, bizId));
    }

    @GetMapping("/export-single-ledger")
    @Operation(summary = "单条记录导出套账凭证")
    @Parameter(name = "bizType", required = true, description = "业务类型")
    @Parameter(name = "bizId", required = true, description = "业务主键")
    @Parameter(name = "ledgerSide", required = true, description = "导出账套侧：external 或 internal")
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-ledger-result:export')")
    public void exportSingleLedger(@RequestParam("bizType") Integer bizType,
                                   @RequestParam("bizId") Long bizId,
                                   @RequestParam("ledgerSide") String ledgerSide,
                                   HttpServletResponse response) throws IOException {
        requireDualLedgerAccessForWrite(bizType);
        dualLedgerResultService.exportSingleLedger(bizType, bizId, ledgerSide, response);
    }

    private boolean canAccessDualLedger(Integer bizType) {
        return financeDataPermissionService.canAccessDualLedger(getLoginUserId(), bizType);
    }

    private void requireDualLedgerAccessForWrite(Integer bizType) {
        if (!canAccessDualLedger(bizType)) {
            throw exception(NOT_FOUND);
        }
    }
}
