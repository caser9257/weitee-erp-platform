package cn.iocoder.yudao.module.project.dal.dataobject.task;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

@TableName("project_task_user")
@KeySequence("project_task_user_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTaskUserDO extends BaseDO {
    @TableId
    private Long id;
    private Long projectId;
    private Long taskId;
    private Long taskPid;
    private Long userId;
    private Boolean owner;
}