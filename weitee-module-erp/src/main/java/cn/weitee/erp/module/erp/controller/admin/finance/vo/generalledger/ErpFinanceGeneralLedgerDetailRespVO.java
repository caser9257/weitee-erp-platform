package cn.weitee.erp.module.erp.controller.admin.finance.vo.generalledger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 总账明细 Response VO")
@Data
public class ErpFinanceGeneralLedgerDetailRespVO {

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

    @Schema(description = "查询区间期初借方余额", example = "0")
    private BigDecimal openingDebitAmount;

    @Schema(description = "查询区间期初贷方余额", example = "0")
    private BigDecimal openingCreditAmount;

    @Schema(description = "查询区间借方发生额", example = "300")
    private BigDecimal totalDebitAmount;

    @Schema(description = "查询区间贷方发生额", example = "300")
    private BigDecimal totalCreditAmount;

    @Schema(description = "查询区间期末借方余额", example = "0")
    private BigDecimal endingDebitAmount;

    @Schema(description = "查询区间期末贷方余额", example = "0")
    private BigDecimal endingCreditAmount;

    @Schema(description = "明细列表")
    private List<Item> items;

    @Schema(description = "管理后台 - ERP 总账明细分录项")
    @Data
    public static class Item {

        @Schema(description = "凭证编号", example = "1")
        private Long voucherId;

        @Schema(description = "凭证号", example = "CWPZ20260429000001")
        private String voucherNo;

        @Schema(description = "凭证时间")
        private LocalDateTime voucherTime;

        @Schema(description = "业务类型", example = "40")
        private Integer bizType;

        @Schema(description = "业务类型名称", example = "费用报销")
        private String bizTypeName;

        @Schema(description = "业务单号", example = "LSBX20260429000001")
        private String bizNo;

        @Schema(description = "凭证状态", example = "30")
        private Integer voucherStatus;

        @Schema(description = "凭证状态名称", example = "已过账")
        private String voucherStatusName;

        @Schema(description = "分录顺序", example = "1")
        private Integer entryNo;

        @Schema(description = "摘要", example = "费用报销凭证")
        private String summary;

        @Schema(description = "借方金额", example = "300")
        private BigDecimal debitAmount;

        @Schema(description = "贷方金额", example = "0")
        private BigDecimal creditAmount;

        @Schema(description = "分录后借方余额", example = "300")
        private BigDecimal runningDebitAmount;

        @Schema(description = "分录后贷方余额", example = "0")
        private BigDecimal runningCreditAmount;

    }

}
