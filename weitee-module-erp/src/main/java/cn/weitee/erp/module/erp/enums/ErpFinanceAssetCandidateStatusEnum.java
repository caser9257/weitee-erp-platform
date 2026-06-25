package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpFinanceAssetCandidateStatusEnum implements ArrayValuable<Integer> {

    PENDING_CONFIRM(0, "待确认"),
    CONFIRMED(10, "已确认"),
    REJECTED(20, "已驳回");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpFinanceAssetCandidateStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
