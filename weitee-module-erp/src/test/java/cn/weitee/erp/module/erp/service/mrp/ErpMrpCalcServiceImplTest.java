package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMaterialPlanRuleDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpDemandDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpPlanDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpResultDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpStockReservationDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpStockReservationSummaryDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpTraceNodeDO;
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
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpMrpTraceNodeMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpMrpStockReservationMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpMrpShortageMapper;
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
import cn.weitee.erp.module.erp.service.mrp.support.ErpMrpNettingRuntimePolicy;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.product.ErpProductUnitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErpMrpCalcServiceImplTest {

    private final AtomicReference<ErpMrpPlanDO> planRef = new AtomicReference<>();
    private final AtomicReference<List<ErpSaleOrderDO>> saleOrdersRef = new AtomicReference<>(Collections.emptyList());
    private final AtomicReference<List<ErpSaleOrderItemDO>> saleItemsRef = new AtomicReference<>(Collections.emptyList());
    private final AtomicReference<Set<Long>> requestedSaleOrderIdsRef = new AtomicReference<>(Collections.emptySet());
    private final AtomicReference<ErpBomDO> bomRef = new AtomicReference<>();
    private final AtomicReference<List<ErpBomItemDO>> bomItemsRef = new AtomicReference<>(Collections.emptyList());
    private final AtomicReference<Map<Long, ErpMaterialPlanRuleDO>> ruleMapRef = new AtomicReference<>(Collections.emptyMap());
    private final AtomicReference<Map<Long, BigDecimal>> stockQtyMapRef = new AtomicReference<>(Collections.emptyMap());
    private final AtomicReference<List<ErpPurchaseOrderDO>> purchaseOrdersRef = new AtomicReference<>(Collections.emptyList());
    private final AtomicReference<List<ErpPurchaseOrderItemDO>> purchaseOrderItemsRef = new AtomicReference<>(Collections.emptyList());
    private final AtomicReference<List<ErpProductionOrderDO>> productionOrdersRef = new AtomicReference<>(Collections.emptyList());
    private final AtomicReference<List<ErpMrpStockReservationSummaryDO>> activeReservationSummariesRef =
            new AtomicReference<>(Collections.emptyList());
    private final AtomicReference<Map<Long, ErpProductDO>> productMapRef = new AtomicReference<>(Collections.emptyMap());
    private final AtomicReference<Map<Long, ErpProductUnitDO>> productUnitMapRef = new AtomicReference<>(Collections.emptyMap());

    private final List<ErpMrpDemandDO> insertedDemands = new ArrayList<>();
    private final List<ErpMrpResultDO> insertedResults = new ArrayList<>();
    private final List<ErpMrpTraceNodeDO> insertedTraceNodes = new ArrayList<>();
    private final List<ErpPurchaseSuggestDO> insertedPurchaseSuggests = new ArrayList<>();
    private final List<ErpProductionSuggestDO> insertedProductionSuggests = new ArrayList<>();
    private final List<ErpMrpStockReservationDO> insertedReservations = new ArrayList<>();

    private ErpMrpCalcServiceImpl mrpCalcService;

    @BeforeEach
    void setUp() throws Exception {
        mrpCalcService = new ErpMrpCalcServiceImpl();
        insertedDemands.clear();
        insertedResults.clear();
        insertedTraceNodes.clear();
        insertedPurchaseSuggests.clear();
        insertedProductionSuggests.clear();
        insertedReservations.clear();
        planRef.set(null);
        saleOrdersRef.set(Collections.emptyList());
        saleItemsRef.set(Collections.emptyList());
        requestedSaleOrderIdsRef.set(Collections.emptySet());
        bomRef.set(null);
        bomItemsRef.set(Collections.emptyList());
        ruleMapRef.set(Collections.emptyMap());
        stockQtyMapRef.set(Collections.emptyMap());
        purchaseOrdersRef.set(Collections.emptyList());
        purchaseOrderItemsRef.set(Collections.emptyList());
        productionOrdersRef.set(Collections.emptyList());
        activeReservationSummariesRef.set(Collections.emptyList());
        productMapRef.set(Collections.emptyMap());
        productUnitMapRef.set(Collections.emptyMap());

        setField(mrpCalcService, "mrpPlanMapper", createProxy(ErpMrpPlanMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return planRef.get();
            }
            return null;
        }));
        setField(mrpCalcService, "demandMapper", createProxy(ErpMrpDemandMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertedDemands.add((ErpMrpDemandDO) args[0]);
                return 1;
            }
            if ("deleteByPlanId".equals(methodName)) {
                insertedDemands.clear();
                return 1;
            }
            return null;
        }));
        setField(mrpCalcService, "resultMapper", createProxy(ErpMrpResultMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertedResults.add((ErpMrpResultDO) args[0]);
                return 1;
            }
            if ("deleteByPlanId".equals(methodName)) {
                insertedResults.clear();
                return 1;
            }
            return null;
        }));
        setField(mrpCalcService, "erpMrpTraceNodeMapper", createProxy(ErpMrpTraceNodeMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertedTraceNodes.add((ErpMrpTraceNodeDO) args[0]);
                return 1;
            }
            if ("deleteByPlanId".equals(methodName)) {
                insertedTraceNodes.clear();
                return 1;
            }
            return null;
        }));
        setField(mrpCalcService, "shortageMapper", createProxy(ErpMrpShortageMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                return 1;
            }
            if ("deleteByPlanId".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(mrpCalcService, "purchaseSuggestMapper", createProxy(ErpPurchaseSuggestMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertedPurchaseSuggests.add((ErpPurchaseSuggestDO) args[0]);
                return 1;
            }
            if ("deleteByPlanId".equals(methodName)) {
                insertedPurchaseSuggests.clear();
                return 1;
            }
            return null;
        }));
        setField(mrpCalcService, "productionSuggestMapper", createProxy(ErpProductionSuggestMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertedProductionSuggests.add((ErpProductionSuggestDO) args[0]);
                return 1;
            }
            if ("deleteByPlanId".equals(methodName)) {
                insertedProductionSuggests.clear();
                return 1;
            }
            return null;
        }));
        setField(mrpCalcService, "bomMapper", createProxy(ErpBomMapper.class, (methodName, args) -> {
            if ("selectEffectiveByProductId".equals(methodName)) {
                return bomRef.get();
            }
            return null;
        }));
        setField(mrpCalcService, "bomItemMapper", createProxy(ErpBomItemMapper.class, (methodName, args) -> {
            if ("selectListByBomId".equals(methodName)) {
                return bomItemsRef.get();
            }
            return Collections.emptyList();
        }));
        setField(mrpCalcService, "materialPlanRuleResolver",
                createProxy(ErpMaterialPlanRuleResolver.class, (methodName, args) -> {
                    if ("resolveByProductId".equals(methodName)) {
                        return ruleMapRef.get().get(args[0]);
                    }
                    return null;
                }));
        setField(mrpCalcService, "stockMapper", createProxy(ErpStockMapper.class, (methodName, args) -> {
            if ("selectSumByProductId".equals(methodName)) {
                return stockQtyMapRef.get().getOrDefault((Long) args[0], BigDecimal.ZERO);
            }
            return BigDecimal.ZERO;
        }));
        setField(mrpCalcService, "saleOrderMapper", createProxy(ErpSaleOrderMapper.class, (methodName, args) -> {
            if ("selectList".equals(methodName)) {
                return saleOrdersRef.get();
            }
            return null;
        }));
        setField(mrpCalcService, "saleOrderItemMapper", createProxy(ErpSaleOrderItemMapper.class, (methodName, args) -> {
            if ("selectListByOrderIds".equals(methodName)) {
                Set<Long> orderIds = Set.copyOf((java.util.Collection<Long>) args[0]);
                requestedSaleOrderIdsRef.set(orderIds);
                List<ErpSaleOrderItemDO> result = new ArrayList<>();
                for (ErpSaleOrderItemDO item : saleItemsRef.get()) {
                    if (orderIds.contains(item.getOrderId())) {
                        result.add(item);
                    }
                }
                return result;
            }
            return null;
        }));
        setField(mrpCalcService, "purchaseOrderMapper", createProxy(ErpPurchaseOrderMapper.class, (methodName, args) -> {
            if ("selectList".equals(methodName)) {
                return purchaseOrdersRef.get();
            }
            return null;
        }));
        setField(mrpCalcService, "purchaseOrderItemMapper", createProxy(ErpPurchaseOrderItemMapper.class, (methodName, args) -> {
            if ("selectListByOrderIds".equals(methodName)) {
                Set<Long> orderIds = Set.copyOf((java.util.Collection<Long>) args[0]);
                List<ErpPurchaseOrderItemDO> result = new ArrayList<>();
                for (ErpPurchaseOrderItemDO item : purchaseOrderItemsRef.get()) {
                    if (orderIds.contains(item.getOrderId())) {
                        result.add(item);
                    }
                }
                return result;
            }
            return Collections.emptyList();
        }));
        setField(mrpCalcService, "productionOrderMapper", createProxy(ErpProductionOrderMapper.class, (methodName, args) -> {
            if ("selectListForWip".equals(methodName)) {
                return productionOrdersRef.get();
            }
            return null;
        }));
        setField(mrpCalcService, "stockReservationMapper", createProxy(ErpMrpStockReservationMapper.class, (methodName, args) -> {
            if ("selectListBySourceOrderIds".equals(methodName)) {
                Set<Long> orderIds = Set.copyOf((java.util.Collection<Long>) args[0]);
                List<ErpMrpStockReservationDO> result = new ArrayList<>();
                for (ErpMrpStockReservationDO reservation : insertedReservations) {
                    if (orderIds.contains(reservation.getSourceOrderId())) {
                        result.add(reservation);
                    }
                }
                return result;
            }
            if ("insert".equals(methodName)) {
                insertedReservations.add((ErpMrpStockReservationDO) args[0]);
                return 1;
            }
            if ("updateById".equals(methodName)) {
                return 1;
            }
            if ("updateStatusBySourceOrderIds".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(mrpCalcService, "stockReservationSummaryService",
                createProxy(ErpMrpStockReservationSummaryService.class, (methodName, args) -> {
                    if ("getActiveSummaryList".equals(methodName)) {
                        return activeReservationSummariesRef.get();
                    }
                    return null;
                }));
        setField(mrpCalcService, "mrpNettingPolicyResolver",
                createProxy(ErpMrpNettingPolicyResolver.class, (methodName, args) -> {
                    if ("resolve".equals(methodName)) {
                        return ErpMrpNettingRuntimePolicy.standard();
                    }
                    return null;
                }));
        setField(mrpCalcService, "mrpNettingService", new ErpMrpNettingServiceImpl());
        setField(mrpCalcService, "resultComponentService",
                createProxy(ErpMrpResultComponentService.class, (methodName, args) -> null));
        setField(mrpCalcService, "productService", createProxy(ErpProductService.class, (methodName, args) -> {
            if ("getProduct".equals(methodName)) {
                return productMapRef.get().get(args[0]);
            }
            return null;
        }));
        setField(mrpCalcService, "productUnitService", createProxy(ErpProductUnitService.class, (methodName, args) -> {
            if ("getProductUnit".equals(methodName)) {
                return productUnitMapRef.get().get(args[0]);
            }
            return null;
        }));
    }

    @Test
    void run_shouldPreferDeliveryDateAndCarryProjectIdToPurchaseSuggest() {
        planRef.set(new ErpMrpPlanDO().setId(1L)
                .setPlanStartDate(LocalDate.of(2026, 4, 1))
                .setPlanEndDate(LocalDate.of(2026, 4, 30)));
        saleOrdersRef.set(List.of(new ErpSaleOrderDO().setId(10L)
                .setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setProjectId(88L)
                .setOrderTime(LocalDateTime.of(2026, 4, 6, 10, 0))
                .setDeliveryDate(LocalDate.of(2026, 4, 20))));
        saleItemsRef.set(List.of(new ErpSaleOrderItemDO().setId(100L)
                .setOrderId(10L)
                .setProductId(1000L)
                .setCount(new BigDecimal("5"))));

        mrpCalcService.run(1L);

        assertEquals(1, insertedDemands.size());
        assertEquals(LocalDate.of(2026, 4, 20), insertedDemands.get(0).getDemandDate());
        assertEquals(88L, insertedDemands.get(0).getProjectId());

        assertEquals(1, insertedPurchaseSuggests.size());
        assertEquals(88L, insertedPurchaseSuggests.get(0).getProjectId());
        assertEquals(LocalDate.of(2026, 4, 20), insertedPurchaseSuggests.get(0).getSuggestArrivalDate());
        assertDecimalEquals("5", insertedPurchaseSuggests.get(0).getGrossDemandQty());
        assertDecimalEquals("0", insertedPurchaseSuggests.get(0).getAvailableStockQty());
        assertDecimalEquals("0", insertedPurchaseSuggests.get(0).getIncomingQty());
        assertDecimalEquals("0", insertedPurchaseSuggests.get(0).getWipQty());
        assertDecimalEquals("0", insertedPurchaseSuggests.get(0).getSafetyStockQty());
        assertDecimalEquals("5", insertedPurchaseSuggests.get(0).getNetDemandQty());
    }

    @Test
    void run_shouldFallbackToOrderDateAndCarryProjectIdToProductionSuggest() {
        planRef.set(new ErpMrpPlanDO().setId(1L)
                .setPlanStartDate(LocalDate.of(2026, 4, 1))
                .setPlanEndDate(LocalDate.of(2026, 4, 30)));
        saleOrdersRef.set(List.of(new ErpSaleOrderDO().setId(11L)
                .setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setProjectId(99L)
                .setOrderTime(LocalDateTime.of(2026, 4, 8, 9, 30))));
        saleItemsRef.set(List.of(new ErpSaleOrderItemDO().setId(101L)
                .setOrderId(11L)
                .setProductId(1001L)
                .setCount(new BigDecimal("2"))));
        bomRef.set(new ErpBomDO().setId(900L).setProductId(1001L));

        mrpCalcService.run(1L);

        assertEquals(1, insertedDemands.size());
        assertEquals(LocalDate.of(2026, 4, 8), insertedDemands.get(0).getDemandDate());
        assertEquals(99L, insertedDemands.get(0).getProjectId());

        assertEquals(1, insertedProductionSuggests.size());
        assertEquals(99L, insertedProductionSuggests.get(0).getProjectId());
        assertEquals(LocalDate.of(2026, 4, 8), insertedProductionSuggests.get(0).getSuggestEndDate());
        assertDecimalEquals("2", insertedProductionSuggests.get(0).getGrossDemandQty());
        assertDecimalEquals("0", insertedProductionSuggests.get(0).getAvailableStockQty());
        assertDecimalEquals("0", insertedProductionSuggests.get(0).getIncomingQty());
        assertDecimalEquals("0", insertedProductionSuggests.get(0).getWipQty());
        assertDecimalEquals("0", insertedProductionSuggests.get(0).getSafetyStockQty());
        assertDecimalEquals("2", insertedProductionSuggests.get(0).getNetDemandQty());
    }

    @Test
    void runForSaleOrders_shouldOnlyProcessGivenOrders() {
        saleItemsRef.set(List.of(
                new ErpSaleOrderItemDO().setId(201L).setOrderId(21L).setProductId(1002L).setCount(new BigDecimal("3")),
                new ErpSaleOrderItemDO().setId(202L).setOrderId(22L).setProductId(1003L).setCount(new BigDecimal("7"))
        ));

        mrpCalcService.runForSaleOrders(1L, List.of(
                new ErpSaleOrderDO().setId(21L)
                        .setProjectId(77L)
                        .setOrderTime(LocalDateTime.of(2026, 4, 9, 8, 0))
        ));

        assertEquals(Set.of(21L), requestedSaleOrderIdsRef.get());
        assertEquals(1, insertedDemands.size());
        assertEquals(21L, insertedDemands.get(0).getSourceId());
        assertEquals(201L, insertedDemands.get(0).getSourceItemId());
        assertEquals(1002L, insertedDemands.get(0).getProductId());
    }

    @Test
    void run_shouldFilterOrdersByResolvedDemandDate() {
        planRef.set(new ErpMrpPlanDO().setId(1L)
                .setPlanStartDate(LocalDate.of(2026, 4, 1))
                .setPlanEndDate(LocalDate.of(2026, 4, 30)));
        saleOrdersRef.set(List.of(
                new ErpSaleOrderDO().setId(31L)
                        .setProjectId(301L)
                        .setOrderTime(LocalDateTime.of(2026, 4, 5, 10, 0))
                        .setDeliveryDate(LocalDate.of(2026, 5, 3)),
                new ErpSaleOrderDO().setId(32L)
                        .setProjectId(302L)
                        .setOrderTime(LocalDateTime.of(2026, 4, 7, 10, 0))
                        .setDeliveryDate(LocalDate.of(2026, 4, 18))
        ));
        saleItemsRef.set(List.of(
                new ErpSaleOrderItemDO().setId(301L).setOrderId(31L).setProductId(1101L).setCount(new BigDecimal("1")),
                new ErpSaleOrderItemDO().setId(302L).setOrderId(32L).setProductId(1102L).setCount(new BigDecimal("2"))
        ));

        mrpCalcService.run(1L);

        assertEquals(1, insertedDemands.size());
        assertEquals(32L, insertedDemands.get(0).getSourceId());
        assertEquals(LocalDate.of(2026, 4, 18), insertedDemands.get(0).getDemandDate());
        assertEquals(Set.of(32L), requestedSaleOrderIdsRef.get());
    }

    @Test
    void runForSaleOrders_shouldIgnoreIncomingFromOtherProject() {
        purchaseOrdersRef.set(List.of(new ErpPurchaseOrderDO().setId(501L)
                .setStatus(ErpAuditStatus.APPROVE.getStatus())));
        purchaseOrderItemsRef.set(List.of(new ErpPurchaseOrderItemDO().setId(601L)
                .setOrderId(501L)
                .setProductId(2001L)
                .setProjectId(902L)
                .setCount(new BigDecimal("8"))
                .setInCount(BigDecimal.ZERO)));
        saleItemsRef.set(List.of(new ErpSaleOrderItemDO().setId(511L)
                .setOrderId(51L)
                .setProductId(2001L)
                .setCount(new BigDecimal("5"))));

        mrpCalcService.runForSaleOrders(1L, List.of(new ErpSaleOrderDO().setId(51L)
                .setProjectId(901L)
                .setOrderTime(LocalDateTime.of(2026, 4, 10, 9, 0))));

        assertEquals(1, insertedPurchaseSuggests.size());
        assertDecimalEquals("0", insertedPurchaseSuggests.get(0).getIncomingQty());
        assertDecimalEquals("5", insertedPurchaseSuggests.get(0).getNetDemandQty());
    }

    @Test
    void runForSaleOrders_shouldIgnoreWipFromOtherProject() {
        productionOrdersRef.set(List.of(new ErpProductionOrderDO().setId(701L)
                .setProductId(2002L)
                .setProjectId(903L)
                .setPlanQty(new BigDecimal("9"))
                .setFinishedQty(new BigDecimal("1"))));
        saleItemsRef.set(List.of(new ErpSaleOrderItemDO().setId(521L)
                .setOrderId(52L)
                .setProductId(2002L)
                .setCount(new BigDecimal("4"))));

        mrpCalcService.runForSaleOrders(1L, List.of(new ErpSaleOrderDO().setId(52L)
                .setProjectId(904L)
                .setOrderTime(LocalDateTime.of(2026, 4, 10, 9, 30))));

        assertEquals(1, insertedPurchaseSuggests.size());
        assertDecimalEquals("0", insertedPurchaseSuggests.get(0).getWipQty());
        assertDecimalEquals("4", insertedPurchaseSuggests.get(0).getNetDemandQty());
    }

    @Test
    void runForSaleOrders_shouldApplyFixedLotRule() {
        Map<Long, ErpMaterialPlanRuleDO> ruleMap = new HashMap<>();
        ruleMap.put(2100L, ErpMaterialPlanRuleDO.builder()
                .productId(2100L)
                .supplyType("PURCHASE")
                .replenishMode(ErpMaterialPlanReplenishModeEnum.FIXED_LOT.getMode())
                .fixedOrderQty(new BigDecimal("6"))
                .orderMultiple(BigDecimal.ONE)
                .purchaseLeadDay(3)
                .enableFlag(Boolean.TRUE)
                .shortageWarnFlag(Boolean.TRUE)
                .build());
        ruleMapRef.set(ruleMap);
        saleItemsRef.set(List.of(new ErpSaleOrderItemDO().setId(551L)
                .setOrderId(55L)
                .setProductId(2100L)
                .setCount(new BigDecimal("7"))));

        mrpCalcService.runForSaleOrders(1L, List.of(new ErpSaleOrderDO().setId(55L)
                .setProjectId(907L)
                .setOrderTime(LocalDateTime.of(2026, 4, 10, 12, 0))));

        assertEquals(1, insertedPurchaseSuggests.size());
        assertDecimalEquals("12", insertedPurchaseSuggests.get(0).getSuggestQty());
        assertDecimalEquals("12", insertedPurchaseSuggests.get(0).getNetDemandQty());
    }

    @Test
    void runForSaleOrders_shouldKeepTheoreticalDecimalForBomExplosionWhileRoundingDiscreteExecutionQty() throws Exception {
        Map<Long, ErpProductDO> productMap = new HashMap<>();
        productMap.put(4100L, new ErpProductDO().setId(4100L).setUnitId(1L));
        productMap.put(4101L, new ErpProductDO().setId(4101L).setUnitId(1L));
        productMap.put(4102L, new ErpProductDO().setId(4102L).setUnitId(1L));
        productMapRef.set(productMap);
        Map<Long, ErpProductUnitDO> productUnitMap = new HashMap<>();
        productUnitMap.put(1L, new ErpProductUnitDO().setId(1L).setName("套"));
        productUnitMapRef.set(productUnitMap);

        saleItemsRef.set(List.of(new ErpSaleOrderItemDO().setId(711L)
                .setOrderId(71L)
                .setProductId(4100L)
                .setCount(BigDecimal.ONE)));

        Map<Long, ErpBomDO> bomMap = new HashMap<>();
        bomMap.put(4100L, new ErpBomDO().setId(9100L).setProductId(4100L));
        bomMap.put(4101L, new ErpBomDO().setId(9101L).setProductId(4101L));
        setField(mrpCalcService, "bomMapper", createProxy(ErpBomMapper.class, (methodName, args) -> {
            if ("selectEffectiveByProductId".equals(methodName)) {
                return bomMap.get(args[0]);
            }
            return null;
        }));
        setField(mrpCalcService, "bomItemMapper", createProxy(ErpBomItemMapper.class, (methodName, args) -> {
            if ("selectListByBomId".equals(methodName)) {
                Long bomId = (Long) args[0];
                if (Objects.equals(9100L, bomId)) {
                    return List.of(buildBomItem(9100L, 4101L, "1", null, true)
                            .setMaterialType(1)
                            .setLossRate(new BigDecimal("0.015")));
                }
                if (Objects.equals(9101L, bomId)) {
                    return List.of(buildBomItem(9101L, 4102L, "1", null, true).setLossRate(new BigDecimal("0.015")));
                }
            }
            return Collections.emptyList();
        }));

        mrpCalcService.runForSaleOrders(1L, List.of(new ErpSaleOrderDO().setId(71L)
                .setProjectId(1201L)
                .setOrderTime(LocalDateTime.of(2026, 4, 12, 9, 0))));

        assertDecimalEquals("1", findResult(4100L).getNetDemandQty());
        assertDecimalEquals("2", findResult(4101L).getNetDemandQty());
        assertDecimalEquals("1.015000", findResult(4101L).getGrossDemandQty());
        assertDecimalEquals("1.030225", findResult(4102L).getGrossDemandQty());
        assertDecimalEquals("2", findResult(4102L).getNetDemandQty());

        assertEquals(2, insertedProductionSuggests.size());
        assertDecimalEquals("2", insertedProductionSuggests.get(1).getSuggestQty());
        assertEquals(1, insertedPurchaseSuggests.size());
        assertDecimalEquals("2", insertedPurchaseSuggests.get(0).getSuggestQty());
    }

    @Test
    void runForSaleOrders_shouldReserveGlobalStockAcrossRuns() {
        Map<Long, BigDecimal> stockMap = new HashMap<>();
        stockMap.put(2003L, new BigDecimal("10"));
        stockQtyMapRef.set(stockMap);
        saleItemsRef.set(List.of(new ErpSaleOrderItemDO().setId(531L)
                .setOrderId(53L)
                .setProductId(2003L)
                .setCount(new BigDecimal("6"))));

        mrpCalcService.runForSaleOrders(1L, List.of(new ErpSaleOrderDO().setId(53L)
                .setProjectId(905L)
                .setOrderTime(LocalDateTime.of(2026, 4, 10, 10, 0))));

        assertEquals(1, insertedReservations.size());
        assertDecimalEquals("6", insertedReservations.get(0).getReservedQty());
        assertEquals(53L, insertedReservations.get(0).getSourceOrderId());
        assertEquals(0, insertedPurchaseSuggests.size());

        activeReservationSummariesRef.set(List.of(new ErpMrpStockReservationSummaryDO()
                .setProductId(2003L)
                .setActiveReservedQty(new BigDecimal("6"))));
        insertedDemands.clear();
        insertedResults.clear();
        insertedPurchaseSuggests.clear();
        insertedProductionSuggests.clear();
        saleItemsRef.set(List.of(new ErpSaleOrderItemDO().setId(541L)
                .setOrderId(54L)
                .setProductId(2003L)
                .setCount(new BigDecimal("5"))));

        mrpCalcService.runForSaleOrders(2L, List.of(new ErpSaleOrderDO().setId(54L)
                .setProjectId(906L)
                .setOrderTime(LocalDateTime.of(2026, 4, 10, 11, 0))));

        assertEquals(1, insertedPurchaseSuggests.size());
        assertDecimalEquals("4", insertedPurchaseSuggests.get(0).getAvailableStockQty());
        assertDecimalEquals("1", insertedPurchaseSuggests.get(0).getNetDemandQty());
        assertDecimalEquals("4", insertedPurchaseSuggests.get(0).getReservedStockQty());
    }

    @Test
    void runForSaleOrders_shouldRejectOrdersWithoutAnyDemandDate() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                mrpCalcService.runForSaleOrders(1L, List.of(
                        new ErpSaleOrderDO().setId(41L).setProjectId(401L)
                )));

        assertEquals("Sale order 41 must have deliveryDate or orderTime", exception.getMessage());
    }

    @Test
    void runForSaleOrders_shouldSkipCustomerOwnedBomItemForCustomerSuppliedBusiness() {
        bomRef.set(new ErpBomDO().setId(901L).setProductId(3000L));
        bomItemsRef.set(List.of(buildBomItem(901L, 3001L, "1", "CUSTOMER", true)));
        saleItemsRef.set(List.of(new ErpSaleOrderItemDO().setId(611L)
                .setOrderId(61L)
                .setProductId(3000L)
                .setCount(new BigDecimal("2"))));

        mrpCalcService.runForSaleOrders(1L, List.of(new ErpSaleOrderDO().setId(61L)
                .setProjectId(1001L)
                .setBusinessType(ErpBusinessTypeConstants.CUSTOMER_SUPPLIED)
                .setOrderTime(LocalDateTime.of(2026, 4, 11, 9, 0))));

        assertEquals(1, insertedProductionSuggests.size());
        assertEquals(0, insertedPurchaseSuggests.size());
        ErpMrpResultDO childResult = findResult(3001L);
        assertEquals(BigDecimal.ZERO, childResult.getNetDemandQty());
        assertEquals(ErpBusinessTypeConstants.CUSTOMER_SUPPLIED, readOptionalField(childResult, "businessType"));
        assertEquals("CUSTOMER", readOptionalField(childResult, "supplyOwner"));
        assertEquals("CUSTOMER_SUPPLIED_CUSTOMER_OWNED", readOptionalField(childResult, "skipReason"));
    }

    @Test
    void runForSaleOrders_shouldSkipBomItemWhenMrpDisabled() {
        bomRef.set(new ErpBomDO().setId(902L).setProductId(3100L));
        bomItemsRef.set(List.of(buildBomItem(902L, 3101L, "1", "COMPANY", false)));
        saleItemsRef.set(List.of(new ErpSaleOrderItemDO().setId(621L)
                .setOrderId(62L)
                .setProductId(3100L)
                .setCount(new BigDecimal("3"))));

        mrpCalcService.runForSaleOrders(1L, List.of(new ErpSaleOrderDO().setId(62L)
                .setProjectId(1002L)
                .setBusinessType(ErpBusinessTypeConstants.SELF_RESEARCH)
                .setOrderTime(LocalDateTime.of(2026, 4, 11, 10, 0))));

        assertEquals(1, insertedProductionSuggests.size());
        assertEquals(0, insertedPurchaseSuggests.size());
        ErpMrpResultDO childResult = findResult(3101L);
        assertEquals(BigDecimal.ZERO, childResult.getNetDemandQty());
        assertEquals(Boolean.FALSE, readOptionalField(childResult, "mrpEnableFlag"));
        assertEquals("MRP_DISABLED", readOptionalField(childResult, "skipReason"));
    }

    @Test
    void runForSaleOrders_shouldDefaultSkipBomItemForTollManufacturing() {
        bomRef.set(new ErpBomDO().setId(903L).setProductId(3200L));
        bomItemsRef.set(List.of(buildBomItem(903L, 3201L, "1", null, true)));
        saleItemsRef.set(List.of(new ErpSaleOrderItemDO().setId(631L)
                .setOrderId(63L)
                .setProductId(3200L)
                .setCount(new BigDecimal("4"))));

        mrpCalcService.runForSaleOrders(1L, List.of(new ErpSaleOrderDO().setId(63L)
                .setProjectId(1003L)
                .setBusinessType(ErpBusinessTypeConstants.TOLL_MANUFACTURING)
                .setOrderTime(LocalDateTime.of(2026, 4, 11, 11, 0))));

        assertEquals(1, insertedProductionSuggests.size());
        assertEquals(0, insertedPurchaseSuggests.size());
        ErpMrpResultDO childResult = findResult(3201L);
        assertEquals(BigDecimal.ZERO, childResult.getNetDemandQty());
        assertEquals(ErpBusinessTypeConstants.TOLL_MANUFACTURING, readOptionalField(childResult, "businessType"));
        assertEquals("TOLL_MANUFACTURING_DEFAULT_SKIP", readOptionalField(childResult, "skipReason"));
    }

    @Test
    void runForSaleOrders_shouldKeepSameMaterialTraceSeparatedAcrossBomBranches() throws Exception {
        Map<Long, ErpBomDO> bomMap = new HashMap<>();
        bomMap.put(5000L, new ErpBomDO().setId(9500L).setProductId(5000L));
        bomMap.put(5100L, new ErpBomDO().setId(9510L).setProductId(5100L));
        bomMap.put(5200L, new ErpBomDO().setId(9520L).setProductId(5200L));
        setField(mrpCalcService, "bomMapper", createProxy(ErpBomMapper.class, (methodName, args) -> {
            if ("selectEffectiveByProductId".equals(methodName)) {
                return bomMap.get(args[0]);
            }
            return null;
        }));
        setField(mrpCalcService, "bomItemMapper", createProxy(ErpBomItemMapper.class, (methodName, args) -> {
            if ("selectListByBomId".equals(methodName)) {
                Long bomId = (Long) args[0];
                if (Objects.equals(9500L, bomId)) {
                    return List.of(
                            buildBomItem(9500L, 5100L, "1", null, true).setMaterialType(1),
                            buildBomItem(9500L, 5200L, "1", null, true).setMaterialType(1)
                    );
                }
                if (Objects.equals(9510L, bomId)) {
                    return List.of(buildBomItem(9510L, 5300L, "1", null, true));
                }
                if (Objects.equals(9520L, bomId)) {
                    return List.of(buildBomItem(9520L, 5300L, "1", null, true));
                }
            }
            return Collections.emptyList();
        }));
        saleItemsRef.set(List.of(new ErpSaleOrderItemDO().setId(651L)
                .setOrderId(65L)
                .setProductId(5000L)
                .setCount(new BigDecimal("2"))));

        mrpCalcService.runForSaleOrders(1L, List.of(new ErpSaleOrderDO().setId(65L)
                .setProjectId(1101L)
                .setOrderTime(LocalDateTime.of(2026, 4, 12, 14, 0))));

        List<ErpMrpTraceNodeDO> sameMaterialNodes = insertedTraceNodes.stream()
                .filter(item -> Objects.equals(item.getMaterialId(), 5300L))
                .collect(Collectors.toList());
        assertEquals(2, sameMaterialNodes.size());
        assertNotEquals(sameMaterialNodes.get(0).getTracePathKey(), sameMaterialNodes.get(1).getTracePathKey());
        assertEquals(Set.of(5100L, 5200L), sameMaterialNodes.stream()
                .map(ErpMrpTraceNodeDO::getParentMaterialId)
                .collect(Collectors.toSet()));
    }

    @Test
    void runForSaleOrders_shouldReplacePreviousTraceDatasetOnRerun() {
        saleItemsRef.set(List.of(new ErpSaleOrderItemDO().setId(661L)
                .setOrderId(66L)
                .setProductId(5400L)
                .setCount(new BigDecimal("3"))));

        mrpCalcService.runForSaleOrders(1L, List.of(new ErpSaleOrderDO().setId(66L)
                .setProjectId(1202L)
                .setOrderTime(LocalDateTime.of(2026, 4, 13, 9, 0))));

        saleItemsRef.set(List.of(new ErpSaleOrderItemDO().setId(662L)
                .setOrderId(67L)
                .setProductId(5401L)
                .setCount(new BigDecimal("4"))));

        mrpCalcService.runForSaleOrders(1L, List.of(new ErpSaleOrderDO().setId(67L)
                .setProjectId(1202L)
                .setOrderTime(LocalDateTime.of(2026, 4, 14, 9, 0))));

        assertEquals(Set.of(5401L), insertedTraceNodes.stream()
                .map(ErpMrpTraceNodeDO::getMaterialId)
                .collect(Collectors.toSet()));
        assertEquals(Set.of(5401L), insertedResults.stream()
                .map(ErpMrpResultDO::getMaterialId)
                .collect(Collectors.toSet()));
        assertEquals(Set.of(5401L), insertedPurchaseSuggests.stream()
                .map(ErpPurchaseSuggestDO::getMaterialId)
                .collect(Collectors.toSet()));
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> type, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        if ("toString".equals(method.getName())) {
                            return type.getSimpleName() + "Proxy";
                        }
                        if ("hashCode".equals(method.getName())) {
                            return System.identityHashCode(proxy);
                        }
                        if ("equals".equals(method.getName())) {
                            return proxy == args[0];
                        }
                    }
                    return handler.handle(method.getName(), args);
                });
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> type, String fieldName) throws NoSuchFieldException {
        for (String candidate : resolveFieldCandidates(fieldName)) {
            try {
                return type.getDeclaredField(candidate);
            } catch (NoSuchFieldException ignored) {
                // try next candidate
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private String[] resolveFieldCandidates(String fieldName) {
        return switch (fieldName) {
            case "demandMapper" -> new String[]{fieldName, "erpMrpDemandMapper"};
            case "resultMapper" -> new String[]{fieldName, "erpMrpResultMapper"};
            case "shortageMapper" -> new String[]{fieldName, "erpMrpShortageMapper"};
            case "stockReservationMapper" -> new String[]{fieldName, "erpMrpStockReservationMapper"};
            default -> fieldName.startsWith("erp")
                    ? new String[]{fieldName}
                    : new String[]{fieldName, "erp" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1)};
        };
    }

    private ErpBomItemDO buildBomItem(Long bomId, Long materialId, String usageQty, String supplyOwner, Boolean mrpEnableFlag) {
        ErpBomItemDO item = new ErpBomItemDO()
                .setBomId(bomId)
                .setMaterialId(materialId)
                .setMaterialType(0)
                .setUsageQty(new BigDecimal(usageQty))
                .setLossRate(BigDecimal.ZERO)
                .setLeadTimeDay(0);
        setOptionalField(item, "supplyOwner", supplyOwner);
        setOptionalField(item, "mrpEnableFlag", mrpEnableFlag);
        return item;
    }

    private ErpMrpResultDO findResult(Long materialId) {
        return insertedResults.stream()
                .filter(result -> materialId.equals(result.getMaterialId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing result for material " + materialId));
    }

    private void assertDecimalEquals(String expected, BigDecimal actual) {
        assertTrue(new BigDecimal(expected).compareTo(actual) == 0,
                () -> "expected " + expected + " but was " + actual);
    }

    private Object readOptionalField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (NoSuchFieldException ex) {
            throw new AssertionError("Missing field " + fieldName + " on " + target.getClass().getSimpleName(), ex);
        } catch (IllegalAccessException ex) {
            throw new AssertionError("Cannot read field " + fieldName, ex);
        }
    }

    private void setOptionalField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException ex) {
            throw new AssertionError("Missing field " + fieldName + " on " + target.getClass().getSimpleName(), ex);
        } catch (IllegalAccessException ex) {
            throw new AssertionError("Cannot set field " + fieldName, ex);
        }
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }

}
