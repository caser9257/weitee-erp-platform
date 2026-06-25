package cn.weitee.erp.module.erp.service.sale.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ErpSaleOrderClosureSummaryBO {

    private Long saleOrderId;
    private String saleOrderNo;
    private BigDecimal remainingShipQty;
    private BigDecimal productionQualifiedQty;
    private String deliveryReadyStatus;
    private String closureStage;
    private List<String> blockerCodes;

    private Long purchaseSuggestCount;
    private Long purchaseConfirmedSuggestCount;
    private Long purchaseConvertedSuggestCount;
    private Long purchaseRejectedSuggestCount;

    private Long productionSuggestCount;
    private Long productionConfirmedSuggestCount;
    private Long productionConvertedSuggestCount;
    private Long productionRejectedSuggestCount;

    private Long purchaseOrderCount;
    private Long approvedPurchaseOrderCount;

    private Long purchaseInCount;
    private Long approvedPurchaseInCount;
    private Long pendingQaPurchaseInCount;
    private Long pendingStockInPurchaseInCount;
    private Long stockedPurchaseInCount;
    private Long noNeedStockInPurchaseInCount;
}
