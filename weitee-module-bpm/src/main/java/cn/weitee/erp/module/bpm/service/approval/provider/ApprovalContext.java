package cn.weitee.erp.module.bpm.service.approval.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 审批上下文
 *
 * 由 ApprovalContextProvider 提供，包含审批所需的所有业务信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalContext {

    /**
     * 业务单据 ID
     */
    private Long bizId;

    /**
     * 业务单据编号
     */
    private String bizNo;

    /**
     * 业务单据标题
     */
    private String bizTitle;

    /**
     * 金额（可选）
     */
    private BigDecimal amount;

    /**
     * 部门 ID（可选）
     */
    private Long deptId;

    /**
     * 项目 ID（可选）
     */
    private Long projectId;

    /**
     * 发起人 ID
     */
    private Long startUserId;

    /**
     * 组织主体 ID（可选）
     */
    private Long organId;

    /**
     * 详情页链接
     */
    private String detailUrl;

    /**
     * 流程变量
     */
    @Builder.Default
    private Map<String, Object> variables = new HashMap<>();

    /**
     * 通知模板参数
     */
    @Builder.Default
    private Map<String, Object> notifyParams = new HashMap<>();

}
