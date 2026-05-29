package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpPurchaseInQualityStatusEnum implements ArrayValuable<Integer> {

    DRAFT(10, "草稿"),
    FIRST_CHECKING(20,"初检中"),
    WAIT_RECHECK(30, "待复检"),
    RECHECKING(40, "复检中"),
    DONE(50, "已完成"),
    VOID(60, "已作废");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpPurchaseInQualityStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
