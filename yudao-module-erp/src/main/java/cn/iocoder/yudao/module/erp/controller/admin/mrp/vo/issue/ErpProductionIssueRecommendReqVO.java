package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issue;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ErpProductionIssueRecommendReqVO {

    @NotNull(message = "工单物料编号不能为空")
    private Long productionMaterialId;

    @NotNull(message = "仓库编号不能为空")
    private Long warehouseId;

    @NotNull(message = "领料数量不能为空")
    @DecimalMin(value = "0.000001", message = "领料数量必须大于 0")
    private BigDecimal issueQty;

}
