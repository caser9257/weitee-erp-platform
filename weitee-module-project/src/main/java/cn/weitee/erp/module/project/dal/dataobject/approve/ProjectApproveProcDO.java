package cn.weitee.erp.module.project.dal.dataobject.approve;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

@TableName("project_approve_proc")
@KeySequence("project_approve_proc_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectApproveProcDO extends BaseDO {
    @TableId
    private Long id;
    private String name;
    private Integer status;
}
