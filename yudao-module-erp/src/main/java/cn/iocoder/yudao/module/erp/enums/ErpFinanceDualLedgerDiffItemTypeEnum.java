package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpFinanceDualLedgerDiffItemTypeEnum implements ArrayValuable<Integer> {

    LABOR(20, "人工成本"),
    DEPRECIATION(30, "折旧"),
    POWER(40, "电费"),
    OTHER(50, "其他制造费用"),
    MANUFACTURING_OVERHEAD(60, "制造费用");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpFinanceDualLedgerDiffItemTypeEnum::getType)
            .toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    public static String resolveName(Integer type) {
        return Arrays.stream(values())
                .filter(item -> item.type.equals(type))
                .map(ErpFinanceDualLedgerDiffItemTypeEnum::getName)
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
