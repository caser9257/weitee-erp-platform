package cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 批次调整单 Response VO")
@Data
public class ErpStockBatchAdjustmentRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "501")
    private Long id;

    @Schema(description = "调整单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "TZ202604290001")
    private String adjustNo;

    @Schema(description = "批次库存编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11")
    private Long stockBatchId;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long productId;

    @Schema(description = "产品名称", example = "钢板")
    private String productName;

    @Schema(description = "物料编码", example = "MAT-001")
    private String materialCode;

    @Schema(description = "单位", example = "KG")
    private String unitName;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Long warehouseId;

    @Schema(description = "仓库名称", example = "原料仓")
    private String warehouseName;

    @Schema(description = "批次号", requiredMode = Schema.RequiredMode.REQUIRED, example = "B001")
    private String batchNo;

    @Schema(description = "调整类型：1 调增，2 调减", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer adjustType;

    @Schema(description = "调整数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "3.000")
    private BigDecimal adjustQty;

    @Schema(description = "调整前总量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5.000")
    private BigDecimal beforeTotalQty;

    @Schema(description = "调整前可用量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5.000")
    private BigDecimal beforeAvailableQty;

    @Schema(description = "调整后总量", requiredMode = Schema.RequiredMode.REQUIRED, example = "8.000")
    private BigDecimal afterTotalQty;

    @Schema(description = "调整后可用量", requiredMode = Schema.RequiredMode.REQUIRED, example = "8.000")
    private BigDecimal afterAvailableQty;

    @Schema(description = "备注", example = "期初补录")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
