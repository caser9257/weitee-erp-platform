package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * ERP 凭证生成失败记录状态枚举
 *
 * 状态流转：PENDING → SUCCESS（重试成功）/ PENDING（重试失败）/ CONFIRMED（人工确认关闭）
 * SUCCESS、CONFIRMED 为终态
 *
 * @author WeTai
 */
@RequiredArgsConstructor
@Getter
public enum ErpFinanceVoucherFailureStatusEnum implements ArrayValuable<Integer> {

    PENDING(0, "待重试"),
    SUCCESS(1, "重试成功"),
    CONFIRMED(2, "人工确认关闭");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(ErpFinanceVoucherFailureStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
