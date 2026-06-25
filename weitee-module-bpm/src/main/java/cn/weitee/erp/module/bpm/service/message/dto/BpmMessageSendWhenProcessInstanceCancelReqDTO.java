package cn.weitee.erp.module.bpm.service.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 流程实例撤回时发送消息的 DTO
 */
@Data
@Schema(description = "流程实例撤回时发送消息的 DTO")
public class BpmMessageSendWhenProcessInstanceCancelReqDTO {

    @Schema(description = "流程实例的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "流程实例的编号不能为空")
    private String processInstanceId;

    @Schema(description = "流程实例的名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
    @NotNull(message = "流程实例的名称不能为空")
    private String processInstanceName;

    @Schema(description = "发起人的用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "发起人的用户编号不能为空")
    private Long startUserId;

    @Schema(description = "撤回原因", example = "信息有误")
    private String reason;

}
