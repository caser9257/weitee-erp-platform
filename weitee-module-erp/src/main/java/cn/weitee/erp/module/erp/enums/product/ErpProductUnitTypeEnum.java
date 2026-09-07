package cn.weitee.erp.module.erp.enums.product;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * ERP 产品单位类型枚举
 *
 * 单层换算模型：辅助单位只能挂在基本单位下，禁止链式多级换算。
 */
@RequiredArgsConstructor
@Getter
public enum ErpProductUnitTypeEnum implements ArrayValuable<Integer> {

    BASE(0, "基本单位"),
    AUXILIARY(1, "辅助单位");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ErpProductUnitTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型值
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
