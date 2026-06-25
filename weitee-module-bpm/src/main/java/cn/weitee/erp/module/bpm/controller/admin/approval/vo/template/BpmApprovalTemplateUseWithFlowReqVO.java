package cn.weitee.erp.module.bpm.controller.admin.approval.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

@Schema(description = "管理后台 - 使用审批模板（带自定义流程配置）请求 VO")
@Data
public class BpmApprovalTemplateUseWithFlowReqVO {

    @Schema(description = "模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "模板编号不能为空")
    private Long templateId;

    @Schema(description = "自定义流程配置 JSON", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "流程配置不能为空")
    private Map<String, Object> flowConfig;

}
