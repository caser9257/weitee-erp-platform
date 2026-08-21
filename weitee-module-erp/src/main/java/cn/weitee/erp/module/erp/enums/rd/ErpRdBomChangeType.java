package cn.weitee.erp.module.erp.enums.rd;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 研发 BOM 变更记录类型
 */
@Getter
@AllArgsConstructor
public enum ErpRdBomChangeType {

    CREATE("CREATE", "创建"),
    UPDATE("UPDATE", "修改"),
    SUBMIT("SUBMIT", "提交审批"),
    APPROVE("APPROVE", "审批通过"),
    REJECT("REJECT", "审批驳回"),
    CANCEL("CANCEL", "撤回审批"),
    CHANGE_CREATE("CHANGE_CREATE", "发起变更");

    /**
     * 变更类型编码
     */
    private final String type;
    /**
     * 变更类型名称
     */
    private final String name;

}
