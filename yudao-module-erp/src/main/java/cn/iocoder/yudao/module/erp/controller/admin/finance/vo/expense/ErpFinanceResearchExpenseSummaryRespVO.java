package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 研发费用项目维度汇总 Response VO")
@Data
public class ErpFinanceResearchExpenseSummaryRespVO {
    
    @Schema(description = "项目编号")
    private Long projectId;
    
    @Schema(description = "项目名称")
    private String projectName;
    
    @Schema(description = "研发支出分类")
    private Integer researchCategory;
    
    @Schema(description = "研发支出分类名称")
    private String researchCategoryName;
    
    @Schema(description = "费用总额")
    private BigDecimal totalAmount;
    
    @Schema(description = "费用化金额")
    private BigDecimal expenseAmount;
    
    @Schema(description = "资本化金额")
    private BigDecimal capitalizeAmount;
    
    @Schema(description = "费用笔数")
    private Integer count;
}