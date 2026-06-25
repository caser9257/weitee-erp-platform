package cn.weitee.erp.module.erp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 收款规则枚举
 *
 * 业务确认（2026-06-03）："发货付款"包含三种情况：
 * ①发货前付款 ②发货时付款 ③发货后一定期限内付款
 *
 * @author system
 */
@Getter
@AllArgsConstructor
public enum CollectionRule {

    /**
     * 发货前付款：必须在发货前收到货款
     */
    BEFORE_SHIPMENT("发货前付款", "必须在发货前收到货款", 1),
    /**
     * 发货时付款：在发货的同时收到货款
     */
    ON_SHIPMENT("发货时付款", "在发货的同时收到货款", 2),
    /**
     * 发货后约定期限付款：发货后在约定时间内付款
     */
    AFTER_SHIPMENT("发货后约定期限付款", "发货后在约定时间内付款", 3);

    /**
     * 规则名称
     */
    private final String name;
    /**
     * 规则描述
     */
    private final String description;
    /**
     * 排序
     */
    private final int sortOrder;

}
