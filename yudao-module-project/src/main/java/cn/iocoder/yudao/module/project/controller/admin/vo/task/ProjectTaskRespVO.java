package cn.iocoder.yudao.module.project.controller.admin.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 任务 Response VO")
@Data
public class ProjectTaskRespVO {
    @Schema(description = "任务编号")
    private Long id;
    @Schema(description = "父任务ID")
    private Long parentId;
    @Schema(description = "项目编号")
    private Long projectId;
    @Schema(description = "列表编号")
    private Long columnId;
    @Schema(description = "工作流状态ID")
    private Long flowItemId;
    @Schema(description = "工作流状态名称")
    private String flowItemName;
    @Schema(description = "任务标题")
    private String name;
    @Schema(description = "任务描述")
    private String description;
    @Schema(description = "背景色")
    private String color;
    @Schema(description = "计划开始时间")
    private LocalDateTime startAt;
    @Schema(description = "计划结束时间")
    private LocalDateTime endAt;
    @Schema(description = "完成时间")
    private LocalDateTime completeAt;
    @Schema(description = "归档时间")
    private LocalDateTime archivedAt;
    @Schema(description = "可见性")
    private Integer visibility;
    @Schema(description = "优先级等级")
    private Integer priorityLevel;
    @Schema(description = "优先级名称")
    private String priorityName;
    @Schema(description = "优先级颜色")
    private String priorityColor;
    @Schema(description = "排序")
    private Integer sort;
    @Schema(description = "负责人列表")
    private List<TaskUserVO> owners;
    @Schema(description = "协助人列表")
    private List<TaskUserVO> assistants;
    @Schema(description = "标签列表")
    private List<TaskTagVO> taskTags;
    @Schema(description = "子任务数量")
    private Integer subTaskCount;
    @Schema(description = "子任务完成数量")
    private Integer subTaskCompleteCount;
    @Schema(description = "进度(0-100)")
    private Integer percent;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "创建人")
    private String creator;

    @Data
    public static class TaskUserVO {
        private Long userId;
        private String userName;
        private Boolean owner;
    }

    @Data
    public static class TaskTagVO {
        private String name;
        private String color;
    }
}
