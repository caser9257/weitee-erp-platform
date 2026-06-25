package cn.weitee.erp.module.project.controller.admin.vo.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 项目统计概览 Response VO")
@Data
public class ProjectStatisticsOverviewRespVO {

    @Schema(description = "总任务数", example = "100")
    private Integer totalTasks;

    @Schema(description = "已完成任务数", example = "50")
    private Integer completedTasks;

    @Schema(description = "进行中任务数", example = "30")
    private Integer inProgressTasks;

    @Schema(description = "逾期任务数", example = "5")
    private Integer overdueTasks;

    @Schema(description = "完成率", example = "50.00")
    private BigDecimal completionRate;

    @Schema(description = "成员数", example = "10")
    private Integer totalMembers;

}
