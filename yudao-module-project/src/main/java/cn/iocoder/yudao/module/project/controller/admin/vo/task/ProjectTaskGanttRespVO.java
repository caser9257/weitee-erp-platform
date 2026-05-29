package cn.iocoder.yudao.module.project.controller.admin.vo.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 甘特图 Response VO")
@Data
public class ProjectTaskGanttRespVO {

    @Schema(description = "任务编号", example = "1024")
    private Long id;

    @Schema(description = "父任务编号")
    private Long parentId;

    @Schema(description = "项目编号", example = "1024")
    private Long projectId;

    @Schema(description = "任务名称", example = "设计UI原型")
    private String name;

    @Schema(description = "计划开始时间")
    private LocalDateTime startAt;

    @Schema(description = "计划结束时间")
    private LocalDateTime endAt;

    @Schema(description = "完成时间")
    private LocalDateTime completeAt;

    @Schema(description = "进度(0-100)", example = "50")
    private Integer progress;

    @Schema(description = "优先级颜色")
    private String priorityColor;

    @Schema(description = "负责人姓名")
    private String assigneeName;

    @Schema(description = "依赖的任务编号列表")
    private List<Long> dependencies;

}
