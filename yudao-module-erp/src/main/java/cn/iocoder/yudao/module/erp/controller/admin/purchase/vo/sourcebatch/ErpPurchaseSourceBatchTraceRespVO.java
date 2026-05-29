package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.sourcebatch;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 采购来源批次追溯 Response VO")
@Data
public class ErpPurchaseSourceBatchTraceRespVO {

    @Schema(description = "采购来源批次")
    private ErpPurchaseSourceBatchRespVO sourceBatch;

    @Schema(description = "追溯明细列表")
    private List<TraceItem> traceItems;

    @Data
    public static class TraceItem {

        @Schema(description = "编号", example = "1")
        private Long id;

        @Schema(description = "采购入库执行明细编号", example = "1")
        private Long executeItemId;

        @Schema(description = "采购入库执行单编号", example = "1")
        private Long executeId;

        @Schema(description = "采购入库执行单号", example = "RKZX202604270001")
        private String executeNo;

        @Schema(description = "采购入库明细编号", example = "1")
        private Long purchaseInItemId;

        @Schema(description = "库存批次编号", example = "1")
        private Long stockBatchId;

        @Schema(description = "产品编号", example = "1")
        private Long productId;

        @Schema(description = "产品名称", example = "物料A")
        private String productName;

        @Schema(description = "仓库编号", example = "1")
        private Long warehouseId;

        @Schema(description = "仓库名称", example = "原料仓")
        private String warehouseName;

        @Schema(description = "库存批次号", example = "B202604270001")
        private String batchNo;

        @Schema(description = "采购来源批次编号", example = "1")
        private Long purchaseSourceBatchId;

        @Schema(description = "采购来源批次号", example = "CGLY20260427000001")
        private String purchaseSourceBatchNo;

        @Schema(description = "数量", example = "100.000")
        private BigDecimal count;

        @Schema(description = "入库时间")
        private LocalDateTime inboundTime;

        @Schema(description = "生产日期")
        private LocalDate produceDate;

        @Schema(description = "过期日期")
        private LocalDate expireDate;

        @Schema(description = "执行单状态", example = "20")
        private Integer executeStatus;

        @Schema(description = "执行单备注", example = "分批入库")
        private String executeRemark;

        @Schema(description = "备注", example = "批次追溯")
        private String remark;

        @Schema(description = "创建时间")
        private LocalDateTime createTime;

    }

}
