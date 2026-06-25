package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * ERP 双写状态枚举
 */
@Getter
@AllArgsConstructor
public enum ErpFinanceDualWriteStatusEnum implements ArrayValuable<Integer> {

    SUCCESS(1, "成功"),
    FAILED(2, "失败"),
    RETRYING(3, "重试中");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ErpFinanceDualWriteStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isSuccess(Integer status) {
        return SUCCESS.getStatus().equals(status);
    }

    public static boolean isFailed(Integer status) {
        return FAILED.getStatus().equals(status);
    }

    public static boolean isRetrying(Integer status) {
        return RETRYING.getStatus().equals(status);
    }
}
