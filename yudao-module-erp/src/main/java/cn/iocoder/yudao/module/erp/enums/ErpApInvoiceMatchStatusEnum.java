package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpApInvoiceMatchStatusEnum implements ArrayValuable<Integer> {

    UNMATCHED(10, "未匹配"),
    PARTIAL(20, "部分匹配"),
    MATCHED(30, "已匹配"),
    EXCEPTION(40, "异常");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpApInvoiceMatchStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
