package cn.iocoder.yudao.module.project.controller.admin.vo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 日报 Response VO")
@Data
public class ProjectReportRespVO {
    @Schema(description = "日报编号", example = "1024")
    private Long id;
    @Schema(description = "用户编号")
    private Long userId;
    @Schema(description = "日报类型", example = "daily")
    private String type;
    @Schema(description = "日报内容")
    private String content;
    @Schema(description = "签名")
    private String sign;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
