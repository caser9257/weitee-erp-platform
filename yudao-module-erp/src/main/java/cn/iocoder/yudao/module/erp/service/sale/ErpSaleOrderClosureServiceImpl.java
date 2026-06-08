package cn.iocoder.yudao.module.erp.service.sale;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionSuggestDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionFinishQualityDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpPurchaseSuggestDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionFinishQualityMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionSuggestMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpPurchaseSuggestMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpPurchaseInStockInStatusEnum;
import cn.iocoder.yudao.module.erp.enums.ErpQaStatusEnum;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpMrpSuggestStatusEnum;
import cn.iocoder.yudao.module.erp.enums.sale.ErpSaleOrderClosureStageEnum;
import cn.iocoder.yudao.module.erp.enums.sale.ErpSaleOrderDeliveryReadyStatusEnum;
import cn.iocoder.yudao.module.erp.service.sale.bo.ErpSaleOrderClosureSummaryBO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Service
@Validated
public class ErpSaleOrderClosureServiceImpl implements ErpSaleOrderClosureService {

    @Resource
    private ErpSaleOrderMapper erpSaleOrderMapper;
    @Resource
    private ErpPurchaseSuggestMapper erpPurchaseSuggestMapper;
    @Resource
    private ErpProductionSuggestMapper erpProductionSuggestMapper;
    @Resource
    private ErpPurchaseOrderMapper erpPurchaseOrderMapper;
    @Resource
    private ErpPurchaseInMapper erpPurchaseInMapper;
    @Resource
    private ErpProductionFinishQualityMapper erpProductionFinishQualityMapper;

    @Override
    public ErpSaleOrderClosureSummaryBO getClosureSummary(Long saleOrderId) {
        return getClosureSummaryMap(List.of(saleOrderId)).get(saleOrderId);
    }

    @Override
    public Map<Long, ErpSaleOrderClosureSummaryBO> getClosureSummaryMap(Collection<Long> saleOrderIds) {
        if (saleOrderIds == null || saleOrderIds.isEmpty()) {
            return Map.of();
        }
        List<ErpSaleOrderDO> saleOrders = emptyIfNull(erpSaleOrderMapper.selectByIds(saleOrderIds));
        if (saleOrders == null || saleOrders.isEmpty()) {
            return Map.of();
        }
        Set<Long> normalizedSaleOrderIds = saleOrders.stream()
                .map(ErpSaleOrderDO::getId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        List<ErpPurchaseSuggestDO> purchaseSuggests =
                emptyIfNull(erpPurchaseSuggestMapper.selectListBySourceOrderIds(normalizedSaleOrderIds));
        List<ErpProductionSuggestDO> productionSuggests =
                emptyIfNull(erpProductionSuggestMapper.selectListBySourceOrderIds(normalizedSaleOrderIds));
        List<ErpProductionFinishQualityDO> finishQualities =
                emptyIfNull(erpProductionFinishQualityMapper.selectListBySourceOrderIds(normalizedSaleOrderIds));

        Map<Long, List<ErpPurchaseSuggestDO>> purchaseSuggestMap = new HashMap<>();
        for (ErpPurchaseSuggestDO purchaseSuggest : purchaseSuggests) {
            if (purchaseSuggest.getSourceOrderId() != null) {
                purchaseSuggestMap.computeIfAbsent(purchaseSuggest.getSourceOrderId(), key -> new ArrayList<>())
                        .add(purchaseSuggest);
            }
        }
        Map<Long, List<ErpProductionSuggestDO>> productionSuggestMap = new HashMap<>();
        for (ErpProductionSuggestDO productionSuggest : productionSuggests) {
            if (productionSuggest.getSourceOrderId() != null) {
                productionSuggestMap.computeIfAbsent(productionSuggest.getSourceOrderId(), key -> new ArrayList<>())
                        .add(productionSuggest);
            }
        }
        Map<Long, BigDecimal> productionQualifiedQtyMap = new HashMap<>();
        for (ErpProductionFinishQualityDO finishQuality : finishQualities) {
            if (!Objects.equals(finishQuality.getStatus(), ErpQaStatusEnum.PARTIAL.getStatus())
                    && !Objects.equals(finishQuality.getStatus(), ErpQaStatusEnum.PASSED.getStatus())) {
                continue;
            }
            productionQualifiedQtyMap.merge(finishQuality.getSourceOrderId(),
                    ObjectUtil.defaultIfNull(finishQuality.getQualifiedQty(), BigDecimal.ZERO), BigDecimal::add);
        }

        Set<Long> purchaseOrderIds = convertPurchaseOrderIds(purchaseSuggests);
        List<ErpPurchaseOrderDO> purchaseOrders = purchaseOrderIds.isEmpty()
                ? List.of()
                : emptyIfNull(erpPurchaseOrderMapper.selectByIds(purchaseOrderIds));
        Map<Long, ErpPurchaseOrderDO> purchaseOrderMap = new HashMap<>();
        for (ErpPurchaseOrderDO purchaseOrder : purchaseOrders) {
            purchaseOrderMap.put(purchaseOrder.getId(), purchaseOrder);
        }
        List<ErpPurchaseInDO> purchaseIns = purchaseOrderIds.isEmpty()
                ? List.of()
                : emptyIfNull(erpPurchaseInMapper.selectListByOrderIds(purchaseOrderIds));
        Map<Long, List<ErpPurchaseInDO>> purchaseInMap = new HashMap<>();
        for (ErpPurchaseInDO purchaseIn : purchaseIns) {
            if (purchaseIn.getOrderId() != null) {
                purchaseInMap.computeIfAbsent(purchaseIn.getOrderId(), key -> new ArrayList<>()).add(purchaseIn);
            }
        }

        Map<Long, ErpSaleOrderClosureSummaryBO> summaryMap = new HashMap<>();
        for (ErpSaleOrderDO saleOrder : saleOrders) {
            List<ErpPurchaseSuggestDO> currentPurchaseSuggests =
                    purchaseSuggestMap.getOrDefault(saleOrder.getId(), List.of());
            List<ErpProductionSuggestDO> currentProductionSuggests =
                    productionSuggestMap.getOrDefault(saleOrder.getId(), List.of());
            List<ErpPurchaseOrderDO> currentPurchaseOrders = resolvePurchaseOrders(currentPurchaseSuggests, purchaseOrderMap);
            List<ErpPurchaseInDO> currentPurchaseIns = resolvePurchaseIns(currentPurchaseOrders, purchaseInMap);
            BigDecimal remainingShipQty = calculateRemainingShipQty(saleOrder);
            BigDecimal productionQualifiedQty = productionQualifiedQtyMap.getOrDefault(saleOrder.getId(), BigDecimal.ZERO);

            ErpSaleOrderClosureSummaryBO summary = new ErpSaleOrderClosureSummaryBO();
            summary.setSaleOrderId(saleOrder.getId());
            summary.setSaleOrderNo(saleOrder.getNo());
            summary.setRemainingShipQty(remainingShipQty);
            summary.setProductionQualifiedQty(productionQualifiedQty);
            summary.setDeliveryReadyStatus(saleOrder.getDeliveryReadyStatus());

            summary.setPurchaseSuggestCount((long) currentPurchaseSuggests.size());
            summary.setPurchaseConfirmedSuggestCount(countPurchaseSuggestsByStatus(currentPurchaseSuggests,
                    ErpMrpSuggestStatusEnum.CONFIRMED.getStatus()));
            summary.setPurchaseConvertedSuggestCount(countPurchaseSuggestsByStatus(currentPurchaseSuggests,
                    ErpMrpSuggestStatusEnum.CONVERTED.getStatus()));
            summary.setPurchaseRejectedSuggestCount(countPurchaseSuggestsByStatus(currentPurchaseSuggests,
                    ErpMrpSuggestStatusEnum.REJECTED.getStatus()));

            summary.setProductionSuggestCount((long) currentProductionSuggests.size());
            summary.setProductionConfirmedSuggestCount(countProductionSuggestsByStatus(currentProductionSuggests,
                    ErpMrpSuggestStatusEnum.CONFIRMED.getStatus()));
            summary.setProductionConvertedSuggestCount(countProductionSuggestsByStatus(currentProductionSuggests,
                    ErpMrpSuggestStatusEnum.CONVERTED.getStatus()));
            summary.setProductionRejectedSuggestCount(countProductionSuggestsByStatus(currentProductionSuggests,
                    ErpMrpSuggestStatusEnum.REJECTED.getStatus()));

            summary.setPurchaseOrderCount((long) currentPurchaseOrders.size());
            summary.setApprovedPurchaseOrderCount(currentPurchaseOrders.stream()
                    .filter(order -> Objects.equals(order.getStatus(), ErpAuditStatus.APPROVE.getStatus()))
                    .count());

            summary.setPurchaseInCount((long) currentPurchaseIns.size());
            summary.setApprovedPurchaseInCount(currentPurchaseIns.stream()
                    .filter(purchaseIn -> Objects.equals(purchaseIn.getStatus(), ErpAuditStatus.APPROVE.getStatus()))
                    .count());
            summary.setPendingQaPurchaseInCount(currentPurchaseIns.stream()
                    .filter(this::isPendingQaPurchaseIn)
                    .count());
            summary.setPendingStockInPurchaseInCount(currentPurchaseIns.stream()
                    .filter(this::isPendingStockInPurchaseIn)
                    .count());
            summary.setStockedPurchaseInCount(currentPurchaseIns.stream()
                    .filter(this::isFullyStockedPurchaseIn)
                    .count());
            summary.setNoNeedStockInPurchaseInCount(currentPurchaseIns.stream()
                    .filter(this::isNoNeedStockInPurchaseIn)
                    .count());

            List<String> blockerCodes = resolveBlockerCodes(saleOrder, summary, remainingShipQty);
            summary.setBlockerCodes(blockerCodes);
            summary.setClosureStage(resolveClosureStage(saleOrder, blockerCodes, remainingShipQty));
            summaryMap.put(saleOrder.getId(), summary);
        }
        return summaryMap;
    }

    private <T> List<T> emptyIfNull(List<T> records) {
        return records == null ? List.of() : records;
    }

    private Set<Long> convertPurchaseOrderIds(List<ErpPurchaseSuggestDO> purchaseSuggests) {
        if (CollUtil.isEmpty(purchaseSuggests)) {
            return Set.of();
        }
        return purchaseSuggests.stream()
                .map(ErpPurchaseSuggestDO::getConvertPurchaseOrderId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private List<ErpPurchaseOrderDO> resolvePurchaseOrders(List<ErpPurchaseSuggestDO> purchaseSuggests,
                                                           Map<Long, ErpPurchaseOrderDO> purchaseOrderMap) {
        if (CollUtil.isEmpty(purchaseSuggests)) {
            return List.of();
        }
        List<ErpPurchaseOrderDO> result = new ArrayList<>();
        Set<Long> addedIds = new LinkedHashSet<>();
        for (ErpPurchaseSuggestDO purchaseSuggest : purchaseSuggests) {
            Long purchaseOrderId = purchaseSuggest.getConvertPurchaseOrderId();
            if (purchaseOrderId == null || !addedIds.add(purchaseOrderId)) {
                continue;
            }
            ErpPurchaseOrderDO purchaseOrder = purchaseOrderMap.get(purchaseOrderId);
            if (purchaseOrder != null) {
                result.add(purchaseOrder);
            }
        }
        return result;
    }

    private List<ErpPurchaseInDO> resolvePurchaseIns(List<ErpPurchaseOrderDO> purchaseOrders,
                                                     Map<Long, List<ErpPurchaseInDO>> purchaseInMap) {
        if (CollUtil.isEmpty(purchaseOrders)) {
            return List.of();
        }
        List<ErpPurchaseInDO> result = new ArrayList<>();
        for (ErpPurchaseOrderDO purchaseOrder : purchaseOrders) {
            result.addAll(purchaseInMap.getOrDefault(purchaseOrder.getId(), List.of()));
        }
        return result;
    }

    private BigDecimal calculateRemainingShipQty(ErpSaleOrderDO saleOrder) {
        BigDecimal totalCount = ObjectUtil.defaultIfNull(saleOrder.getTotalCount(), BigDecimal.ZERO);
        BigDecimal outCount = ObjectUtil.defaultIfNull(saleOrder.getOutCount(), BigDecimal.ZERO);
        BigDecimal returnCount = ObjectUtil.defaultIfNull(saleOrder.getReturnCount(), BigDecimal.ZERO);
        return totalCount.subtract(outCount).add(returnCount);
    }

    private long countPurchaseSuggestsByStatus(List<ErpPurchaseSuggestDO> suggests, Integer status) {
        return suggests.stream()
                .filter(suggest -> Objects.equals(suggest.getStatus(), status))
                .count();
    }

    private long countProductionSuggestsByStatus(List<ErpProductionSuggestDO> suggests, Integer status) {
        return suggests.stream()
                .filter(suggest -> Objects.equals(suggest.getStatus(), status))
                .count();
    }

    private boolean isPendingQaPurchaseIn(ErpPurchaseInDO purchaseIn) {
        return Objects.equals(purchaseIn.getStatus(), ErpAuditStatus.APPROVE.getStatus())
                && (purchaseIn.getQaStatus() == null
                || Objects.equals(purchaseIn.getQaStatus(), ErpQaStatusEnum.TO_INSPECT.getStatus()));
    }

    private boolean isPendingStockInPurchaseIn(ErpPurchaseInDO purchaseIn) {
        if (!Objects.equals(purchaseIn.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            return false;
        }
        if (!isQualityFinished(purchaseIn.getQaStatus())) {
            return false;
        }
        if (Objects.equals(purchaseIn.getStockInStatus(), ErpPurchaseInStockInStatusEnum.NO_NEED_STOCK_IN.getStatus())
                || Objects.equals(purchaseIn.getStockInStatus(), ErpPurchaseInStockInStatusEnum.STOCKED_IN.getStatus())) {
            return false;
        }
        BigDecimal qaPassCount = ObjectUtil.defaultIfNull(purchaseIn.getQaPassCount(), BigDecimal.ZERO);
        BigDecimal stockInCount = ObjectUtil.defaultIfNull(purchaseIn.getStockInCount(), BigDecimal.ZERO);
        return qaPassCount.compareTo(stockInCount) > 0
                || Objects.equals(purchaseIn.getStockInStatus(), ErpPurchaseInStockInStatusEnum.TO_STOCK_IN.getStatus())
                || Objects.equals(purchaseIn.getStockInStatus(), ErpPurchaseInStockInStatusEnum.PARTIAL_STOCKED_IN.getStatus());
    }

    private boolean isFullyStockedPurchaseIn(ErpPurchaseInDO purchaseIn) {
        return Objects.equals(purchaseIn.getStatus(), ErpAuditStatus.APPROVE.getStatus())
                && Objects.equals(purchaseIn.getStockInStatus(), ErpPurchaseInStockInStatusEnum.STOCKED_IN.getStatus());
    }

    private boolean isNoNeedStockInPurchaseIn(ErpPurchaseInDO purchaseIn) {
        return Objects.equals(purchaseIn.getStatus(), ErpAuditStatus.APPROVE.getStatus())
                && Objects.equals(purchaseIn.getStockInStatus(), ErpPurchaseInStockInStatusEnum.NO_NEED_STOCK_IN.getStatus());
    }

    private boolean isQualityFinished(Integer qaStatus) {
        return Objects.equals(qaStatus, ErpQaStatusEnum.PARTIAL.getStatus())
                || Objects.equals(qaStatus, ErpQaStatusEnum.PASSED.getStatus())
                || Objects.equals(qaStatus, ErpQaStatusEnum.REJECTED.getStatus());
    }

    private List<String> resolveBlockerCodes(ErpSaleOrderDO saleOrder,
                                             ErpSaleOrderClosureSummaryBO summary,
                                             BigDecimal remainingShipQty) {
        List<String> blockers = new ArrayList<>();
        if (!Objects.equals(saleOrder.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            blockers.add(ErpSaleOrderClosureStageEnum.WAIT_SALE_APPROVAL.getStage());
            return blockers;
        }
        if (summary.getPurchaseSuggestCount() > 0
                && summary.getPurchaseConfirmedSuggestCount() + summary.getPurchaseConvertedSuggestCount()
                + summary.getPurchaseRejectedSuggestCount() < summary.getPurchaseSuggestCount()) {
            blockers.add(ErpSaleOrderClosureStageEnum.WAIT_PURCHASE_SUGGEST_CONFIRM.getStage());
        }
        if (summary.getPurchaseConfirmedSuggestCount() > 0) {
            blockers.add(ErpSaleOrderClosureStageEnum.WAIT_PURCHASE_ORDER_CONVERT.getStage());
        }
        if (summary.getPurchaseOrderCount() > summary.getApprovedPurchaseOrderCount()) {
            blockers.add(ErpSaleOrderClosureStageEnum.WAIT_PURCHASE_APPROVAL.getStage());
        }
        if (summary.getPendingQaPurchaseInCount() > 0) {
            blockers.add(ErpSaleOrderClosureStageEnum.WAIT_PURCHASE_IQC.getStage());
        }
        if (summary.getPendingStockInPurchaseInCount() > 0) {
            blockers.add(ErpSaleOrderClosureStageEnum.WAIT_PURCHASE_STOCK_IN.getStage());
        }
        if (summary.getProductionSuggestCount() > 0
                && summary.getProductionConfirmedSuggestCount() + summary.getProductionConvertedSuggestCount()
                + summary.getProductionRejectedSuggestCount() < summary.getProductionSuggestCount()) {
            blockers.add(ErpSaleOrderClosureStageEnum.WAIT_PRODUCTION_SUGGEST_CONFIRM.getStage());
        }
        if (summary.getProductionConfirmedSuggestCount() > 0) {
            blockers.add(ErpSaleOrderClosureStageEnum.WAIT_PRODUCTION_ORDER_CONVERT.getStage());
        }
        if (remainingShipQty.compareTo(BigDecimal.ZERO) > 0
                && !Objects.equals(summary.getDeliveryReadyStatus(), ErpSaleOrderDeliveryReadyStatusEnum.READY_TO_SHIP.getStatus())) {
            blockers.add(ErpSaleOrderClosureStageEnum.WAIT_DELIVERY_READY.getStage());
        }
        return blockers;
    }

    private String resolveClosureStage(ErpSaleOrderDO saleOrder, List<String> blockerCodes, BigDecimal remainingShipQty) {
        if (CollUtil.isNotEmpty(blockerCodes)) {
            if (blockerCodes.size() == 1
                    && Objects.equals(blockerCodes.get(0), ErpSaleOrderClosureStageEnum.WAIT_DELIVERY_READY.getStage())
                    && Objects.equals(saleOrder.getDeliveryReadyStatus(), ErpSaleOrderDeliveryReadyStatusEnum.PART_READY.getStatus())) {
                return ErpSaleOrderClosureStageEnum.PART_READY.getStage();
            }
            return blockerCodes.get(0);
        }
        if (remainingShipQty.compareTo(BigDecimal.ZERO) <= 0) {
            return ErpSaleOrderClosureStageEnum.CLOSED.getStage();
        }
        if (Objects.equals(saleOrder.getDeliveryReadyStatus(), ErpSaleOrderDeliveryReadyStatusEnum.PART_READY.getStatus())) {
            return ErpSaleOrderClosureStageEnum.PART_READY.getStage();
        }
        return ErpSaleOrderClosureStageEnum.READY_TO_SHIP.getStage();
    }
}
