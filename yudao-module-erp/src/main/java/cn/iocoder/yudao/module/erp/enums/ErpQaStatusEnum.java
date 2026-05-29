package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpQaStatusEnum implements ArrayValuable<Integer> {

    TO_INSPECT(10, "待质检"),
    PARTIAL(20, "部分合格"),
    PASSED(30, "全部合格"),
    REJECTED(40, "全部不合格");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpQaStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
