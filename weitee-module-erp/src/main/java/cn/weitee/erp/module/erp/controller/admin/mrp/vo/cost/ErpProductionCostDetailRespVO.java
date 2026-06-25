package cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 生产成本明细穿透 Response VO")
@Data
public class ErpProductionCostDetailRespVO {

    @Schema(description = "生产工单编号", example = "1")
    private Long productionOrderId;

    @Schema(description = "生产工单号", example = "SCGD202604280001")
    private String productionOrderNo;

    @Schema(description = "产品编号", example = "1001")
    private Long productId;

    @Schema(description = "产品名称", example = "精密组件A")
    private String productName;

    @Schema(description = "项目编号", example = "2001")
    private Long projectId;

    @Schema(description = "项目号", example = "XM202604280001")
    private String projectNo;

    @Schema(description = "项目名称", example = "新产品项目")
    private String projectName;

    @Schema(description = "完工数量", example = "20")
    private BigDecimal finishedQty;

    @Schema(description = "材料成本", example = "120.00")
    private BigDecimal materialCost;

    @Schema(description = "人工成本", example = "30.00")
    private BigDecimal laborCost;

    @Schema(description = "折旧成本", example = "10.00")
    private BigDecimal depreciationCost;

    @Schema(description = "电费成本", example = "5.00")
    private BigDecimal powerCost;

    @Schema(description = "其他制造费用", example = "2.00")
    private BigDecimal otherCost;

    @Schema(description = "总成本", example = "167.00")
    private BigDecimal totalCost;

    @Schema(description = "单位成本", example = "8.350000")
    private BigDecimal unitCost;

    @Schema(description = "成本快照时间")
    private LocalDateTime costSnapshotTime;

    @Schema(description = "材料成本明细")
    private List<MaterialDetail> materialDetails;

    @Schema(description = "人工成本明细")
    private List<CostEntry> laborDetails;

    @Schema(description = "制造费用明细")
    private List<CostEntry> manufacturingCostDetails;

    @Schema(description = "手工成本归集明细")
    private List<CostEntry> costEntries;

    @Data
    public static class MaterialDetail {

        @Schema(description = "领料单编号", example = "11")
        private Long issueId;

        @Schema(description = "领料单号", example = "SCLL202604280001")
        private String issueNo;

        @Schema(description = "领料时间")
        private LocalDateTime issueTime;

        @Schema(description = "领料金额", example = "100.00")
        private BigDecimal issueAmount;

        @Schema(description = "备注", example = "主料")
        private String remark;

    }

    @Data
    public static class CostEntry {

        @Schema(description = "编号", example = "101")
        private Long id;

        @Schema(description = "成本类型", example = "20")
        private Integer costType;

        @Schema(description = "成本类型名称", example = "直接人工")
        private String costTypeName;

        @Schema(description = "来源类型", example = "10")
        private Integer sourceType;

        @Schema(description = "来源类型名称", example = "手工录入")
        private String sourceTypeName;

        @Schema(description = "来源分摊结果编号", example = "301")
        private Long sourceAllocationResultId;

        @Schema(description = "来源分摊单编号", example = "1")
        private Long sourceAllocationId;

        @Schema(description = "来源分摊单号", example = "CBFT202604280001")
        private String sourceAllocationNo;

        @Schema(description = "归集月份", example = "2026-04")
        private String accountingMonth;

        @Schema(description = "金额", example = "30.00")
        private BigDecimal amount;

        @Schema(description = "备注", example = "人工费")
        private String remark;

        @Schema(description = "创建时间")
        private LocalDateTime createTime;

    }

}
