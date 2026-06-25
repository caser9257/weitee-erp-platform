package cn.weitee.erp.module.erp.controller.admin.finance.vo.expense;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.weitee.erp.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 费用报销分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpFinanceExpensePageReqVO extends PageParam {

    @Schema(description = "报销单号", example = "LSBX20260428000001")
    private String no;

    @Schema(description = "报销时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] expenseTime;

    @Schema(description = "费用类型", example = "10")
    private Integer expenseType;

    @Schema(description = "研发支出口径（10-费用化，20-资本化）", example = "10")
    private Integer rdAccountingType;

    @Schema(description = "部门编号", example = "1001")
    private Long deptId;

    @Schema(description = "项目编号", example = "1001")
    private Long projectId;

    @Schema(description = "付款对象编号", example = "2001")
    private Long supplierId;

    @Schema(description = "创建人", example = "666")
    private String creator;

    @Schema(description = "财务人员编号", example = "888")
    private Long financeUserId;

    @Schema(description = "结算账户编号", example = "31189")
    private Long accountId;

    @Schema(description = "状态", example = "10")
    private Integer status;

    @Schema(description = "备注", example = "零星采购")
    private String remark;

}
