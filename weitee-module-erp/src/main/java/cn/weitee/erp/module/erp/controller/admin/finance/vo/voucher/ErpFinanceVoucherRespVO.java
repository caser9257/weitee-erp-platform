package cn.weitee.erp.module.erp.controller.admin.finance.vo.voucher;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 财务凭证 Response VO")
@Data
public class ErpFinanceVoucherRespVO {

    private Long id;
    private String voucherNo;
    private Long ledgerId;
    private String ledgerName;
    private Long periodId;
    private String periodCode;
    private Long templateId;
    private String templateName;
    private Integer bizType;
    private String bizTypeName;
    private Long bizId;
    private String bizNo;
    private LocalDateTime voucherTime;
    private Integer status;
    private String statusName;
    private BigDecimal totalDebitAmount;
    private BigDecimal totalCreditAmount;
    private Long approveUserId;
    private LocalDateTime approveTime;
    private Long postUserId;
    private LocalDateTime postTime;
    private Long reverseUserId;
    private LocalDateTime reverseTime;
    private Long reverseVoucherId;
    private String reverseVoucherNo;
    private Long reverseFromVoucherId;
    private String reverseFromVoucherNo;
    private String reverseRemark;
    private String remark;
    private LocalDateTime createTime;
    private List<Item> entries;

    @Data
    public static class Item {
        private Long id;
        private Integer entryNo;
        private String summary;
        private String subjectCode;
        private String subjectName;
        private BigDecimal debitAmount;
        private BigDecimal creditAmount;
    }
}
