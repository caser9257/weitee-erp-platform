package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ErpFinanceVoucherEntryDirectionEnum implements ArrayValuable<Integer> {

    DEBIT(10, "借方"),
    CREDIT(20, "贷方");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ErpFinanceVoucherEntryDirectionEnum::getType)
            .toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
