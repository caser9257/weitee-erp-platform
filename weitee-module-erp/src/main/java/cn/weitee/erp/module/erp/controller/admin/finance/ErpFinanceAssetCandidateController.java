package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetCandidateConfirmReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetCandidatePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetCandidateRespVO;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceAssetCandidateService;
import cn.weitee.erp.module.erp.service.finance.interceptor.FinanceDataPermission;
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

@Tag(name = "管理后台 - ERP 固定资产候选")
@RestController
@RequestMapping("/erp/finance-asset-candidate")
@Validated
@FinanceDataPermission
public class ErpFinanceAssetCandidateController {

    @Resource
    private ErpFinanceAssetCandidateService financeAssetCandidateService;

    @GetMapping("/page")
    @Operation(summary = "获得固定资产候选分页")
    @PreAuthorize("@ss.hasPermission('erp:finance-asset-candidate:query')")
    public CommonResult<PageResult<ErpFinanceAssetCandidateRespVO>> getFinanceAssetCandidatePage(@Valid ErpFinanceAssetCandidatePageReqVO pageReqVO) {
        return success(BeanUtils.toBean(financeAssetCandidateService.getFinanceAssetCandidatePage(pageReqVO),
                ErpFinanceAssetCandidateRespVO.class));
    }

    @PostMapping("/confirm")
    @Operation(summary = "确认固定资产候选")
    @PreAuthorize("@ss.hasPermission('erp:finance-asset-candidate:confirm')")
    public CommonResult<Long> confirmFinanceAssetCandidate(@Valid @RequestBody ErpFinanceAssetCandidateConfirmReqVO reqVO) {
        return success(financeAssetCandidateService.confirmFinanceAssetCandidate(reqVO));
    }
}
