package cn.iocoder.yudao.module.erp.controller.admin.mrp;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.returning.ErpProductionReturnCreateReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.returning.ErpProductionReturnableBatchesRespVO;
import cn.iocoder.yudao.module.erp.service.mrp.ErpProductionReturnService;
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

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 生产退料")
@RestController
@RequestMapping("/erp/production-material-return")
@Validated
public class ErpProductionReturnController {

    @Resource
    private ErpProductionReturnService productionReturnService;

    @GetMapping("/returnable-batches")
    @Operation(summary = "获得可退批次")
    @Parameter(name = "productionMaterialId", required = true)
    @PreAuthorize("@ss.hasPermission('erp:production-material-issue:query')")
    public CommonResult<ErpProductionReturnableBatchesRespVO> getReturnableBatches(
            @RequestParam("productionMaterialId") Long productionMaterialId) {
        return success(productionReturnService.getReturnableBatches(productionMaterialId));
    }

    @PostMapping("/create")
    @Operation(summary = "创建生产退料")
    @PreAuthorize("@ss.hasPermission('erp:production-material-return:create')")
    public CommonResult<Long> createProductionReturn(@Valid @RequestBody ErpProductionReturnCreateReqVO reqVO) {
        return success(productionReturnService.createProductionReturn(reqVO));
    }

}
