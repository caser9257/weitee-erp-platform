package cn.iocoder.yudao.module.bpm.enums.approval;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 审批场景状态枚举
 */
@Getter
@AllArgsConstructor
public enum BpmApprovalSceneStatusEnum implements ArrayValuable<Integer> {

    ENABLED(1, "启用"),
    DISABLED(0, "禁用");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(BpmApprovalSceneStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String desc;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
