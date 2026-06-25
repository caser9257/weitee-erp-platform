package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ErpFinanceVoucherAmountSourceEnum implements ArrayValuable<Integer> {

    BIZ_AMOUNT(10, "业务金额", false),
    FIXED_AMOUNT(20, "固定金额", true),
    BIZ_AMOUNT_RATE(30, "业务金额比例", true),
    RESEARCH_EXPENSE_CATEGORY(40, "研发支出分类", false),
    RESEARCH_PROJECT_SUMMARY(50, "研发项目汇总", false);

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ErpFinanceVoucherAmountSourceEnum::getType)
            .toArray(Integer[]::new);

    private final Integer type;
    private final String name;
    private final boolean needValue;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static ErpFinanceVoucherAmountSourceEnum fromType(Integer type) {
        return Arrays.stream(values())
                .filter(value -> value.type.equals(type))
                .findFirst()
                .orElse(null);
    }

    public BigDecimal resolveAmount(BigDecimal sourceValue, BigDecimal bizAmount) {
        if (this == BIZ_AMOUNT) {
            return bizAmount;
        }
        if (this == FIXED_AMOUNT) {
            return sourceValue == null ? BigDecimal.ZERO : sourceValue.setScale(6, RoundingMode.HALF_UP);
        }
        if (this == BIZ_AMOUNT_RATE) {
            BigDecimal baseAmount = bizAmount == null ? BigDecimal.ZERO : bizAmount;
            BigDecimal rate = sourceValue == null ? BigDecimal.ZERO : sourceValue;
            return baseAmount.multiply(rate).setScale(6, RoundingMode.HALF_UP);
        }
        if (this == RESEARCH_EXPENSE_CATEGORY) {
            return bizAmount == null ? BigDecimal.ZERO : bizAmount.setScale(6, RoundingMode.HALF_UP);
        }
        if (this == RESEARCH_PROJECT_SUMMARY) {
            return bizAmount == null ? BigDecimal.ZERO : bizAmount.setScale(6, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
