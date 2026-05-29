package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 生产成本分摊单明细 Response VO")
@Data
public class ErpProductionCostAllocationDetailRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "分摊单号", example = "CBFT202604280001")
    private String allocationNo;

    @Schema(description = "核算月份", example = "2026-04")
    private String accountingMonth;

    @Schema(description = "成本类型", example = "20")
    private Integer costType;

    @Schema(description = "成本类型名称", example = "直接人工")
    private String costTypeName;

    @Schema(description = "分摊规则编号", example = "11")
    private Long ruleId;

    @Schema(description = "分摊规则名称", example = "按人工工时")
    private String ruleName;

    @Schema(description = "分摊基准", example = "10")
    private Integer basisType;

    @Schema(description = "分摊基准名称", example = "人工工时")
    private String basisTypeName;

    @Schema(description = "待分摊总金额", example = "1000.00")
    private BigDecimal totalAmount;

    @Schema(description = "状态", example = "20")
    private Integer status;

    @Schema(description = "状态名称", example = "已执行")
    private String statusName;

    @Schema(description = "执行时间")
    private LocalDateTime executedTime;

    @Schema(description = "备注", example = "4 月人工分摊")
    private String remark;

    @Schema(description = "分摊结果明细")
    private List<ResultItem> results;

    @Data
    public static class ResultItem {

        @Schema(description = "结果编号", example = "1")
        private Long id;

        @Schema(description = "生产工单编号", example = "1001")
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

        @Schema(description = "分摊基数", example = "3.000000")
        private BigDecimal basisValue;

        @Schema(description = "分摊比例", example = "0.600000")
        private BigDecimal basisRatio;

        @Schema(description = "分摊金额", example = "600.000000")
        private BigDecimal allocatedAmount;

        @Schema(description = "生成的成本分录编号", example = "5001")
        private Long generatedCostEntryId;

        @Schema(description = "备注", example = "4 月人工分摊")
        private String remark;

    }

}
