package cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - ERP 生产成本趋势分析 Response VO")
@Data
public class ErpProductionCostTrendRespVO {

    @Schema(description = "期间列表")
    private List<String> periods;

    @Schema(description = "直接材料成本趋势")
    private List<BigDecimal> materialCosts;

    @Schema(description = "直接人工成本趋势")
    private List<BigDecimal> laborCosts;

    @Schema(description = "折旧成本趋势")
    private List<BigDecimal> depreciationCosts;

    @Schema(description = "电费成本趋势")
    private List<BigDecimal> powerCosts;

    @Schema(description = "其他制造费用趋势")
    private List<BigDecimal> otherCosts;

    @Schema(description = "总成本趋势")
    private List<BigDecimal> totalCosts;

    @Schema(description = "完工数量趋势")
    private List<BigDecimal> outputQtys;

    @Schema(description = "单位成本趋势")
    private List<BigDecimal> unitCosts;

    @Schema(description = "成本构成数据")
    private CompositionData compositionData;

    @Schema(description = "产品对比数据")
    private List<ProductCompareItem> productCompareData;

    @Schema(description = "期间工单信息")
    private java.util.Map<String, OrderInfo> productOrderData;

    @Data
    @Schema(description = "成本构成数据")
    public static class CompositionData {

        @Schema(description = "直接材料成本")
        private BigDecimal materialCost;

        @Schema(description = "直接人工成本")
        private BigDecimal laborCost;

        @Schema(description = "折旧成本")
        private BigDecimal depreciationCost;

        @Schema(description = "电费成本")
        private BigDecimal powerCost;

        @Schema(description = "其他制造费用")
        private BigDecimal otherCost;

        @Schema(description = "总成本")
        private BigDecimal totalCost;
    }

    @Data
    @Schema(description = "产品对比项")
    public static class ProductCompareItem {

        @Schema(description = "产品ID")
        private Long productId;

        @Schema(description = "产品名称")
        private String productName;

        @Schema(description = "直接材料成本")
        private BigDecimal materialCost;

        @Schema(description = "直接人工成本")
        private BigDecimal laborCost;

        @Schema(description = "制造费用（折旧+电费+其他）")
        private BigDecimal overheadCost;

        @Schema(description = "总成本")
        private BigDecimal totalCost;
    }

    @Data
    @Schema(description = "期间工单信息")
    public static class OrderInfo {

        @Schema(description = "工单编号")
        private String orderNo;

        @Schema(description = "工单ID")
        private Long orderId;
    }
}
