package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpQcSamplingSchemeTypeEnum implements ArrayValuable<Integer> {

    FULL(10, "全检"),
    RATIO(20, "比例抽检"),
    FIXED(30, "固定数抽检"),
    SKIP(40, "免检");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpQcSamplingSchemeTypeEnum::getType)
            .toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
