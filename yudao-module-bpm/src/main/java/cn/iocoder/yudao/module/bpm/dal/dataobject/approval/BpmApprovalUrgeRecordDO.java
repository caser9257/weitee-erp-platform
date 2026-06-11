package cn.iocoder.yudao.module.bpm.dal.dataobject.approval;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 催办记录 DO
 *
 * 记录审批催办操作
 */
@TableName("bpm_approval_urge_record")
@KeySequence("bpm_approval_urge_record_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmApprovalUrgeRecordDO extends BaseDO {

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
     * 催办人 ID
     */
    private Long urgeUserId;

    /**
     * 催办消息
     */
    private String urgeMessage;

    /**
     * 催办时间
     */
    private Date urgeTime;

}
