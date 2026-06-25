package cn.weitee.erp.module.project.controller.admin.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 文件 Response VO")
@Data
public class ProjectFileRespVO {
    @Schema(description = "文件编号", example = "1024")
    private Long id;
    @Schema(description = "文件名称", example = "test.txt")
    private String name;
    @Schema(description = "文件扩展名", example = "txt")
    private String ext;
    @Schema(description = "文件大小", example = "1024")
    private Long size;
    @Schema(description = "文件类型", example = "1")
    private Integer type;
    @Schema(description = "文件URL", example = "http://example.com/test.txt")
    private String url;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
