package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * ERP 零星报销费用类型枚举
 */
@RequiredArgsConstructor
@Getter
public enum ErpFinanceExpenseTypeEnum implements ArrayValuable<Integer> {

    RESEARCH(10, "研发费用", true),
    TRAVEL(20, "差旅费", false),
    MATERIAL(30, "材料费", false),
    TEST(40, "测试费", false),
    ENTERTAINMENT(50, "招待费", false),
    SERVICE(60, "服务费", false),
    LABOR(70, "人工费", false),
    PETTY_PURCHASE(80, "零星采购", false),
    OTHER(90, "其他", false),
    RENT(100, "房租费", false),
    WATER_ELECTRIC(110, "水电费", false),
    LEASE(120, "仪器租赁费", false),
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ErpFinanceExpenseTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;
    private final boolean projectRequired;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static ErpFinanceExpenseTypeEnum fromType(Integer type) {
        return Arrays.stream(values())
                .filter(item -> item.getType().equals(type))
                .findFirst()
                .orElse(null);
    }

}
