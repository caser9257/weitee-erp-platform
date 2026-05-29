package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.productdualcost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 产品双账成本 Response VO")
@Data
public class ErpFinanceDualProductCostRespVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "产品ID", example = "1")
    private Long productId;

    @Schema(description = "产品编号", example = "PRD-001")
    private String productNo;

    @Schema(description = "产品名称", example = "产品A")
    private String productName;

    @Schema(description = "批次号", example = "BATCH-001")
    private String productBatchNo;

    @Schema(description = "生产工单ID", example = "1")
    private Long productionOrderId;

    @Schema(description = "生产工单编号", example = "WO-001")
    private String productionOrderNo;

    @Schema(description = "期间（YYYY-MM）", example = "2026-05")
    private String period;

    @Schema(description = "材料成本-外部账", example = "5000.00")
    private BigDecimal externalMaterialAmount;

    @Schema(description = "材料成本-内部账", example = "5500.00")
    private BigDecimal internalMaterialAmount;

    @Schema(description = "人工成本-外部账", example = "3000.00")
    private BigDecimal externalLaborAmount;

    @Schema(description = "人工成本-内部账", example = "3200.00")
    private BigDecimal internalLaborAmount;

    @Schema(description = "制造费用-外部账", example = "2000.00")
    private BigDecimal externalOverheadAmount;

    @Schema(description = "制造费用-内部账", example = "2100.00")
    private BigDecimal internalOverheadAmount;

    @Schema(description = "总成本-外部账", example = "10000.00")
    private BigDecimal externalTotalAmount;

    @Schema(description = "总成本-内部账", example = "10800.00")
    private BigDecimal internalTotalAmount;

    @Schema(description = "差异金额（内部-外部）", example = "800.00")
    private BigDecimal diffAmount;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "版本号", example = "1")
    private Integer versionNo;

    @Schema(description = "最后重建时间")
    private LocalDateTime lastRebuildTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
