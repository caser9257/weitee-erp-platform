package cn.iocoder.yudao.module.erp.enums.mrp;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpProductionCostAllocationBasisTypeEnum implements ArrayValuable<Integer> {

    MAN_HOUR(10, "人工工时"),
    OUTPUT(20, "产量"),
    WEIGHT(30, "重量");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpProductionCostAllocationBasisTypeEnum::getType)
            .toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Deprecated
    public static final ErpProductionCostAllocationBasisTypeEnum FINISHED_QTY = OUTPUT;
    @Deprecated
    public static final ErpProductionCostAllocationBasisTypeEnum FINISHED_WEIGHT = WEIGHT;

    public static String resolveName(Integer type) {
        return Arrays.stream(values())
                .filter(item -> item.type.equals(type))
                .map(ErpProductionCostAllocationBasisTypeEnum::getName)
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
