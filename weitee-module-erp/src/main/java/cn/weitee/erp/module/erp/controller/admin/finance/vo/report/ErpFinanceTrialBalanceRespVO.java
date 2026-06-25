package cn.weitee.erp.module.erp.controller.admin.finance.vo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - ERP 试算平衡表 Response VO")
@Data
public class ErpFinanceTrialBalanceRespVO {

    @Schema(description = "账簿编号", example = "1")
    private Long ledgerId;

    @Schema(description = "账簿名称", example = "标准账簿")
    private String ledgerName;

    @Schema(description = "期间编号", example = "1")
    private Long periodId;

    @Schema(description = "期间编码", example = "2026-04")
    private String periodCode;

    @Schema(description = "期初借方合计", example = "0")
    private BigDecimal totalOpeningDebitAmount;

    @Schema(description = "期初贷方合计", example = "0")
    private BigDecimal totalOpeningCreditAmount;

    @Schema(description = "本期借方合计", example = "300")
    private BigDecimal totalCurrentDebitAmount;

    @Schema(description = "本期贷方合计", example = "300")
    private BigDecimal totalCurrentCreditAmount;

    @Schema(description = "期末借方合计", example = "300")
    private BigDecimal totalEndingDebitAmount;

    @Schema(description = "期末贷方合计", example = "300")
    private BigDecimal totalEndingCreditAmount;

    @Schema(description = "本期发生额是否平衡", example = "true")
    private Boolean currentBalanced;

    @Schema(description = "期末余额是否平衡", example = "true")
    private Boolean endingBalanced;

    @Schema(description = "科目数量", example = "2")
    private Integer subjectCount;

    @Schema(description = "科目余额明细")
    private List<Item> items;

    @Schema(description = "管理后台 - ERP 试算平衡表明细项")
    @Data
    public static class Item {

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

        @Schema(description = "本期贷方发生额", example = "0")
        private BigDecimal currentCreditAmount;

        @Schema(description = "期末借方余额", example = "300")
        private BigDecimal endingDebitAmount;

        @Schema(description = "期末贷方余额", example = "0")
        private BigDecimal endingCreditAmount;

    }

}
