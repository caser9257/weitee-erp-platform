package cn.weitee.erp.module.erp.controller.admin.finance.vo.apinvoice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 采购发票 Response VO")
@Data
public class ErpApInvoiceRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "供应商编号", example = "201")
    private Long supplierId;

    @Schema(description = "供应商名称", example = "广州供应商")
    private String supplierName;

    @Schema(description = "发票号", example = "FP-20260428-001")
    private String invoiceNo;

    @Schema(description = "发票日期")
    private LocalDateTime invoiceDate;

    @Schema(description = "发票类型", example = "10")
    private Integer invoiceType;

    @Schema(description = "发票类型名称", example = "专票")
    private String invoiceTypeName;

    @Schema(description = "发票总数量", example = "10")
    private BigDecimal totalCount;

    @Schema(description = "已匹配数量", example = "8")
    private BigDecimal matchedCount;

    @Schema(description = "发票总金额", example = "100.40")
    private BigDecimal totalAmount;

    @Schema(description = "已匹配金额", example = "100")
    private BigDecimal matchedAmount;

    @Schema(description = "未匹配金额", example = "0.40")
    private BigDecimal unmatchedAmount;

    @Schema(description = "尾差容忍金额", example = "1")
    private BigDecimal toleranceAmount;

    @Schema(description = "差异金额", example = "0.40")
    private BigDecimal differenceAmount;

    @Schema(description = "匹配状态", example = "30")
    private Integer matchStatus;

    @Schema(description = "匹配状态名称", example = "已匹配")
    private String matchStatusName;

    @Schema(description = "差异原因", example = "供应商尾差")
    private String differenceReason;

    @Schema(description = "备注", example = "月末集中开票")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "匹配明细列表")
    private List<Item> items;

    @Data
    public static class Item {

        @Schema(description = "匹配明细编号", example = "1")
        private Long id;

        @Schema(description = "应付台账编号", example = "31")
        private Long apStatementId;

        @Schema(description = "采购订单编号", example = "21")
        private Long sourceOrderId;

        @Schema(description = "采购订单号", example = "PO-001")
        private String sourceOrderNo;

        @Schema(description = "采购入库编号", example = "11")
        private Long sourcePurchaseInId;

        @Schema(description = "采购入库单号", example = "PI-001")
        private String sourcePurchaseInNo;

        @Schema(description = "采购入库明细编号", example = "1001")
        private Long sourcePurchaseInItemId;

        @Schema(description = "产品编号", example = "301")
        private Long productId;

        @Schema(description = "产品名称", example = "螺丝")
        private String productName;

        @Schema(description = "匹配数量", example = "10")
        private BigDecimal matchCount;

        @Schema(description = "匹配金额", example = "100")
        private BigDecimal matchAmount;

        @Schema(description = "状态", example = "10")
        private Integer status;

        @Schema(description = "状态名称", example = "生效")
        private String statusName;

        @Schema(description = "备注", example = "整单收票")
        private String remark;

        @Schema(description = "创建时间")
        private LocalDateTime createTime;

    }

}
