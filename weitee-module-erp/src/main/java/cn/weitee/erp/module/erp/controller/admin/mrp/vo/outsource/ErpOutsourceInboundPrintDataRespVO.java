package cn.weitee.erp.module.erp.controller.admin.mrp.vo.outsource;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ErpOutsourceInboundPrintDataRespVO {

    private ErpOutsourceInboundRespVO outsourceInbound;

    private FinancialFacts financialFacts;

    private List<SourceAttachment> sourceAttachments;

    private List<ReconciliationRecord> reconciliationRecords;

    @Data
    public static class FinancialFacts {

        private BigDecimal plannedQty;
        private BigDecimal finishedQty;
        private BigDecimal lossQty;
        private BigDecimal pendingInboundQty;
        private BigDecimal unresolvedQty;
        private BigDecimal overInboundQty;
        private BigDecimal materialCost;
        private BigDecimal returnMaterialCost;
        private BigDecimal netMaterialCost;
        private BigDecimal processFee;
        private BigDecimal totalCost;
        private BigDecimal unitCost;
        private Integer issueCount;
        private Integer returnCount;
        private Integer feeCount;
        private Integer inboundCount;
        private Boolean hasSupplementIssue;

    }

    @Data
    public static class SourceAttachment {

        private String name;
        private String url;

    }

    @Data
    public static class ReconciliationRecord {

        private Long paymentId;
        private String paymentNo;
        private BigDecimal allocateAmount;
        private LocalDateTime paymentTime;
        private String operatorName;
        private Integer status;
        private String statusName;
        private String remark;

    }

}
