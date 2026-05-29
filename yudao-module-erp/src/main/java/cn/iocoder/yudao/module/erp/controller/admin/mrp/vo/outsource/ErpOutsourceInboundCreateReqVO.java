package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.outsource;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ErpOutsourceInboundCreateReqVO {
    @NotNull(message = "orderId cannot be empty")
    private Long orderId;
    @NotNull(message = "warehouseId cannot be empty")
    private Long warehouseId;
    @NotBlank(message = "batchNo cannot be empty")
    private String batchNo;
    @NotNull(message = "inboundQty cannot be empty")
    @DecimalMin(value = "0.000001", message = "inboundQty must be greater than 0")
    private BigDecimal inboundQty;
    private LocalDate produceDate;
    private LocalDate expireDate;
    private String remark;
}
