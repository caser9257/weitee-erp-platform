package cn.weitee.erp.module.erp.controller.admin.product.vo.product;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "ERP product response")
@Data
@ExcelIgnoreUnannotated
public class ErpProductRespVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "15672")
    @ExcelProperty("ID")
    private Long id;

    @Schema(description = "Product name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Steel Plate")
    @ExcelProperty("Product Name")
    private String name;

    @Schema(description = "Material code", example = "MAT-001")
    @ExcelProperty("Material Code")
    private String materialCode;

    @Schema(description = "Bar code", requiredMode = Schema.RequiredMode.REQUIRED, example = "X110")
    @ExcelProperty("Bar Code")
    private String barCode;

    @Schema(description = "Category ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "11161")
    private Long categoryId;

    @Schema(description = "Category name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Raw Material")
    @ExcelProperty("Category Name")
    private String categoryName;

    @Schema(description = "Unit ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "8869")
    private Long unitId;

    @Schema(description = "Unit name", requiredMode = Schema.RequiredMode.REQUIRED, example = "KG")
    @ExcelProperty("Unit Name")
    private String unitName;

    @Schema(description = "Status", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("Status")
    private Integer status;

    @Schema(description = "Standard", example = "1.2mm")
    @ExcelProperty("Standard")
    private String standard;

    @Schema(description = "Remark", example = "Key material")
    @ExcelProperty("Remark")
    private String remark;

    @Schema(description = "Expiry days", example = "365")
    @ExcelProperty("Expiry Days")
    private Integer expiryDay;

    @Schema(description = "Batch control flag", example = "true")
    @ExcelProperty("Batch Control")
    private Boolean batchControlFlag;

    @Schema(description = "Inspection required flag", example = "true")
    @ExcelProperty("Inspection Required")
    private Boolean inspectionRequiredFlag;

    @Schema(description = "Weight mg", example = "1.00")
    @ExcelProperty("Weight mg")
    private BigDecimal weight;

    @Schema(description = "Purchase price", example = "10.30")
    @ExcelProperty("Purchase Price")
    private BigDecimal purchasePrice;

    @Schema(description = "Sale price", example = "74.32")
    @ExcelProperty("Sale Price")
    private BigDecimal salePrice;

    @Schema(description = "Min price", example = "61.87")
    @ExcelProperty("Min Price")
    private BigDecimal minPrice;

    @Schema(description = "Asset candidate flag", example = "true")
    private Boolean assetFlag;

    @Schema(description = "Create time", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("Create Time")
    private LocalDateTime createTime;

}
