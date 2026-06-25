package cn.weitee.erp.module.erp.framework.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 销售订单审批通过事件
 */
@Getter
@AllArgsConstructor
public class ErpSaleOrderApprovedEvent {

    private final Long saleOrderId;

}
