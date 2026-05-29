package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - ERP 销售订单闭环摘要 Response VO")
@Data
public class ErpSaleOrderClosureSummaryRespVO {

    @Schema(description = "销售单编号", example = "1024")
    private Long saleOrderId;

    @Schema(description = "销售单号", example = "XS202604210001")
    private String saleOrderNo;

    @Schema(description = "剩余待交付数量", example = "10")
    private BigDecimal remainingShipQty;

    @Schema(description = "成品质检合格数量", example = "8")
    private BigDecimal productionQualifiedQty;

    @Schema(description = "交付准备状态", example = "PART_READY")
    private String deliveryReadyStatus;

    @Schema(description = "当前主阶段", example = "WAIT_PURCHASE_IQC")
    private String closureStage;

    @Schema(description = "阻塞原因编码")
    private List<String> blockerCodes;

    @Schema(description = "采购建议总数", example = "2")
    private Long purchaseSuggestCount;
    @Schema(description = "已确认采购建议数", example = "1")
    private Long purchaseConfirmedSuggestCount;
    @Schema(description = "已转采购建议数", example = "1")
    private Long purchaseConvertedSuggestCount;
    @Schema(description = "已驳回采购建议数", example = "0")
    private Long purchaseRejectedSuggestCount;

    @Schema(description = "生产建议总数", example = "1")
    private Long productionSuggestCount;
    @Schema(description = "已确认生产建议数", example = "1")
    private Long productionConfirmedSuggestCount;
    @Schema(description = "已转工单建议数", example = "1")
    private Long productionConvertedSuggestCount;
    @Schema(description = "已驳回生产建议数", example = "0")
    private Long productionRejectedSuggestCount;

    @Schema(description = "采购单总数", example = "1")
    private Long purchaseOrderCount;
    @Schema(description = "已审批采购单数", example = "1")
    private Long approvedPurchaseOrderCount;

    @Schema(description = "采购入库单总数", example = "1")
    private Long purchaseInCount;
    @Schema(description = "已审批采购入库单数", example = "1")
    private Long approvedPurchaseInCount;
    @Schema(description = "待质检采购入库单数", example = "1")
    private Long pendingQaPurchaseInCount;
    @Schema(description = "待入库采购入库单数", example = "0")
    private Long pendingStockInPurchaseInCount;
    @Schema(description = "已完成入库单数", example = "0")
    private Long stockedPurchaseInCount;
    @Schema(description = "无需入库单数", example = "0")
    private Long noNeedStockInPurchaseInCount;
}
