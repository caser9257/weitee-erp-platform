package cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "ERP stock batch response")
@Data
@ExcelIgnoreUnannotated
public class ErpStockBatchRespVO {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "Product id", example = "1")
    private Long productId;

    @Schema(description = "Product name", example = "Steel Plate")
    @ExcelProperty("Product Name")
    private String productName;

    @Schema(description = "Material code", example = "MAT-001")
    @ExcelProperty("Material Code")
    private String materialCode;

    @Schema(description = "Unit name", example = "KG")
    @ExcelProperty("Unit Name")
    private String unitName;

    @Schema(description = "Warehouse id", example = "1")
    private Long warehouseId;

    @Schema(description = "Warehouse name", example = "Main Warehouse")
    @ExcelProperty("Warehouse Name")
    private String warehouseName;

    @Schema(description = "Batch no", example = "B20260424001")
    @ExcelProperty("Batch No")
    private String batchNo;

    @Schema(description = "Inbound time")
    @ExcelProperty("Inbound Time")
    private LocalDateTime inboundTime;

    @Schema(description = "Produce date")
    @ExcelProperty("Produce Date")
    private LocalDate produceDate;

    @Schema(description = "Expire date")
    @ExcelProperty("Expire Date")
    private LocalDate expireDate;

    @Schema(description = "Total qty", example = "100.000")
    @ExcelProperty("Total Qty")
    private BigDecimal totalQty;

    @Schema(description = "Available qty", example = "80.000")
    @ExcelProperty("Available Qty")
    private BigDecimal availableQty;

    @Schema(description = "Locked qty", example = "0.000")
    @ExcelProperty("Locked Qty")
    private BigDecimal lockedQty;

    @Schema(description = "Virtual flag", example = "false")
    @ExcelProperty("Virtual Flag")
    private Boolean virtualFlag;

    @Schema(description = "Source biz type", example = "PURCHASE_IN")
    private String sourceBizType;

    @Schema(description = "Source biz no", example = "CGRK20260424001")
    private String sourceBizNo;

    @Schema(description = "Purchase source batch id", example = "1")
    private Long purchaseSourceBatchId;

    @Schema(description = "Purchase source batch no", example = "CGLY20260427000001")
    private String purchaseSourceBatchNo;

    @Schema(description = "Remark", example = "Initial batch")
    private String remark;

}
