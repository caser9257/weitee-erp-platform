package cn.weitee.erp.module.erp.service.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMaterialPlanRuleDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpDemandDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpPlanDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpResultDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpShortageDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpTraceNodeDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpStockReservationDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpStockReservationSummaryDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionSuggestDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpPurchaseSuggestDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductUnitDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderItemDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpMrpDemandMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpMrpPlanMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpMrpResultMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpMrpShortageMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpMrpTraceNodeMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpMrpStockReservationMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionSuggestMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpPurchaseSuggestMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseOrderMapper;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOrderItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpBusinessTypeConstants;
import cn.weitee.erp.module.erp.enums.mrp.ErpMaterialPlanReplenishModeEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpMrpStockReservationStatusEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpMrpSuggestStatusEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpMrpSupplyTypeEnum;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.product.ErpProductUnitService;
import cn.weitee.erp.module.erp.service.mrp.support.ErpMrpMaterialProjectKey;
import cn.weitee.erp.module.erp.service.mrp.support.ErpMrpNettingRequest;
import cn.weitee.erp.module.erp.service.mrp.support.ErpMrpNettingResult;
import cn.weitee.erp.module.erp.service.mrp.support.ErpMrpNettingRuntimePolicy;
import cn.weitee.erp.module.erp.service.mrp.support.ErpMrpSupplyContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.MRP_BOM_CYCLE;

@Service
@Slf4j
public class ErpMrpCalcServiceImpl implements ErpMrpCalcService {

    private static final String SUPPLY_OWNER_COMPANY = "COMPANY";
    private static final String SUPPLY_OWNER_CUSTOMER = "CUSTOMER";
    private static final String SKIP_REASON_MRP_DISABLED = "MRP_DISABLED";
    private static final String SKIP_REASON_CUSTOMER_SUPPLIED_CUSTOMER_OWNED = "CUSTOMER_SUPPLIED_CUSTOMER_OWNED";
    private static final String SKIP_REASON_TOLL_MANUFACTURING_DEFAULT_SKIP = "TOLL_MANUFACTURING_DEFAULT_SKIP";
    private static final int THEORETICAL_QTY_SCALE = 6;
    private static final int DEFAULT_EXECUTION_QTY_SCALE = 3;
    private static final Set<String> DISCRETE_UNITS = Set.of("套", "个", "件", "支", "条", "台", "只", "张",
            "根", "块", "盒", "包", "卷", "双", "副", "瓶", "片", "颗", "把", "盘", "pc", "pcs", "ea");
    private static final Set<String> CONTINUOUS_UNITS = Set.of("米", "m", "cm", "mm", "平方米", "㎡", "m2",
            "立方米", "m3", "kg", "g", "mg", "吨", "t", "公斤", "千克", "克", "l", "ml", "升", "毫升");

    @Resource
    private ErpMrpPlanMapper erpMrpPlanMapper;
    @Resource
    private ErpMrpDemandMapper erpMrpDemandMapper;
    @Resource
    private ErpMrpResultMapper erpMrpResultMapper;
    @Resource
    private ErpMrpShortageMapper erpMrpShortageMapper;
    @Resource
    private ErpMrpTraceNodeMapper erpMrpTraceNodeMapper;
    @Resource
    private ErpPurchaseSuggestMapper erpPurchaseSuggestMapper;
    @Resource
    private ErpProductionSuggestMapper erpProductionSuggestMapper;
    @Resource
    private ErpBomMapper erpBomMapper;
    @Resource
    private ErpBomItemMapper erpBomItemMapper;
    @Resource
    private ErpMaterialPlanRuleResolver materialPlanRuleResolver;
    @Resource
    private ErpStockMapper erpStockMapper;
    @Resource
    private ErpSaleOrderMapper erpSaleOrderMapper;
    @Resource
    private ErpSaleOrderItemMapper erpSaleOrderItemMapper;
    @Resource
    private ErpPurchaseOrderMapper erpPurchaseOrderMapper;
    @Resource
    private ErpPurchaseOrderItemMapper erpPurchaseOrderItemMapper;
    @Resource
    private ErpProductionOrderMapper erpProductionOrderMapper;
    @Resource
    private ErpMrpStockReservationMapper erpMrpStockReservationMapper;
    @Resource
    private ErpMrpStockReservationSummaryService stockReservationSummaryService;
    @Resource
    private ErpMrpNettingPolicyResolver mrpNettingPolicyResolver;
    @Resource
    private ErpMrpNettingService mrpNettingService;
    @Resource
    private ErpMrpResultComponentService resultComponentService;
    @Resource
    private ErpProductService productService;
    @Resource
    private ErpProductUnitService productUnitService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(Long planId) {
        ErpMrpPlanDO plan = erpMrpPlanMapper.selectById(planId);
        List<ErpSaleOrderDO> saleOrders = erpSaleOrderMapper.selectList(new LambdaQueryWrapperX<ErpSaleOrderDO>()
                .eq(ErpSaleOrderDO::getStatus, ErpAuditStatus.APPROVE.getStatus()));
        saleOrders = saleOrders.stream()
                .filter(this::hasDemandDate)
                .filter(saleOrder -> {
                    LocalDate demandDate = resolveDemandDate(saleOrder);
                    return !demandDate.isBefore(plan.getPlanStartDate()) && !demandDate.isAfter(plan.getPlanEndDate());
                })
                .collect(Collectors.toList());
        runForSaleOrders(planId, saleOrders);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void runForSaleOrders(Long planId, List<ErpSaleOrderDO> saleOrders) {
        clearOldResult(planId);
        if (CollUtil.isEmpty(saleOrders)) {
            return;
        }
        Set<Long> saleOrderIds = convertSet(saleOrders, ErpSaleOrderDO::getId);
        Set<Long> releasedProductIds = releaseStockReservations(saleOrderIds);
        stockReservationSummaryService.refreshSummaryByProductIds(releasedProductIds);
        List<ErpSaleOrderItemDO> saleItems = erpSaleOrderItemMapper.selectListByOrderIds(saleOrderIds);
        Map<Long, List<ErpSaleOrderItemDO>> saleItemMap = convertMultiMap(saleItems, ErpSaleOrderItemDO::getOrderId);
        ErpMrpSupplyContext context = buildContext();
        Set<Long> changedReservationProductIds = new HashSet<>();
        for (ErpSaleOrderDO saleOrder : saleOrders) {
            LocalDate demandDate = resolveDemandDate(saleOrder);
            for (ErpSaleOrderItemDO item : saleItemMap.getOrDefault(saleOrder.getId(), Collections.emptyList())) {
                erpMrpDemandMapper.insert(new ErpMrpDemandDO().setPlanId(planId).setSourceType("SALE_ORDER")
                        .setSourceId(saleOrder.getId()).setSourceItemId(item.getId()).setProjectId(saleOrder.getProjectId())
                        .setProductId(item.getProductId()).setDemandQty(item.getCount()).setDemandDate(demandDate));
                explode(planId, item.getProductId(), item.getProductId(), item.getCount(), demandDate, null,
                        saleOrder.getProjectId(), saleOrder.getId(), item.getId(), context, new HashSet<>(),
                        saleOrder.getBusinessType(), null, null, null, null, changedReservationProductIds);
            }
        }
        stockReservationSummaryService.refreshSummaryByProductIds(changedReservationProductIds);
    }

    private LocalDate resolveDemandDate(ErpSaleOrderDO saleOrder) {
        if (saleOrder.getDeliveryDate() != null) {
            return saleOrder.getDeliveryDate();
        }
        if (saleOrder.getOrderTime() != null) {
            return saleOrder.getOrderTime().toLocalDate();
        }
        throw new IllegalStateException("Sale order " + saleOrder.getId() + " must have deliveryDate or orderTime");
    }

    private boolean hasDemandDate(ErpSaleOrderDO saleOrder) {
        return saleOrder.getDeliveryDate() != null || saleOrder.getOrderTime() != null;
    }

    private void explode(Long planId, Long rootProductId, Long materialId, BigDecimal grossDemandQty, LocalDate demandDate,
                         String preferredSupplyType, Long projectId, Long sourceOrderId, Long sourceItemId, ErpMrpSupplyContext context,
                         Set<Long> path, String businessType, Boolean mrpEnableFlag, String supplyOwner,
                         Long incomingBomItemId, TraceFrame parentTraceFrame, Set<Long> changedReservationProductIds) {
        if (grossDemandQty == null || grossDemandQty.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        if (!path.add(materialId)) {
            throw exception(MRP_BOM_CYCLE);
        }
        try {
            ErpMaterialPlanRuleDO rule = context.getRuleMap().computeIfAbsent(materialId,
                    materialPlanRuleResolver::resolveByProductId);
            ErpBomDO bom = context.getBomMap().computeIfAbsent(materialId, erpBomMapper::selectEffectiveByProductId);
            String supplyType = decideSupplyType(rule, preferredSupplyType, bom);
            BigDecimal stock = context.getAvailableStockMap().computeIfAbsent(materialId, key -> {
                BigDecimal physicalStock = defaultDecimal(erpStockMapper.selectSumByProductId(key));
                BigDecimal reservedStock = context.getReservedStockMap().getOrDefault(key, BigDecimal.ZERO);
                return physicalStock.subtract(reservedStock).max(BigDecimal.ZERO);
            });
            ErpMrpMaterialProjectKey projectKey = new ErpMrpMaterialProjectKey(projectId, materialId);
            BigDecimal incoming = context.getProjectIncomingMap().getOrDefault(projectKey, BigDecimal.ZERO);
            BigDecimal wip = context.getProjectWipMap().getOrDefault(projectKey, BigDecimal.ZERO);
            BigDecimal safetyStock = defaultDecimal(rule == null ? null : rule.getSafetyStock());
            ErpMrpNettingRuntimePolicy policy = mrpNettingPolicyResolver.resolve(businessType);
            ErpMrpNettingResult nettingResult = mrpNettingService.calculate(policy, context, ErpMrpNettingRequest.builder()
                    .materialId(materialId)
                    .projectId(projectId)
                    .grossDemandQty(grossDemandQty)
                    .safetyStockQty(safetyStock)
                    .build());
            BigDecimal theoreticalNetDemandQty = defaultDecimal(nettingResult.getNetDemandQty());
            BigDecimal executionNetDemandQty = theoreticalNetDemandQty.compareTo(BigDecimal.ZERO) > 0
                    ? normalizeExecutionQty(materialId, theoreticalNetDemandQty, rule, context) : BigDecimal.ZERO;
            SkipDecision skipDecision = resolveSkipDecision(businessType, mrpEnableFlag, supplyOwner);
            BigDecimal actualExecutionNetDemandQty = skipDecision.skip ? BigDecimal.ZERO : executionNetDemandQty;
            TraceFrame traceFrame = insertTraceNode(planId, rootProductId, materialId, grossDemandQty, demandDate, projectId,
                    sourceOrderId, sourceItemId, context, stock, incoming, wip, safetyStock, theoreticalNetDemandQty,
                    actualExecutionNetDemandQty, nettingResult.getReservedStockQty(), nettingResult.getPolicyCode(), nettingResult.getPolicyVersion(), businessType,
                    mrpEnableFlag, supplyOwner, supplyType, skipDecision.reason, incomingBomItemId, parentTraceFrame, bom);

            ErpMrpResultDO result = new ErpMrpResultDO().setPlanId(planId).setTraceNodeId(traceFrame.traceNodeId)
                    .setTracePathKey(traceFrame.tracePathKey).setTraceLevel(traceFrame.traceLevel)
                    .setParentMaterialId(parentTraceFrame == null ? null : parentTraceFrame.materialId)
                    .setBomItemId(incomingBomItemId)
                    .setRootProductId(rootProductId).setMaterialId(materialId)
                    .setGrossDemandQty(grossDemandQty).setAvailableStockQty(stock).setIncomingQty(incoming).setWipQty(wip)
                    .setReservedStockQty(nettingResult.getReservedStockQty()).setNetDemandQty(actualExecutionNetDemandQty)
                    .setPolicyCode(nettingResult.getPolicyCode()).setPolicyVersion(nettingResult.getPolicyVersion())
                    .setBusinessType(businessType).setMrpEnableFlag(mrpEnableFlag).setSupplyOwner(supplyOwner)
                    .setSuggestType(supplyType).setSuggestDate(demandDate).setSourceOrderId(sourceOrderId)
                    .setSourceItemId(sourceItemId).setDemandDate(demandDate)
                    .setSkipReason(skipDecision.skip ? skipDecision.reason : null);
            erpMrpResultMapper.insert(result);
            resultComponentService.saveComponents(planId, result.getId(), materialId, nettingResult.getComponentResults());
            if (nettingResult.getReservedStockQty().compareTo(BigDecimal.ZERO) > 0) {
                erpMrpStockReservationMapper.insert(new ErpMrpStockReservationDO().setPlanId(planId).setProjectId(projectId)
                        .setProductId(materialId).setSourceOrderId(sourceOrderId).setSourceItemId(sourceItemId)
                        .setReservedQty(nettingResult.getReservedStockQty())
                        .setStatus(ErpMrpStockReservationStatusEnum.ACTIVE.getStatus()));
                changedReservationProductIds.add(materialId);
            }
            if (skipDecision.skip) {
                path.remove(materialId);
                return;
            }
            if (executionNetDemandQty.compareTo(BigDecimal.ZERO) <= 0) {
                path.remove(materialId);
                return;
            }
            if (ErpMrpSupplyTypeEnum.PURCHASE.getType().equals(supplyType)) {
                erpPurchaseSuggestMapper.insert(new ErpPurchaseSuggestDO().setPlanId(planId).setTraceNodeId(traceFrame.traceNodeId)
                        .setProjectId(projectId).setMaterialId(materialId).setTracePathKey(traceFrame.tracePathKey)
                        .setTraceLevel(traceFrame.traceLevel).setParentMaterialId(parentTraceFrame == null ? null : parentTraceFrame.materialId)
                        .setBomItemId(incomingBomItemId).setSuggestQty(executionNetDemandQty).setSuggestArrivalDate(demandDate)
                        .setGrossDemandQty(grossDemandQty).setAvailableStockQty(stock).setIncomingQty(incoming)
                        .setWipQty(wip).setReservedStockQty(nettingResult.getReservedStockQty()).setSafetyStockQty(safetyStock)
                        .setNetDemandQty(executionNetDemandQty).setSourceOrderId(sourceOrderId).setSourceItemId(sourceItemId)
                        .setStatus(ErpMrpSuggestStatusEnum.TO_CONFIRM.getStatus()));
                path.remove(materialId);
                return;
            }
            if (bom == null) {
                erpMrpShortageMapper.insert(new ErpMrpShortageDO().setPlanId(planId).setTraceNodeId(traceFrame.traceNodeId)
                        .setRootProductId(rootProductId).setMaterialId(materialId).setTracePathKey(traceFrame.tracePathKey)
                        .setTraceLevel(traceFrame.traceLevel).setParentMaterialId(parentTraceFrame == null ? null : parentTraceFrame.materialId)
                        .setBomItemId(incomingBomItemId).setShortageQty(executionNetDemandQty).setRequiredDate(demandDate)
                        .setSourceOrderId(sourceOrderId).setSourceItemId(sourceItemId));
                path.remove(materialId);
                return;
            }
            Integer makeLeadDay = rule == null || rule.getMakeLeadDay() == null ? 0 : rule.getMakeLeadDay();
            erpProductionSuggestMapper.insert(new ErpProductionSuggestDO().setPlanId(planId).setTraceNodeId(traceFrame.traceNodeId)
                    .setProjectId(projectId).setProductId(materialId).setTracePathKey(traceFrame.tracePathKey)
                    .setTraceLevel(traceFrame.traceLevel).setParentMaterialId(parentTraceFrame == null ? null : parentTraceFrame.materialId)
                    .setBomItemId(incomingBomItemId).setSuggestQty(executionNetDemandQty)
                    .setSuggestStartDate(demandDate.minusDays(makeLeadDay))
                    .setSuggestEndDate(demandDate).setSourceOrderId(sourceOrderId).setSourceItemId(sourceItemId)
                    .setGrossDemandQty(grossDemandQty).setAvailableStockQty(stock).setIncomingQty(incoming).setWipQty(wip)
                    .setReservedStockQty(nettingResult.getReservedStockQty()).setSafetyStockQty(safetyStock)
                    .setNetDemandQty(executionNetDemandQty).setStatus(ErpMrpSuggestStatusEnum.TO_CONFIRM.getStatus()));
            List<ErpBomItemDO> bomItems = context.getBomItemMap().computeIfAbsent(bom.getId(), erpBomItemMapper::selectListByBomId);
            for (ErpBomItemDO bomItem : bomItems) {
                BigDecimal lossRate = defaultDecimal(bomItem.getLossRate());
                BigDecimal childDemand = theoreticalNetDemandQty.multiply(defaultDecimal(bomItem.getUsageQty()))
                        .multiply(BigDecimal.ONE.add(lossRate)).setScale(THEORETICAL_QTY_SCALE, RoundingMode.UP);
                LocalDate childDemandDate = demandDate.minusDays(bomItem.getLeadTimeDay() == null ? 0 : bomItem.getLeadTimeDay());
                explode(planId, rootProductId, bomItem.getMaterialId(), childDemand, childDemandDate,
                        translateMaterialType(bomItem.getMaterialType()), projectId, sourceOrderId, sourceItemId, context, path,
                        businessType, resolveMrpEnableFlag(bomItem), bomItem.getSupplyOwner(), bomItem.getId(),
                        traceFrame, changedReservationProductIds);
            }
        } catch (Exception ex) {
            log.error("[explode][planId({}) rootProductId({}) materialId({}) projectId({}) sourceOrderId({}) sourceItemId({}) demandDate({}) businessType({}) path({}) 计算失败]",
                    planId, rootProductId, materialId, projectId, sourceOrderId, sourceItemId,
                    demandDate, businessType, path, ex);
            throw ex;
        }
        path.remove(materialId);
    }

    private TraceFrame insertTraceNode(Long planId, Long rootProductId, Long materialId, BigDecimal grossDemandQty,
                                       LocalDate demandDate, Long projectId, Long sourceOrderId, Long sourceItemId,
                                       ErpMrpSupplyContext context, BigDecimal stock, BigDecimal incoming, BigDecimal wip,
                                       BigDecimal safetyStock, BigDecimal theoreticalNetDemandQty,
                                       BigDecimal executionNetDemandQty, BigDecimal reservedStockQty, String policyCode, Integer policyVersion,
                                       String businessType, Boolean mrpEnableFlag, String supplyOwner, String supplyType,
                                       String skipReason, Long incomingBomItemId, TraceFrame parentTraceFrame, ErpBomDO bom) {
        int traceLevel = parentTraceFrame == null ? 0 : parentTraceFrame.traceLevel + 1;
        String tracePathKey = buildTracePathKey(planId, rootProductId, materialId, sourceOrderId, sourceItemId, incomingBomItemId,
                parentTraceFrame);
        ErpMrpTraceNodeDO traceNode = new ErpMrpTraceNodeDO().setPlanId(planId).setRootProductId(rootProductId)
                .setParentTraceNodeId(parentTraceFrame == null ? null : parentTraceFrame.traceNodeId)
                .setParentMaterialId(parentTraceFrame == null ? null : parentTraceFrame.materialId)
                .setTraceLevel(traceLevel).setMaterialId(materialId)
                .setBomId(bom == null ? null : bom.getId()).setBomItemId(incomingBomItemId)
                .setTracePathKey(tracePathKey).setProjectId(projectId).setSourceOrderId(sourceOrderId)
                .setSourceItemId(sourceItemId).setGrossDemandQty(grossDemandQty).setAvailableStockQty(stock)
                .setIncomingQty(incoming).setWipQty(wip).setReservedStockQty(reservedStockQty)
                .setSafetyStockQty(safetyStock).setTheoreticalNetDemandQty(theoreticalNetDemandQty)
                .setExecutionNetDemandQty(executionNetDemandQty).setPolicyCode(policyCode)
                .setPolicyVersion(policyVersion).setBusinessType(businessType).setMrpEnableFlag(mrpEnableFlag)
                .setSupplyOwner(supplyOwner).setSuggestType(supplyType).setSkipReason(skipReason)
                .setSuggestDate(demandDate).setDemandDate(demandDate);
        erpMrpTraceNodeMapper.insert(traceNode);
        return new TraceFrame(traceNode.getId(), traceLevel, tracePathKey, materialId);
    }

    private String buildTracePathKey(Long planId, Long rootProductId, Long materialId, Long sourceOrderId, Long sourceItemId,
                                     Long bomItemId, TraceFrame parentTraceFrame) {
        String currentToken = (bomItemId == null ? "ROOT" : String.valueOf(bomItemId)) + ":" + materialId;
        if (parentTraceFrame == null || parentTraceFrame.tracePathKey == null || parentTraceFrame.tracePathKey.isBlank()) {
            return planId + "|" + sourceOrderId + "|" + sourceItemId + "|" + rootProductId + "|" + currentToken;
        }
        return parentTraceFrame.tracePathKey + ">" + currentToken;
    }

    private static class TraceFrame {
        private final Long traceNodeId;
        private final Integer traceLevel;
        private final String tracePathKey;
        private final Long materialId;

        private TraceFrame(Long traceNodeId, Integer traceLevel, String tracePathKey, Long materialId) {
            this.traceNodeId = traceNodeId;
            this.traceLevel = traceLevel;
            this.tracePathKey = tracePathKey;
            this.materialId = materialId;
        }
    }

    private ErpMrpSupplyContext buildContext() {
        ErpMrpSupplyContext context = new ErpMrpSupplyContext();
        List<ErpPurchaseOrderDO> purchaseOrders = erpPurchaseOrderMapper.selectList(new LambdaQueryWrapperX<ErpPurchaseOrderDO>()
                .eq(ErpPurchaseOrderDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
                .apply("in_count < total_count"));
        if (CollUtil.isNotEmpty(purchaseOrders)) {
            List<ErpPurchaseOrderItemDO> orderItems = erpPurchaseOrderItemMapper.selectListByOrderIds(
                    convertSet(purchaseOrders, ErpPurchaseOrderDO::getId));
            for (ErpPurchaseOrderItemDO item : orderItems) {
                BigDecimal incomingQty = defaultDecimal(item.getCount()).subtract(defaultDecimal(item.getInCount()));
                if (incomingQty.compareTo(BigDecimal.ZERO) > 0) {
                    context.getProjectIncomingMap().merge(new ErpMrpMaterialProjectKey(item.getProjectId(), item.getProductId()),
                            incomingQty, BigDecimal::add);
                }
            }
        }
        for (ErpProductionOrderDO order : erpProductionOrderMapper.selectListForWip()) {
            BigDecimal wipQty = defaultDecimal(order.getPlanQty()).subtract(defaultDecimal(order.getFinishedQty()));
            if (wipQty.compareTo(BigDecimal.ZERO) > 0) {
                context.getProjectWipMap().merge(new ErpMrpMaterialProjectKey(order.getProjectId(), order.getProductId()),
                        wipQty, BigDecimal::add);
            }
        }
        for (ErpMrpStockReservationSummaryDO summary : stockReservationSummaryService.getActiveSummaryList()) {
            context.getReservedStockMap().merge(summary.getProductId(),
                    defaultDecimal(summary.getActiveReservedQty()), BigDecimal::add);
        }
        return context;
    }

    private Set<Long> releaseStockReservations(Set<Long> saleOrderIds) {
        if (CollUtil.isEmpty(saleOrderIds)) {
            return Collections.emptySet();
        }
        List<ErpMrpStockReservationDO> reservations = erpMrpStockReservationMapper.selectListBySourceOrderIds(saleOrderIds);
        if (CollUtil.isEmpty(reservations)) {
            return Collections.emptySet();
        }
        erpMrpStockReservationMapper.updateStatusBySourceOrderIds(saleOrderIds,
                ErpMrpStockReservationStatusEnum.ACTIVE.getStatus(),
                ErpMrpStockReservationStatusEnum.RELEASED.getStatus());
        return reservations.stream().map(ErpMrpStockReservationDO::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private void clearOldResult(Long planId) {
        erpMrpDemandMapper.deleteByPlanId(planId);
        erpMrpTraceNodeMapper.deleteByPlanId(planId);
        resultComponentService.deleteByPlanId(planId);
        erpMrpResultMapper.deleteByPlanId(planId);
        erpMrpShortageMapper.deleteByPlanId(planId);
        erpPurchaseSuggestMapper.deleteByPlanId(planId);
        erpProductionSuggestMapper.deleteByPlanId(planId);
    }

    private BigDecimal normalizeExecutionQty(Long materialId, BigDecimal theoreticalNetDemandQty, ErpMaterialPlanRuleDO rule,
                                             ErpMrpSupplyContext context) {
        BigDecimal precisionAdjustedQty = theoreticalNetDemandQty
                .setScale(resolveExecutionQtyScale(materialId, context), RoundingMode.UP);
        return applyLotRule(precisionAdjustedQty, rule);
    }

    private BigDecimal applyLotRule(BigDecimal netDemandQty, ErpMaterialPlanRuleDO rule) {
        if (rule == null) {
            return netDemandQty;
        }
        if (ErpMaterialPlanReplenishModeEnum.FIXED_LOT.getMode().equals(rule.getReplenishMode())) {
            BigDecimal fixedOrderQty = defaultDecimal(rule.getFixedOrderQty());
            if (fixedOrderQty.compareTo(BigDecimal.ZERO) > 0) {
                return netDemandQty.divide(fixedOrderQty, 0, RoundingMode.UP).multiply(fixedOrderQty);
            }
        }
        BigDecimal qty = netDemandQty;
        BigDecimal minOrderQty = defaultDecimal(rule.getMinOrderQty());
        if (minOrderQty.compareTo(BigDecimal.ZERO) > 0 && qty.compareTo(minOrderQty) < 0) {
            qty = minOrderQty;
        }
        BigDecimal multiple = defaultDecimal(rule.getOrderMultiple());
        if (multiple.compareTo(BigDecimal.ZERO) > 0) {
            qty = qty.divide(multiple, 0, RoundingMode.UP).multiply(multiple);
        }
        return qty;
    }

    private int resolveExecutionQtyScale(Long materialId, ErpMrpSupplyContext context) {
        ErpProductDO product = context.getProductMap().computeIfAbsent(materialId, productService::getProduct);
        if (product == null || product.getUnitId() == null) {
            return DEFAULT_EXECUTION_QTY_SCALE;
        }
        ErpProductUnitDO unit = context.getProductUnitMap().computeIfAbsent(product.getUnitId(),
                productUnitService::getProductUnit);
        if (unit == null || unit.getName() == null) {
            return DEFAULT_EXECUTION_QTY_SCALE;
        }
        String normalizedUnitName = normalizeUnitName(unit.getName());
        if (DISCRETE_UNITS.contains(normalizedUnitName)) {
            return 0;
        }
        if (CONTINUOUS_UNITS.contains(normalizedUnitName)) {
            return DEFAULT_EXECUTION_QTY_SCALE;
        }
        return DEFAULT_EXECUTION_QTY_SCALE;
    }

    private String normalizeUnitName(String unitName) {
        return unitName == null ? "" : unitName.replace(" ", "").trim().toLowerCase(Locale.ROOT);
    }

    private String decideSupplyType(ErpMaterialPlanRuleDO rule, String preferredSupplyType, ErpBomDO bom) {
        if (rule != null && Boolean.TRUE.equals(rule.getEnableFlag()) && rule.getSupplyType() != null) {
            return rule.getSupplyType();
        }
        if (preferredSupplyType != null) {
            return preferredSupplyType;
        }
        return bom != null ? ErpMrpSupplyTypeEnum.MAKE.getType() : ErpMrpSupplyTypeEnum.PURCHASE.getType();
    }

    private String translateMaterialType(Integer materialType) {
        if (materialType == null) {
            return null;
        }
        return materialType == 1 ? ErpMrpSupplyTypeEnum.MAKE.getType() : ErpMrpSupplyTypeEnum.PURCHASE.getType();
    }

    private Boolean resolveMrpEnableFlag(ErpBomItemDO bomItem) {
        return bomItem.getMrpEnableFlag() == null ? Boolean.TRUE : bomItem.getMrpEnableFlag();
    }

    private SkipDecision resolveSkipDecision(String businessType, Boolean mrpEnableFlag, String supplyOwner) {
        if (mrpEnableFlag == null && supplyOwner == null) {
            return SkipDecision.PARTICIPATE;
        }
        if (Boolean.FALSE.equals(mrpEnableFlag)) {
            return new SkipDecision(true, SKIP_REASON_MRP_DISABLED);
        }
        if (ErpBusinessTypeConstants.CUSTOMER_SUPPLIED.equals(businessType)
                && SUPPLY_OWNER_CUSTOMER.equals(supplyOwner)) {
            return new SkipDecision(true, SKIP_REASON_CUSTOMER_SUPPLIED_CUSTOMER_OWNED);
        }
        if (ErpBusinessTypeConstants.TOLL_MANUFACTURING.equals(businessType)
                && !SUPPLY_OWNER_COMPANY.equals(supplyOwner)) {
            return new SkipDecision(true, SKIP_REASON_TOLL_MANUFACTURING_DEFAULT_SKIP);
        }
        return SkipDecision.PARTICIPATE;
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
    private static class SkipDecision {
        private static final SkipDecision PARTICIPATE = new SkipDecision(false, null);

        private final boolean skip;
        private final String reason;

        private SkipDecision(boolean skip, String reason) {
            this.skip = skip;
            this.reason = reason;
        }
    }

}
