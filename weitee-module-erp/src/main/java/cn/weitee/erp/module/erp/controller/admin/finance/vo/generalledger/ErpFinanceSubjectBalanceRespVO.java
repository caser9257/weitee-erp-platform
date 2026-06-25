package cn.weitee.erp.module.erp.controller.admin.finance.vo.generalledger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - ERP 科目余额 Response VO")
@Data
public class ErpFinanceSubjectBalanceRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "账簿编号", example = "1")
    private Long ledgerId;

    @Schema(description = "账簿名称", example = "标准账簿")
    private String ledgerName;

    @Schema(description = "期间编号", example = "1")
    private Long periodId;

    @Schema(description = "期间编码", example = "2026-04")
    private String periodCode;

    @Schema(description = "科目编码", example = "660201")
    private String subjectCode;

    @Schema(description = "科目名称", example = "管理费用-研发费")
    private String subjectName;

    @Schema(description = "期初借方余额", example = "0")
    private BigDecimal openingDebitAmount;

    @Schema(description = "期初贷方余额", example = "0")
    private BigDecimal openingCreditAmount;

    @Schema(description = "本期借方发生额", example = "300")
    private BigDecimal currentDebitAmount;

    @Schema(description = "本期贷方发生额", example = "300")
    private BigDecimal currentCreditAmount;

    @Schema(description = "期末借方余额", example = "0")
    private BigDecimal endingDebitAmount;

    @Schema(description = "期末贷方余额", example = "0")
    private BigDecimal endingCreditAmount;

}
