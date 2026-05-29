package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.issue;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ErpProductionIssuePrintDataRespVO {

    private ErpProductionIssueRespVO productionIssue;

    private FinancialFacts financialFacts;

    private List<SourceAttachment> sourceAttachments;

    @Data
    public static class FinancialFacts {

        private String voucherNo;

        private String voucherStatusName;

        private LocalDateTime costSnapshotTime;

        private Integer issueItemCount;

        private Integer issueBatchCount;

        private Integer costIssueCount;

        private BigDecimal materialCost;

        private BigDecimal totalCost;

        private BigDecimal unitCost;

    }

    @Data
    public static class SourceAttachment {

        private String name;
        private String url;

    }

}
