package cn.weitee.erp.module.erp.controller.admin.finance.vo.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 费用项目汇总 Response VO")
@Data
public class ErpFinanceExpenseProjectSummaryRespVO {

    @Schema(description = "项目编号", example = "1001")
    private Long projectId;

    @Schema(description = "项目名称", example = "高新研发项目")
    private String projectName;

    @Schema(description = "费用类型", example = "10")
    private Integer expenseType;

    @Schema(description = "费用类型名称", example = "研发费用")
    private String expenseTypeName;

    @Schema(description = "研发支出口径（10-费用化，20-资本化）", example = "10")
    private Integer rdAccountingType;

    @Schema(description = "研发支出口径名称", example = "费用化")
    private String rdAccountingTypeName;

    @Schema(description = "单据数", example = "3")
    private Long expenseCount;

    @Schema(description = "报销合计", example = "10000")
    private BigDecimal totalExpensePrice;

    @Schema(description = "已付款合计", example = "6000")
    private BigDecimal totalPaidPrice;

    @Schema(description = "未付款合计", example = "4000")
    private BigDecimal totalRemainPrice;

}
