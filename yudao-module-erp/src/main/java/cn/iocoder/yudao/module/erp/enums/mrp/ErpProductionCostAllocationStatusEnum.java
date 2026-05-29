package cn.iocoder.yudao.module.erp.enums.mrp;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpProductionCostAllocationStatusEnum implements ArrayValuable<Integer> {

    DRAFT(10, "草稿"),
    EXECUTED(20, "已执行");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpProductionCostAllocationStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    public static String resolveName(Integer status) {
        return Arrays.stream(values())
                .filter(item -> item.status.equals(status))
                .map(ErpProductionCostAllocationStatusEnum::getName)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
