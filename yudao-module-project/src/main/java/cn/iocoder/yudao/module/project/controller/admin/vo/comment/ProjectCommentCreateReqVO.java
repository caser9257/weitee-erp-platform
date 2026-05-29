package cn.iocoder.yudao.module.project.controller.admin.vo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 评论创建 Request VO")
@Data
public class ProjectCommentCreateReqVO {

    @Schema(description = "项目编号", required = true, example = "1024")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "任务编号", required = true, example = "1024")
    @NotNull(message = "任务编号不能为空")
    private Long taskId;

    @Schema(description = "父评论编号（回复时必填）", example = "1024")
    private Long parentId;

    @Schema(description = "评论内容", required = true, example = "这个任务需要优先完成")
    @NotEmpty(message = "评论内容不能为空")
    private String content;

    @Schema(description = "评论内容（富文本JSON）")
    private String contentJson;

    @Schema(description = "回复目标用户编号", example = "1024")
    private Long replyUserId;

    @Schema(description = "回复目标评论编号", example = "1024")
    private Long replyCommentId;

    @Schema(description = "@提及的用户编号列表")
    private List<Long> mentionUserIds;

}
