package cn.iocoder.yudao.module.bpm.controller.admin.approval;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scheme.BpmApprovalSchemePageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scheme.BpmApprovalSchemePublishReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scheme.BpmApprovalSchemeRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scheme.BpmApprovalSchemeSaveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.scheme.BpmApprovalSchemeSubmitReqVO;
import cn.iocoder.yudao.module.bpm.service.approval.BpmApprovalSchemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 审批方案")
@RestController
@RequestMapping("/bpm/approval-scheme")
@Validated
public class BpmApprovalSchemeController {

    @Resource
    private BpmApprovalSchemeService approvalSchemeService;

    @GetMapping("/page")
    @Operation(summary = "获取审批方案分页")
    @PreAuthorize("@ss.hasPermission('bpm:approval-scheme:query')")
    public CommonResult<PageResult<BpmApprovalSchemeRespVO>> getSchemePage(@Valid BpmApprovalSchemePageReqVO pageReqVO) {
        return success(approvalSchemeService.getSchemePage(pageReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获取审批方案详情")
    @Parameter(name = "id", description = "方案编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('bpm:approval-scheme:query')")
    public CommonResult<BpmApprovalSchemeRespVO> getScheme(@RequestParam("id") Long id) {
        return success(approvalSchemeService.getScheme(id));
    }

    @PostMapping("/create-draft")
    @Operation(summary = "创建审批方案草稿")
    @PreAuthorize("@ss.hasPermission('bpm:approval-scheme:create')")
    public CommonResult<Long> createDraft(@Valid @RequestBody BpmApprovalSchemeSaveReqVO createReqVO) {
        return success(approvalSchemeService.createDraft(createReqVO));
    }

    @PutMapping("/update-draft")
    @Operation(summary = "更新审批方案草稿")
    @PreAuthorize("@ss.hasPermission('bpm:approval-scheme:update')")
    public CommonResult<Boolean> updateDraft(@Valid @RequestBody BpmApprovalSchemeSaveReqVO updateReqVO) {
        approvalSchemeService.updateDraft(updateReqVO);
        return success(true);
    }

    @PutMapping("/submit")
    @Operation(summary = "提交审批方案待发布")
    @PreAuthorize("@ss.hasPermission('bpm:approval-scheme:update')")
    public CommonResult<Boolean> submit(@Valid @RequestBody BpmApprovalSchemeSubmitReqVO submitReqVO) {
        approvalSchemeService.submit(submitReqVO);
        return success(true);
    }

    @PutMapping("/publish")
    @Operation(summary = "发布审批方案")
    @PreAuthorize("@ss.hasPermission('bpm:approval-scheme:publish')")
    public CommonResult<Boolean> publish(@Valid @RequestBody BpmApprovalSchemePublishReqVO publishReqVO) {
        approvalSchemeService.publish(publishReqVO);
        return success(true);
    }

    @PutMapping("/disable")
    @Operation(summary = "停用审批方案")
    @Parameter(name = "versionId", description = "版本编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('bpm:approval-scheme:publish')")
    public CommonResult<Boolean> disable(@RequestParam("versionId") Long versionId) {
        approvalSchemeService.disable(versionId);
        return success(true);
    }

    @PutMapping("/switch-active-version")
    @Operation(summary = "切换当前生效版本")
    @Parameter(name = "versionId", description = "目标版本编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('bpm:approval-scheme:publish')")
    public CommonResult<Boolean> switchActiveVersion(@RequestParam("versionId") Long versionId) {
        approvalSchemeService.switchActiveVersion(versionId);
        return success(true);
    }

}
