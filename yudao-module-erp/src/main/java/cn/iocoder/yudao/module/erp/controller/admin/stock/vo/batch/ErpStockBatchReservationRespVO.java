package cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 批次出库预占 Response VO")
@Data
public class ErpStockBatchReservationRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Integer bizType;

    @Schema(description = "业务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long bizId;

    @Schema(description = "业务明细编号", example = "101")
    private Long bizItemId;

    @Schema(description = "业务单号", example = "XSCK202604300001")
    private String bizNo;

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

    @Schema(description = "批次库存编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11")
    private Long stockBatchId;

    @Schema(description = "批次号", requiredMode = Schema.RequiredMode.REQUIRED, example = "B001")
    private String batchNo;

    @Schema(description = "预占数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "7.000")
    private BigDecimal reservedQty;

    @Schema(description = "入库时间")
    private LocalDateTime inboundTime;

    @Schema(description = "生产日期")
    private LocalDate produceDate;

    @Schema(description = "失效日期")
    private LocalDate expireDate;

    @Schema(description = "备注")
    private String remark;

}
