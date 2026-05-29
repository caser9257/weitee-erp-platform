package cn.iocoder.yudao.module.project.controller.admin.vo.meeting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 会议创建/更新 Request VO")
@Data
public class ProjectMeetingSaveReqVO {
    @Schema(description = "会议编号", example = "1024")
    private Long id;
    @Schema(description = "会议名称", required = true, example = "项目周会")
    @NotBlank(message = "会议名称不能为空")
    private String name;
    @Schema(description = "会议描述", example = "讨论项目进度")
    private String description;
    @Schema(description = "开始时间")
    private LocalDateTime startAt;
    @Schema(description = "结束时间")
    private LocalDateTime endAt;
}
