package cn.iocoder.yudao.module.bpm.controller.admin.approval;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.template.BpmApprovalTemplatePageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.template.BpmApprovalTemplateRespVO;
import cn.iocoder.yudao.module.bpm.service.approval.BpmApprovalTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 审批模板")
@RestController
@RequestMapping("/bpm/approval-template")
@Validated
public class BpmApprovalTemplateController {

    @Resource
    private BpmApprovalTemplateService approvalTemplateService;

    @GetMapping("/page")
    @Operation(summary = "获取审批模板分页")
    @PreAuthorize("@ss.hasPermission('bpm:approval-template:query')")
    public CommonResult<PageResult<BpmApprovalTemplateRespVO>> getTemplatePage(@Valid BpmApprovalTemplatePageReqVO pageReqVO) {
        return success(approvalTemplateService.getTemplatePage(pageReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获取审批模板详情")
    @Parameter(name = "id", description = "模板编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('bpm:approval-template:query')")
    public CommonResult<BpmApprovalTemplateRespVO> getTemplate(@RequestParam("id") Long id) {
        return success(approvalTemplateService.getTemplate(id));
    }

    @GetMapping("/get-by-code")
    @Operation(summary = "根据模板编码获取审批模板")
    @Parameter(name = "code", description = "模板编码", required = true, example = "LEAVE_APPROVAL")
    @PreAuthorize("@ss.hasPermission('bpm:approval-template:query')")
    public CommonResult<BpmApprovalTemplateRespVO> getTemplateByCode(@RequestParam("code") String code) {
        return success(approvalTemplateService.getTemplateByCode(code));
    }

    @PostMapping("/use")
    @Operation(summary = "使用模板创建审批场景和方案")
    @Parameter(name = "templateId", description = "模板编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('bpm:approval-template:use')")
    public CommonResult<Long> useTemplate(@RequestParam("templateId") Long templateId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(approvalTemplateService.useTemplate(templateId, userId));
    }

}
