package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.productdualcost;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 产品双账成本重跑 Request VO")
@Data
public class ErpFinanceDualProductCostRebuildReqVO {

    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "产品ID不能为空")
    private Long productId;

    @Schema(description = "生产工单ID", example = "1")
    private Long productionOrderId;

    @Schema(description = "批次号", example = "BATCH-001")
    private String productBatchNo;

    @Schema(description = "期间（YYYY-MM）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-05")
    @NotEmpty(message = "期间不能为空")
    private String period;

    @Schema(description = "备注", example = "手动触发重跑")
    private String remark;
}
