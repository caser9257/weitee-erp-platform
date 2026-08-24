package cn.weitee.erp.module.bpm.controller.admin.approval.generic;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import cn.weitee.erp.module.bpm.controller.admin.approval.vo.generic.BpmGenericApprovalCancelReqVO;
import cn.weitee.erp.module.bpm.service.approval.generic.GenericApprovalSubmitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 通用审批接入")
@RestController
@RequestMapping("/bpm/approval-runtime/generic")
@Validated
public class GenericApprovalController {

    @Resource
    private GenericApprovalSubmitService genericApprovalSubmitService;

    @PostMapping("/submit")
    @Operation(summary = "通用提交审批（配置驱动，无需业务代码接入）")
    @Parameter(name = "sceneCode", description = "场景编码（需已配置 generic_config）", required = true, example = "erp.stock.check.submit")
    @Parameter(name = "bizId", description = "业务单据 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('bpm:approval:submit')")
    public CommonResult<String> submitApproval(@RequestParam("sceneCode") String sceneCode,
                                               @RequestParam("bizId") Long bizId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(genericApprovalSubmitService.submit(sceneCode, bizId, userId));
    }

    @PostMapping("/cancel")
    @Operation(summary = "通用撤回审批（配置驱动）")
    @PreAuthorize("@ss.hasPermission('bpm:approval:query')")
    public CommonResult<Boolean> cancelApproval(@Valid @RequestBody BpmGenericApprovalCancelReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        genericApprovalSubmitService.cancel(reqVO.getSceneCode(), reqVO.getBizId(), userId, reqVO.getReason());
        return success(true);
    }

}
