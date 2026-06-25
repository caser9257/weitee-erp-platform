package cn.weitee.erp.module.bpm.controller.admin.approval.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 使用模板 Request VO")
@Data
public class BpmApprovalTemplateUseReqVO {

    @Schema(description = "模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "模板编号不能为空")
    private Long templateId;

}
