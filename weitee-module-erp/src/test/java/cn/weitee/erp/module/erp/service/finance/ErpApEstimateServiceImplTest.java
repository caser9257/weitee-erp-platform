package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.module.erp.controller.admin.finance.vo.apestimate.ErpApEstimateActionReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apestimate.ErpApEstimateScanReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApEstimateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApEstimateItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpApEstimateItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpApEstimateMapper;
import cn.weitee.erp.module.erp.dal.mysql.finance.ErpApStatementMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInMapper;
import cn.weitee.erp.module.erp.enums.ErpApEstimateReverseTypeEnum;
import cn.weitee.erp.module.erp.enums.ErpApEstimateStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpApInvoiceStatusEnum;
import cn.weitee.erp.module.erp.enums.ErpApStatementStatusEnum;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseOrderService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ErpApEstimateServiceImplTest {

    @Test
    void generateMonthEstimate_shouldCreateEstimateAndItems() throws Exception {
        ErpApEstimateServiceImpl service = new ErpApEstimateServiceImpl();
        ErpApStatementDO statement = new ErpApStatementDO()
                .setId(1L)
                .setBizType(ErpBizTypeEnum.PURCHASE_IN.getType())
                .setBizId(11L)
                .setBizNo("RK-001")
                .setBizDate(LocalDateTime.of(2026, 4, 12, 10, 0))
                .setInvoiceStatus(ErpApInvoiceStatusEnum.NONE.getStatus())
                .setStatus(ErpApStatementStatusEnum.UNPAID.getStatus());
        ErpPurchaseInDO purchaseIn = new ErpPurchaseInDO()
                .setId(11L)
                .setNo("RK-001")
                .setOrderId(21L)
                .setOrderNo("PO-001")
                .setSupplierId(31L)
                .setAccountId(41L)
                .setTotalProductPrice(new BigDecimal("100.00"))
                .setDiscountPrice(new BigDecimal("10.00"))
                .setOtherPrice(new BigDecimal("5.00"))
                .setRemark("purchase in");
        List<ErpPurchaseInItemDO> purchaseInItems = List.of(
                new ErpPurchaseInItemDO()
                        .setId(1001L)
                        .setInId(11L)
                        .setOrderItemId(201L)
                        .setWarehouseId(301L)
                        .setProductId(401L)
                        .setCount(new BigDecimal("6"))
                        .setTotalPrice(new BigDecimal("60.00"))
                        .setTaxPrice(new BigDecimal("6.00"))
                        .setRemark("item-1"),
                new ErpPurchaseInItemDO()
                        .setId(1002L)
                        .setInId(11L)
                        .setOrderItemId(202L)
                        .setWarehouseId(302L)
                        .setProductId(402L)
                        .setCount(new BigDecimal("4"))
                        .setTotalPrice(new BigDecimal("40.00"))
                        .setTaxPrice(new BigDecimal("4.00"))
                        .setRemark("item-2"));
        ErpPurchaseOrderDO purchaseOrder = new ErpPurchaseOrderDO()
                .setId(21L)
                .setNo("PO-001");
        List<ErpPurchaseOrderItemDO> purchaseOrderItems = List.of(
                new ErpPurchaseOrderItemDO().setId(201L).setOrderId(21L).setProjectId(501L),
                new ErpPurchaseOrderItemDO().setId(202L).setOrderId(21L).setProjectId(502L));
        AtomicReference<ErpApEstimateDO> insertedEstimateRef = new AtomicReference<>();
        AtomicReference<List<ErpApEstimateItemDO>> insertedEstimateItemsRef = new AtomicReference<>();

        setField(service, "apStatementMapper", createProxy(ErpApStatementMapper.class, (methodName, args) -> {
            if ("selectList".equals(methodName)) {
                return List.of(statement);
            }
            return null;
        }));
        setField(service, "apEstimateMapper", createProxy(ErpApEstimateMapper.class, (methodName, args) -> {
            if ("selectBySourceBizTypeAndSourceBizId".equals(methodName)) {
                return null;
            }
            if ("insert".equals(methodName)) {
                ErpApEstimateDO estimate = (ErpApEstimateDO) args[0];
                estimate.setId(9001L);
                insertedEstimateRef.set(estimate);
                return 1;
            }
            return null;
        }));
        setField(service, "apEstimateItemMapper", createProxy(ErpApEstimateItemMapper.class, (methodName, args) -> {
            if ("insertBatch".equals(methodName)) {
                insertedEstimateItemsRef.set(new ArrayList<>((List<ErpApEstimateItemDO>) args[0]));
                return true;
            }
            return null;
        }));
        setField(service, "purchaseInMapper", createProxy(ErpPurchaseInMapper.class, (methodName, args) -> {
            if ("selectBatchIds".equals(methodName)) {
                return List.of(purchaseIn);
            }
            return null;
        }));
        setField(service, "purchaseInItemMapper", createProxy(ErpPurchaseInItemMapper.class, (methodName, args) -> {
            if ("selectList".equals(methodName)) {
                return purchaseInItems;
            }
            return null;
        }));
        setField(service, "purchaseOrderService", createProxy(ErpPurchaseOrderService.class, (methodName, args) -> {
            if ("getPurchaseOrderList".equals(methodName)) {
                return List.of(purchaseOrder);
            }
            if ("getPurchaseOrderItemListByOrderIds".equals(methodName)) {
                return purchaseOrderItems;
            }
            return null;
        }));

        int generatedCount = service.generateMonthEstimate(new ErpApEstimateScanReqVO().setEstimateMonth("2026-04"));

        assertEquals(1, generatedCount);
        assertNotNull(insertedEstimateRef.get());
        assertEquals("ZG-RK-001", insertedEstimateRef.get().getEstimateNo());
        assertEquals(21L, insertedEstimateRef.get().getSourceOrderId());
        assertEquals("PO-001", insertedEstimateRef.get().getSourceOrderNo());
        assertEquals(2, insertedEstimateItemsRef.get().size());
        assertEquals(new BigDecimal("95.00"), insertedEstimateRef.get().getAmount().stripTrailingZeros().setScale(2));
        assertEquals(new BigDecimal("57.000000"), insertedEstimateItemsRef.get().get(0).getAmount());
        assertEquals(new BigDecimal("38.000000"), insertedEstimateItemsRef.get().get(1).getAmount());
    }

    @Test
    void generateMonthEstimate_shouldSkipExistingEstimate() throws Exception {
        ErpApEstimateServiceImpl service = new ErpApEstimateServiceImpl();
        ErpApStatementDO statement = new ErpApStatementDO()
                .setId(1L)
                .setBizType(ErpBizTypeEnum.PURCHASE_IN.getType())
                .setBizId(11L)
                .setBizNo("RK-001")
                .setBizDate(LocalDateTime.of(2026, 4, 12, 10, 0))
                .setInvoiceStatus(ErpApInvoiceStatusEnum.NONE.getStatus())
                .setStatus(ErpApStatementStatusEnum.UNPAID.getStatus());
        AtomicInteger insertCount = new AtomicInteger();

        setField(service, "apStatementMapper", createProxy(ErpApStatementMapper.class, (methodName, args) -> {
            if ("selectList".equals(methodName)) {
                return List.of(statement);
            }
            return null;
        }));
        setField(service, "apEstimateMapper", createProxy(ErpApEstimateMapper.class, (methodName, args) -> {
            if ("selectBySourceBizTypeAndSourceBizId".equals(methodName)) {
                return new ErpApEstimateDO().setId(9001L);
            }
            if ("insert".equals(methodName)) {
                insertCount.incrementAndGet();
            }
            return null;
        }));
        setField(service, "apEstimateItemMapper", createProxy(ErpApEstimateItemMapper.class, (methodName, args) -> null));
        setField(service, "purchaseInMapper", createProxy(ErpPurchaseInMapper.class, (methodName, args) -> {
            if ("selectBatchIds".equals(methodName)) {
                return List.of(new ErpPurchaseInDO().setId(11L).setNo("RK-001"));
            }
            return null;
        }));
        setField(service, "purchaseInItemMapper", createProxy(ErpPurchaseInItemMapper.class, (methodName, args) -> List.of()));
        setField(service, "purchaseOrderService", createProxy(ErpPurchaseOrderService.class, (methodName, args) -> {
            if ("getPurchaseOrderList".equals(methodName)) {
                return List.of();
            }
            if ("getPurchaseOrderItemListByOrderIds".equals(methodName)) {
                return List.of();
            }
            return null;
        }));

        int generatedCount = service.generateMonthEstimate(new ErpApEstimateScanReqVO().setEstimateMonth("2026-04"));

        assertEquals(0, generatedCount);
        assertEquals(0, insertCount.get());
    }

    @Test
    void confirmApEstimate_shouldMarkConfirmed() throws Exception {
        ErpApEstimateServiceImpl service = new ErpApEstimateServiceImpl();
        ErpApEstimateDO estimate = new ErpApEstimateDO()
                .setId(1L)
                .setEstimateNo("ZG-RK-001")
                .setStatus(ErpApEstimateStatusEnum.GENERATED.getStatus());
        AtomicReference<ErpApEstimateDO> updatedEstimateRef = new AtomicReference<>();

        setField(service, "apEstimateMapper", createProxy(ErpApEstimateMapper.class, (methodName, args) -> {
            if ("selectBatchIds".equals(methodName)) {
                return List.of(estimate);
            }
            if ("updateById".equals(methodName)) {
                updatedEstimateRef.set((ErpApEstimateDO) args[0]);
                return 1;
            }
            return null;
        }));

        service.confirmApEstimate(88L, new ErpApEstimateActionReqVO().setIds(List.of(1L)));

        assertNotNull(updatedEstimateRef.get());
        assertEquals(ErpApEstimateStatusEnum.CONFIRMED.getStatus(), updatedEstimateRef.get().getStatus());
        assertEquals(88L, updatedEstimateRef.get().getConfirmUserId());
        assertNotNull(updatedEstimateRef.get().getConfirmTime());
    }

    @Test
    void reverseApEstimate_shouldMarkReversed() throws Exception {
        ErpApEstimateServiceImpl service = new ErpApEstimateServiceImpl();
        ErpApEstimateDO estimate = new ErpApEstimateDO()
                .setId(1L)
                .setEstimateNo("ZG-RK-001")
                .setStatus(ErpApEstimateStatusEnum.GENERATED.getStatus());
        AtomicReference<ErpApEstimateDO> updatedEstimateRef = new AtomicReference<>();

        setField(service, "apEstimateMapper", createProxy(ErpApEstimateMapper.class, (methodName, args) -> {
            if ("selectBatchIds".equals(methodName)) {
                return List.of(estimate);
            }
            if ("updateReverseInfoById".equals(methodName)) {
                updatedEstimateRef.set(new ErpApEstimateDO()
                        .setId((Long) args[0])
                        .setStatus((Integer) args[1])
                        .setReverseUserId((Long) args[2])
                        .setReverseTime((LocalDateTime) args[3])
                        .setReverseType((Integer) args[4])
                        .setReverseSourceId((Long) args[5])
                        .setReverseSourceNo((String) args[6])
                        .setReverseRemark((String) args[7]));
                return 1;
            }
            return null;
        }));

        service.reverseApEstimate(88L, new ErpApEstimateActionReqVO().setIds(List.of(1L)).setRemark("manual reverse"));

        assertNotNull(updatedEstimateRef.get());
        assertEquals(ErpApEstimateStatusEnum.REVERSED.getStatus(), updatedEstimateRef.get().getStatus());
        assertEquals(88L, updatedEstimateRef.get().getReverseUserId());
        assertEquals(ErpApEstimateReverseTypeEnum.MANUAL.getStatus(), updatedEstimateRef.get().getReverseType());
        assertEquals("manual reverse", updatedEstimateRef.get().getReverseRemark());
        assertNotNull(updatedEstimateRef.get().getReverseTime());
    }

    @Test
    void syncByStatementInvoiceChange_shouldReverseWhenInvoiceReceived() throws Exception {
        ErpApEstimateServiceImpl service = new ErpApEstimateServiceImpl();
        ErpApEstimateDO estimate = new ErpApEstimateDO()
                .setId(9L)
                .setStatus(ErpApEstimateStatusEnum.CONFIRMED.getStatus());
        ErpApStatementDO statement = new ErpApStatementDO()
                .setBizType(ErpBizTypeEnum.PURCHASE_IN.getType())
                .setBizId(11L)
                .setStatus(ErpApStatementStatusEnum.UNPAID.getStatus());
        AtomicReference<Object[]> updateArgsRef = new AtomicReference<>();

        setField(service, "apEstimateMapper", createProxy(ErpApEstimateMapper.class, (methodName, args) -> {
            if ("selectBySourceBizTypeAndSourceBizId".equals(methodName)) {
                return estimate;
            }
            if ("updateReverseInfoById".equals(methodName)) {
                updateArgsRef.set(args);
                return 1;
            }
            return null;
        }));

        service.syncByStatementInvoiceChange(statement, ErpApInvoiceStatusEnum.NONE.getStatus(),
                ErpApInvoiceStatusEnum.RECEIVED.getStatus(), 66L, 1001L, "INV-001");

        assertEquals(9L, updateArgsRef.get()[0]);
        assertEquals(ErpApEstimateStatusEnum.REVERSED.getStatus(), updateArgsRef.get()[1]);
        assertEquals(66L, updateArgsRef.get()[2]);
        assertEquals(ErpApEstimateReverseTypeEnum.INVOICE.getStatus(), updateArgsRef.get()[4]);
        assertEquals(1001L, updateArgsRef.get()[5]);
        assertEquals("INV-001", updateArgsRef.get()[6]);
        assertEquals("收票联动自动冲回暂估", updateArgsRef.get()[7]);
    }

    @Test
    void syncByStatementInvoiceChange_shouldRestoreWhenInvoiceResetToNone() throws Exception {
        ErpApEstimateServiceImpl service = new ErpApEstimateServiceImpl();
        ErpApEstimateDO estimate = new ErpApEstimateDO()
                .setId(10L)
                .setStatus(ErpApEstimateStatusEnum.REVERSED.getStatus())
                .setReverseType(ErpApEstimateReverseTypeEnum.INVOICE.getStatus())
                .setConfirmTime(LocalDateTime.of(2026, 4, 28, 10, 0));
        ErpApStatementDO statement = new ErpApStatementDO()
                .setBizType(ErpBizTypeEnum.PURCHASE_IN.getType())
                .setBizId(12L)
                .setStatus(ErpApStatementStatusEnum.UNPAID.getStatus());
        AtomicReference<Object[]> restoreArgsRef = new AtomicReference<>();

        setField(service, "apEstimateMapper", createProxy(ErpApEstimateMapper.class, (methodName, args) -> {
            if ("selectBySourceBizTypeAndSourceBizId".equals(methodName)) {
                return estimate;
            }
            if ("restoreOpenStatusById".equals(methodName)) {
                restoreArgsRef.set(args);
                return 1;
            }
            return null;
        }));

        service.syncByStatementInvoiceChange(statement, ErpApInvoiceStatusEnum.RECEIVED.getStatus(),
                ErpApInvoiceStatusEnum.NONE.getStatus(), 66L, null, null);

        assertEquals(10L, restoreArgsRef.get()[0]);
        assertEquals(ErpApEstimateStatusEnum.CONFIRMED.getStatus(), restoreArgsRef.get()[1]);
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
