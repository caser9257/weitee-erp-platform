package cn.weitee.erp.module.erp.controller.admin.finance.vo.ledger;

import cn.weitee.erp.framework.excel.core.annotations.DictFormat;
import cn.weitee.erp.module.system.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 财务账簿 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpFinanceLedgerRespVO {

    @Schema(description = "账簿编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("账簿编号")
    private Long id;

    @Schema(description = "账簿编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "BOOK-STD")
    @ExcelProperty("账簿编码")
    private String no;

    @Schema(description = "账簿名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "标准账簿")
    @ExcelProperty("账簿名称")
    private String name;

    @Schema(description = "启用状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("启用状态")
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @ExcelProperty("排序")
    private Integer sort;

    @Schema(description = "是否默认账簿", example = "true")
    @ExcelProperty("是否默认")
    private Boolean defaultStatus;

    @Schema(description = "备注", example = "采购财务主账簿")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
