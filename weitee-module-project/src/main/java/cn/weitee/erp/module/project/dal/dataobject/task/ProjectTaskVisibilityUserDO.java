package cn.weitee.erp.module.project.dal.dataobject.task;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

@TableName("project_task_visibility_user")
@KeySequence("project_task_visibility_user_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTaskVisibilityUserDO extends BaseDO {
    @TableId
    private Long id;
    private Long projectId;
    private Long taskId;
    private Long userId;
}