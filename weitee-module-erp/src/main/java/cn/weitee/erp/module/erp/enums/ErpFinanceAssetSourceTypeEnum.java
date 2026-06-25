package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpFinanceAssetSourceTypeEnum implements ArrayValuable<Integer> {

    MANUAL(0, "手工新增"),
    PURCHASE_IN(10, "采购入库"),
    FINANCE_EXPENSE(20, "费用报销");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpFinanceAssetSourceTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
