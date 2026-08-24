package cn.weitee.erp.module.erp.enums.rd;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 研发 BOM 完整性校验问题类型
 */
@Getter
@AllArgsConstructor
public enum RdBomIntegrityIssueType {

    FLOATING_MATERIAL(1, "ERROR", "子件物料不存在（悬浮件）"),
    USAGE_INVALID(2, "ERROR", "用量无效：为空或小于等于 0"),
    DESIGNATOR_ON_ASSEMBLY(3, "WARN", "装配体（自制件）不应填写位号"),
    FLOATING_ASSEMBLY(4, "WARN", "自制件未挂接下层 BOM（悬空装配体）"),
    MISSING_DESIGNATOR(5, "ERROR", "需位号的元器件未填写位号"),
    DESIGNATOR_COUNT_MISMATCH(6, "ERROR", "位号数量与用量不一致");

    /**
     * 问题类型编码
     */
    private final Integer type;
    /**
     * 严重程度：ERROR / WARN
     */
    private final String severity;
    /**
     * 默认问题描述
     */
    private final String defaultMessage;

}
