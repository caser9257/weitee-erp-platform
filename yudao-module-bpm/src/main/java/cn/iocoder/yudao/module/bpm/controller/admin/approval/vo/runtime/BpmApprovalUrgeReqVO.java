package cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.runtime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;

@Schema(description = "管理后台 - 审批催办 Request VO")
@Data
public class BpmApprovalUrgeReqVO {

    @Schema(description = "审批ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "xxx")
    @NotEmpty(message = "审批ID不能为空")
    private String approvalId;

    @Schema(description = "任务ID", example = "xxx")
    private String taskId;

    @Schema(description = "催办消息", requiredMode = Schema.RequiredMode.REQUIRED, example = "请尽快处理")
    @NotEmpty(message = "催办消息不能为空")
    private String message;

}
