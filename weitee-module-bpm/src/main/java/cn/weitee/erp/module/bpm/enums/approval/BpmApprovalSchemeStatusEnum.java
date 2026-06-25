package cn.weitee.erp.module.bpm.enums.approval;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 审批方案版本状态枚举
 *
 * 状态流转：DRAFT(10) → PENDING_PUBLISH(20) → ACTIVE(30) ↔ DISABLED(40)
 */
@Getter
@AllArgsConstructor
public enum BpmApprovalSchemeStatusEnum implements ArrayValuable<Integer> {

    DRAFT(10, "草稿"),
    PENDING_PUBLISH(20, "待发布"),
    ACTIVE(30, "生效中"),
    DISABLED(40, "已停用");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(BpmApprovalSchemeStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String desc;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
