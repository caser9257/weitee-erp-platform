package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.voucher;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "管理后台 - 凭证完整性检查 Request VO")
@Data
public class ErpFinanceVoucherIntegrityCheckReqVO {

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "21")
    @NotNull(message = "业务类型不能为空")
    private Integer bizType;

    @Schema(description = "账簿编号（可选，不传则检查所有账簿）", example = "1")
    private Long ledgerId;

    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-01-01")
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-04-29")
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

}
