package cn.weitee.erp.module.erp.controller.admin.finance.vo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - ERP 财务正式报表 Response VO")
@Data
public class ErpFinanceStatementRespVO {

    @Schema(description = "账簿编号", example = "1")
    private Long ledgerId;

    @Schema(description = "账簿名称", example = "标准账簿")
    private String ledgerName;

    @Schema(description = "期间编号", example = "1")
    private Long periodId;

    @Schema(description = "期间编码", example = "2026-04")
    private String periodCode;

    @Schema(description = "报表类型", example = "10")
    private Integer reportType;

    @Schema(description = "报表类型名称", example = "资产负债表")
    private String reportTypeName;

    @Schema(description = "资产合计", example = "10000")
    private BigDecimal assetAmount;

    @Schema(description = "负债合计", example = "6000")
    private BigDecimal liabilityAmount;

    @Schema(description = "权益合计", example = "4000")
    private BigDecimal equityAmount;

    @Schema(description = "资产负债表是否平衡", example = "true")
    private Boolean balanceSheetBalanced;

    @Schema(description = "收入合计", example = "10000")
    private BigDecimal revenueAmount;

    @Schema(description = "成本费用合计", example = "3000")
    private BigDecimal costExpenseAmount;

    @Schema(description = "利润合计", example = "7000")
    private BigDecimal profitAmount;

    @Schema(description = "现金流入合计", example = "10000")
    private BigDecimal cashInflowAmount;

    @Schema(description = "现金流出合计", example = "3000")
    private BigDecimal cashOutflowAmount;

    @Schema(description = "现金流量净额", example = "7000")
    private BigDecimal netCashFlowAmount;

    @Schema(description = "报表项目")
    private List<Item> items;

    @Schema(description = "管理后台 - ERP 财务正式报表项目")
    @Data
    public static class Item {

        @Schema(description = "报表项目编号", example = "1")
        private Long itemId;

        @Schema(description = "项目编码", example = "BS-ASSET")
        private String itemCode;

        @Schema(description = "项目名称", example = "资产合计")
        private String itemName;

        @Schema(description = "项目分类", example = "10")
        private Integer itemCategory;

        @Schema(description = "项目分类名称", example = "资产")
        private String itemCategoryName;

        @Schema(description = "金额", example = "10000")
        private BigDecimal amount;

        @Schema(description = "排序", example = "1")
        private Integer sort;
    }
}
