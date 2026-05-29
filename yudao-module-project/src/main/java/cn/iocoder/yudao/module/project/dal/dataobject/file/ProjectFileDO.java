package cn.iocoder.yudao.module.project.dal.dataobject.file;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

@TableName("project_file")
@KeySequence("project_file_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectFileDO extends BaseDO {
    @TableId
    private Long id;
    private String name;
    private String ext;
    private Long size;
    private Integer type;
    private String path;
    private String url;
}
