package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 生产成本产品汇总 Response VO")
@Data
public class ErpProductionCostProductSummaryRespVO {

    @Schema(description = "产品编号", example = "1001")
    private Long productId;

    @Schema(description = "产品名称", example = "精密组件A")
    private String productName;

    @Schema(description = "产品规格", example = "V2.0")
    private String productSpec;

    @Schema(description = "产品单位", example = "pcs")
    private String productUnit;

    @Schema(description = "关联项目数", example = "3")
    private Integer projectCount;

    @Schema(description = "关联工单数", example = "5")
    private Integer productionOrderCount;

    @Schema(description = "总工时", example = "120.00")
    private BigDecimal totalManHour;

    @Schema(description = "直接材料成本", example = "5000.00")
    private BigDecimal materialCost;

    @Schema(description = "直接人工成本", example = "2000.00")
    private BigDecimal laborCost;

    @Schema(description = "折旧成本", example = "800.00")
    private BigDecimal depreciationCost;

    @Schema(description = "电费成本", example = "300.00")
    private BigDecimal powerCost;

    @Schema(description = "其他制造费用", example = "200.00")
    private BigDecimal otherCost;

    @Schema(description = "成本合计", example = "8300.00")
    private BigDecimal totalCost;

    @Schema(description = "完工入库数量", example = "100")
    private BigDecimal outputQty;

    @Schema(description = "单位成本", example = "83.00")
    private BigDecimal unitCost;

    @Schema(description = "上月成本合计", example = "7500.00")
    private BigDecimal lastMonthTotalCost;

    @Schema(description = "环比变化率（%）", example = "10.67")
    private BigDecimal costChangeRate;

    @Schema(description = "是否成本异常", example = "false")
    private Boolean costAnomaly;

}
