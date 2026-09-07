package cn.weitee.erp.module.erp.controller.admin.product.vo.unit;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.validation.InEnum;
import cn.weitee.erp.module.erp.enums.product.ErpProductUnitTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 产品单位新增/修改 Request VO")
@Data
public class ErpProductUnitSaveReqVO {

    @Schema(description = "单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31254")
    private Long id;

    @Schema(description = "单位名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotEmpty(message = "单位名字不能为空")
    private String name;

    @Schema(description = "单位状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "单位状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "单位类型，0 基本单位 1 辅助单位", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "单位类型不能为空")
    @InEnum(ErpProductUnitTypeEnum.class)
    private Integer unitType;

    @Schema(description = "基本单位编号，辅助单位必填；基本单位不传", example = "1")
    private Long baseUnitId;

    @Schema(description = "换算率：1 辅助单位 = conversionRate 基本单位；基本单位不传", example = "12")
    @DecimalMin(value = "0", inclusive = false, message = "换算率必须大于 0")
    private BigDecimal conversionRate;

    @Schema(description = "数量精度，0 表示只允许整数", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "数量精度不能为空")
    @Min(value = 0, message = "数量精度必须在 0 到 6 之间")
    @Max(value = 6, message = "数量精度必须在 0 到 6 之间")
    private Integer quantityPrecision;

}
