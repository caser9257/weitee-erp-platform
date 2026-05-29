package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.suggest;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class ErpProductionSuggestConvertReqVO {

    @NotEmpty(message = "建议单不能为空")
    private List<Long> ids;

    private String remark;

}
