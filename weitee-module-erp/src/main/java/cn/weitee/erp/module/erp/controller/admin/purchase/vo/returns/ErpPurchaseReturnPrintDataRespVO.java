package cn.weitee.erp.module.erp.controller.admin.purchase.vo.returns;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ErpPurchaseReturnPrintDataRespVO {

    private ErpPurchaseReturnRespVO purchaseReturn;

    private FinancialFacts financialFacts;

    private List<SourceAttachment> sourceAttachments;

    @Data
    public static class FinancialFacts {

        private Long accountId;

        private String accountName;

        private BigDecimal totalCount;

        private BigDecimal totalProductPrice;

        private BigDecimal totalTaxPrice;

        private BigDecimal discountPercent;

        private BigDecimal discountPrice;

        private BigDecimal otherPrice;

        private BigDecimal totalPrice;

        private BigDecimal refundPrice;

        private BigDecimal remainingPrice;

        private String refundProgressName;

    }

    @Data
    public static class SourceAttachment {

        private String name;
        private String url;

    }

}
