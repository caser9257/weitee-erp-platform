package cn.weitee.erp.module.erp.controller.admin.mrp.vo.workcenter;

import cn.weitee.erp.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ErpWorkCenterPageReqVO extends PageParam {

    private String centerCode;

    private String centerName;

    private Integer status;
}
