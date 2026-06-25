package cn.weitee.erp.module.project.dal.dataobject.project;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

@TableName("project_invite")
@KeySequence("project_invite_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectInviteDO extends BaseDO {
    @TableId
    private Long id;
    private Long projectId;
    private Long userId;
    private Long inviteUserId;
    private Integer status;
}
