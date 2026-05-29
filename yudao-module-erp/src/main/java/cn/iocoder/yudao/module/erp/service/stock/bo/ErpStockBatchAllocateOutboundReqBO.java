package cn.iocoder.yudao.module.erp.service.stock.bo;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ErpStockBatchAllocateOutboundReqBO {

    @NotNull(message = "产品编号不能为空")
    private Long productId;

    @NotNull(message = "仓库编号不能为空")
    private Long warehouseId;

    @NotNull(message = "出库数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "出库数量必须大于 0")
    private BigDecimal count;

    @NotNull(message = "业务类型不能为空")
    private Integer bizType;

    @NotNull(message = "业务编号不能为空")
    private Long bizId;

    private Long bizItemId;

    private String bizNo;

    private String remark;
}
