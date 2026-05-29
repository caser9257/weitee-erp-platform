package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpApInvoiceStatusEnum implements ArrayValuable<Integer> {

    NONE(0, "未收票"),
    PARTIAL(1, "部分收票"),
    RECEIVED(2, "已收票");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpApInvoiceStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
