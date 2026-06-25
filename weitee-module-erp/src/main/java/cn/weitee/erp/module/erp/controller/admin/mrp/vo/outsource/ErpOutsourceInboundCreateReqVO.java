package cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
