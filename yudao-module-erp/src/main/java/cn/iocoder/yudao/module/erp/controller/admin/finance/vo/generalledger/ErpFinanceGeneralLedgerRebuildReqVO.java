package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.generalledger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - ERP 总账科目余额重建 Request VO")
@Data
public class ErpFinanceGeneralLedgerRebuildReqVO {

    @Schema(description = "账簿编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "账簿编号不能为空")
    private Long ledgerId;

}
