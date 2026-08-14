package cn.weitee.erp.module.erp.controller.admin.mrp.vo.route;

import cn.weitee.erp.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ErpProcessRoutePageReqVO extends PageParam {
    private String routeCode;
    private String routeName;
    private Long productId;
    private Integer status;
}
