package cn.weitee.erp.module.erp.controller.admin.mrp.vo.inbound;

import cn.weitee.erp.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpProductionInboundPageReqVO extends PageParam {

    private String no;

    private Long finishQualityId;

    private Long productionOrderId;

    private Integer status;

}
