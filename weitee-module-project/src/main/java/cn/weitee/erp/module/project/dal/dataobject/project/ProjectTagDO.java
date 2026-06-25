package cn.weitee.erp.module.project.dal.dataobject.project;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

@TableName("project_tag")
@KeySequence("project_tag_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTagDO extends BaseDO {
    @TableId
    private Long id;
    private Long projectId;
    private String name;
    private String color;
    private Integer sort;
}
