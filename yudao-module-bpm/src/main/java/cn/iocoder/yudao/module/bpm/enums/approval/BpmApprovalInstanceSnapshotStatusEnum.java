package cn.iocoder.yudao.module.bpm.enums.approval;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 审批运行时快照状态枚举
 */
@Getter
@AllArgsConstructor
public enum BpmApprovalInstanceSnapshotStatusEnum implements ArrayValuable<Integer> {

    PROCESSING(1, "审批中"),
    APPROVE(2, "通过"),
    REJECT(3, "驳回"),
    CANCEL(4, "撤回"),
    FAILED(5, "提交失败");

    /**
     * PROCESSING 状态的整数值，用于 SQL 注解中的常量引用
     */
    public static final int PROCESSING_STATUS_VALUE = 1;

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(BpmApprovalInstanceSnapshotStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String desc;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
