package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpMrpStockReservationPageReqVO extends PageParam {

    private Long planId;

    private Long projectId;

    private Long productId;

    private Long sourceOrderId;

    private Integer status;

}
