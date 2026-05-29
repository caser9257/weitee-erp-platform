package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ErpFinanceReportAmountRuleEnum implements ArrayValuable<Integer> {

    OPENING_DEBIT(10, "期初借方"),
    OPENING_CREDIT(20, "期初贷方"),
    CURRENT_DEBIT(30, "本期借方"),
    CURRENT_CREDIT(40, "本期贷方"),
    ENDING_DEBIT(50, "期末借方"),
    ENDING_CREDIT(60, "期末贷方"),
    CURRENT_NET_DEBIT(70, "本期借方净额"),
    CURRENT_NET_CREDIT(80, "本期贷方净额"),
    ENDING_NET_DEBIT(90, "期末借方净额"),
    ENDING_NET_CREDIT(100, "期末贷方净额");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ErpFinanceReportAmountRuleEnum::getType)
            .toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
