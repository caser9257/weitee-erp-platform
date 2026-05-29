package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.quality;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ErpProductionFinishQualitySubmitReqVO {

    @NotNull(message = "质检单不能为空")
    private Long id;

    @NotNull(message = "合格数量不能为空")
    private BigDecimal qualifiedQty;

    @NotNull(message = "不合格数量不能为空")
    private BigDecimal unqualifiedQty;

    private String remark;

}
