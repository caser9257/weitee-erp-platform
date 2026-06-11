package cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.runtime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;

@Schema(description = "管理后台 - 驳回任意节点 Request VO")
@Data
public class BpmApprovalReturnAnyNodeReqVO {

    @Schema(description = "任务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "xxx")
    @NotEmpty(message = "任务ID不能为空")
    private String taskId;

    @Schema(description = "目标节点ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "startNode")
    @NotEmpty(message = "目标节点ID不能为空")
    private String targetNodeId;

    @Schema(description = "驳回原因", example = "信息有误，需要修改")
    private String reason;

}
