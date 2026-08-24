package cn.weitee.erp.module.erp.controller.admin.product.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

    @Schema(description = "产品类型")
    private Integer productType;

    @Schema(description = "生产方式")
    private Integer produceType;

    @Schema(description = "是否启用批次")
    private Boolean batchEnable;

    @Schema(description = "是否启用序列号")
    private Boolean snEnable;

    @Schema(description = "默认工艺路线编号")
    private Long defaultRouteId;

    @Schema(description = "是否启用质检")
    private Boolean qcEnable;

    @Schema(description = "是否支持委外")
    private Boolean outsourceEnable;

    @Schema(description = "成本方式")
    private Integer costMethod;

    @Schema(description = "Status", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "status cannot be empty")
    private Integer status;

    @Schema(description = "Standard", example = "1.2mm")
    private String standard;

    @Schema(description = "产品封装")
    private String packaging;

    @Schema(description = "质量等级")
    private String qualityGrade;

    @Schema(description = "品牌/制造商")
    private String brandManufacturer;

    @Schema(description = "替代型号")
    private String alternativeModel;

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

    @Schema(description = "Whether to participate in MRP", example = "true")
    private Boolean mrpEnable;

    @Schema(description = "Asset candidate flag", example = "true")
    private Boolean assetFlag;

}
