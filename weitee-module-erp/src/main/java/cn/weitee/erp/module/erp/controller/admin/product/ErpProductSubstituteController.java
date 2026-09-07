package cn.weitee.erp.module.erp.controller.admin.product;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.module.erp.controller.admin.product.vo.substitute.ErpProductSubstituteRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.substitute.ErpProductSubstituteSaveReqVO;
import cn.weitee.erp.module.erp.service.product.ErpProductSubstituteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 物料替代料")
@RestController
@RequestMapping("/erp/product/substitute")
@Validated
public class ErpProductSubstituteController {

    @Resource
    private ErpProductSubstituteService productSubstituteService;

    @GetMapping("/list")
    @Operation(summary = "获得物料的替代料列表")
    @PreAuthorize("@ss.hasPermission('erp:product:query')")
    public CommonResult<List<ErpProductSubstituteRespVO>> getList(@RequestParam("productId") Long productId) {
        return success(productSubstituteService.getListByProductId(productId));
    }

    @GetMapping("/has-map")
    @Operation(summary = "批量判断物料是否有替代料")
    @PreAuthorize("@ss.hasPermission('erp:product:query')")
    public CommonResult<Map<Long, Boolean>> getHasMap(@RequestParam("productIds") List<Long> productIds) {
        return success(productSubstituteService.getHasSubstituteMap(productIds));
    }

    @PostMapping("/create")
    @Operation(summary = "创建替代料关联")
    @PreAuthorize("@ss.hasPermission('erp:product:update')")
    public CommonResult<Long> create(@Valid @RequestBody ErpProductSubstituteSaveReqVO reqVO) {
        return success(productSubstituteService.createSubstitute(reqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除替代料关联")
    @PreAuthorize("@ss.hasPermission('erp:product:update')")
    public CommonResult<Boolean> delete(@RequestParam("productId") Long productId,
                                        @RequestParam("substituteProductId") Long substituteProductId) {
        productSubstituteService.deleteSubstitute(productId, substituteProductId);
        return success(true);
    }

    @PostMapping("/batch-update")
    @Operation(summary = "批量新增/更新物料的替代料列表")
    @PreAuthorize("@ss.hasPermission('erp:product:update')")
    public CommonResult<Boolean> batchUpdate(@RequestParam("productId") Long productId,
                                             @RequestBody @Valid List<ErpProductSubstituteSaveReqVO> list) {
        productSubstituteService.batchUpdateSubstitutes(productId, list);
        return success(true);
    }

}
