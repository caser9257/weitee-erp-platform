package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.module.erp.controller.admin.mrp.vo.suggest.ErpPurchaseSuggestConvertReqVO;
import cn.weitee.erp.module.erp.controller.admin.purchase.vo.order.ErpPurchaseOrderSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpPurchaseSuggestDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionSuggestMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpPurchaseSuggestMapper;
import cn.weitee.erp.module.erp.enums.mrp.ErpMrpSuggestStatusEnum;
import cn.weitee.erp.module.erp.service.finance.ErpAccountService;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.project.ErpProjectRoleTaskService;
import cn.weitee.erp.module.erp.service.purchase.ErpPurchaseOrderService;
import cn.weitee.erp.module.erp.service.purchase.ErpSupplierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ErpMrpSuggestServiceImplTest {

    private final AtomicReference<List<ErpPurchaseSuggestDO>> purchaseSuggestsRef = new AtomicReference<>(List.of());
    private final AtomicReference<ErpPurchaseOrderSaveReqVO> createdPurchaseOrderRef = new AtomicReference<>();
    private final AtomicReference<Long> completedMcTaskProjectIdRef = new AtomicReference<>();
    private final AtomicReference<String> completedMcTaskRemarkRef = new AtomicReference<>();
    private final AtomicReference<List<Long>> convertedPurchaseSuggestIdsRef = new AtomicReference<>(List.of());
    private final AtomicReference<Integer> convertedPurchaseSuggestOldStatusRef = new AtomicReference<>();
    private final AtomicReference<ErpPurchaseSuggestDO> convertedPurchaseSuggestUpdateRef = new AtomicReference<>();
    private final List<ErpPurchaseSuggestDO> updatedSuggests = new ArrayList<>();

    private ErpMrpSuggestServiceImpl suggestService;

    @BeforeEach
    void setUp() throws Exception {
        suggestService = new ErpMrpSuggestServiceImpl();
        purchaseSuggestsRef.set(List.of());
        createdPurchaseOrderRef.set(null);
        completedMcTaskProjectIdRef.set(null);
        completedMcTaskRemarkRef.set(null);
        convertedPurchaseSuggestIdsRef.set(List.of());
        convertedPurchaseSuggestOldStatusRef.set(null);
        convertedPurchaseSuggestUpdateRef.set(null);
        updatedSuggests.clear();

        setField(suggestService, "purchaseSuggestMapper", createProxy(ErpPurchaseSuggestMapper.class, (methodName, args) -> {
            if ("selectListByIds".equals(methodName)) {
                return purchaseSuggestsRef.get();
            }
            if ("updateById".equals(methodName)) {
                updatedSuggests.add((ErpPurchaseSuggestDO) args[0]);
                return 1;
            }
            if ("updateStatusByIdsAndStatus".equals(methodName)) {
                convertedPurchaseSuggestIdsRef.set((List<Long>) args[0]);
                convertedPurchaseSuggestOldStatusRef.set((Integer) args[1]);
                convertedPurchaseSuggestUpdateRef.set((ErpPurchaseSuggestDO) args[2]);
                return ((List<?>) args[0]).size();
            }
            if ("selectCountByProjectIdAndStatus".equals(methodName)) {
                return 0L;
            }
            return null;
        }));
        setField(suggestService, "productionSuggestMapper", createProxy(ErpProductionSuggestMapper.class, (methodName, args) -> {
            if ("selectCountByProjectIdAndStatus".equals(methodName)) {
                return 0L;
            }
            return null;
        }));
        setField(suggestService, "supplierService", createProxy(ErpSupplierService.class, (methodName, args) -> null));
        setField(suggestService, "accountService", createProxy(ErpAccountService.class, (methodName, args) -> null));
        setField(suggestService, "productService", createProxy(ErpProductService.class, (methodName, args) -> {
            if ("validProductList".equals(methodName)) {
                return List.of(new ErpProductDO().setId(3001L).setUnitId(91L).setPurchasePrice(new BigDecimal("12.50")));
            }
            return null;
        }));
        setField(suggestService, "purchaseOrderService", createProxy(ErpPurchaseOrderService.class, (methodName, args) -> {
            if ("createPurchaseOrder".equals(methodName)) {
                createdPurchaseOrderRef.set((ErpPurchaseOrderSaveReqVO) args[0]);
                return 8001L;
            }
            return null;
        }));
        setField(suggestService, "productionOrderService", createProxy(ErpProductionOrderService.class, (methodName, args) -> null));
        setField(suggestService, "projectRoleTaskService", createProxy(ErpProjectRoleTaskService.class, (methodName, args) -> {
            if ("completeMcTask".equals(methodName)) {
                completedMcTaskProjectIdRef.set((Long) args[0]);
                completedMcTaskRemarkRef.set((String) args[1]);
            }
            return null;
        }));
    }

    @Test
    void convertPurchaseSuggest_shouldSplitItemsByProjectAndMaterial() {
        purchaseSuggestsRef.set(List.of(
                new ErpPurchaseSuggestDO().setId(1L).setProjectId(1001L).setMaterialId(3001L)
                        .setSuggestQty(new BigDecimal("2")).setStatus(ErpMrpSuggestStatusEnum.CONFIRMED.getStatus()),
                new ErpPurchaseSuggestDO().setId(2L).setProjectId(1002L).setMaterialId(3001L)
                        .setSuggestQty(new BigDecimal("3")).setStatus(ErpMrpSuggestStatusEnum.CONFIRMED.getStatus())
        ));
        ErpPurchaseSuggestConvertReqVO reqVO = new ErpPurchaseSuggestConvertReqVO();
        reqVO.setIds(List.of(1L, 2L));
        reqVO.setSupplierId(9L);
        reqVO.setAccountId(11L);
        reqVO.setRemark("from mrp");

        Long orderId = suggestService.convertPurchaseSuggest(reqVO);

        assertEquals(8001L, orderId);
        assertEquals(2, createdPurchaseOrderRef.get().getItems().size());
        assertEquals(1001L, createdPurchaseOrderRef.get().getItems().get(0).getProjectId());
        assertEquals(new BigDecimal("2"), createdPurchaseOrderRef.get().getItems().get(0).getCount());
        assertEquals(1002L, createdPurchaseOrderRef.get().getItems().get(1).getProjectId());
        assertEquals(new BigDecimal("3"), createdPurchaseOrderRef.get().getItems().get(1).getCount());
        assertEquals(2, updatedSuggests.size());
        assertEquals(List.of(1L, 2L), convertedPurchaseSuggestIdsRef.get());
        assertEquals(ErpMrpSuggestStatusEnum.CONFIRMED.getStatus(), convertedPurchaseSuggestOldStatusRef.get());
        assertNotNull(convertedPurchaseSuggestUpdateRef.get());
        assertEquals(ErpMrpSuggestStatusEnum.CONVERTED.getStatus(), convertedPurchaseSuggestUpdateRef.get().getStatus());
        assertEquals(8001L, updatedSuggests.get(0).getConvertPurchaseOrderId());
    }

    @Test
    void rejectPurchaseSuggest_shouldUpdateRejectedStatus() {
        purchaseSuggestsRef.set(List.of(
                new ErpPurchaseSuggestDO().setId(1L).setProjectId(1001L).setMaterialId(3001L)
                        .setSuggestQty(new BigDecimal("2")).setStatus(ErpMrpSuggestStatusEnum.TO_CONFIRM.getStatus())
        ));

        suggestService.rejectPurchaseSuggest(List.of(1L));

        assertEquals(1, updatedSuggests.size());
        assertEquals(ErpMrpSuggestStatusEnum.REJECTED.getStatus(), updatedSuggests.get(0).getStatus());
    }

    @Test
    void rejectPurchaseSuggest_shouldCompleteMcTaskWhenNoPendingSuggestLeft() {
        purchaseSuggestsRef.set(List.of(
                new ErpPurchaseSuggestDO().setId(1L).setProjectId(1001L).setMaterialId(3001L)
                        .setSuggestQty(new BigDecimal("2")).setStatus(ErpMrpSuggestStatusEnum.TO_CONFIRM.getStatus())
        ));

        suggestService.rejectPurchaseSuggest(List.of(1L));

        assertEquals(1001L, completedMcTaskProjectIdRef.get());
        assertEquals("MRP 建议已处理完成", completedMcTaskRemarkRef.get());
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
        Field field = target.getClass().getDeclaredField(mapFieldName(fieldName));
        field.setAccessible(true);
        field.set(target, value);
    }

    private String mapFieldName(String fieldName) {
        return switch (fieldName) {
            case "purchaseSuggestMapper" -> "erpPurchaseSuggestMapper";
            case "productionSuggestMapper" -> "erpProductionSuggestMapper";
            default -> fieldName;
        };
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }

}
