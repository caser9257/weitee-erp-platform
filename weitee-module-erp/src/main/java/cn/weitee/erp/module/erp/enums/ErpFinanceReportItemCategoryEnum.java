package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ErpFinanceReportItemCategoryEnum implements ArrayValuable<Integer> {

    ASSET(10, "资产"),
    LIABILITY(20, "负债"),
    EQUITY(30, "权益"),
    REVENUE(40, "收入"),
    COST_EXPENSE(50, "成本费用"),
    CASH_INFLOW(60, "现金流入"),
    CASH_OUTFLOW(70, "现金流出");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ErpFinanceReportItemCategoryEnum::getType)
            .toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
