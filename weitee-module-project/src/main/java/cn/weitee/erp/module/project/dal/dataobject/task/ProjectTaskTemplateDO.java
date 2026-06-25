package cn.weitee.erp.module.project.dal.dataobject.task;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

@TableName("project_task_template")
@KeySequence("project_task_template_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTaskTemplateDO extends BaseDO {
    @TableId
    private Long id;
    private Long projectId;
    private String name;
    private String content;
    private Integer sort;
}