package cn.weitee.erp.module.project.dal.dataobject.task;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

@TableName("project_task_flow_change")
@KeySequence("project_task_flow_change_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTaskFlowChangeDO extends BaseDO {
    @TableId
    private Long id;
    private Long taskId;
    private Long userId;
    private Long beforeFlowItemId;
    private String beforeFlowItemName;
    private Long afterFlowItemId;
    private String afterFlowItemName;
}