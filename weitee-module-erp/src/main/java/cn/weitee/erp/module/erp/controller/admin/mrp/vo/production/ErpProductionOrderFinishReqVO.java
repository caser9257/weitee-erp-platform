package cn.weitee.erp.module.erp.controller.admin.mrp.vo.production;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ErpProductionOrderFinishReqVO {

    @NotNull(message = "工单不能为空")
    private Long id;

    @NotNull(message = "完工数量不能为空")
    private BigDecimal finishedQty;

    @NotNull(message = "入库仓库不能为空")
    private Long warehouseId;

}
