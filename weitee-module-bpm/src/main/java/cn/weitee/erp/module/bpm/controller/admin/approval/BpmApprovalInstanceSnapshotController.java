package cn.weitee.erp.module.bpm.controller.admin.approval;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalInstanceSnapshotDO;
import cn.weitee.erp.module.bpm.service.approval.BpmApprovalInstanceSnapshotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 审批快照")
@RestController
@RequestMapping("/bpm/approval-snapshot")
@Validated
public class BpmApprovalInstanceSnapshotController {

    @Resource
    private BpmApprovalInstanceSnapshotService approvalInstanceSnapshotService;

    @GetMapping("/get-by-process-instance-id")
    @Operation(summary = "根据流程实例ID获取审批快照")
    @Parameter(name = "processInstanceId", description = "流程实例ID", required = true)
    public CommonResult<BpmApprovalInstanceSnapshotDO> getSnapshotByProcessInstanceId(
            @RequestParam("processInstanceId") String processInstanceId) {
        return success(approvalInstanceSnapshotService.getSnapshotByProcessInstanceId(processInstanceId));
    }

    @GetMapping("/get-by-scene-code-and-biz-id")
    @Operation(summary = "根据场景编码和业务ID获取审批快照")
    @Parameter(name = "sceneCode", description = "场景编码", required = true)
    @Parameter(name = "bizId", description = "业务单据ID", required = true)
    public CommonResult<BpmApprovalInstanceSnapshotDO> getSnapshotBySceneCodeAndBizId(
            @RequestParam("sceneCode") String sceneCode,
            @RequestParam("bizId") String bizId) {
        return success(approvalInstanceSnapshotService.getEffectiveSnapshotBySceneCodeAndBizId(sceneCode, bizId));
    }

}
