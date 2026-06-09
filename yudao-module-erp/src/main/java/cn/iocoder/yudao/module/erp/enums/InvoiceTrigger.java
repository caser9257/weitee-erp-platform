package cn.iocoder.yudao.module.erp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 开票触发条件枚举
 *
 * 业务确认（2026-06-03）：
 * - "预付款开票"包含三种情况：①仅对预付款金额开票 ②预付款阶段先开部分票 ③签约后即可开全额票
 * - "执行完再开票"指交付完成收款后
 *
 * @author system
 */
@Getter
@AllArgsConstructor
public enum InvoiceTrigger {

    /**
     * 预付款全额开票：签约后即可开全额发票
     */
    PREPAYMENT_FULL("预付款全额开票", "签约后即可开全额发票", 1),
    /**
     * 预付款部分开票：预付款阶段先开部分发票
     */
    PREPAYMENT_PARTIAL("预付款部分开票", "预付款阶段先开部分发票", 2),
    /**
     * 仅预付款开票：仅对预付款金额开具发票
     */
    PREPAYMENT_ONLY("仅预付款开票", "仅对预付款金额开具发票", 3),
    /**
     * 发货后开票：销售出库完成后开票
     */
    AFTER_SHIPMENT("发货后开票", "销售出库完成后开票", 4),
    /**
     * 交付收款后开票：交付完成并收款后开票（执行完再开票）
     */
    AFTER_DELIVERY_RECEIPT("交付收款后开票", "交付完成并收款后开票（执行完再开票）", 5),
    /**
     * 手工决定：由业务人员手动决定开票时机
     */
    MANUAL("手工决定", "由业务人员手动决定开票时机", 99);

    /**
     * 触发条件名称
     */
    private final String name;
    /**
     * 触发条件描述
     */
    private final String description;
    /**
     * 排序
     */
    private final int sortOrder;

    /**
     * 判断是否为预付款相关开票
     *
     * @return 是否为预付款相关开票
     */
    public boolean isPrepaymentRelated() {
        return this == PREPAYMENT_FULL || this == PREPAYMENT_PARTIAL || this == PREPAYMENT_ONLY;
    }

}
