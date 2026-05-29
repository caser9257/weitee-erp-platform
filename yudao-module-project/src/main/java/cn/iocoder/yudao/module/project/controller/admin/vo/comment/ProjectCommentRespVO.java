package cn.iocoder.yudao.module.project.controller.admin.vo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 评论 Response VO")
@Data
public class ProjectCommentRespVO {

    @Schema(description = "评论编号", example = "1024")
    private Long id;

    @Schema(description = "项目编号", example = "1024")
    private Long projectId;

    @Schema(description = "任务编号", example = "1024")
    private Long taskId;

    @Schema(description = "父评论编号")
    private Long parentId;

    @Schema(description = "评论内容", example = "这个任务需要优先完成")
    private String content;

    @Schema(description = "评论内容（富文本JSON）")
    private Object contentJson;

    @Schema(description = "评论人编号", example = "1024")
    private Long userId;

    @Schema(description = "评论人姓名", example = "张三")
    private String userName;

    @Schema(description = "评论人头像")
    private String userAvatar;

    @Schema(description = "回复目标用户编号")
    private Long replyUserId;

    @Schema(description = "回复目标用户姓名")
    private String replyUserName;

    @Schema(description = "回复目标评论编号")
    private Long replyCommentId;

    @Schema(description = "被回复的评论内容摘要")
    private String replyCommentContent;

    @Schema(description = "点赞数", example = "5")
    private Integer likeCount;

    @Schema(description = "当前用户是否已点赞", example = "false")
    private Boolean likedByMe;

    @Schema(description = "回复列表")
    private List<ProjectCommentRespVO> replies;

    @Schema(description = "@提及的用户列表")
    private List<MentionUserVO> mentionUsers;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "@提及用户信息")
    @Data
    public static class MentionUserVO {

        @Schema(description = "用户编号", example = "1024")
        private Long userId;

        @Schema(description = "用户姓名", example = "李四")
        private String userName;

        @Schema(description = "用户头像")
        private String userAvatar;

    }

}
