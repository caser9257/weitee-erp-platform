package cn.iocoder.yudao.module.bpm.service.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 任务委托时发送消息的 DTO
 */
@Data
@Schema(description = "任务委托时发送消息的 DTO")
public class BpmMessageSendWhenTaskDelegateReqDTO {

    @Schema(description = "任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "任务编号不能为空")
    private String taskId;

    @Schema(description = "流程实例的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "流程实例的编号不能为空")
    private String processInstanceId;

    @Schema(description = "流程实例的名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
    @NotNull(message = "流程实例的名称不能为空")
    private String processInstanceName;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "部门经理审批")
    @NotNull(message = "任务名称不能为空")
    private String taskName;

    @Schema(description = "代理人的用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "代理人的用户编号不能为空")
    private Long delegateUserId;

    @Schema(description = "委托人的用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "委托人的用户编号不能为空")
    private Long ownerUserId;

    @Schema(description = "委托原因", example = "出差期间")
    private String reason;

}
