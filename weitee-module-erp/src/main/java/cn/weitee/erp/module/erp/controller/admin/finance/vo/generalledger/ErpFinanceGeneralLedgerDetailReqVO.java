package cn.weitee.erp.module.erp.controller.admin.finance.vo.generalledger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static cn.weitee.erp.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 总账明细查询 Request VO")
@Data
public class ErpFinanceGeneralLedgerDetailReqVO {

    @Schema(description = "账簿编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "账簿编号不能为空")
    private Long ledgerId;

    @Schema(description = "期间编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "期间编号不能为空")
    private Long periodId;

    @Schema(description = "科目编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "660201")
    @NotBlank(message = "科目编码不能为空")
    private String subjectCode;

    @Schema(description = "凭证时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] voucherTime;

}
