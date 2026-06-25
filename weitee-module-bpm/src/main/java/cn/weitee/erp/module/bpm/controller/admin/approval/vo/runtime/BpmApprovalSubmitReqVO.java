package cn.weitee.erp.module.bpm.controller.admin.approval.vo.runtime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 审批提交 Request VO")
@Data
public class BpmApprovalSubmitReqVO {

    @Schema(description = "场景编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "erp.sale.order.submit")
    @NotEmpty(message = "场景编码不能为空")
    private String sceneCode;

    @Schema(description = "业务单据ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "业务单据ID不能为空")
    private Long bizId;

}
