package cn.weitee.erp.module.erp.controller.admin.purchase.vo.in;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "ERP purchase in response")
@Data
@ExcelIgnoreUnannotated
public class ErpPurchaseInRespVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17386")
    @ExcelProperty("ID")
    private Long id;

    @Schema(description = "Purchase in no", requiredMode = Schema.RequiredMode.REQUIRED, example = "RK20260424001")
    @ExcelProperty("Purchase In No")
    private String no;

    @Schema(description = "Status", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    @ExcelProperty("Status")
    private Integer status;

    private Integer qaStatus;
    private String processInstanceId;

    @Schema(description = "Supplier id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1724")
    private Long supplierId;

    @Schema(description = "Supplier name", example = "Supplier A")
    @ExcelProperty("Supplier Name")
    private String supplierName;

    @Schema(description = "Account id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("Account ID")
    private Long accountId;

    @Schema(description = "In time", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("In Time")
    private LocalDateTime inTime;

    @Schema(description = "Order id", requiredMode = Schema.RequiredMode.REQUIRED, example = "17386")
    private Long orderId;

    @Schema(description = "Order no", requiredMode = Schema.RequiredMode.REQUIRED, example = "CG20260424001")
    private String orderNo;

    @Schema(description = "Total count", requiredMode = Schema.RequiredMode.REQUIRED, example = "15663")
    @ExcelProperty("Total Count")
    private BigDecimal totalCount;

    @Schema(description = "Total price", requiredMode = Schema.RequiredMode.REQUIRED, example = "24906")
    @ExcelProperty("Total Price")
    private BigDecimal totalPrice;

    @Schema(description = "Payment price", requiredMode = Schema.RequiredMode.REQUIRED, example = "7127")
    private BigDecimal paymentPrice;

    @Schema(description = "Total product price", requiredMode = Schema.RequiredMode.REQUIRED, example = "7127")
    private BigDecimal totalProductPrice;

    @Schema(description = "Total tax price", requiredMode = Schema.RequiredMode.REQUIRED, example = "7127")
    private BigDecimal totalTaxPrice;

    @Schema(description = "Discount percent", requiredMode = Schema.RequiredMode.REQUIRED, example = "99.88")
    private BigDecimal discountPercent;

    @Schema(description = "Discount price", requiredMode = Schema.RequiredMode.REQUIRED, example = "7127")
    private BigDecimal discountPrice;

    @Schema(description = "Other price", requiredMode = Schema.RequiredMode.REQUIRED, example = "7127")
    private BigDecimal otherPrice;

    @Schema(description = "File url", example = "https://www.iocoder.cn")
    @ExcelProperty("File URL")
    private String fileUrl;

    @Schema(description = "Remark", example = "Remark")
    @ExcelProperty("Remark")
    private String remark;

    private String lastRejectReason;
    private LocalDateTime lastRejectTime;
    private Long lastRejectUserId;
    private LocalDateTime qaTime;
    private Long qaUserId;
    private String qaUserNickname;
    private String qaRemark;
    private BigDecimal qaPassCount;
    private BigDecimal qaRejectCount;
    private BigDecimal stockInCount;
    private BigDecimal remainingStockInCount;
    private Integer stockInStatus;
    private LocalDateTime stockInTime;
    private Long stockInUserId;
    private String stockInUserNickname;

    @Schema(description = "Creator", example = "admin")
    private String creator;

    @Schema(description = "Creator name", example = "Admin")
    private String creatorName;

    @Schema(description = "Create time", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("Create Time")
    private LocalDateTime createTime;

    @Schema(description = "Items", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Item> items;

    @Schema(description = "Product names", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("Product Names")
    private String productNames;

    @Schema(description = "Audit logs")
    private List<ErpPurchaseInAuditLogRespVO> auditLogs;

    private List<StockExecute> stockExecuteList;

    @Data
    public static class Item {

        @Schema(description = "ID", example = "11756")
        private Long id;

        @Schema(description = "Order item id", requiredMode = Schema.RequiredMode.REQUIRED, example = "11756")
        private Long orderItemId;

        @Schema(description = "Warehouse id", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        private Long warehouseId;

        @Schema(description = "Product id", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        private Long productId;

        @Schema(description = "Purchase source batch id", example = "1")
        private Long purchaseSourceBatchId;

        @Schema(description = "Purchase source batch no", example = "CGLY20260427000001")
        private String purchaseSourceBatchNo;

        @Schema(description = "Product unit id", requiredMode = Schema.RequiredMode.REQUIRED, example = "3113")
        private Long productUnitId;

        @Schema(description = "Product price", example = "100.00")
        private BigDecimal productPrice;

        @Schema(description = "Engineering fee", example = "2000.00")
        private BigDecimal engineeringFee;

        @Schema(description = "Pricing bom id", example = "1001")
        private Long pricingBomId;

        @Schema(description = "Pricing bom version", example = "V1.0")
        private String pricingBomVersion;

        @Schema(description = "Count", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
        @NotNull(message = "count cannot be empty")
        private BigDecimal count;

        @Schema(description = "Tax percent", example = "99.88")
        private BigDecimal taxPercent;

        @Schema(description = "Tax price", example = "100.00")
        private BigDecimal taxPrice;

        private BigDecimal qaPassCount;
        private BigDecimal qaRejectCount;
        private BigDecimal stockInCount;
        private BigDecimal remainingStockInCount;
        private String remark;
        private String qaRemark;
        private String productName;
        private String productBarCode;
        private String productUnitName;
        private Boolean batchControlFlag;
        private BigDecimal stockCount;

    }

    @Data
    public static class StockExecute {

        private Long id;
        private String no;
        private Integer status;
        private String remark;
        private LocalDateTime createTime;
        private String creator;
        private String creatorName;
        private List<StockExecuteItem> items;

    }

    @Data
    public static class StockExecuteItem {

        private Long id;
        private Long executeId;
        private Long purchaseInItemId;
        private Long productId;
        private Long warehouseId;
        private BigDecimal count;
        private String remark;
        private String productName;
        private String productBarCode;
        private String productUnitName;
        private List<StockExecuteItemBatch> batches;

    }

    @Data
    public static class StockExecuteItemBatch {

        private Long id;
        private Long executeItemId;
        private Long purchaseInItemId;
        private Long stockBatchId;
        private Long purchaseSourceBatchId;
        private String batchNo;
        private String purchaseSourceBatchNo;
        private BigDecimal count;
        private LocalDateTime inboundTime;
        private LocalDate produceDate;
        private LocalDate expireDate;
        private String remark;

    }

}
