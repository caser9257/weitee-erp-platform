package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpFinanceDualLedgerCompareStatusEnum implements ArrayValuable<Integer> {

    MATCHED(10, "一致"),
    MISSING(20, "缺少凭证"),
    AMOUNT_DIFF(30, "金额不一致"),
    STATUS_DIFF(40, "状态不一致"),
    ITEM_DIFF(50, "明细不一致");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpFinanceDualLedgerCompareStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    public static String resolveName(Integer status) {
        return Arrays.stream(values())
                .filter(item -> item.status.equals(status))
                .map(ErpFinanceDualLedgerCompareStatusEnum::getName)
                .findFirst()
                .orElse(null);
    }

    public static boolean isConsistent(Integer status) {
        return MATCHED.status.equals(status);
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
