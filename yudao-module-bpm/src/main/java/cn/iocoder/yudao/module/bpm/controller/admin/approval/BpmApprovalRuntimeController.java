package cn.iocoder.yudao.module.bpm.controller.admin.approval;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.runtime.*;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailRespVO;
import cn.iocoder.yudao.module.bpm.service.approval.BpmApprovalRuntimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 审批运行时")
@RestController
@RequestMapping("/bpm/approval-runtime")
@Validated
public class BpmApprovalRuntimeController {

    @Resource
    private BpmApprovalRuntimeService approvalRuntimeService;

    @PostMapping("/submit")
    @Operation(summary = "提交审批")
    @PreAuthorize("@ss.hasPermission('bpm:approval:submit')")
    public CommonResult<String> submitApproval(@Valid @RequestBody BpmApprovalSubmitReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String processInstanceId = approvalRuntimeService.submit(reqVO.getSceneCode(), reqVO.getBizId(), userId);
        return success(processInstanceId);
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获取审批详情")
    @Parameter(name = "sceneCode", description = "场景编码", required = true)
    @Parameter(name = "bizId", description = "业务单据ID", required = true)
    @PreAuthorize("@ss.hasPermission('bpm:approval:query')")
    public CommonResult<BpmApprovalDetailRespVO> getApprovalDetail(
            @RequestParam("sceneCode") String sceneCode,
            @RequestParam("bizId") Long bizId) {
        return success(approvalRuntimeService.getApprovalDetail(sceneCode, bizId));
    }

    @GetMapping("/get-trail")
    @Operation(summary = "获取审批轨迹")
    @Parameter(name = "sceneCode", description = "场景编码", required = true)
    @Parameter(name = "bizId", description = "业务单据ID", required = true)
    @PreAuthorize("@ss.hasPermission('bpm:approval:query')")
    public CommonResult<List<BpmApprovalDetailRespVO.ActivityNode>> getApprovalTrail(
            @RequestParam("sceneCode") String sceneCode,
            @RequestParam("bizId") Long bizId) {
        return success(approvalRuntimeService.getApprovalTrail(sceneCode, bizId));
    }

    @PostMapping("/cancel")
    @Operation(summary = "撤回审批")
    @PreAuthorize("@ss.hasPermission('bpm:approval:cancel')")
    public CommonResult<Boolean> cancelApproval(@Valid @RequestBody BpmApprovalCancelReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        approvalRuntimeService.cancel(reqVO.getSceneCode(), reqVO.getBizId(), userId, reqVO.getReason());
        return success(true);
    }

}
