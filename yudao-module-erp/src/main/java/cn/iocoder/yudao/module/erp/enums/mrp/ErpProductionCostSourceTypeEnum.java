package cn.iocoder.yudao.module.erp.enums.mrp;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpProductionCostSourceTypeEnum implements ArrayValuable<Integer> {

    MANUAL(10, "手工录入"),
    ALLOCATION(20, "分摊生成"),
    SYSTEM(30, "系统生成");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpProductionCostSourceTypeEnum::getType)
            .toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    public static String resolveName(Integer type) {
        return Arrays.stream(values())
                .filter(item -> item.type.equals(type))
                .map(ErpProductionCostSourceTypeEnum::getName)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
