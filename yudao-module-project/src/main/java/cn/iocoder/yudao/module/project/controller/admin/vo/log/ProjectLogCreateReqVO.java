package cn.iocoder.yudao.module.project.controller.admin.vo.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "管理后台 - 操作日志创建 Request VO")
@Data
public class ProjectLogCreateReqVO {

    @Schema(description = "项目编号", required = true, example = "1024")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "列表编号", example = "1024")
    private Long columnId;

    @Schema(description = "任务编号", example = "1024")
    private Long taskId;

    @Schema(description = "日志详情", required = true, example = "创建了任务")
    @NotEmpty(message = "日志详情不能为空")
    @Size(max = 500, message = "日志详情长度不能超过 500 个字符")
    private String detail;

    @Schema(description = "变更记录")
    private Object record;

}
