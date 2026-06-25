package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpApEstimateReverseTypeEnum implements ArrayValuable<Integer> {

    MANUAL(10, "手工冲回"),
    INVOICE(20, "收票冲回"),
    STATEMENT_CLOSED(30, "台账关闭冲回");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpApEstimateReverseTypeEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    public static ErpApEstimateReverseTypeEnum fromStatus(Integer status) {
        if (status == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(item -> item.status.equals(status))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
