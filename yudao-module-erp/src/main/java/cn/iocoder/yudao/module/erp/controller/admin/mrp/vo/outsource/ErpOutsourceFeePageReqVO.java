package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.outsource;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpOutsourceFeePageReqVO extends PageParam {
    private String feeNo;
    private Long orderId;
    private Integer status;
}
