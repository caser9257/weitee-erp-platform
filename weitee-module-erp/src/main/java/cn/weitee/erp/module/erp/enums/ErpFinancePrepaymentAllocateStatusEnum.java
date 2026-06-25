package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum ErpFinancePrepaymentAllocateStatusEnum implements ArrayValuable<Integer> {

    DRAFT(10, "草稿"),
    APPROVED(20, "已生效"),
    CANCELED(30, "已撤销");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpFinancePrepaymentAllocateStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
