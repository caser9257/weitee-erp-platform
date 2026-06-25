package cn.weitee.erp.module.bpm.controller.admin.approval.vo.scene;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 审批场景新增/修改 Request VO")
@Data
public class BpmApprovalSceneSaveReqVO {

    @Schema(description = "场景编号", example = "1")
    private Long id;

    @Schema(description = "场景编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "erp.finance.payment.submit")
    @NotEmpty(message = "场景编码不能为空")
    private String sceneCode;

    @Schema(description = "场景名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "付款单提交审批")
    @NotEmpty(message = "场景名称不能为空")
    private String name;

    @Schema(description = "模块编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "erp_finance")
    @NotEmpty(message = "模块编码不能为空")
    private String moduleCode;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "payment")
    @NotEmpty(message = "业务类型不能为空")
    private String bizType;

    @Schema(description = "动作编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "submit")
    @NotEmpty(message = "动作编码不能为空")
    private String actionCode;

    @Schema(description = "当前生效方案编号", example = "1")
    private Long activeSchemeId;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "付款单提交审批场景")
    private String remark;

}
