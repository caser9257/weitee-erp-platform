package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - ERP 生产成本产品汇总 Request VO")
@Data
public class ErpProductionCostProductSummaryReqVO {

    @Schema(description = "归集月份（YYYY-MM）", example = "2026-06")
    private String accountingMonth;

    @Schema(description = "产品名称（模糊匹配）", example = "精密组件")
    private String productName;

    @Schema(description = "产品编号（模糊匹配）", example = "P-001")
    private String productNo;

    @Schema(description = "工单编号（模糊匹配）", example = "WO-20260601")
    private String productionOrderNo;

}
