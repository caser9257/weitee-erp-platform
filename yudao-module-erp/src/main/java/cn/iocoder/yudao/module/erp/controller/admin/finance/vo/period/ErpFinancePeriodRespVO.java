package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.period;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - ERP 会计期间 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ErpFinancePeriodRespVO {

    @Schema(description = "期间编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("期间编号")
    private Long id;

    @Schema(description = "账簿编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("账簿编号")
    private Long ledgerId;

    @Schema(description = "账簿名称", example = "标准账簿")
    @ExcelProperty("账簿名称")
    private String ledgerName;

    @Schema(description = "期间编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-04")
    @ExcelProperty("期间编码")
    private String periodCode;

    @Schema(description = "会计年度", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026")
    @ExcelProperty("会计年度")
    private Integer periodYear;

    @Schema(description = "会计月份", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    @ExcelProperty("会计月份")
    private Integer periodMonth;

    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("结束日期")
    private LocalDate endDate;

    @Schema(description = "期间状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @ExcelProperty("期间状态")
    private Integer status;

    @Schema(description = "关账时间")
    @ExcelProperty("关账时间")
    private LocalDateTime closeTime;

    @Schema(description = "关账人编号", example = "1")
    @ExcelProperty("关账人编号")
    private Long closeUserId;

    @Schema(description = "备注", example = "年度初始化")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
