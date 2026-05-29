package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpApInvoiceMatchItemStatusEnum implements ArrayValuable<Integer> {

    ACTIVE(10, "生效"),
    CANCELED(20, "已撤销");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpApInvoiceMatchItemStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
