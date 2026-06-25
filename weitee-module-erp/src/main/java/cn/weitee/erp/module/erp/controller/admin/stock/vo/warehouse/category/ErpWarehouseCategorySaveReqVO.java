package cn.weitee.erp.module.erp.controller.admin.stock.vo.warehouse.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 仓库分类新增/修改 Request VO")
@Data
public class ErpWarehouseCategorySaveReqVO {

    @Schema(description = "分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "5860")
    private Long id;

    @Schema(description = "父分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "21829")
    @NotNull(message = "父分类编号不能为空")
    private Long parentId;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "原料仓")
    @NotEmpty(message = "分类名称不能为空")
    private String name;

    @Schema(description = "分类编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "RAW")
    @NotEmpty(message = "分类编码不能为空")
    private String code;

    @Schema(description = "分类排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "分类排序不能为空")
    private Integer sort;

    @Schema(description = "开启状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "开启状态不能为空")
    private Integer status;

}
