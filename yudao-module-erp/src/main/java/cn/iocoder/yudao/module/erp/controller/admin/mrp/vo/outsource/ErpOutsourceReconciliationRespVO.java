package cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.outsource;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ErpOutsourceReconciliationRespVO {

    private Long orderId;
    private String orderNo;
    private BigDecimal plannedQty;
    private BigDecimal finishedQty;
    private BigDecimal lossQty;
    private BigDecimal pendingInboundQty;
    private BigDecimal unresolvedQty;
    private BigDecimal overInboundQty;
    private BigDecimal normalIssueAmount;
    private BigDecimal supplementIssueAmount;
    private BigDecimal totalIssueAmount;
    private BigDecimal returnAmount;
    private BigDecimal netMaterialCost;
    private BigDecimal processFee;
    private BigDecimal totalCost;
    private BigDecimal unitCost;
    private Boolean hasSupplementIssue;
    private String closeRemark;
    private List<IssueDetail> issueDetails;
    private List<ReturnDetail> returnDetails;
    private List<FeeDetail> feeDetails;
    private List<InboundDetail> inboundDetails;

    @Data
    public static class IssueDetail {
        private Long id;
        private String issueNo;
        private Integer issueType;
        private String issueTypeName;
        private LocalDateTime issueTime;
        private BigDecimal issueAmount;
        private String remark;
    }

    @Data
    public static class ReturnDetail {
        private Long id;
        private String returnNo;
        private LocalDateTime returnTime;
        private BigDecimal returnAmount;
        private String remark;
    }

    @Data
    public static class FeeDetail {
        private Long id;
        private String feeNo;
        private LocalDateTime feeTime;
        private BigDecimal feeAmount;
        private String remark;
    }

    @Data
    public static class InboundDetail {
        private Long id;
        private String inboundNo;
        private LocalDateTime inboundTime;
        private BigDecimal inboundQty;
        private BigDecimal totalCost;
        private BigDecimal unitCost;
        private String remark;
    }

}
