package cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.erp.enums.stock.ErpStockBatchAdjustTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 批次库存调整 Request VO")
@Data
public class ErpStockBatchAdjustReqVO {

    @Schema(description = "批次库存编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11")
    @NotNull(message = "批次库存编号不能为空")
    private Long stockBatchId;

    @Schema(description = "调整类型：1 调增，2 调减", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "调整类型不能为空")
    @InEnum(value = ErpStockBatchAdjustTypeEnum.class, message = "调整类型必须是 {value}")
    private Integer adjustType;

    @Schema(description = "调整数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "3.000")
    @NotNull(message = "调整数量不能为空")
    @DecimalMin(value = "0.000001", message = "调整数量必须大于 0")
    private BigDecimal count;

    @Schema(description = "调整单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "TZ202604290001")
    @NotBlank(message = "调整单号不能为空")
    @Size(max = 64, message = "调整单号长度不能超过 64 个字符")
    private String adjustNo;

    @Schema(description = "备注", example = "期初补录")
    @Size(max = 255, message = "备注长度不能超过 255 个字符")
    private String remark;

}
