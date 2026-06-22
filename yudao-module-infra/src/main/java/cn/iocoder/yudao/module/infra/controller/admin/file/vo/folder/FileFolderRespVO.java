package cn.iocoder.yudao.module.infra.controller.admin.file.vo.folder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 文件夹 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FileFolderRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "文件夹名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "父文件夹ID（0=根目录）")
    private Long parentId;

    @Schema(description = "文件夹路径", requiredMode = Schema.RequiredMode.REQUIRED)
    private String path;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：0-正常 1-停用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "子文件夹列表")
    private List<FileFolderRespVO> children;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
