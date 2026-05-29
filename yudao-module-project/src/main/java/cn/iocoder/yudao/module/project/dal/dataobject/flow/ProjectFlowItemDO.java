package cn.iocoder.yudao.module.project.dal.dataobject.flow;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

@TableName(value = "project_flow_item", autoResultMap = true)
@KeySequence("project_flow_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectFlowItemDO extends BaseDO {
    @TableId
    private Long id;
    private Long flowId;
    private Long projectId;
    private String name;
    private String status;
    private String color;
    private Integer sort;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String turns;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String userIds;
    private String userType;
    private Boolean userLimit;
    private Long columnId;
}
