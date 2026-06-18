package cn.iocoder.yudao.module.infra.controller.admin.file.vo.folder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;

@Schema(description = "管理后台 - 文件夹新增/修改 Request VO")
@Data
public class FileFolderSaveReqVO {

    @Schema(description = "文件夹编号", example = "1")
    private Long id;

    @Schema(description = "文件夹名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "合同文件")
    @NotEmpty(message = "文件夹名称不能为空")
    private String name;

    @Schema(description = "父文件夹ID", example = "0")
    private Long parentId;

    @Schema(description = "图标", example = "ep:folder")
    private String icon;

    @Schema(description = "排序", example = "0")
    private Integer sort;

    @Schema(description = "备注", example = "合同文件目录")
    private String remark;

}
