package cn.iocoder.yudao.module.erp.service.sale;

import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionFinishQualityDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionSuggestDO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class ErpSaleOrderClosureServiceImplTest {

    private final AtomicReference<ErpSaleOrderDO> saleOrderRef = new AtomicReference<>();
    private final AtomicReference<List<ErpSaleOrderDO>> saleOrdersRef = new AtomicReference<>(List.of());
    private final AtomicReference<List<ErpPurchaseSuggestDO>> purchaseSuggestsRef = new AtomicReference<>(List.of());
    private final AtomicReference<List<ErpProductionSuggestDO>> productionSuggestsRef = new AtomicReference<>(List.of());
    private final AtomicReference<List<ErpPurchaseOrderDO>> purchaseOrdersRef = new AtomicReference<>(List.of());
    private final AtomicReference<List<ErpPurchaseInDO>> purchaseInsRef = new AtomicReference<>(List.of());
    private final AtomicReference<List<ErpProductionFinishQualityDO>> finishQualitiesRef = new AtomicReference<>(List.of());

    private ErpSaleOrderClosureServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new ErpSaleOrderClosureServiceImpl();
        saleOrderRef.set(null);
        saleOrdersRef.set(List.of());
        purchaseSuggestsRef.set(List.of());
        productionSuggestsRef.set(List.of());
        purchaseOrdersRef.set(List.of());
        purchaseInsRef.set(List.of());
        finishQualitiesRef.set(List.of());

        setField(service, "saleOrderMapper", createProxy(ErpSaleOrderMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return saleOrderRef.get();
            }
            if ("selectByIds".equals(methodName)) {
                if (!saleOrdersRef.get().isEmpty()) {
                    return saleOrdersRef.get();
                }
                return saleOrderRef.get() == null ? List.of() : List.of(saleOrderRef.get());
            }
            return null;
        }));
        setField(service, "purchaseSuggestMapper", createProxy(ErpPurchaseSuggestMapper.class, (methodName, args) -> {
            if ("selectListBySourceOrderIds".equals(methodName)) {
                return purchaseSuggestsRef.get();
            }
            return null;
        }));
        setField(service, "productionSuggestMapper", createProxy(ErpProductionSuggestMapper.class, (methodName, args) -> {
            if ("selectListBySourceOrderIds".equals(methodName)) {
                return productionSuggestsRef.get();
            }
            return null;
        }));
        setField(service, "purchaseOrderMapper", createProxy(ErpPurchaseOrderMapper.class, (methodName, args) -> {
            if ("selectByIds".equals(methodName)) {
                return purchaseOrdersRef.get();
            }
            return null;
        }));
        setField(service, "purchaseInMapper", createProxy(ErpPurchaseInMapper.class, (methodName, args) -> {
            if ("selectListByOrderIds".equals(methodName)) {
                return purchaseInsRef.get();
            }
            return null;
        }));
        setField(service, "productionFinishQualityMapper", createProxy(ErpProductionFinishQualityMapper.class, (methodName, args) -> {
            if ("selectListBySourceOrderIds".equals(methodName)) {
                return finishQualitiesRef.get();
            }
            return null;
        }));
    }

    @Test
    void getClosureSummary_shouldResolvePrimaryPurchaseBlockers() {
        saleOrderRef.set(buildApprovedSaleOrder()
                .setDeliveryReadyStatus(ErpSaleOrderDeliveryReadyStatusEnum.NOT_READY.getStatus()));
        purchaseSuggestsRef.set(List.of(
                purchaseSuggest(11L, ErpMrpSuggestStatusEnum.TO_CONFIRM.getStatus(), null),
                purchaseSuggest(12L, ErpMrpSuggestStatusEnum.CONFIRMED.getStatus(), null)
        ));

        ErpSaleOrderClosureSummaryBO summary = service.getClosureSummary(1L);

        assertEquals(2L, summary.getPurchaseSuggestCount());
        assertEquals(1L, summary.getPurchaseConfirmedSuggestCount());
        assertEquals(0L, summary.getPurchaseConvertedSuggestCount());
        assertEquals(ErpSaleOrderClosureStageEnum.WAIT_PURCHASE_SUGGEST_CONFIRM.getStage(), summary.getClosureStage());
        assertIterableEquals(List.of(
                ErpSaleOrderClosureStageEnum.WAIT_PURCHASE_SUGGEST_CONFIRM.getStage(),
                ErpSaleOrderClosureStageEnum.WAIT_PURCHASE_ORDER_CONVERT.getStage(),
                ErpSaleOrderClosureStageEnum.WAIT_DELIVERY_READY.getStage()
        ), summary.getBlockerCodes());
    }

    @Test
    void getClosureSummary_shouldResolvePendingInboundQualityAndStockIn() {
        saleOrderRef.set(buildApprovedSaleOrder()
                .setDeliveryReadyStatus(ErpSaleOrderDeliveryReadyStatusEnum.NOT_READY.getStatus()));
        purchaseSuggestsRef.set(List.of(
                purchaseSuggest(11L, ErpMrpSuggestStatusEnum.CONVERTED.getStatus(), 101L)
        ));
        purchaseOrdersRef.set(List.of(
                purchaseOrder(101L, ErpAuditStatus.APPROVE.getStatus(), "10", "0")
        ));
        purchaseInsRef.set(List.of(
                purchaseIn(201L, 101L, ErpAuditStatus.APPROVE.getStatus(),
                        ErpQaStatusEnum.TO_INSPECT.getStatus(), null, "0")
        ));

        ErpSaleOrderClosureSummaryBO summary = service.getClosureSummary(1L);

        assertEquals(1L, summary.getApprovedPurchaseOrderCount());
        assertEquals(1L, summary.getApprovedPurchaseInCount());
        assertEquals(1L, summary.getPendingQaPurchaseInCount());
        assertEquals(ErpSaleOrderClosureStageEnum.WAIT_PURCHASE_IQC.getStage(), summary.getClosureStage());
        assertIterableEquals(List.of(
                ErpSaleOrderClosureStageEnum.WAIT_PURCHASE_IQC.getStage(),
                ErpSaleOrderClosureStageEnum.WAIT_DELIVERY_READY.getStage()
        ), summary.getBlockerCodes());
    }

    @Test
    void getClosureSummary_shouldResolveReadyToShipWhenAllChainsClosed() {
        saleOrderRef.set(buildApprovedSaleOrder()
                .setDeliveryReadyStatus(ErpSaleOrderDeliveryReadyStatusEnum.READY_TO_SHIP.getStatus()));
        purchaseSuggestsRef.set(List.of(
                purchaseSuggest(11L, ErpMrpSuggestStatusEnum.CONVERTED.getStatus(), 101L)
        ));
        productionSuggestsRef.set(List.of(
                productionSuggest(21L, ErpMrpSuggestStatusEnum.CONVERTED.getStatus())
        ));
        purchaseOrdersRef.set(List.of(
                purchaseOrder(101L, ErpAuditStatus.APPROVE.getStatus(), "10", "10")
        ));
        purchaseInsRef.set(List.of(
                purchaseIn(201L, 101L, ErpAuditStatus.APPROVE.getStatus(),
                        ErpQaStatusEnum.PASSED.getStatus(), ErpPurchaseInStockInStatusEnum.STOCKED_IN.getStatus(), "10")
        ));
        finishQualitiesRef.set(List.of(
                ErpProductionFinishQualityDO.builder()
                        .id(301L)
                        .sourceOrderId(1L)
                        .qualifiedQty(new BigDecimal("10"))
                        .status(ErpQaStatusEnum.PASSED.getStatus())
                        .build()
        ));

        ErpSaleOrderClosureSummaryBO summary = service.getClosureSummary(1L);

        assertEquals(ErpSaleOrderClosureStageEnum.READY_TO_SHIP.getStage(), summary.getClosureStage());
        assertEquals(List.of(), summary.getBlockerCodes());
        assertEquals(new BigDecimal("10"), summary.getProductionQualifiedQty());
        assertEquals(new BigDecimal("10"), summary.getRemainingShipQty());
        assertEquals(1L, summary.getStockedPurchaseInCount());
    }

    @Test
    void getClosureSummaryMap_shouldBuildBatchSummaries() {
        saleOrdersRef.set(List.of(
                buildApprovedSaleOrder().setId(1L).setNo("SO-001")
                        .setDeliveryReadyStatus(ErpSaleOrderDeliveryReadyStatusEnum.NOT_READY.getStatus()),
                buildApprovedSaleOrder().setId(2L).setNo("SO-002")
                        .setDeliveryReadyStatus(ErpSaleOrderDeliveryReadyStatusEnum.READY_TO_SHIP.getStatus())
        ));
        purchaseSuggestsRef.set(List.of(
                ErpPurchaseSuggestDO.builder().id(11L).sourceOrderId(1L)
                        .status(ErpMrpSuggestStatusEnum.TO_CONFIRM.getStatus())
                        .suggestQty(new BigDecimal("5")).build()
        ));

        Map<Long, ErpSaleOrderClosureSummaryBO> summaryMap = service.getClosureSummaryMap(List.of(1L, 2L));

        assertEquals(2, summaryMap.size());
        assertEquals(ErpSaleOrderClosureStageEnum.WAIT_PURCHASE_SUGGEST_CONFIRM.getStage(),
                summaryMap.get(1L).getClosureStage());
        assertEquals(ErpSaleOrderClosureStageEnum.READY_TO_SHIP.getStage(),
                summaryMap.get(2L).getClosureStage());
    }

    private ErpSaleOrderDO buildApprovedSaleOrder() {
        return new ErpSaleOrderDO()
                .setId(1L)
                .setNo("SO-001")
                .setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setTotalCount(new BigDecimal("10"))
                .setOutCount(BigDecimal.ZERO)
                .setReturnCount(BigDecimal.ZERO);
    }

    private ErpPurchaseSuggestDO purchaseSuggest(Long id, Integer status, Long purchaseOrderId) {
        return ErpPurchaseSuggestDO.builder()
                .id(id)
                .sourceOrderId(1L)
                .status(status)
                .convertPurchaseOrderId(purchaseOrderId)
                .suggestQty(new BigDecimal("5"))
                .build();
    }

    private ErpProductionSuggestDO productionSuggest(Long id, Integer status) {
        return ErpProductionSuggestDO.builder()
                .id(id)
                .sourceOrderId(1L)
                .status(status)
                .suggestQty(new BigDecimal("10"))
                .build();
    }

    private ErpPurchaseOrderDO purchaseOrder(Long id, Integer status, String totalCount, String inCount) {
        return new ErpPurchaseOrderDO()
                .setId(id)
                .setStatus(status)
                .setTotalCount(new BigDecimal(totalCount))
                .setInCount(new BigDecimal(inCount));
    }

    private ErpPurchaseInDO purchaseIn(Long id, Long orderId, Integer status, Integer qaStatus,
                                       Integer stockInStatus, String stockInCount) {
        return new ErpPurchaseInDO()
                .setId(id)
                .setOrderId(orderId)
                .setStatus(status)
                .setQaStatus(qaStatus)
                .setStockInStatus(stockInStatus)
                .setStockInCount(new BigDecimal(stockInCount));
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
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }
}
