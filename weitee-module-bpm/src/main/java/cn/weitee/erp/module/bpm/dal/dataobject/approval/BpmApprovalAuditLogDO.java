package cn.weitee.erp.module.bpm.dal.dataobject.approval;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 审批审计日志 DO
 *
 * 记录审批过程中的所有操作，用于审计和追溯
 */
@TableName("bpm_approval_audit_log")
@KeySequence("bpm_approval_audit_log_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmApprovalAuditLogDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 审批 ID
     */
    private String approvalId;

    /**
     * 任务 ID
     */
    private String taskId;

    /**
     * 操作类型
     *
     * SUBMIT: 提交审批
     * APPROVE: 审批通过
     * REJECT: 审批拒绝
     * RETURN: 审批驳回
     * CANCEL: 撤回审批
     * TRANSFER: 转办任务
     * DELEGATE: 委托任务
     * URGE: 催办任务
     * TIMEOUT: 超时处理
     * CANCEL_BY_ADMIN: 管理员终止
     */
    private String action;

    /**
     * 操作人 ID
     */
    private Long operatorUserId;

    /**
     * 操作人姓名
     */
    private String operatorUserName;

    /**
     * 目标用户 ID
     */
    private Long targetUserId;

    /**
     * 目标用户姓名
     */
    private String targetUserName;

    /**
     * 操作意见
     */
    private String comment;

    /**
     * IP 地址
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;

}
