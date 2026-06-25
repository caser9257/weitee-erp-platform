package cn.weitee.erp.module.erp.controller.admin.finance.vo.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.weitee.erp.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 费用项目汇总 Request VO")
@Data
public class ErpFinanceExpenseProjectSummaryReqVO {

    @Schema(description = "报销时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] expenseTime;

    @Schema(description = "项目编号", example = "1001")
    private Long projectId;

    @Schema(description = "部门编号", example = "1001")
    private Long deptId;

    @Schema(description = "费用类型", example = "10")
    private Integer expenseType;

    @Schema(description = "研发支出口径（10-费用化，20-资本化）", example = "10")
    private Integer rdAccountingType;

    @Schema(description = "状态", example = "20")
    private Integer status;

}
