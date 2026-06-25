package cn.weitee.erp.module.erp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 发货放行规则枚举
 *
 * 业务确认（2026-06-03）：所有发货都需要财务审核
 *
 * @author system
 */
@Getter
@AllArgsConstructor
public enum ShipmentReleaseRule {

    /**
     * 签约即发：合同生效后即可发货，但仍需财务审核
     */
    SIGN_AND_SHIP("签约即发", "合同生效后即可发货，但仍需财务审核", 1),
    /**
     * 到账后发：收到全额货款后方可发货
     */
    AFTER_PAYMENT("到账后发", "收到全额货款后方可发货", 2),
    /**
     * 达到预付款比例后发：收到约定比例的预付款后方可发货
     */
    AFTER_PREPAYMENT("达到预付款比例后发", "收到约定比例的预付款后方可发货", 3),
    /**
     * 财务审核后发：需财务人员审核通过后方可发货（默认所有发货都需财务审核）
     */
    FINANCE_APPROVAL("财务审核后发", "需财务人员审核通过后方可发货（默认所有发货都需财务审核）", 4);

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
