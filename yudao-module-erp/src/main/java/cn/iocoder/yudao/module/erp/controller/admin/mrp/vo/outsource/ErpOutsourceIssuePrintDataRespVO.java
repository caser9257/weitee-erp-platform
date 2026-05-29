package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.outsource;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ErpOutsourceIssuePrintDataRespVO {

    private ErpOutsourceIssueRespVO outsourceIssue;

    private FinancialFacts financialFacts;

    private List<SourceAttachment> sourceAttachments;

    @Data
    public static class FinancialFacts {

        private BigDecimal plannedQty;
        private BigDecimal finishedQty;
        private BigDecimal issueQty;
        private BigDecimal issueAmount;
        private BigDecimal materialCost;
        private BigDecimal returnMaterialCost;
        private BigDecimal netMaterialCost;
        private BigDecimal processFee;
        private BigDecimal totalCost;
        private BigDecimal unitCost;
        private Integer issueItemCount;
        private Integer issueBatchCount;
        private Integer issueCount;
        private Integer returnCount;
        private Integer feeCount;
        private Integer inboundCount;

    }

    @Data
    public static class SourceAttachment {

        private String name;
        private String url;

    }

}
