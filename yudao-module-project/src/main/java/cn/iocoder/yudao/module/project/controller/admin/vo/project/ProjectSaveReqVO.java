package cn.iocoder.yudao.module.project.controller.admin.vo.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Schema(description = "管理后台 - 项目创建/更新 Request VO")
@Data
public class ProjectSaveReqVO {
    @Schema(description = "项目编号", example = "1024")
    private Long id;
    @Schema(description = "项目名称", required = true, example = "测试项目")
    @NotBlank(message = "项目名称不能为空")
    @Size(min = 2, max = 32, message = "项目名称长度为 2-32 个字符")
    private String name;
    @Schema(description = "项目描述", example = "这是一个项目")
    @Size(max = 500, message = "项目描述最多 500 个字符")
    private String description;
    @Schema(description = "是否个人项目", example = "false")
    private Boolean personal;
}
