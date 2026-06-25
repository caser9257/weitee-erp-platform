package cn.weitee.erp.module.infra.controller.admin.file.vo.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;

@Schema(description = "管理后台 - 文件标签新增/修改 Request VO")
@Data
public class FileTagSaveReqVO {

    @Schema(description = "标签编号", example = "1")
    private Long id;

    @Schema(description = "标签名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "合同")
    @NotEmpty(message = "标签名称不能为空")
    private String name;

    @Schema(description = "标签颜色", example = "#409EFF")
    private String color;

    @Schema(description = "图标", example = "ep:document")
    private String icon;

    @Schema(description = "排序", example = "0")
    private Integer sort;

}
