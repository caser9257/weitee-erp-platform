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
 * 审批排班 DO
 *
 * 记录用户的排班信息，用于审批时判断是否为工作日
 */
@TableName("bpm_approval_schedule")
@KeySequence("bpm_approval_schedule_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmApprovalScheduleDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 排班日期
     */
    private Date scheduleDate;

    /**
     * 排班类型
     *
     * WORK: 工作日
     * REST: 休息日
     * LEAVE: 请假
     */
    private String scheduleType;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 备注
     */
    private String remark;

}
