package cn.weitee.erp.module.erp.controller.admin.product.vo.product;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 研发物料(Cadence)导出 VO。
 *
 * 导出面向 Cadence/ODBC 对接场景，列名统一使用英文（与 v_cadence_component 视图一致）。
 */
@Schema(description = "管理后台 - 研发物料(Cadence)导出 VO")
@Data
@ExcelIgnoreUnannotated
public class ErpRdCadenceExportVO {

    @ExcelProperty("Part_Number")
    private String materialCode;

    @ExcelProperty("Part_Name")
    private String name;

    @ExcelProperty("Category")
    private String categoryName;

    @ExcelProperty("Unit")
    private String unitName;

    @ExcelProperty("Value")
    private String standard;

    @ExcelProperty("Schematic_Part")
    private String schematicPart;

    @ExcelProperty("PCB_Footprint")
    private String pcbFootprint;

    @ExcelProperty("Description")
    private String cadenceDescription;

    @ExcelProperty("Manufacturer")
    private String manufacturerPartNumber;

    @ExcelProperty("Dimension")
    private String dimension;

    @ExcelProperty("3D_Lib")
    private String threeDLib;

    @ExcelProperty("Datasheet")
    private String datasheet;

    @ExcelProperty("Lifecycle")
    private String lifecycle;

    @ExcelProperty("Preferred_Part")
    private Boolean preferredPart;

    @ExcelProperty("Operating_Temperature")
    private String operatingTemperature;

    @ExcelProperty("Mounting_Type")
    private String mountingType;

    @ExcelProperty("DNP")
    private Boolean dnp;

    @ExcelProperty("Imported_or_Replacement")
    private String importedOrReplacement;

    @ExcelProperty("Second_Description")
    private String secondDescription;

    @ExcelProperty("Third_Description")
    private String thirdDescription;

    @ExcelProperty("Fourth_Description")
    private String fourthDescription;

    @ExcelProperty("Audit_Status")
    private Integer auditStatus;

    @ExcelProperty("Create_Time")
    private LocalDateTime createTime;

}
