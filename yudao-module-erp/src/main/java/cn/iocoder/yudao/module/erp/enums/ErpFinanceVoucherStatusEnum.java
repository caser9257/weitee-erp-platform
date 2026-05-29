package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ErpFinanceVoucherStatusEnum implements ArrayValuable<Integer> {

    GENERATED(10, "已生成"),
    APPROVED(20, "已审核"),
    POSTED(30, "已过账"),
    REVERSED(40, "已冲销"),
    VOIDED(50, "已作废");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ErpFinanceVoucherStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isGenerated(Integer status) {
        return GENERATED.getStatus().equals(status);
    }

    public static boolean isApproved(Integer status) {
        return APPROVED.getStatus().equals(status);
    }

    public static boolean isPosted(Integer status) {
        return POSTED.getStatus().equals(status);
    }

    public static boolean isReversed(Integer status) {
        return REVERSED.getStatus().equals(status);
    }

    public static boolean isVoided(Integer status) {
        return VOIDED.getStatus().equals(status);
    }

}
