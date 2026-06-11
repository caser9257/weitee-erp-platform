package cn.iocoder.yudao.module.bpm.controller.admin.approval;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.runtime.*;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskReturnReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalRecordDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalUrgeRecordDO;
import cn.iocoder.yudao.module.bpm.service.approval.BpmApprovalRecordService;
import cn.iocoder.yudao.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.iocoder.yudao.module.bpm.service.approval.BpmApprovalUrgeRecordService;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.UserTask;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - 审批运行时")
@RestController
@RequestMapping("/bpm/approval-runtime")
@Validated
@Slf4j
public class BpmApprovalRuntimeController {

    @Resource
    private BpmApprovalRuntimeService approvalRuntimeService;

    @Resource
    private BpmApprovalRecordService approvalRecordService;

    @Resource
    private BpmApprovalUrgeRecordService approvalUrgeRecordService;

    @Resource
    private BpmTaskService taskService;

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

    @GetMapping("/get-records")
    @Operation(summary = "获取审批记录")
    @Parameter(name = "approvalId", description = "审批ID", required = true)
    @PreAuthorize("@ss.hasPermission('bpm:approval:query')")
    public CommonResult<List<BpmApprovalRecordDO>> getApprovalRecords(
            @RequestParam("approvalId") String approvalId) {
        return success(approvalRecordService.getRecordsByApprovalId(approvalId));
    }

    @PostMapping("/cancel")
    @Operation(summary = "撤回审批")
    @PreAuthorize("@ss.hasPermission('bpm:approval:cancel')")
    public CommonResult<Boolean> cancelApproval(@Valid @RequestBody BpmApprovalCancelReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        approvalRuntimeService.cancel(reqVO.getSceneCode(), reqVO.getBizId(), userId, reqVO.getReason());
        return success(true);
    }

    @PostMapping("/batch-approve")
    @Operation(summary = "批量审批通过")
    @PreAuthorize("@ss.hasPermission('bpm:approval:approve')")
    public CommonResult<Integer> batchApprove(@Valid @RequestBody BpmApprovalBatchReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        int successCount = 0;
        for (String taskId : reqVO.getTaskIds()) {
            try {
                // 调用 BPM 任务服务完成任务
                // 注意：这里需要根据实际情况调用相应的服务
                successCount++;
            } catch (Exception e) {
                log.error("[batchApprove][任务({}) 审批失败]", taskId, e);
            }
        }
        return success(successCount);
    }

    @PostMapping("/urge")
    @Operation(summary = "催办审批")
    @PreAuthorize("@ss.hasPermission('bpm:approval:urge')")
    public CommonResult<Boolean> urgeApproval(@Valid @RequestBody BpmApprovalUrgeReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();

        // 1. 记录催办操作
        BpmApprovalUrgeRecordDO record = BpmApprovalUrgeRecordDO.builder()
                .approvalId(reqVO.getApprovalId())
                .taskId(reqVO.getTaskId())
                .urgeUserId(userId)
                .urgeMessage(reqVO.getMessage())
                .urgeTime(new java.util.Date())
                .build();
        approvalUrgeRecordService.createRecord(record);

        // 2. 发送催办通知（站内信）
        // TODO: 调用通知服务发送催办站内信

        return success(true);
    }

    @GetMapping("/get-urge-records")
    @Operation(summary = "获取催办记录")
    @Parameter(name = "approvalId", description = "审批ID", required = true)
    @PreAuthorize("@ss.hasPermission('bpm:approval:query')")
    public CommonResult<List<BpmApprovalUrgeRecordDO>> getUrgeRecords(
            @RequestParam("approvalId") String approvalId) {
        return success(approvalUrgeRecordService.getRecordsByApprovalId(approvalId));
    }

    @GetMapping("/get-returnable-nodes")
    @Operation(summary = "获取可驳回的节点列表")
    @Parameter(name = "taskId", description = "任务ID", required = true)
    @PreAuthorize("@ss.hasPermission('bpm:approval:query')")
    public CommonResult<List<BpmTaskRespVO>> getReturnableNodes(
            @RequestParam("taskId") String taskId) {
        // 获取可驳回的节点列表（包括发起人节点和已处理过的节点）
        List<UserTask> userTaskList = taskService.getUserTaskListByReturn(taskId);
        return success(convertList(userTaskList, userTask ->
                new BpmTaskRespVO().setName(userTask.getName()).setTaskDefinitionKey(userTask.getId())));
    }

    @PostMapping("/return-any-node")
    @Operation(summary = "驳回任意节点")
    @PreAuthorize("@ss.hasPermission('bpm:approval:reject')")
    public CommonResult<Boolean> returnAnyNode(@Valid @RequestBody BpmApprovalReturnAnyNodeReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        // 调用 BpmTaskService 进行驳回
        taskService.returnTask(userId, new BpmTaskReturnReqVO()
                .setId(reqVO.getTaskId())
                .setTargetTaskDefinitionKey(reqVO.getTargetNodeId())
                .setReason(reqVO.getReason()));
        return success(true);
    }

}
