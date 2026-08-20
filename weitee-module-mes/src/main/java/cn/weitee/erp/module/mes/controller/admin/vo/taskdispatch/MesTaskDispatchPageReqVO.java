package cn.weitee.erp.module.mes.controller.admin.vo.taskdispatch;

import cn.weitee.erp.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 派工工作台分页参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesTaskDispatchPageReqVO extends PageParam {

    private String taskNo;
    private String productionOrderNo;
    private Long workCenterId;
    private Integer taskStatus;
    private Integer dispatchStatus;
}
