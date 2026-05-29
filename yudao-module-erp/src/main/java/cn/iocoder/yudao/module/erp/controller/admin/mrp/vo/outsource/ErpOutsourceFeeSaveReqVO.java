package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.outsource;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ErpOutsourceFeeSaveReqVO {
    private Long id;
    @NotNull(message = "orderId cannot be empty")
    private Long orderId;
    private LocalDateTime feeTime;
    @NotNull(message = "feeAmount cannot be empty")
    @DecimalMin(value = "0.000001", message = "feeAmount must be greater than 0")
    private BigDecimal feeAmount;
    private String remark;
}
