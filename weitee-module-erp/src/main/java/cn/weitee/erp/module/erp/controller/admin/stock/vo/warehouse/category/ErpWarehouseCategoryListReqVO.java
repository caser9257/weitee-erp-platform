package cn.weitee.erp.module.erp.controller.admin.stock.vo.warehouse.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - ERP 仓库分类列表 Request VO")
@Data
public class ErpWarehouseCategoryListReqVO {

    @Schema(description = "分类名称", example = "原料仓")
    private String name;

    @Schema(description = "开启状态", example = "1")
    private Integer status;

}
