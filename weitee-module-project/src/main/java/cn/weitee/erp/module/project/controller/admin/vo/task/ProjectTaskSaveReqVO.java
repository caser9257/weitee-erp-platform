package cn.weitee.erp.module.project.controller.admin.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 任务创建/更新 Request VO")
@Data
public class ProjectTaskSaveReqVO {
    @Schema(description = "任务编号")
    private Long id;
    @Schema(description = "父任务ID（0=顶级任务）")
    private Long parentId;
    @Schema(description = "项目编号", required = true)
    @NotNull(message = "项目编号不能为空")
    private Long projectId;
    @Schema(description = "列表编号", required = true)
    @NotNull(message = "列表编号不能为空")
    private Long columnId;
    @Schema(description = "任务标题", required = true)
    @NotBlank(message = "任务标题不能为空")
    @Size(max = 255, message = "任务标题最多 255 个字符")
    private String name;
    @Schema(description = "任务描述")
    private String description;
    @Schema(description = "背景色")
    private String color;
    @Schema(description = "计划开始时间")
    private LocalDateTime startAt;
    @Schema(description = "计划结束时间")
    private LocalDateTime endAt;
    @Schema(description = "可见性(1项目人员 2任务人员 3指定成员)")
    private Integer visibility;
    @Schema(description = "优先级等级")
    private Integer priorityLevel;
    @Schema(description = "优先级名称")
    private String priorityName;
    @Schema(description = "优先级颜色")
    private String priorityColor;
    @Schema(description = "负责人用户ID列表")
    private List<Long> ownerUserIds;
    @Schema(description = "协助人用户ID列表")
    private List<Long> assistUserIds;
    @Schema(description = "标签列表")
    private List<TaskTagVO> taskTags;
    @Schema(description = "富文本内容")
    private String content;

    @Data
    public static class TaskTagVO {
        private String name;
        private String color;
    }
}
