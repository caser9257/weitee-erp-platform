package cn.weitee.erp.module.project.dal.dataobject.task;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

@TableName("project_task_relation")
@KeySequence("project_task_relation_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTaskRelationDO extends BaseDO {
    @TableId
    private Long id;
    private Long taskId;
    private Long relatedTaskId;
    private String relationType;
}