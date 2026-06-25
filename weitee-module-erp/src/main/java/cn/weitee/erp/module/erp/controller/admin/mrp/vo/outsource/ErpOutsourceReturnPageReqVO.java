package cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource;

import cn.weitee.erp.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpOutsourceReturnPageReqVO extends PageParam {
    private String returnNo;
    private Long orderId;
    private Integer status;
}
