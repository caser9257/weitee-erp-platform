package cn.iocoder.yudao.module.erp.service.stock.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErpStockBatchInboundReqBO {

    @NotNull
    private Long productId;

    @NotNull
    private Long warehouseId;

    @NotBlank
    private String batchNo;

    @NotNull
    private LocalDateTime inboundTime;

    private LocalDate produceDate;

    private LocalDate expireDate;

    @NotNull
    @DecimalMin("0.000001")
    private BigDecimal count;

    private Boolean virtualFlag;

    @NotNull
    private Integer bizType;

    @NotNull
    private Long bizId;

    private Long bizItemId;

    @NotBlank
    private String bizNo;

    private String sourceBizType;

    private Long purchaseSourceBatchId;

    private String purchaseSourceBatchNo;

    private String remark;

}
