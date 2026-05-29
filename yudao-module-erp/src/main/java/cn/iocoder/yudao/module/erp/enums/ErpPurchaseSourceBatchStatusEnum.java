package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * ERP 采购来源批次状态
 */
@RequiredArgsConstructor
@Getter
public enum ErpPurchaseSourceBatchStatusEnum implements ArrayValuable<Integer> {

    ACTIVE(20, "生效"),
    CLOSED(30, "关闭");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpPurchaseSourceBatchStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
