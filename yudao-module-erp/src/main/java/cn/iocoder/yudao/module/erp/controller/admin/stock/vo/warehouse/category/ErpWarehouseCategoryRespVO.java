package cn.iocoder.yudao.module.erp.controller.admin.stock.vo.warehouse.category;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 仓库分类 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpWarehouseCategoryRespVO {

    @Schema(description = "分类编号", example = "5860")
    @ExcelProperty("分类编号")
    private Long id;

    @Schema(description = "父分类编号", example = "21829")
    @ExcelProperty("父分类编号")
    private Long parentId;

    @Schema(description = "分类名称", example = "原料仓")
    @ExcelProperty("分类名称")
    private String name;

    @Schema(description = "分类编码", example = "RAW")
    @ExcelProperty("分类编码")
    private String code;

    @Schema(description = "分类排序", example = "10")
    @ExcelProperty("分类排序")
    private Integer sort;

    @Schema(description = "开启状态", example = "1")
    @ExcelProperty(value = "开启状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
