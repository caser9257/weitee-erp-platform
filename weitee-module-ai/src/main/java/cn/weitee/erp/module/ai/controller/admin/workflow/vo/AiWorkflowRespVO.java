package cn.weitee.erp.module.ai.controller.admin.workflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI 工作�?Response VO")
@Data
public class AiWorkflowRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "工作流标�?, requiredMode = Schema.RequiredMode.REQUIRED, example = "FLOW")
    private String code;

    @Schema(description = "工作流名�?, requiredMode = Schema.RequiredMode.REQUIRED, example = "工作�?)
    private String name;

    @Schema(description = "备注", requiredMode = Schema.RequiredMode.REQUIRED, example = "工作�?)
    private String remark;

    @Schema(description = "状�?, requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "工作流模�?JSON", requiredMode = Schema.RequiredMode.REQUIRED, example = "{}")
    private String graph;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "时间戳格�?)
    private LocalDateTime createTime;

}
