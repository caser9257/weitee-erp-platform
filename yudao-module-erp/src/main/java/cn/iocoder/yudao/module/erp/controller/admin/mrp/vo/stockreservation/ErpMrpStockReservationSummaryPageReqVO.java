package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpMrpStockReservationSummaryPageReqVO extends PageParam {

    private Long productId;

}
