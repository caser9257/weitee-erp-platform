package cn.iocoder.yudao.module.infra.controller.admin.file.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 文件 Response VO,不返回 content 字段，太大")
@Data
public class FileRespVO {

    @Schema(description = "文件编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "配置编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11")
    private Long configId;

    @Schema(description = "文件路径", requiredMode = Schema.RequiredMode.REQUIRED, example = "yudao.jpg")
    private String path;

    @Schema(description = "原文件名", requiredMode = Schema.RequiredMode.REQUIRED, example = "yudao.jpg")
    private String name;

    @Schema(description = "文件 URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/yudao.jpg")
    private String url;

    @Schema(description = "文件MIME类型", example = "application/octet-stream")
    private String type;

    @Schema(description = "文件大小", example = "2048", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long size;

    @Schema(description = "文件夹ID")
    private Long folderId;

    @Schema(description = "标签ID（多个标签用逗号分隔）")
    private String tagIds;

    @Schema(description = "总查看次数")
    private Integer viewCount;

    @Schema(description = "总下载次数")
    private Integer downloadCount;

    @Schema(description = "最后访问时间")
    private LocalDateTime lastAccessTime;

    @Schema(description = "删除人ID")
    private Long deleteUserId;

    @Schema(description = "删除人名称")
    private String deleteUserName;

    @Schema(description = "删除时间")
    private LocalDateTime deleteTime;

    @Schema(description = "删除原因")
    private String deleteReason;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
