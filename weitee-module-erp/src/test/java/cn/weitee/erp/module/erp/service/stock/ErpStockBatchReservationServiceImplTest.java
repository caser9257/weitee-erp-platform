package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchAllocationDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchReservationDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockBatchAllocationMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockBatchReservationMapper;
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

import static org.assertj.core.api.Assertions.assertThat;

class ErpStockBatchReservationServiceImplTest {

    @Test
    void reserveOutbound_shouldLockByFifoAndPersistReservations() throws Exception {
        ErpStockBatchReservationServiceImpl service = new ErpStockBatchReservationServiceImpl();
        List<ErpStockBatchChangeReqBO> lockReqs = new ArrayList<>();
        List<ErpStockBatchReservationDO> insertedReservations = new ArrayList<>();

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
            if ("lockBatch".equals(methodName)) {
                lockReqs.add((ErpStockBatchChangeReqBO) args[0]);
            }
            return null;
        }));
        setField(service, "productService", createProxy(ErpProductService.class, (methodName, args) -> {
            if ("getProduct".equals(methodName)) {
                return new ErpProductDO().setId(1L).setBatchControlFlag(true);
            }
            return null;
        }));
        setField(service, "stockBatchReservationMapper", createProxy(ErpStockBatchReservationMapper.class, (methodName, args) -> {
            if ("selectListByBizItem".equals(methodName)) {
                return List.of();
            }
            if ("insert".equals(methodName)) {
                ErpStockBatchReservationDO reservation = (ErpStockBatchReservationDO) args[0];
                reservation.setId((long) insertedReservations.size() + 1);
                insertedReservations.add(reservation);
                return 1;
            }
            return null;
        }));

        List<ErpStockBatchReservationDO> reservations = service.reserveOutbound(new ErpStockBatchAllocateOutboundReqBO()
                .setProductId(1L)
                .setWarehouseId(2L)
                .setCount(new BigDecimal("7.000"))
                .setBizType(ErpStockRecordBizTypeEnum.SALE_OUT.getType())
                .setBizId(100L)
                .setBizItemId(101L)
                .setBizNo("XSCK202604300001")
                .setRemark("销售出库预占"));

        assertThat(reservations).hasSize(2);
        assertThat(lockReqs).hasSize(2);
        assertThat(lockReqs.get(0).getStockBatchId()).isEqualTo(11L);
        assertThat(lockReqs.get(0).getCount()).isEqualByComparingTo("5.000");
        assertThat(lockReqs.get(1).getStockBatchId()).isEqualTo(12L);
        assertThat(lockReqs.get(1).getCount()).isEqualByComparingTo("2.000");
        assertThat(insertedReservations.get(0).getBatchNo()).isEqualTo("B001");
        assertThat(insertedReservations.get(1).getReservedQty()).isEqualByComparingTo("2.000");
    }

    @Test
    void deductReservation_shouldDeductLockedBatchesCreateAllocationsAndDeleteReservations() throws Exception {
        ErpStockBatchReservationServiceImpl service = new ErpStockBatchReservationServiceImpl();
        List<ErpStockBatchChangeReqBO> deductReqs = new ArrayList<>();
        List<ErpStockBatchAllocationDO> insertedAllocations = new ArrayList<>();
        AtomicReference<Object[]> deleteArgsRef = new AtomicReference<>();

        setField(service, "stockBatchService", createProxy(ErpStockBatchService.class, (methodName, args) -> {
            if ("deductLockedBatch".equals(methodName)) {
                deductReqs.add((ErpStockBatchChangeReqBO) args[0]);
            }
            return null;
        }));
        setField(service, "stockBatchReservationMapper", createProxy(ErpStockBatchReservationMapper.class, (methodName, args) -> {
            if ("selectListByBiz".equals(methodName)) {
                return List.of(
                        new ErpStockBatchReservationDO().setId(1L).setBizType(50).setBizId(100L)
                                .setBizItemId(101L).setBizNo("XSCK202604300001").setProductId(1L)
                                .setWarehouseId(2L).setStockBatchId(11L).setBatchNo("B001")
                                .setReservedQty(new BigDecimal("5.000")),
                        new ErpStockBatchReservationDO().setId(2L).setBizType(50).setBizId(100L)
                                .setBizItemId(101L).setBizNo("XSCK202604300001").setProductId(1L)
                                .setWarehouseId(2L).setStockBatchId(12L).setBatchNo("B002")
                                .setReservedQty(new BigDecimal("2.000")));
            }
            if ("deleteByBiz".equals(methodName)) {
                deleteArgsRef.set(args);
                return 2;
            }
            return null;
        }));
        setField(service, "stockBatchAllocationMapper", createProxy(ErpStockBatchAllocationMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ErpStockBatchAllocationDO allocation = (ErpStockBatchAllocationDO) args[0];
                allocation.setId((long) insertedAllocations.size() + 1);
                insertedAllocations.add(allocation);
                return 1;
            }
            return null;
        }));

        List<ErpStockBatchAllocationDO> allocations = service.deductReservation(
                ErpStockRecordBizTypeEnum.SALE_OUT.getType(), 100L, "销售出库实扣");

        assertThat(allocations).hasSize(2);
        assertThat(deductReqs).hasSize(2);
        assertThat(deductReqs.get(0).getStockBatchId()).isEqualTo(11L);
        assertThat(deductReqs.get(0).getCount()).isEqualByComparingTo("5.000");
        assertThat(insertedAllocations).hasSize(2);
        assertThat(insertedAllocations.get(1).getOutQty()).isEqualByComparingTo("2.000");
        assertThat(deleteArgsRef.get()).containsExactly(ErpStockRecordBizTypeEnum.SALE_OUT.getType(), 100L);
    }

    @Test
    void releaseReservation_shouldReleaseLockedBatchesAndDeleteReservations() throws Exception {
        ErpStockBatchReservationServiceImpl service = new ErpStockBatchReservationServiceImpl();
        List<ErpStockBatchChangeReqBO> releaseReqs = new ArrayList<>();
        AtomicReference<Object[]> deleteArgsRef = new AtomicReference<>();

        setField(service, "stockBatchService", createProxy(ErpStockBatchService.class, (methodName, args) -> {
            if ("releaseLockedBatch".equals(methodName)) {
                releaseReqs.add((ErpStockBatchChangeReqBO) args[0]);
            }
            return null;
        }));
        setField(service, "stockBatchReservationMapper", createProxy(ErpStockBatchReservationMapper.class, (methodName, args) -> {
            if ("selectListByBiz".equals(methodName)) {
                return List.of(new ErpStockBatchReservationDO().setId(1L).setBizType(50).setBizId(100L)
                        .setBizItemId(101L).setBizNo("XSCK202604300001").setStockBatchId(11L)
                        .setReservedQty(new BigDecimal("5.000")));
            }
            if ("deleteByBiz".equals(methodName)) {
                deleteArgsRef.set(args);
                return 1;
            }
            return null;
        }));

        service.releaseReservation(ErpStockRecordBizTypeEnum.SALE_OUT.getType(), 100L, "销售出库预占释放");

        assertThat(releaseReqs).hasSize(1);
        assertThat(releaseReqs.get(0).getStockBatchId()).isEqualTo(11L);
        assertThat(releaseReqs.get(0).getCount()).isEqualByComparingTo("5.000");
        assertThat(deleteArgsRef.get()).containsExactly(ErpStockRecordBizTypeEnum.SALE_OUT.getType(), 100L);
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
