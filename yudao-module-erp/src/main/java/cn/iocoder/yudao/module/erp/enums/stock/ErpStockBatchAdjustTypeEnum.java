package cn.iocoder.yudao.module.erp.enums.stock;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * ERP 批次库存调整类型
 */
@RequiredArgsConstructor
@Getter
public enum ErpStockBatchAdjustTypeEnum implements ArrayValuable<Integer> {

    INCREASE(1, "调增"),
    DECREASE(2, "调减");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpStockBatchAdjustTypeEnum::getType)
            .toArray(Integer[]::new);

    private final Integer type;

    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
