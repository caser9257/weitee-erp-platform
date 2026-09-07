package cn.weitee.erp.module.erp.enums.rd;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErpRdBomStatusEnum {

    DRAFT(0),
    PROCESS(10),
    APPROVE(20),
    CR_PENDING(21),
    EDITING(22),
    CONFIRM_PENDING(23),
    REJECT(30),
    VOID(50),
    FAILED(60);

    private final Integer status;

    /**
     * 是否处于两段式审批流状态（禁止流转阶段、禁止修改）
     */
    public static boolean isInApprovalFlow(Integer status) {
        return CR_PENDING.status.equals(status)
                || CONFIRM_PENDING.status.equals(status);
    }

    /**
     * 是否处于编辑中状态（已获编辑权限，允许修改）
     */
    public static boolean isEditing(Integer status) {
        return EDITING.status.equals(status);
    }

}
