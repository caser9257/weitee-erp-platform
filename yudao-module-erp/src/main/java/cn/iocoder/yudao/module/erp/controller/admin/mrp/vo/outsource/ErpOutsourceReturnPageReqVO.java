package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.outsource;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
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
