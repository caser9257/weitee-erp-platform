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
    // 位号族问题统一 WARN 不阻断保存：模块类 BOM（FM-RR-RD004）天然只有物料位置无位号，
    // 位号属工程补充信息，缺失不应禁止 BOM 落草稿
    MISSING_DESIGNATOR(5, "WARN", "元器件未填写位号，请补充以便 PCB 贴片核对"),
    DESIGNATOR_COUNT_MISMATCH(6, "WARN", "位号数量与用量不一致，请核对");

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

    /**
     * 按问题类型编码取默认描述（VO 仅序列化编码，回显文案时反查）
     */
    public static String defaultMessageOf(Integer type) {
        for (RdBomIntegrityIssueType issueType : values()) {
            if (issueType.type.equals(type)) {
                return issueType.defaultMessage;
            }
        }
        return "完整性校验未通过";
    }

}
