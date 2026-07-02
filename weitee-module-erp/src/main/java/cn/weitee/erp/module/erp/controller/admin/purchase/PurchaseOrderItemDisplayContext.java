package cn.weitee.erp.module.erp.controller.admin.purchase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

final class PurchaseOrderItemDisplayContext {

    private final Map<Long, String> projectNameMap;
    private final Map<Long, LocalDateTime> deliveryDateMap;
    private final Map<Long, BigDecimal> paymentAllocatedAmountMap;
    private final Map<Long, BigDecimal> relatedCountMap;
    private final Map<Long, BigDecimal> invoicedCountMap;
    private final Map<Long, String> auditorNameMap;

    PurchaseOrderItemDisplayContext(Map<Long, String> projectNameMap,
                                    Map<Long, LocalDateTime> deliveryDateMap,
                                    Map<Long, BigDecimal> paymentAllocatedAmountMap,
                                    Map<Long, BigDecimal> relatedCountMap,
                                    Map<Long, BigDecimal> invoicedCountMap,
                                    Map<Long, String> auditorNameMap) {
        this.projectNameMap = projectNameMap;
        this.deliveryDateMap = deliveryDateMap;
        this.paymentAllocatedAmountMap = paymentAllocatedAmountMap;
        this.relatedCountMap = relatedCountMap;
        this.invoicedCountMap = invoicedCountMap;
        this.auditorNameMap = auditorNameMap;
    }

    static PurchaseOrderItemDisplayContext empty() {
        return new PurchaseOrderItemDisplayContext(
                Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
    }

    String getProjectName(Long projectId) {
        return projectId == null ? null : projectNameMap.get(projectId);
    }

    LocalDateTime getDeliveryDate(Long projectId) {
        return projectId == null ? null : deliveryDateMap.get(projectId);
    }

    BigDecimal getPaymentAllocatedAmount(Long orderItemId) {
        return paymentAllocatedAmountMap.getOrDefault(orderItemId, BigDecimal.ZERO);
    }

    BigDecimal getRelatedCount(Long orderItemId) {
        return relatedCountMap.getOrDefault(orderItemId, BigDecimal.ZERO);
    }

    BigDecimal getInvoicedCount(Long orderItemId) {
        return invoicedCountMap.getOrDefault(orderItemId, BigDecimal.ZERO);
    }

    String getAuditorName(Long orderId) {
        return auditorNameMap.get(orderId);
    }
}
