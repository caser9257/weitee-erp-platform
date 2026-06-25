package cn.weitee.erp.module.project.dal.dataobject.project;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import java.time.LocalDateTime;

@TableName("project_project_user")
@KeySequence("project_project_user_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUserDO extends BaseDO {
    @TableId
    private Long id;
    private Long projectId;
    private Long userId;
    private Boolean owner;
    private Integer sort;
    private LocalDateTime topAt;
}
