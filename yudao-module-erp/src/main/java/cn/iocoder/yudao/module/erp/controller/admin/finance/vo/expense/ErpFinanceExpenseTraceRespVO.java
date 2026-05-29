package cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ERP 费用报销追溯 Response VO")
@Data
public class ErpFinanceExpenseTraceRespVO {

    @Schema(description = "费用单详情")
    private ErpFinanceExpenseRespVO expense;

    @Schema(description = "关联应付台账")
    private Statement statement;

    @Schema(description = "台账流水")
    private List<StatementItem> statementItems;

    @Schema(description = "付款关联记录")
    private List<AllocateItem> allocates;

    @Data
    public static class Statement {
        private Long statementId;
        private String statementNo;
        private Integer bizType;
        private Long bizId;
        private String bizNo;
        private Long supplierId;
        private String supplierName;
        private Long accountId;
        private String accountName;
        private BigDecimal amount;
        private BigDecimal paidAmount;
        private BigDecimal remainAmount;
        private Integer invoiceStatus;
        private Integer status;
        private String statusName;
        private String remark;
        private LocalDateTime bizDate;
        private LocalDateTime dueDate;
    }

    @Data
    public static class StatementItem {
        private Long id;
        private Integer itemType;
        private Long refId;
        private String refNo;
        private BigDecimal amount;
        private BigDecimal afterPaidAmount;
        private BigDecimal afterRemainAmount;
        private String remark;
        private LocalDateTime createTime;
    }

    @Data
    public static class AllocateItem {
        private Long paymentId;
        private Long paymentItemId;
        private String paymentNo;
        private BigDecimal allocateAmount;
        private Integer status;
        private String remark;
    }

}
