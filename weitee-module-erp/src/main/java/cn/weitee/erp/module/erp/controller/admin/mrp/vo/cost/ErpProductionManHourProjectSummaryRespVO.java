package cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "管理后台 - ERP 生产工时项目汇总 Response VO")
@Data
public class ErpProductionManHourProjectSummaryRespVO {

    @Schema(description = "核算月份", example = "2026-04")
    private String accountingMonth;

    @Schema(description = "项目编号", example = "2001")
    private Long projectId;

    @Schema(description = "项目号", example = "XM202604280001")
    private String projectNo;

    @Schema(description = "项目名称", example = "新产品项目")
    private String projectName;

    @Schema(description = "生产工单数", example = "3")
    private Integer productionOrderCount;

    @Schema(description = "工时", example = "16.500000")
    private BigDecimal manHour;

    @Schema(description = "人工成本", example = "328.000000")
    private BigDecimal laborCost;

    @Schema(description = "人工单小时成本", example = "19.878788")
    private BigDecimal laborUnitCost;

    @Schema(description = "最近工时日期")
    private LocalDate lastWorkDate;

}
