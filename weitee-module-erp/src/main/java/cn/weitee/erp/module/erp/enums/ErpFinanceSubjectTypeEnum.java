package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ErpFinanceSubjectTypeEnum implements ArrayValuable<Integer> {

    ASSET(10, "资产"),
    LIABILITY(20, "负债"),
    EQUITY(30, "权益"),
    REVENUE(40, "收入"),
    COST(50, "成本"),
    EXPENSE(60, "费用"),
    CASH_FLOW(70, "现金流量");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ErpFinanceSubjectTypeEnum::getType)
            .toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
