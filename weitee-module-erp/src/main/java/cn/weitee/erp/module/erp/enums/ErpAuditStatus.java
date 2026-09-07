package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * ERP 审核状态枚举
 *
 * TODO 芋艿：目前只有待审批、已审批两个状态，未来接入工作流后，会丰富下：待提交（草稿）=》已提交（待审核）=》审核通过、审核不通过；另外，工作流需要支持“反审核”，把工作流退回到原点；
 *
 * @author WeTai
 */
@RequiredArgsConstructor
@Getter
public enum ErpAuditStatus implements ArrayValuable<Integer> {

    DRAFT(0, "草稿"),     // 草稿，可编辑
    PROCESS(10, "未审核"), // 审核中
    APPROVE(20, "已审核"), // 审核通过
    CR_PENDING(21, "变更申请审批中"), // 两段式阶段一审批中
    EDITING(22, "变更编辑中"),       // 两段式编辑中（已获编辑权限）
    CONFIRM_PENDING(23, "变更确认审批中"), // 两段式阶段二审批中
    OBSOLETE_CR_PENDING(24, "废除审批中"), // 废除审批中（一段式：通过即销号）
    OBSOLETED(27, "已废除"),     // 已废除（终态）
    STOP_PENDING(28, "启停审批中"), // 启停审批中（一段式：通过即切换 status）
    REJECT(30, "已驳回"), // 审核驳回
    CARRY_FORWARD(40, "已结转"), // 月末结转
    VOID(50, "已作废"), // 作废
    FAILED(60, "处理失败"); // BPM 创建失败，可重试

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ErpAuditStatus::getStatus).toArray(Integer[]::new);

    /**
     * 按状态值取状态名；未知值返回原值的字符串形式，避免提示信息丢失上下文
     */
    public static String nameOf(Integer status) {
        return Arrays.stream(values())
                .filter(item -> item.getStatus().equals(status))
                .map(ErpAuditStatus::getName)
                .findFirst()
                .orElse(String.valueOf(status));
    }

    /**
     * 是否处于审批流状态（变更两段/废除/启停的任一审批中阶段）。
     * 审批流状态中的对象禁止流转阶段、禁止修改（全局守卫）。
     */
    public static boolean isInApprovalFlow(Integer status) {
        return CR_PENDING.getStatus().equals(status)
                || CONFIRM_PENDING.getStatus().equals(status)
                || OBSOLETE_CR_PENDING.getStatus().equals(status)
                || STOP_PENDING.getStatus().equals(status);
    }

    /**
     * 是否处于编辑中状态（已获编辑权限，允许修改暂存）。
     */
    public static boolean isEditing(Integer status) {
        return EDITING.getStatus().equals(status);
    }

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }


}
