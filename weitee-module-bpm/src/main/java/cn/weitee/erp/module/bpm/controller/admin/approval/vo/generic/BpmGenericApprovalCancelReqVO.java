package cn.weitee.erp.module.bpm.controller.admin.approval.vo.generic;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 通用审批撤回 Request VO")
@Data
public class BpmGenericApprovalCancelReqVO {

    @Schema(description = "场景编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "erp.stock.check.submit")
    @NotEmpty(message = "场景编码不能为空")
    private String sceneCode;

    @Schema(description = "业务单据 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "业务单据 ID 不能为空")
    private Long bizId;

    @Schema(description = "撤回原因", example = "单据信息有误")
    private String reason;

}
