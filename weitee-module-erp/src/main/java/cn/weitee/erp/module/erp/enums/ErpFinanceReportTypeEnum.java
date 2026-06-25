package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ErpFinanceReportTypeEnum implements ArrayValuable<Integer> {

    BALANCE_SHEET(10, "资产负债表"),
    INCOME_STATEMENT(20, "利润表"),
    CASH_FLOW_STATEMENT(30, "现金流量表");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ErpFinanceReportTypeEnum::getType)
            .toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
