package cn.weitee.erp.module.project.dal.dataobject.approve;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

@TableName("project_approve_msg")
@KeySequence("project_approve_msg_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectApproveMsgDO extends BaseDO {
    @TableId
    private Long id;
    private Long approveId;
    private Long userId;
    private String action;
    private String content;
}
