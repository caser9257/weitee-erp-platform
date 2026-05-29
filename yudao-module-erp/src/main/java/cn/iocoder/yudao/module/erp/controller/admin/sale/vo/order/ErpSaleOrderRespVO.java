package cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order;

import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 销售订单 Response VO")
@Data
@Accessors(chain = true)
@ExcelIgnoreUnannotated
public class ErpSaleOrderRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17386")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "销售单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "XS001")
    @ExcelProperty("销售单编号")
    private String no;

    @Schema(description = "销售状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("销售状态")
    private Integer status;
    @Schema(description = "BPM 流程实例编号", example = "PI-20260407-001")
    private String processInstanceId;

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1724")
    private Long customerId;
    @Schema(description = "客户名称", example = "芋道")
    @ExcelProperty("客户名称")
    private String customerName;

    @Schema(description = "项目编号", example = "1024")
    private Long projectId;
    @Schema(description = "项目名称", example = "MES 阶段一")
    @ExcelProperty("项目名称")
    private String projectName;
    @Schema(description = "业务类型", example = "SELF_RESEARCH")
    @ExcelProperty("业务类型")
    private String businessType;
    @Schema(description = "来源研发项目编号", example = "1")
    private Long sourceProjectId;
    @Schema(description = "结算类型", example = "PRODUCT_SALE")
    @ExcelProperty("结算类型")
    private String settlementType;
    @Schema(description = "来源产品编号", example = "1")
    private Long sourceProductId;

    @Schema(description = "结算账户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "311.89")
    @ExcelProperty("结算账户编号")
    private Long accountId;

    @Schema(description = "销售员编号", example = "1888")
    private Long saleUserId;

    @Schema(description = "销售员名称", example = "芋道")
    private String saleUserName;

    @Schema(description = "下单时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("下单时间")
    private LocalDateTime orderTime;

    @Schema(description = "交期")
    @ExcelProperty("交期")
    private LocalDateTime deliveryDate;

    @Schema(description = "合计数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "15663")
    @ExcelProperty("合计数量")
    private BigDecimal totalCount;

    @Schema(description = "合计数量单位", example = "PCS")
    private String totalCountUnitName;

    @Schema(description = "最终合计价格", requiredMode = Schema.RequiredMode.REQUIRED, example = "24906")
    @ExcelProperty("最终合计价格")
    private BigDecimal totalPrice;

    @Schema(description = "合计产品价格，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "7127")
    private BigDecimal totalProductPrice;

    @Schema(description = "合计税额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "7127")
    private BigDecimal totalTaxPrice;

    @Schema(description = "优惠率，百分比", requiredMode = Schema.RequiredMode.REQUIRED, example = "99.88")
    private BigDecimal discountPercent;

    @Schema(description = "优惠金额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "7127")
    private BigDecimal discountPrice;

    @Schema(description = "定金金额，单位：元", requiredMode = Schema.RequiredMode.REQUIRED, example = "7127")
    private BigDecimal depositPrice;

    @Schema(description = "最近一次驳回原因", example = "资料不完整")
    private String lastRejectReason;

    @Schema(description = "最近一次驳回时间")
    private LocalDateTime lastRejectTime;

    @Schema(description = "最近一次驳回人编号", example = "1")
    private Long lastRejectUserId;

    @Schema(description = "附件地址", example = "https://www.iocoder.cn")
    @ExcelProperty("附件地址")
    private String fileUrl;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建人", example = "芋道")
    private String creator;
    @Schema(description = "创建人名称", example = "芋道")
    private String creatorName;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "订单项列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Item> items;

    @Schema(description = "驳回历史")
    private List<ErpSaleOrderRejectLogRespVO> rejectLogs;

    @Schema(description = "审批流转历史")
    private List<ErpSaleOrderAuditLogRespVO> auditLogs;

    @Schema(description = "产品信息", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("产品信息")
    private String productNames;

    // ========== 销售出库 ==========

    @Schema(description = "销售出库数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    private BigDecimal outCount;

    // ========== 销售退货（出库）） ==========

    @Schema(description = "销售退货数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    private BigDecimal returnCount;

    @Schema(description = "交付准备状态", example = "READY_TO_SHIP")
    private String deliveryReadyStatus;

    @Schema(description = "销售闭环摘要")
    private ClosureSummary closureSummary;

    @Data
    @Accessors(chain = true)
    public static class Item {

        @Schema(description = "订单项编号", example = "11756")
        private Long id;

        @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        private Long productId;

        @Schema(description = "产品单位单位", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        private Long productUnitId;

        @Schema(description = "产品单价", example = "100.00")
        private BigDecimal productPrice;

        @Schema(description = "产品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        @NotNull(message = "产品数量不能为空")
        private BigDecimal count;

        @Schema(description = "税率，百分比", example = "99.88")
        private BigDecimal taxPercent;

        @Schema(description = "税额，单位：元", example = "100.00")
        private BigDecimal taxPrice;

        @Schema(description = "产品金额", example = "100.00")
        private BigDecimal totalProductPrice;

        @Schema(description = "含税金额", example = "100.00")
        private BigDecimal totalPrice;

        @Schema(description = "备注", example = "随便")
        private String remark;

        // ========== 销售出库 ==========

        @Schema(description = "销售出库数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        private BigDecimal outCount;

        // ========== 销售退货（入库）） ==========

        @Schema(description = "销售退货数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        private BigDecimal returnCount;

        // ========== 关联字段 ==========

        @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "巧克力")
        private String productName;
        @Schema(description = "产品条码", requiredMode = Schema.RequiredMode.REQUIRED, example = "A9985")
        private String productBarCode;
        @Schema(description = "产品单位名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "盒")
        private String productUnitName;

        @Schema(description = "库存数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        private BigDecimal stockCount; // 该字段仅仅在“详情”和“编辑”时使用

    }

    @Data
    public static class ClosureSummary {

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

}
