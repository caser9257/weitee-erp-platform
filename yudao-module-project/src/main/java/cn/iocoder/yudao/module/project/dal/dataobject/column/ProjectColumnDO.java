package cn.iocoder.yudao.module.project.dal.dataobject.column;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("project_column")
@KeySequence("project_column_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectColumnDO extends BaseDO {
    @TableId
    private Long id;
    private Long projectId;
    private String name;
    private Integer sort;
}
