package cn.weitee.erp.module.erp.controller.admin.mrp;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.material.ErpProductionMaterialBatchCandidatesRespVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.material.ErpProductionMaterialRespVO;
import cn.weitee.erp.module.erp.service.mrp.ErpProductionMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 生产工单物料")
@RestController
@RequestMapping("/erp/production-material")
@Validated
public class ErpProductionMaterialController {

    @Resource
    private ErpProductionMaterialService productionMaterialService;

    @GetMapping("/list")
    @Operation(summary = "获得工单物料列表")
    @Parameter(name = "productionOrderId", required = true)
    @PreAuthorize("@ss.hasPermission('erp:production-material-issue:query')")
    public CommonResult<List<ErpProductionMaterialRespVO>> getProductionMaterialList(
            @RequestParam("productionOrderId") Long productionOrderId) {
        return success(productionMaterialService.getProductionMaterialList(productionOrderId));
    }

    @GetMapping("/batch-candidates")
    @Operation(summary = "获得领料批次候选")
    @PreAuthorize("@ss.hasPermission('erp:production-material-issue:query')")
    public CommonResult<ErpProductionMaterialBatchCandidatesRespVO> getBatchCandidates(
            @RequestParam("productionMaterialId") Long productionMaterialId,
            @RequestParam("warehouseId") Long warehouseId) {
        return success(productionMaterialService.getBatchCandidates(productionMaterialId, warehouseId));
    }

}
