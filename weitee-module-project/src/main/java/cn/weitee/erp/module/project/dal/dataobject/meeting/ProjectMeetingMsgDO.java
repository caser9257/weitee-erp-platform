package cn.weitee.erp.module.project.dal.dataobject.meeting;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

@TableName("project_meeting_msg")
@KeySequence("project_meeting_msg_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMeetingMsgDO extends BaseDO {
    @TableId
    private Long id;
    private Long meetingId;
    private Long userId;
    private String content;
}
