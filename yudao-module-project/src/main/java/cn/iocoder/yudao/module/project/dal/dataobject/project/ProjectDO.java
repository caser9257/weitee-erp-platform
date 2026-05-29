package cn.iocoder.yudao.module.project.dal.dataobject.project;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

@TableName("project_project")
@KeySequence("project_project_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDO extends BaseDO {
    @TableId
    private Long id;
    private String name;
    private String description;
    private Long ownerUserId;
    private Boolean personal;
    private String archiveMethod;
    private Integer archiveDays;
    private LocalDateTime archivedAt;
    private Long archivedUserId;
}
