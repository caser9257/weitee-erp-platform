package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.production;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ErpProductionOrderSaveReqVO {

    private Long id;

    @NotNull(message = "产品不能为空")
    private Long productId;

    private Long projectId;

    @NotNull(message = "计划数量不能为空")
    private BigDecimal planQty;

    @NotNull(message = "计划开始时间不能为空")
    private LocalDateTime planStartTime;

    @NotNull(message = "计划结束时间不能为空")
    private LocalDateTime planEndTime;

    private String remark;

}
