package cn.weitee.erp.module.erp.controller.admin.mrp.vo.suggest;

import cn.weitee.erp.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpProductionSuggestPageReqVO extends PageParam {

    private Long planId;

    private Long productId;

    private Long sourceOrderId;

    private Integer status;

}
