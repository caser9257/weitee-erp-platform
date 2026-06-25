package cn.weitee.erp.module.project.dal.dataobject.comment;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

@TableName(value = "project_comment", autoResultMap = true)
@KeySequence("project_comment_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCommentDO extends BaseDO {

    @TableId
    private Long id;
    private Long projectId;
    private Long taskId;
    private Long parentId;
    private String content;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object contentJson;
    private Long userId;
    private Long replyUserId;
    private Long replyCommentId;
    private Integer likeCount;
    private Integer status;

}
