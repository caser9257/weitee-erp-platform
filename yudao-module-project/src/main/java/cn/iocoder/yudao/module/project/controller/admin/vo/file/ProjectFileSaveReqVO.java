package cn.iocoder.yudao.module.project.controller.admin.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Schema(description = "管理后台 - 文件创建/更新 Request VO")
@Data
public class ProjectFileSaveReqVO {
    @Schema(description = "文件编号", example = "1024")
    private Long id;
    @Schema(description = "文件名称", required = true, example = "test.txt")
    @NotBlank(message = "文件名称不能为空")
    private String name;
    @Schema(description = "文件扩展名", example = "txt")
    private String ext;
    @Schema(description = "文件大小", example = "1024")
    private Long size;
    @Schema(description = "文件类型", example = "1")
    private Integer type;
    @Schema(description = "文件路径", example = "/files/test.txt")
    private String path;
    @Schema(description = "文件URL", example = "http://example.com/test.txt")
    private String url;
}
