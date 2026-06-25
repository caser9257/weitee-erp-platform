package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.module.erp.controller.admin.mrp.vo.issue.ErpProductionIssueCreateReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionIssueDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionIssueItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionMaterialDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionIssueBatchMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionIssueItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionIssueMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionMaterialMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInItemMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.service.stock.ErpStockBatchService;
import cn.weitee.erp.module.erp.service.stock.ErpStockRecordService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ErpProductionIssueServiceImplTest {

    @Test
    void createProductionIssue_shouldGenerateVoucherSnapshot() throws Exception {
        ErpProductionIssueServiceImpl service = new ErpProductionIssueServiceImpl();
        AtomicReference<ErpProductionIssueDO> insertedIssueRef = new AtomicReference<>();
        AtomicReference<ErpProductionIssueDO> updatedIssueRef = new AtomicReference<>();
        AtomicReference<List<ErpProductionIssueItemDO>> insertedItemsRef = new AtomicReference<>(new ArrayList<>());
        AtomicReference<Object[]> voucherCallArgsRef = new AtomicReference<>();

        setField(service, "productionOrderService", createProxy(ErpProductionOrderService.class, (methodName, args) -> {
            if ("getProductionOrder".equals(methodName)) {
                return new ErpProductionOrderDO().setId(1L).setOrderNo("SCGD202604280001");
            }
            return null;
        }));
        setField(service, "productionMaterialService", createProxy(ErpProductionMaterialService.class, (methodName, args) -> null));
        setField(service, "productionMaterialMapper", createProxy(ErpProductionMaterialMapper.class, (methodName, args) -> {
            if ("selectListByIds".equals(methodName)) {
                return List.of(new ErpProductionMaterialDO()
                        .setId(11L)
                        .setProductionOrderId(1L)
                        .setMaterialId(1001L)
                        .setRequiredQty(new BigDecimal("20"))
                        .setIssuedQty(BigDecimal.ZERO)
                        .setReturnedQty(BigDecimal.ZERO));
            }
            if ("updateIssuedQtyIncrement".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(service, "productionIssueMapper", createProxy(ErpProductionIssueMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ErpProductionIssueDO issue = (ErpProductionIssueDO) args[0];
                issue.setId(101L);
                insertedIssueRef.set(issue);
                return 1;
            }
            if ("updateById".equals(methodName)) {
                updatedIssueRef.set((ErpProductionIssueDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "productionIssueItemMapper", createProxy(ErpProductionIssueItemMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ErpProductionIssueItemDO item = (ErpProductionIssueItemDO) args[0];
                item.setId(10001L);
                insertedItemsRef.get().add(item);
                return 1;
            }
            return null;
        }));
        setField(service, "productionIssueBatchMapper", createProxy(ErpProductionIssueBatchMapper.class, (methodName, args) -> 1));
        setField(service, "purchaseInItemMapper", createProxy(ErpPurchaseInItemMapper.class, (methodName, args) -> {
            if ("selectListByIds".equals(methodName)) {
                return List.of(new ErpPurchaseInItemDO()
                        .setId(5001L)
                        .setCount(new BigDecimal("20"))
                        .setTotalPrice(new BigDecimal("100.00")));
            }
            return null;
        }));
        setField(service, "stockBatchService", createProxy(ErpStockBatchService.class, (methodName, args) -> {
            if ("getStockBatchList".equals(methodName)) {
                return List.of(new ErpStockBatchDO()
                        .setId(9001L)
                        .setProductId(1001L)
                        .setWarehouseId(2001L)
                        .setBatchNo("B202604280001")
                        .setSourceBizType("PURCHASE_IN")
                        .setSourceBizItemId(5001L));
            }
            return null;
        }));
        setField(service, "stockRecordService", createProxy(ErpStockRecordService.class, (methodName, args) -> null));
        setField(service, "productService", createProxyByName(
                "cn.weitee.erp.module.erp.service.product.ErpProductService", (methodName, args) -> null));
        setField(service, "warehouseService", createProxyByName(
                "cn.weitee.erp.module.erp.service.stock.ErpWarehouseService", (methodName, args) -> null));
        setField(service, "adminUserApi", createProxyByName(
                "cn.weitee.erp.module.system.api.user.AdminUserApi", (methodName, args) -> null));
        setField(service, "noRedisDAO", new ErpNoRedisDAO() {
            @Override
            public String generate(String prefix) {
                return "SCLL202604280001";
            }
        });
        setField(service, "productionIssueVoucherService", createProxyByName(
                "cn.weitee.erp.module.erp.service.mrp.ErpProductionIssueVoucherService", (methodName, args) -> {
                    if ("createVoucher".equals(methodName)) {
                        voucherCallArgsRef.set(args);
                        return 7001L;
                    }
                    return null;
                }));

        Long issueId = service.createProductionIssue(new ErpProductionIssueCreateReqVO()
                .setProductionOrderId(1L)
                .setRemark("车间领料")
                .setItems(List.of(new ErpProductionIssueCreateReqVO.Item()
                        .setProductionMaterialId(11L)
                        .setMaterialId(1001L)
                        .setWarehouseId(2001L)
                        .setIssueQty(new BigDecimal("10"))
                        .setRemark("主料")
                        .setBatches(List.of(new ErpProductionIssueCreateReqVO.Batch()
                                .setStockBatchId(9001L)
                                .setBatchNo("B202604280001")
                                .setIssueQty(new BigDecimal("10")))))));

        assertEquals(101L, issueId);
        assertEquals(new BigDecimal("50.00"), updatedIssueRef.get().getIssueAmount().setScale(2));
        assertNotNull(voucherCallArgsRef.get());
        ErpProductionIssueDO voucherIssue = (ErpProductionIssueDO) voucherCallArgsRef.get()[0];
        @SuppressWarnings("unchecked")
        List<ErpProductionIssueItemDO> voucherItems = (List<ErpProductionIssueItemDO>) voucherCallArgsRef.get()[1];
        assertEquals("SCLL202604280001", insertedIssueRef.get().getIssueNo());
        assertEquals(new BigDecimal("50.00"), voucherIssue.getIssueAmount().setScale(2));
        assertEquals(1, voucherItems.size());
        assertEquals(new BigDecimal("50.00"), voucherItems.get(0).getIssueAmount().setScale(2));
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> type, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
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

    @SuppressWarnings("unchecked")
    private <T> T createProxyByName(String className, MethodHandler handler) {
        try {
            Class<T> type = (Class<T>) Class.forName(className);
            return createProxy(type, handler);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
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
