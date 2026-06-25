package cn.weitee.erp.module.erp.controller.admin.finance.vo.asset;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 固定资产追溯 Response VO")
@Data
public class ErpFinanceAssetTraceRespVO {

    @Schema(description = "固定资产卡片")
    private ErpFinanceAssetRespVO asset;

    @Schema(description = "来源候选记录")
    private ErpFinanceAssetCandidateRespVO candidate;

    @Schema(description = "采购入库来源摘要")
    private PurchaseInSource purchaseIn;

    @Schema(description = "费用报销来源摘要")
    private ExpenseSource expense;

    @Data
    public static class PurchaseInSource {
        private Long id;
        private String no;
        private Integer status;
        private Long supplierId;
        private String supplierName;
        private Long accountId;
        private LocalDateTime inTime;
        private BigDecimal totalCount;
        private BigDecimal totalPrice;
        private BigDecimal paymentPrice;
        private String productNames;
        private String remark;
        private Long matchedItemId;
        private List<PurchaseInItem> items;
    }

    @Data
    public static class PurchaseInItem {
        private Long id;
        private Long productId;
        private String productName;
        private BigDecimal count;
        private BigDecimal totalPrice;
        private String remark;
        private Boolean matched;
    }

    @Data
    public static class ExpenseSource {
        private Long id;
        private String no;
        private Integer status;
        private LocalDateTime expenseTime;
        private Integer expenseType;
        private String expenseTypeName;
        private Long deptId;
        private String deptName;
        private Long projectId;
        private String projectName;
        private Long supplierId;
        private String supplierName;
        private Long financeUserId;
        private String financeUserName;
        private Long accountId;
        private String accountName;
        private BigDecimal expensePrice;
        private BigDecimal paidPrice;
        private BigDecimal remainPrice;
        private String remark;
        private Long matchedItemId;
        private List<ExpenseItem> items;
    }

    @Data
    public static class ExpenseItem {
        private Long id;
        private String itemName;
        private BigDecimal amount;
        private String remark;
        private Boolean assetCandidateFlag;
        private Boolean matched;
    }

}
