package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchAllocationDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockBatchAllocationMapper;
import cn.weitee.erp.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchAllocateOutboundReqBO;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchChangeReqBO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErpStockBatchAllocationServiceImplTest {

    @Test
    void allocateOutbound_shouldAllocateByFifoAndPersistAllocations() throws Exception {
        ErpStockBatchAllocationServiceImpl service = new ErpStockBatchAllocationServiceImpl();
        List<ErpStockBatchChangeReqBO> decreaseReqs = new ArrayList<>();
        List<ErpStockBatchAllocationDO> insertedAllocations = new ArrayList<>();

        setField(service, "productService", createProxy(ErpProductService.class, (methodName, args) -> {
            if ("getProduct".equals(methodName)) {
                return new ErpProductDO().setId(1L).setBatchControlFlag(true);
            }
            return null;
        }));
        setField(service, "stockBatchService", createProxy(ErpStockBatchService.class, (methodName, args) -> {
            if ("getAvailableStockBatchList".equals(methodName)) {
                return List.of(
                        new ErpStockBatchDO().setId(11L).setProductId(1L).setWarehouseId(2L)
                                .setBatchNo("B001").setAvailableQty(new BigDecimal("5.000"))
                                .setInboundTime(LocalDateTime.of(2026, 4, 1, 9, 0))
                                .setProduceDate(LocalDate.of(2026, 3, 31)),
                        new ErpStockBatchDO().setId(12L).setProductId(1L).setWarehouseId(2L)
                                .setBatchNo("B002").setAvailableQty(new BigDecimal("10.000"))
                                .setInboundTime(LocalDateTime.of(2026, 4, 2, 9, 0))
                                .setProduceDate(LocalDate.of(2026, 4, 1)));
            }
            if ("decreaseBatch".equals(methodName)) {
                decreaseReqs.add((ErpStockBatchChangeReqBO) args[0]);
                return null;
            }
            return null;
        }));
        setField(service, "stockBatchAllocationMapper", createProxy(ErpStockBatchAllocationMapper.class, (methodName, args) -> {
            if ("selectListByBizItem".equals(methodName)) {
                return List.of();
            }
            if ("insert".equals(methodName)) {
                ErpStockBatchAllocationDO allocation = (ErpStockBatchAllocationDO) args[0];
                allocation.setId((long) insertedAllocations.size() + 1);
                insertedAllocations.add(allocation);
                return 1;
            }
            return null;
        }));

        List<ErpStockBatchAllocationDO> allocations = service.allocateOutbound(new ErpStockBatchAllocateOutboundReqBO()
                .setProductId(1L)
                .setWarehouseId(2L)
                .setCount(new BigDecimal("7.000"))
                .setBizType(ErpStockRecordBizTypeEnum.SALE_OUT.getType())
                .setBizId(100L)
                .setBizItemId(101L)
                .setBizNo("XSCK202604290001")
                .setRemark("销售出库"));

        assertEquals(2, allocations.size());
        assertEquals(2, decreaseReqs.size());
        assertEquals(11L, decreaseReqs.get(0).getStockBatchId());
        assertEquals(new BigDecimal("5.000"), decreaseReqs.get(0).getCount());
        assertEquals(12L, decreaseReqs.get(1).getStockBatchId());
        assertEquals(new BigDecimal("2.000"), decreaseReqs.get(1).getCount());
        assertEquals(2, insertedAllocations.size());
        assertEquals("B001", insertedAllocations.get(0).getBatchNo());
        assertEquals(new BigDecimal("2.000"), insertedAllocations.get(1).getOutQty());
    }

    @Test
    void rollbackOutbound_shouldRestoreAllocatedBatchesAndDeleteActiveAllocations() throws Exception {
        ErpStockBatchAllocationServiceImpl service = new ErpStockBatchAllocationServiceImpl();
        List<ErpStockBatchChangeReqBO> increaseReqs = new ArrayList<>();
        AtomicReference<Object[]> deleteArgsRef = new AtomicReference<>();

        setField(service, "stockBatchService", createProxy(ErpStockBatchService.class, (methodName, args) -> {
            if ("increaseBatch".equals(methodName)) {
                increaseReqs.add((ErpStockBatchChangeReqBO) args[0]);
            }
            return null;
        }));
        setField(service, "stockBatchAllocationMapper", createProxy(ErpStockBatchAllocationMapper.class, (methodName, args) -> {
            if ("selectListByBiz".equals(methodName)) {
                return List.of(
                        new ErpStockBatchAllocationDO().setId(1L).setStockBatchId(11L)
                                .setOutQty(new BigDecimal("5.000")).setBizId(100L).setBizItemId(101L)
                                .setBizNo("XSCK202604290001"),
                        new ErpStockBatchAllocationDO().setId(2L).setStockBatchId(12L)
                                .setOutQty(new BigDecimal("2.000")).setBizId(100L).setBizItemId(101L)
                                .setBizNo("XSCK202604290001"));
            }
            if ("deleteByBiz".equals(methodName)) {
                deleteArgsRef.set(args);
                return 2;
            }
            return null;
        }));

        service.rollbackOutbound(ErpStockRecordBizTypeEnum.SALE_OUT.getType(), 100L,
                ErpStockRecordBizTypeEnum.SALE_OUT_CANCEL.getType(), "销售出库反审核");

        assertEquals(2, increaseReqs.size());
        assertEquals(11L, increaseReqs.get(0).getStockBatchId());
        assertEquals(new BigDecimal("5.000"), increaseReqs.get(0).getCount());
        assertEquals(12L, increaseReqs.get(1).getStockBatchId());
        assertEquals(new BigDecimal("2.000"), increaseReqs.get(1).getCount());
        assertEquals(ErpStockRecordBizTypeEnum.SALE_OUT.getType(), deleteArgsRef.get()[0]);
        assertEquals(100L, deleteArgsRef.get()[1]);
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
        return fieldName.startsWith("erp")
                ? new String[]{fieldName}
                : new String[]{fieldName, "erp" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1)};
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }
}
