package cn.iocoder.yudao.module.erp.framework.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 采购订单变更事件
 *
 * 当采购订单数量变更或取消时触发
 */
@Getter
@AllArgsConstructor
public class PurchaseOrderChangedEvent {

    /**
     * 采购订单ID
     */
    private final Long purchaseOrderId;

    /**
     * 变更类型
     */
    private final ChangeType changeType;

    /**
     * 变更类型枚举
     */
    public enum ChangeType {
        /**
         * 数量变更
         */
        QUANTITY_CHANGED,
        /**
         * 订单取消
         */
        ORDER_CANCELLED,
        /**
         * 订单反审核
         */
        ORDER_REJECTED
    }

}
