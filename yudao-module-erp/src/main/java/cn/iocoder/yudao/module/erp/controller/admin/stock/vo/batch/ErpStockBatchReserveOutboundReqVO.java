package cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 批次出库预占 Request VO")
@Data
public class ErpStockBatchReserveOutboundReqVO {

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "产品编号不能为空")
    private Long productId;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "仓库编号不能为空")
    private Long warehouseId;

    @Schema(description = "预占数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "7.000")
    @NotNull(message = "预占数量不能为空")
    @DecimalMin(value = "0.000001", message = "预占数量必须大于 0")
    private BigDecimal count;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    @NotNull(message = "业务类型不能为空")
    private Integer bizType;

    @Schema(description = "业务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "业务编号不能为空")
    private Long bizId;

    @Schema(description = "业务明细编号", example = "101")
    private Long bizItemId;

    @Schema(description = "业务单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "XSCK202604300001")
    @NotBlank(message = "业务单号不能为空")
    @Size(max = 64, message = "业务单号长度不能超过 64 个字符")
    private String bizNo;

    @Schema(description = "备注", example = "销售出库预占")
    @Size(max = 255, message = "备注长度不能超过 255 个字符")
    private String remark;

}
