package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpPurchaseInStockInStatusEnum implements ArrayValuable<Integer> {

    TO_STOCK_IN(10, "TO_STOCK_IN"),
    PARTIAL_STOCKED_IN(15, "PARTIAL_STOCKED_IN"),
    STOCKED_IN(20, "STOCKED_IN"),
    NO_NEED_STOCK_IN(30, "NO_NEED_STOCK_IN");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpPurchaseInStockInStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
