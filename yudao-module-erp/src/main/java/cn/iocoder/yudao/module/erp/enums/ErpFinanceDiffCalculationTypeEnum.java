package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * ERP 双账套差异计算类型枚举
 */
@RequiredArgsConstructor
@Getter
public enum ErpFinanceDiffCalculationTypeEnum implements ArrayValuable<Integer> {

    PRO_RATA(1, "按比例分摊"),
    FIXED_VARIANCE(2, "固定差额"),
    SOURCE_MAPPING(3, "来源映射");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpFinanceDiffCalculationTypeEnum::getType)
            .toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    public static String resolveName(Integer type) {
        return Arrays.stream(values())
                .filter(item -> item.type.equals(type))
                .map(ErpFinanceDiffCalculationTypeEnum::getName)
                .findFirst()
                .orElse(null);
    }

    public static boolean isSupported(Integer type) {
        return Arrays.stream(values()).anyMatch(item -> item.type.equals(type));
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
