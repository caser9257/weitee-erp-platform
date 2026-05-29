package cn.iocoder.yudao.module.project.controller.admin.vo.meeting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 会议 Response VO")
@Data
public class ProjectMeetingRespVO {
    @Schema(description = "会议编号", example = "1024")
    private Long id;
    @Schema(description = "会议名称", example = "项目周会")
    private String name;
    @Schema(description = "会议描述")
    private String description;
    @Schema(description = "开始时间")
    private LocalDateTime startAt;
    @Schema(description = "结束时间")
    private LocalDateTime endAt;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
