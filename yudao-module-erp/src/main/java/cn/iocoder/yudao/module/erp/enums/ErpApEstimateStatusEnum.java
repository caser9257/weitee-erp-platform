package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * ERP 暂估状态枚举
 */
@RequiredArgsConstructor
@Getter
public enum ErpApEstimateStatusEnum implements ArrayValuable<Integer> {

    GENERATED(10, "待确认"),
    CONFIRMED(20, "已确认"),
    REVERSED(30, "已冲回");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ErpApEstimateStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    public static ErpApEstimateStatusEnum fromStatus(Integer status) {
        if (status == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(item -> item.status.equals(status))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
