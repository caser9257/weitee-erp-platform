package cn.weitee.erp.module.infra.controller.admin.file.vo.version;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 文件版本 Response VO")
@Data
public class FileVersionRespVO {

    @Schema(description = "版本编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long fileId;

    @Schema(description = "版本号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer version;

    @Schema(description = "文件名", example = "设计文档v2.docx")
    private String name;

    @Schema(description = "文件URL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String url;

    @Schema(description = "文件大小", example = "2048")
    private Long size;

    @Schema(description = "文件类型", example = "application/pdf")
    private String type;

    @Schema(description = "版本说明", example = "修复了排版问题")
    private String description;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}