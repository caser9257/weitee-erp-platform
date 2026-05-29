package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.voucher;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 财务凭证冲销 Request VO")
@Data
public class ErpFinanceVoucherReverseReqVO {

    @Schema(description = "凭证编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "凭证编号不能为空")
    private Long id;

    @Schema(description = "冲销凭证时间，未传则取当前时间", example = "2026-04-29T14:30:00")
    private LocalDateTime voucherTime;

    @Schema(description = "冲销说明", example = "金额口径修正")
    private String remark;

}
