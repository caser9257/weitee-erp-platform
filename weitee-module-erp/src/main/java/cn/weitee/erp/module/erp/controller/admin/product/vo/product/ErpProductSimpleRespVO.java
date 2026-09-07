package cn.weitee.erp.module.erp.controller.admin.product.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 已审核物料精简 Response VO
 *
 * 仅供 BOM 等场景的下拉选择与单位反查使用。
 * 字段为白名单裁剪：万级物料下全字段 VO 会产生 10MB+ 响应体，禁止在此 VO 上随意加字段。
 */
@Schema(description = "ERP product simple response")
@Data
public class ErpProductSimpleRespVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "15672")
    private Long id;

    @Schema(description = "Product name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Steel Plate")
    private String name;

    @Schema(description = "Material code", example = "MAT-001")
    private String materialCode;

    @Schema(description = "最近一次变更前物料编码（编码沿革，无改码历史为 null）", example = "MAT-OLD-001")
    private String prevMaterialCode;

    @Schema(description = "Category ID", example = "11161")
    private Long categoryId;

    @Schema(description = "Category name", example = "Raw Material")
    private String categoryName;

    @Schema(description = "Unit ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "8869")
    private Long unitId;

    @Schema(description = "Unit name", example = "PCS")
    private String unitName;

    @Schema(description = "Status", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "Audit status", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer auditStatus;

}
