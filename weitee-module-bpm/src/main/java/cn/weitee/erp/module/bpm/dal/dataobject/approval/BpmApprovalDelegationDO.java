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
 * 审批委托配置 DO
 *
 * 用于实现长期代理功能，审批人可以将审批任务委托给其他人
 */
@TableName("bpm_approval_delegation")
@KeySequence("bpm_approval_delegation_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmApprovalDelegationDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 委托人 ID
     */
    private Long userId;

    /**
     * 代理人 ID
     */
    private Long delegateUserId;

    /**
     * 委托开始时间
     */
    private Date startTime;

    /**
     * 委托结束时间
     */
    private Date endTime;

    /**
     * 场景编码（为空表示所有场景）
     */
    private String sceneCode;

    /**
     * 委托原因
     */
    private String reason;

    /**
     * 状态，0-正常 1-停用
     */
    private Integer status;

}
