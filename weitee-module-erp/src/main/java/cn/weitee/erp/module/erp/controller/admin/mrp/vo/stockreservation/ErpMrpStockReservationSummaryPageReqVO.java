package cn.weitee.erp.module.erp.controller.admin.mrp.vo.stockreservation;

import cn.weitee.erp.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpMrpStockReservationSummaryPageReqVO extends PageParam {

    private Long productId;

}
