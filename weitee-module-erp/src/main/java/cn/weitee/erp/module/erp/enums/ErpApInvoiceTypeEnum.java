package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpApInvoiceTypeEnum implements ArrayValuable<Integer> {

    SPECIAL(10, "专票"),
    NORMAL(20, "普票"),
    ELECTRONIC(30, "电子票");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpApInvoiceTypeEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
