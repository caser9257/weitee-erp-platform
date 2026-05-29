package cn.iocoder.yudao.module.project.controller.admin.vo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Schema(description = "管理后台 - 日报创建/更新 Request VO")
@Data
public class ProjectReportSaveReqVO {
    @Schema(description = "日报编号", example = "1024")
    private Long id;
    @Schema(description = "日报类型", required = true, example = "daily")
    @NotBlank(message = "日报类型不能为空")
    private String type;
    @Schema(description = "日报内容", required = true, example = "今日工作内容")
    @NotBlank(message = "日报内容不能为空")
    private String content;
    @Schema(description = "签名", example = "签名")
    private String sign;
}
