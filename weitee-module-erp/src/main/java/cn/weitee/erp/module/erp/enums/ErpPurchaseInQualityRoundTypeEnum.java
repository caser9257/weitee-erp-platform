package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpPurchaseInQualityRoundTypeEnum implements ArrayValuable<Integer> {
    FIRST(1, "初检"),
    RECHECK(2, "复检");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpPurchaseInQualityRoundTypeEnum::getType)
            .toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
