package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpPurchaseInStockExecuteStatusEnum implements ArrayValuable<Integer> {

    EXECUTED(20, "已执行"),
    VOID(30, "已作废");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpPurchaseInStockExecuteStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
