package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpApStatementStatusEnum implements ArrayValuable<Integer> {

    UNPAID(10, "未付款"),
    PARTIAL_PAID(20, "部分付款"),
    SETTLED(30, "已结清"),
    CLOSED(40, "已关闭");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpApStatementStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
