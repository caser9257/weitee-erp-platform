package cn.weitee.erp.module.erp.controller.admin.mrp.vo.production;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ErpProductionOrderSaveReqVO {

    private Long id;

    @NotNull(message = "产品不能为空")
    private Long productId;

    private Long routeId;

    private String routeVersion;

    private Long workCenterId;

    private String batchNo;

    private Long projectId;

    @NotNull(message = "计划数量不能为空")
    @DecimalMin(value = "0.000001", message = "计划数量必须大于 0")
    private BigDecimal planQty;

    private BigDecimal scrapQty;

    @NotNull(message = "计划开始时间不能为空")
    private LocalDateTime planStartTime;

    @NotNull(message = "计划结束时间不能为空")
    private LocalDateTime planEndTime;

    private String remark;

}
