package cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - ERP 历史出库批次重建 Response VO")
@Data
public class ErpStockBatchRebuildOutboundRespVO {

    @Schema(description = "扫描明细数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer scannedCount = 0;

    @Schema(description = "待重建明细数", requiredMode = Schema.RequiredMode.REQUIRED, example = "6")
    private Integer candidateCount = 0;

    @Schema(description = "可重建明细数", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer readyCount = 0;

    @Schema(description = "跳过明细数", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer skippedCount = 0;

    @Schema(description = "失败明细数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer failedCount = 0;

    @Schema(description = "已重建明细数", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer rebuiltCount = 0;

    @Schema(description = "明细")
    private List<Detail> details = new ArrayList<>();

    @Schema(description = "管理后台 - ERP 历史出库批次重建明细 Response VO")
    @Data
    public static class Detail {

        @Schema(description = "处理结果：READY、SKIPPED、FAILED、REBUILT", requiredMode = Schema.RequiredMode.REQUIRED)
        private String result;

        @Schema(description = "原因")
        private String reason;

        @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
        private Integer bizType;

        @Schema(description = "业务单据编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        private Long bizId;

        @Schema(description = "业务明细编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "101")
        private Long bizItemId;

        @Schema(description = "业务单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "XSCK202604290001")
        private String bizNo;

        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Long productId;

        @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
        private Long warehouseId;

        @Schema(description = "需重建数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "7.000")
        private BigDecimal requiredQty;

        @Schema(description = "可用批次数量", example = "10.000")
        private BigDecimal availableQty;

    }

}
