package cn.weitee.erp.module.project.dal.dataobject.meeting;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

@TableName("project_meeting")
@KeySequence("project_meeting_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMeetingDO extends BaseDO {
    @TableId
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
