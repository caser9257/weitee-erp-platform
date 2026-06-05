package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpProductionCostTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 生产成本分摊单新增/修改 Request VO")
@Data
public class ErpProductionCostAllocationSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "核算月份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-04")
    @NotBlank(message = "核算月份不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "核算月份格式必须为 yyyy-MM")
    private String accountingMonth;

    @Schema(description = "成本类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    @NotNull(message = "成本类型不能为空")
    @InEnum(value = ErpProductionCostTypeEnum.class, message = "成本类型必须是 {value}")
    private Integer costType;

    @Schema(description = "分摊规则编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11")
    @NotNull(message = "分摊规则不能为空")
    private Long ruleId;

    @Schema(description = "待分摊总金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000.00")
    @NotNull(message = "待分摊总金额不能为空")
    @DecimalMin(value = "0.000001", message = "待分摊总金额必须大于 0")
    private BigDecimal totalAmount;

    @Schema(description = "备注", example = "4 月人工分摊")
    private String remark;

}
