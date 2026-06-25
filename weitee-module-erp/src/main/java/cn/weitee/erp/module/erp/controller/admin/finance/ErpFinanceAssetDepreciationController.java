package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetDepreciationGenerateReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetDepreciationPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetDepreciationRespVO;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceAssetDepreciationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - ERP 固定资产折旧")
@RestController
@RequestMapping("/erp/finance-asset-depreciation")
@Validated
public class ErpFinanceAssetDepreciationController {

    @Resource
    private ErpFinanceAssetDepreciationService financeAssetDepreciationService;

    @PostMapping("/generate")
    @Operation(summary = "生成固定资产折旧")
    @PreAuthorize("@ss.hasPermission('erp:finance-asset-depreciation:generate')")
    public CommonResult<Integer> generateDepreciation(@Valid @RequestBody ErpFinanceAssetDepreciationGenerateReqVO reqVO) {
        return success(financeAssetDepreciationService.generateDepreciation(reqVO.getPeriod()));
    }

    @GetMapping("/page")
    @Operation(summary = "获得固定资产折旧分页")
    @PreAuthorize("@ss.hasPermission('erp:finance-asset-depreciation:query')")
    public CommonResult<PageResult<ErpFinanceAssetDepreciationRespVO>> getDepreciationPage(@Valid ErpFinanceAssetDepreciationPageReqVO pageReqVO) {
        return success(BeanUtils.toBean(financeAssetDepreciationService.getFinanceAssetDepreciationPage(pageReqVO),
                ErpFinanceAssetDepreciationRespVO.class));
    }
}
