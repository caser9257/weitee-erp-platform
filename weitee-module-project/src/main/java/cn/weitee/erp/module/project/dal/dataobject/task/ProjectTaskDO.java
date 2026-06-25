package cn.weitee.erp.module.project.dal.dataobject.task;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

@TableName("project_task")
@KeySequence("project_task_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTaskDO extends BaseDO {
    @TableId
    private Long id;
    private Long parentId;
    private Long projectId;
    private Long columnId;
    private Long flowItemId;
    private String flowItemName;
    private String name;
    private String description;
    private String color;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime completeAt;
    private LocalDateTime archivedAt;
    private Long archivedUserId;
    private Boolean archivedFollow;
    private Integer visibility;
    private Integer priorityLevel;
    private String priorityName;
    private String priorityColor;
    private Integer sort;
    private String loopRule;
    private LocalDateTime loopAt;
    private Long deletedUserId;
}