package cn.weitee.erp.module.erp.controller.admin.finance.vo.productdualcost;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 产品双账成本分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpFinanceDualProductCostPageReqVO extends PageParam {

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

    @Schema(description = "期间（YYYY-MM）", example = "2026-05")
    private String period;
}
