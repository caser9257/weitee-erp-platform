package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 生产成本项目汇总 Response VO")
@Data
public class ErpProductionCostProjectSummaryRespVO {

    @Schema(description = "项目编号", example = "2001")
    private Long projectId;

    @Schema(description = "项目号", example = "XM-01")
    private String projectNo;

    @Schema(description = "项目名称", example = "项目一")
    private String projectName;

    @Schema(description = "工单数", example = "2")
    private Integer productionOrderCount;

    @Schema(description = "产品数", example = "2")
    private Integer productCount;

    @Schema(description = "总工时", example = "6.000000")
    private BigDecimal totalManHour;

    @Schema(description = "人工成本", example = "50.00")
    private BigDecimal laborCost;

    @Schema(description = "折旧成本", example = "10.00")
    private BigDecimal depreciationCost;

    @Schema(description = "电费成本", example = "5.00")
    private BigDecimal powerCost;

    @Schema(description = "其他制造费用", example = "2.00")
    private BigDecimal otherCost;

    @Schema(description = "成本合计", example = "67.00")
    private BigDecimal totalCost;

}
