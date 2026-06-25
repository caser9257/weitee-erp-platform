package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.projectdualcost.ErpFinanceDualProjectCostPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.projectdualcost.ErpFinanceDualProjectCostRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.projectdualcost.ErpFinanceDualProjectCostRebuildReqVO;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceDualProjectCostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
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
import static cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 项目双账成本 Controller
 */
@Tag(name = "管理后台 - 项目双账成本")
@RestController
@RequestMapping("/erp/finance-dual-project-cost")
@Validated
public class ErpFinanceDualProjectCostController {

    @Resource
    private ErpFinanceDualProjectCostService projectCostService;

    @GetMapping("/page")
    @Operation(summary = "获得项目双账成本分页")
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-project-cost:query')")
    public CommonResult<PageResult<ErpFinanceDualProjectCostRespVO>> getProjectDualCostPage(
            @Valid ErpFinanceDualProjectCostPageReqVO pageReqVO) {
        return success(projectCostService.getProjectDualCostPage(pageReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得单个项目双账成本")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-project-cost:query')")
    public CommonResult<ErpFinanceDualProjectCostRespVO> getProjectDualCost(@RequestParam("id") Long id) {
        return success(projectCostService.getProjectDualCost(id));
    }

    @GetMapping("/items")
    @Operation(summary = "获得项目双账成本明细")
    @Parameter(name = "resultId", description = "结果ID", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-project-cost:query')")
    public CommonResult<List<ErpFinanceDualProjectCostRespVO>> getProjectDualCostItems(
            @RequestParam("resultId") Long resultId) {
        return success(projectCostService.getProjectDualCostItems(resultId));
    }

    @PostMapping("/rebuild")
    @Operation(summary = "项目级重跑")
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-project-cost:rebuild')")
    public CommonResult<Boolean> rebuildProjectDualCost(
            @Valid @RequestBody ErpFinanceDualProjectCostRebuildReqVO reqVO) {
        projectCostService.rebuildProjectDualCost(getLoginUserId(), reqVO);
        return success(true);
    }

    @GetMapping("/export-external")
    @Operation(summary = "导出外部账项目成本")
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-project-cost:export')")
    public void exportExternalProjectCost(
            @Valid ErpFinanceDualProjectCostPageReqVO pageReqVO,
            HttpServletResponse response) throws IOException {
        projectCostService.exportExternalProjectCost(pageReqVO, response);
    }

    @GetMapping("/export-internal")
    @Operation(summary = "导出内部账项目成本")
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-project-cost:export')")
    public void exportInternalProjectCost(
            @Valid ErpFinanceDualProjectCostPageReqVO pageReqVO,
            HttpServletResponse response) throws IOException {
        projectCostService.exportInternalProjectCost(pageReqVO, response);
    }

    @PostMapping("/rebuild-batch")
    @Operation(summary = "按期间批量重跑项目双账成本")
    @Parameter(name = "period", description = "期间（YYYY-MM）", required = true)
    @PreAuthorize("@ss.hasPermission('erp:finance-dual-project-cost:rebuild')")
    public CommonResult<Integer> rebuildBatchByPeriod(@RequestParam("period") String period,
                                                      @RequestParam(value = "remark", required = false) String remark) {
        return success(projectCostService.rebuildBatchByPeriod(getLoginUserId(), period, remark));
    }
}
