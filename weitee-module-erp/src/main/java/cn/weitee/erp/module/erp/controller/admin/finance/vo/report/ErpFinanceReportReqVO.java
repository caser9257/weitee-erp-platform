package cn.weitee.erp.module.erp.controller.admin.finance.vo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 财务报表查询 Request VO")
@Data
public class ErpFinanceReportReqVO {

    @Schema(description = "账簿编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "账簿编号不能为空")
    private Long ledgerId;

    @Schema(description = "期间编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "期间编号不能为空")
    private Long periodId;

    @Schema(description = "科目编码", example = "660201")
    private String subjectCode;

    @Schema(description = "科目名称", example = "管理费用-研发费")
    private String subjectName;

}
