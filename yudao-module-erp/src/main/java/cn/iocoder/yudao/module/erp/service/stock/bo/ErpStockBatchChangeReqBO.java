package cn.iocoder.yudao.module.erp.service.stock.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErpStockBatchChangeReqBO {

    @NotNull
    private Long stockBatchId;

    @NotNull
    @DecimalMin("0.000001")
    private BigDecimal count;

    @NotNull
    private Integer bizType;

    @NotNull
    private Long bizId;

    private Long bizItemId;

    @NotBlank
    private String bizNo;

    private String remark;

}
