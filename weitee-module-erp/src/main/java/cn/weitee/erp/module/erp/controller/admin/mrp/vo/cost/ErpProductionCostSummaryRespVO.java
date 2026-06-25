package cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - ERP 生产成本归集汇总分页 Response VO")
@Data
public class ErpProductionCostSummaryRespVO {

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

    @Schema(description = "归集月份", example = "2026-04")
    private String accountingMonth;

    @Schema(description = "本期归集总成本", example = "1000.00")
    private BigDecimal totalAmount;

    @Schema(description = "明细行数", example = "4")
    private Integer breakdownCount;

    @Schema(description = "归集明细")
    private List<ErpProductionCostEntryRespVO> breakdown;

}
