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
 * 审批记录 DO
 *
 * 记录审批过程中的所有操作，包括通过、拒绝、驳回、撤回、转办、催办等
 */
@TableName("bpm_approval_record")
@KeySequence("bpm_approval_record_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmApprovalRecordDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 审批ID（业务唯一标识）
     */
    private String approvalId;

    /**
     * Flowable 任务 ID
     */
    private String taskId;

    /**
     * 操作类型
     * 
     * APPROVE - 审批通过
     * REJECT - 审批拒绝
     * RETURN - 审批驳回
     * WITHDRAW - 撤回审批
     * TRANSFER - 转办
     * DELEGATE - 委派
     * URGE - 催办
     */
    private String action;

    /**
     * 操作人 ID
     */
    private Long operatorUserId;

    /**
     * 操作意见
     */
    private String comment;

    /**
     * 目标用户 ID（转办/委派时使用）
     */
    private Long targetUserId;

    /**
     * 目标节点 ID（驳回时使用）
     */
    private String targetNodeId;

}
