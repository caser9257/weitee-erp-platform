package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpApEstimateClosureStatusEnum implements ArrayValuable<Integer> {

    GENERATED_OPEN(10, "待确认未收票"),
    CONFIRMED_OPEN(20, "已确认待收票"),
    REVERSED_BY_INVOICE(30, "已随收票冲回"),
    REVERSED_BY_STATEMENT_CLOSED(40, "已随台账关闭冲回"),
    REVERSED_MANUAL(50, "已手工冲回"),
    EXCEPTION_BILLED_NOT_REVERSED(90, "异常：已收票未冲回"),
    EXCEPTION_CLOSED_NOT_REVERSED(91, "异常：台账已关闭未冲回");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpApEstimateClosureStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
