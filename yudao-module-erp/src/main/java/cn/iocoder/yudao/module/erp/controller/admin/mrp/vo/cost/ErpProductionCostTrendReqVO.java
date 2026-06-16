package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - ERP 生产成本趋势分析 Request VO")
@Data
public class ErpProductionCostTrendReqVO {

    @Schema(description = "产品ID", example = "1")
    private Long productId;

    @Schema(description = "开始月份（YYYY-MM）", example = "2026-01")
    private String startMonth;

    @Schema(description = "结束月份（YYYY-MM）", example = "2026-06")
    private String endMonth;

    @Schema(description = "分析维度（month=按月, quarter=按季）", example = "month")
    private String dimension;

}
