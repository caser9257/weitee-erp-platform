package cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "管理后台 - ERP 生产工时项目汇总 Request VO")
@Data
public class ErpProductionManHourProjectSummaryReqVO {

    @Schema(description = "核算月份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-04")
    @NotBlank(message = "核算月份不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "核算月份格式必须为 yyyy-MM")
    private String accountingMonth;

    @Schema(description = "项目编号", example = "2001")
    private Long projectId;

}
