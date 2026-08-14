package cn.weitee.erp.module.mes.controller.admin.vo.worktask;

import cn.weitee.erp.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWorkTaskPageReqVO extends PageParam {

    private String productionOrderNo;

    private Long productionOrderId;

    private Long workCenterId;

    private Integer status;

}
