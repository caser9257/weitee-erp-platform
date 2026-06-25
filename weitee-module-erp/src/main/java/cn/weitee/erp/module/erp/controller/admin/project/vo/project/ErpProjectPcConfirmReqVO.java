package cn.weitee.erp.module.erp.controller.admin.project.vo.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 项目 PC 确认 Request VO")
@Data
public class ErpProjectPcConfirmReqVO {

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "PC 备注", requiredMode = Schema.RequiredMode.REQUIRED, example = "已确认排期与交付节点")
    @NotBlank(message = "PC备注不能为空")
    private String remark;

    @Schema(description = "当前阶段编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "PLAN_CONFIRMED")
    @NotBlank(message = "阶段不能为空")
    private String currentStageCode;

}
