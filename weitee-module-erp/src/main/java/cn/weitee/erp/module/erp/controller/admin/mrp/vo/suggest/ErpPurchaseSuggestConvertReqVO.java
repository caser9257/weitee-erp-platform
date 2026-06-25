package cn.weitee.erp.module.erp.controller.admin.mrp.vo.suggest;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class ErpPurchaseSuggestConvertReqVO {

    @NotEmpty(message = "建议单不能为空")
    private List<Long> ids;

    @NotNull(message = "供应商不能为空")
    private Long supplierId;

    private Long accountId;

    private String remark;

}
