package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 研发支出分类枚举（费用化/资本化）
 */
@RequiredArgsConstructor
@Getter
public enum ErpResearchExpenseCategoryEnum implements ArrayValuable<Integer> {

    EXPENSE(10, "费用化"),
    CAPITALIZE(20, "资本化");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpResearchExpenseCategoryEnum::getType)
            .toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static ErpResearchExpenseCategoryEnum fromType(Integer type) {
        return Arrays.stream(values())
                .filter(item -> item.getType().equals(type))
                .findFirst()
                .orElse(null);
    }
}
