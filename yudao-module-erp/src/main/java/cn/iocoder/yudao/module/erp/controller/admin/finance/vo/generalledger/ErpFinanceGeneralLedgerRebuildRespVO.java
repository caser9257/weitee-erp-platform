package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.generalledger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - ERP 总账科目余额重建 Response VO")
@Data
public class ErpFinanceGeneralLedgerRebuildRespVO {

    @Schema(description = "账簿编号", example = "1")
    private Long ledgerId;

    @Schema(description = "重放凭证数量", example = "10")
    private Integer voucherCount;

    @Schema(description = "重放分录数量", example = "20")
    private Integer entryCount;

    @Schema(description = "涉及科目数量", example = "5")
    private Integer subjectCount;

}
