package cn.iocoder.yudao.module.project.dal.dataobject.project;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.*;

@TableName(value = "project_log", autoResultMap = true)
@KeySequence("project_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectLogDO extends BaseDO {
    @TableId
    private Long id;
    private Long projectId;
    private Long columnId;
    private Long taskId;
    private Long userId;
    private String detail;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object record;
}
