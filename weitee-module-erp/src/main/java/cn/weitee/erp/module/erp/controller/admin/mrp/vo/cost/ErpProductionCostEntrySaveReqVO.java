package cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 生产成本归集明细新增/修改 Request VO")
@Data
public class ErpProductionCostEntrySaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "生产工单不能为空")
    private Long productionOrderId;

    @Schema(description = "成本类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    @NotNull(message = "成本类型不能为空")
    private Integer costType;

    @Schema(description = "归集月份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-04")
    @NotBlank(message = "归集月份不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "归集月份格式必须为 yyyy-MM")
    private String accountingMonth;

    @Schema(description = "金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000.00")
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.000001", message = "金额必须大于 0")
    private BigDecimal amount;

    @Schema(description = "备注", example = "4 月人工费")
    private String remark;

}
