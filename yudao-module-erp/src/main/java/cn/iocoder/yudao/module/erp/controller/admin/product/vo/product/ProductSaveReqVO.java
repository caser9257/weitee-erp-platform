package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "ERP product save request")
@Data
public class ProductSaveReqVO {

    @Schema(description = "ID", example = "15672")
    private Long id;

    @Schema(description = "Product name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Steel Plate")
    @NotEmpty(message = "name cannot be empty")
    private String name;

    @Schema(description = "Material code", example = "MAT-001")
    private String materialCode;

    @Schema(description = "Bar code", requiredMode = Schema.RequiredMode.REQUIRED, example = "X110")
    @NotEmpty(message = "barCode cannot be empty")
    private String barCode;

    @Schema(description = "Category ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "11161")
    @NotNull(message = "categoryId cannot be empty")
    private Long categoryId;

    @Schema(description = "Unit ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "8869")
    @NotNull(message = "unitId cannot be empty")
    private Long unitId;

    @Schema(description = "Status", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "status cannot be empty")
    private Integer status;

    @Schema(description = "Standard", example = "1.2mm")
    private String standard;

    @Schema(description = "Remark", example = "Key material")
    private String remark;

    @Schema(description = "Expiry days", example = "365")
    private Integer expiryDay;

    @Schema(description = "Batch control flag", example = "true")
    private Boolean batchControlFlag;

    @Schema(description = "Inspection required flag", example = "true")
    private Boolean inspectionRequiredFlag;

    @Schema(description = "Weight mg", example = "1.00")
    private BigDecimal weight;

    @Schema(description = "Purchase price", example = "10.30")
    private BigDecimal purchasePrice;

    @Schema(description = "Sale price", example = "74.32")
    private BigDecimal salePrice;

    @Schema(description = "Min price", example = "61.87")
    private BigDecimal minPrice;

    @Schema(description = "Asset candidate flag", example = "true")
    private Boolean assetFlag;

}
