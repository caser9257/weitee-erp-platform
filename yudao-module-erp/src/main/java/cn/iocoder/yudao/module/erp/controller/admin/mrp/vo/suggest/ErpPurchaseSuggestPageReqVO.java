package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.suggest;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpPurchaseSuggestPageReqVO extends PageParam {

    private Long planId;

    private Long materialId;

    private Long sourceOrderId;

    private Integer status;

}
