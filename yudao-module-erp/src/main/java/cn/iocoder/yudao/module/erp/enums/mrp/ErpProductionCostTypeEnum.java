package cn.iocoder.yudao.module.erp.enums.mrp;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpProductionCostTypeEnum implements ArrayValuable<Integer> {

    MATERIAL(10, "直接材料"),
    LABOR(20, "直接人工"),
    DEPRECIATION(30, "折旧"),
    POWER(40, "电费"),
    OTHER(50, "其他制造费用");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpProductionCostTypeEnum::getType)
            .toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    public static String resolveName(Integer type) {
        return Arrays.stream(values())
                .filter(item -> item.type.equals(type))
                .map(ErpProductionCostTypeEnum::getName)
                .findFirst()
                .orElse(null);
    }

    public static boolean isManualType(Integer type) {
        return LABOR.type.equals(type) || DEPRECIATION.type.equals(type)
                || POWER.type.equals(type) || OTHER.type.equals(type);
    }

    public static boolean isAllocatableType(Integer type) {
        return isManualType(type);
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
