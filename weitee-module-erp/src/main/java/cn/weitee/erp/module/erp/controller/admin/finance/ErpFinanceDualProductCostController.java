package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.productdualcost.ErpFinanceDualProductCostPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.productdualcost.ErpFinanceDualProductCostRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.productdualcost.ErpFinanceDualProductCostRebuildReqVO;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceDualProductCostService;
import cn.weitee.erp.module.erp.service.finance.FinanceDataPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;
import static cn.weitee.erp.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_FOUND;
import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 产品双账成本")
@RestController
@RequestMapping("/erp/finance-dual-product-cost")
@Validated
public class ErpFinanceDualProductCostController {

    @Resource
    private ErpFinanceDualProductCostService productCostService;
    @Resource
    private FinanceDataPermissionService financeDataPermissionService;

    @GetMapping("/page")
    @Operation(summary = "获得产品双账成本分页")
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-product-cost:query')")
    public CommonResult<PageResult<ErpFinanceDualProductCostRespVO>> getProductDualCostPage(
            @Valid ErpFinanceDualProductCostPageReqVO pageReqVO) {
        if (!canAccessProductDualCost()) {
            return success(PageResult.empty(0L));
        }
        return success(productCostService.getProductDualCostPage(pageReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得单个产品双账成本")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-product-cost:query')")
    public CommonResult<ErpFinanceDualProductCostRespVO> getProductDualCost(@RequestParam("id") Long id) {
        if (!canAccessProductDualCost()) {
            return success(null);
        }
        return success(productCostService.getProductDualCost(id));
    }

    @GetMapping("/items")
    @Operation(summary = "获得产品双账成本明细")
    @Parameter(name = "resultId", description = "结果ID", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-product-cost:query')")
    public CommonResult<List<ErpFinanceDualProductCostRespVO>> getProductDualCostItems(
            @RequestParam("resultId") Long resultId) {
        if (!canAccessProductDualCost()) {
            return success(java.util.Collections.emptyList());
        }
        return success(productCostService.getProductDualCostItems(resultId));
    }

    @PostMapping("/rebuild")
    @Operation(summary = "产品级重跑")
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-product-cost:rebuild')")
    public CommonResult<Boolean> rebuildProductDualCost(
            @Valid @RequestBody ErpFinanceDualProductCostRebuildReqVO reqVO) {
        requireProductDualCostAccessForWrite();
        productCostService.rebuildProductDualCost(getLoginUserId(), reqVO);
        return success(true);
    }

    @GetMapping("/export-external")
    @Operation(summary = "导出外部账产品成本")
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-product-cost:export')")
    public void exportExternalProductCost(
            @Valid ErpFinanceDualProductCostPageReqVO pageReqVO,
            HttpServletResponse response) throws IOException {
        requireProductDualCostAccessForWrite();
        productCostService.exportExternalProductCost(pageReqVO, response);
    }

    @GetMapping("/export-internal")
    @Operation(summary = "导出内部账产品成本")
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-product-cost:export')")
    public void exportInternalProductCost(
            @Valid ErpFinanceDualProductCostPageReqVO pageReqVO,
            HttpServletResponse response) throws IOException {
        requireProductDualCostAccessForWrite();
        productCostService.exportInternalProductCost(pageReqVO, response);
    }

    @PostMapping("/rebuild-batch")
    @Operation(summary = "按期间批量重跑产品双账成本")
    @Parameter(name = "period", description = "期间（YYYY-MM）", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-product-cost:rebuild')")
    public CommonResult<Integer> rebuildBatchByPeriod(@RequestParam("period") String period,
                                                      @RequestParam(value = "remark", required = false) String remark) {
        requireProductDualCostAccessForWrite();
        return success(productCostService.rebuildBatchByPeriod(getLoginUserId(), period, remark));
    }

    private boolean canAccessProductDualCost() {
        Long userId = getLoginUserId();
        return financeDataPermissionService.canAccessDualLedger(userId,
                cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum.PRODUCTION_INBOUND.getType())
                && financeDataPermissionService.canAccessDualLedger(userId,
                cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum.OUTSOURCE_INBOUND.getType());
    }

    private void requireProductDualCostAccessForWrite() {
        if (!canAccessProductDualCost()) {
            throw exception(NOT_FOUND);
        }
    }
}
