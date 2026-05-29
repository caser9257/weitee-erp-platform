package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.inbound;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
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
