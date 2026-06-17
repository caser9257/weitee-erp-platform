package cn.iocoder.yudao.module.erp.enums.common;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * ERP 业务类型枚举
 */
@RequiredArgsConstructor
@Getter
public enum ErpBizTypeEnum implements ArrayValuable<Integer> {

    PURCHASE_ORDER(10, "采购订单"),
    PURCHASE_IN(11, "采购入库"),
    PURCHASE_RETURN(12, "采购退货"),

    SALE_ORDER(20, "销售订单"),
    SALE_OUT(21, "销售出库"),
    SALE_RETURN(22, "销售退货"),

    OUTSOURCE_FEE(30, "委外加工费"),
    OUTSOURCE_INBOUND(31, "委外入库"),
    PRODUCTION_INBOUND(32, "自制入库"),
    FINANCE_EXPENSE(40, "费用报销"),
    FINANCE_EXPENSE_EXPENSE(41, "研发费用化"),
    FINANCE_EXPENSE_CAPITALIZE(42, "研发资本化"),
    FINANCE_EXPENSE_MONTH_END(43, "研发费用月末结转"),
    RESEARCH_EXPENSE(50, "研发费用"),
    STOCK_CHECK(60, "库存盘点"),
    ASSET_DEPRECIATION(70, "资产折旧"),
    ASSET_AMORTIZATION(71, "无形资产摊销"),
    ASSET_AMORTIZATION_RD(72, "研发无形资产摊销");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ErpBizTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
