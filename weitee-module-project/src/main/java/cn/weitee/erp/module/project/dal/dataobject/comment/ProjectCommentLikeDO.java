package cn.weitee.erp.module.project.dal.dataobject.comment;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("project_comment_like")
@KeySequence("project_comment_like_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCommentLikeDO extends BaseDO {

    @TableId
    private Long id;
    private Long commentId;
    private Long userId;

}
