package cn.weitee.erp.module.erp.controller.admin.finance.vo.projectdualcost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 项目双账成本重跑 Request VO")
@Data
public class ErpFinanceDualProjectCostRebuildReqVO {

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @Schema(description = "期间（YYYY-MM）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-05")
    @NotEmpty(message = "期间不能为空")
    private String period;

    @Schema(description = "备注", example = "手动触发重跑")
    private String remark;
}
