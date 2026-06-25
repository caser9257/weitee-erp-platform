package cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "管理后台 - ERP 生产工时归集新增/修改 Request VO")
@Data
public class ErpProductionManHourSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "生产工单不能为空")
    private Long productionOrderId;

    @Schema(description = "核算月份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-04")
    @NotBlank(message = "核算月份不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "核算月份格式必须为 yyyy-MM")
    private String accountingMonth;

    @Schema(description = "工时日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工时日期不能为空")
    private LocalDate workDate;

    @Schema(description = "工时", requiredMode = Schema.RequiredMode.REQUIRED, example = "8.50")
    @NotNull(message = "工时不能为空")
    @DecimalMin(value = "0.000001", message = "工时必须大于 0")
    private BigDecimal manHour;

    @Schema(description = "备注", example = "4 月总装工时")
    private String remark;

}
