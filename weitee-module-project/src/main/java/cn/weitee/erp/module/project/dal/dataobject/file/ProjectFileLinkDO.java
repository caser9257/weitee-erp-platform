package cn.weitee.erp.module.project.dal.dataobject.file;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

@TableName("project_file_link")
@KeySequence("project_file_link_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectFileLinkDO extends BaseDO {
    @TableId
    private Long id;
    private Long fileId;
    private String sourceType;
    private Long sourceId;
    private Long userId;
}
