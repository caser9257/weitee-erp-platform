package cn.weitee.erp.module.erp.controller.admin.product.vo.unit;

import cn.weitee.erp.framework.excel.core.annotations.DictFormat;
import cn.weitee.erp.module.system.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 产品单位 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpProductUnitRespVO {

    @Schema(description = "单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31254")
    @ExcelProperty("单位编号")
    private Long id;

    @Schema(description = "单位名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("单位名字")
    private String name;

    @Schema(description = "单位状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("单位状态")
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @Schema(description = "单位类型，0 基本单位 1 辅助单位", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("单位类型")
    private Integer unitType;

    @Schema(description = "基本单位编号，辅助单位归属；基本单位为 null", example = "1")
    private Long baseUnitId;

    @Schema(description = "基本单位名称", example = "个")
    @ExcelProperty("基本单位")
    private String baseUnitName;

    @Schema(description = "换算率：1 辅助单位 = conversionRate 基本单位", example = "12")
    @ExcelProperty("换算率")
    private BigDecimal conversionRate;

    @Schema(description = "数量精度，0 表示只允许整数", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @ExcelProperty("数量精度")
    private Integer quantityPrecision;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
