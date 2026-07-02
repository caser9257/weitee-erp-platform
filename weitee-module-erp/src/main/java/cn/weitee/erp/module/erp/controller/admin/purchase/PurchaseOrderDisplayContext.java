package cn.weitee.erp.module.erp.controller.admin.purchase;

import java.util.Map;

final class PurchaseOrderDisplayContext {

    private final Map<Long, String> creatorNameMap;
    private final Map<Long, String> businessOwnerNameMap;
    private final Map<Long, String> sourceOrderNosMap;
    private final Map<Long, String> sourceTypeMap;
    private final Map<Long, Boolean> pendingPurchaseInMap;
    private final Map<Long, Long> pendingPurchaseInIdMap;

    PurchaseOrderDisplayContext(Map<Long, String> creatorNameMap,
                                Map<Long, String> businessOwnerNameMap,
                                Map<Long, String> sourceOrderNosMap,
                                Map<Long, String> sourceTypeMap,
                                Map<Long, Boolean> pendingPurchaseInMap,
                                Map<Long, Long> pendingPurchaseInIdMap) {
        this.creatorNameMap = creatorNameMap;
        this.businessOwnerNameMap = businessOwnerNameMap;
        this.sourceOrderNosMap = sourceOrderNosMap;
        this.sourceTypeMap = sourceTypeMap;
        this.pendingPurchaseInMap = pendingPurchaseInMap;
        this.pendingPurchaseInIdMap = pendingPurchaseInIdMap;
    }

    static PurchaseOrderDisplayContext empty() {
        return new PurchaseOrderDisplayContext(Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
    }

    String getCreatorName(Long purchaseOrderId) {
        return creatorNameMap.get(purchaseOrderId);
    }

    String getBusinessOwnerName(Long purchaseOrderId) {
        return businessOwnerNameMap.get(purchaseOrderId);
    }

    String getSourceOrderNos(Long purchaseOrderId) {
        return sourceOrderNosMap.get(purchaseOrderId);
    }

    String getSourceType(Long purchaseOrderId) {
        return sourceTypeMap.get(purchaseOrderId);
    }

    Boolean hasPendingPurchaseIn(Long purchaseOrderId) {
        return pendingPurchaseInMap.getOrDefault(purchaseOrderId, Boolean.FALSE);
    }

    Long getPendingPurchaseInId(Long purchaseOrderId) {
        return pendingPurchaseInIdMap.get(purchaseOrderId);
    }
}
