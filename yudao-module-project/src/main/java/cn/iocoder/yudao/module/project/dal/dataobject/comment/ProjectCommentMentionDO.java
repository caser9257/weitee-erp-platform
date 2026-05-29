package cn.iocoder.yudao.module.project.dal.dataobject.comment;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("project_comment_mention")
@KeySequence("project_comment_mention_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCommentMentionDO extends BaseDO {

    @TableId
    private Long id;
    private Long commentId;
    private Long userId;
    private Boolean notified;

}
