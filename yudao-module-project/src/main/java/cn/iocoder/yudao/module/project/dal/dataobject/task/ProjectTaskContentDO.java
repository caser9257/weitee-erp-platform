package cn.iocoder.yudao.module.project.dal.dataobject.task;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

@TableName(value = "project_task_content", autoResultMap = true)
@KeySequence("project_task_content_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTaskContentDO extends BaseDO {
    @TableId
    private Long id;
    private Long projectId;
    private Long taskId;
    private Long userId;
    private String description;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object content;
}