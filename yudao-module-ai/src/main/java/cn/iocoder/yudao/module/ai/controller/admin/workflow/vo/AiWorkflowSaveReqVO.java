package cn.iocoder.yudao.module.ai.controller.admin.workflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - AI 工作流新�?修改 Request VO")
@Data
public class AiWorkflowSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "工作流标�?, requiredMode = Schema.RequiredMode.REQUIRED, example = "FLOW")
    @NotEmpty(message = "工作流标识不能为�?)
    private String code;

    @Schema(description = "工作流名�?, requiredMode = Schema.RequiredMode.REQUIRED, example = "工作�?)
    @NotEmpty(message = "工作流名称不能为�?)
    private String name;

    @Schema(description = "备注", example = "FLOW")
    private String remark;

    @Schema(description = "工作流模�?, requiredMode = Schema.RequiredMode.REQUIRED, example = "{}")
    @NotEmpty(message = "工作流模型不能为�?)
    private String graph;

    @Schema(description = "状�?, requiredMode = Schema.RequiredMode.REQUIRED, example = "FLOW")
    @NotNull(message = "状态不能为�?)
    private Integer status;

}
