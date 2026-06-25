package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.module.erp.controller.admin.stock.vo.batch.ErpStockBatchAdjustReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchAdjustmentDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchRecordDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockBatchAdjustmentMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockBatchMapper;
import cn.weitee.erp.module.erp.enums.stock.ErpStockBatchAdjustTypeEnum;
import cn.weitee.erp.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchChangeReqBO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ErpStockBatchServiceImplTest {

    @Test
    void adjustBatch_shouldIncreaseBatchAndWriteAdjustmentRecord() throws Exception {
        ErpStockBatchServiceImpl service = new ErpStockBatchServiceImpl();
        AtomicInteger selectTimes = new AtomicInteger();
        AtomicReference<Object[]> updateArgsRef = new AtomicReference<>();
        AtomicReference<ErpStockBatchAdjustmentDO> insertedAdjustmentRef = new AtomicReference<>();
        AtomicReference<ErpStockBatchAdjustmentDO> updatedAdjustmentRef = new AtomicReference<>();
        List<ErpStockBatchRecordDO> records = new ArrayList<>();

        setField(service, "stockBatchMapper", createProxy(ErpStockBatchMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                if (selectTimes.getAndIncrement() < 2) {
                    return new ErpStockBatchDO().setId(11L).setProductId(1L).setWarehouseId(2L)
                            .setBatchNo("B001").setAvailableQty(new BigDecimal("5.000"))
                            .setTotalQty(new BigDecimal("5.000"));
                }
                return new ErpStockBatchDO().setId(11L).setProductId(1L).setWarehouseId(2L)
                        .setBatchNo("B001").setAvailableQty(new BigDecimal("8.000"))
                        .setTotalQty(new BigDecimal("8.000"));
            }
            if ("updateQtyIncrement".equals(methodName)) {
                updateArgsRef.set(args);
                return 1;
            }
            return null;
        }));
        setField(service, "stockBatchAdjustmentMapper", createProxy(ErpStockBatchAdjustmentMapper.class, (methodName, args) -> {
            if ("selectByAdjustNo".equals(methodName)) {
                return null;
            }
            if ("insert".equals(methodName)) {
                ErpStockBatchAdjustmentDO adjustment = (ErpStockBatchAdjustmentDO) args[0];
                adjustment.setId(501L);
                insertedAdjustmentRef.set(adjustment);
                return 1;
            }
            if ("updateById".equals(methodName)) {
                updatedAdjustmentRef.set((ErpStockBatchAdjustmentDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "stockBatchRecordService", createProxy(ErpStockBatchRecordService.class, (methodName, args) -> {
            if ("createStockBatchRecord".equals(methodName)) {
                records.add((ErpStockBatchRecordDO) args[0]);
            }
            return null;
        }));

        ErpStockBatchAdjustReqVO reqVO = new ErpStockBatchAdjustReqVO();
        reqVO.setStockBatchId(11L);
        reqVO.setAdjustType(ErpStockBatchAdjustTypeEnum.INCREASE.getType());
        reqVO.setCount(new BigDecimal("3.000"));
        reqVO.setAdjustNo("TZ202604290001");
        reqVO.setRemark("期初补录");

        ErpStockBatchDO result = service.adjustBatch(reqVO);

        assertThat(result.getAvailableQty()).isEqualByComparingTo("8.000");
        assertThat(updateArgsRef.get()).containsExactly(11L, new BigDecimal("3.000"));
        assertThat(insertedAdjustmentRef.get())
                .extracting(ErpStockBatchAdjustmentDO::getAdjustNo,
                        ErpStockBatchAdjustmentDO::getStockBatchId,
                        ErpStockBatchAdjustmentDO::getBeforeAvailableQty,
                        ErpStockBatchAdjustmentDO::getBeforeTotalQty)
                .containsExactly("TZ202604290001", 11L, new BigDecimal("5.000"), new BigDecimal("5.000"));
        assertThat(updatedAdjustmentRef.get())
                .extracting(ErpStockBatchAdjustmentDO::getAfterAvailableQty,
                        ErpStockBatchAdjustmentDO::getAfterTotalQty)
                .containsExactly(new BigDecimal("8.000"), new BigDecimal("8.000"));
        assertThat(records).hasSize(1);
        assertThat(records.get(0))
                .extracting(ErpStockBatchRecordDO::getBizType, ErpStockBatchRecordDO::getBizId,
                        ErpStockBatchRecordDO::getBizNo, ErpStockBatchRecordDO::getCount,
                        ErpStockBatchRecordDO::getAfterAvailableQty)
                .containsExactly(ErpStockRecordBizTypeEnum.BATCH_ADJUST_IN.getType(), 501L,
                        "TZ202604290001", new BigDecimal("3.000"), new BigDecimal("8.000"));
    }

    @Test
    void adjustBatch_shouldDecreaseBatchAndWriteNegativeAdjustmentRecord() throws Exception {
        ErpStockBatchServiceImpl service = new ErpStockBatchServiceImpl();
        AtomicInteger selectTimes = new AtomicInteger();
        AtomicReference<Object[]> updateArgsRef = new AtomicReference<>();
        AtomicReference<ErpStockBatchAdjustmentDO> insertedAdjustmentRef = new AtomicReference<>();
        AtomicReference<ErpStockBatchAdjustmentDO> updatedAdjustmentRef = new AtomicReference<>();
        List<ErpStockBatchRecordDO> records = new ArrayList<>();

        setField(service, "stockBatchMapper", createProxy(ErpStockBatchMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                if (selectTimes.getAndIncrement() < 2) {
                    return new ErpStockBatchDO().setId(11L).setProductId(1L).setWarehouseId(2L)
                            .setBatchNo("B001").setAvailableQty(new BigDecimal("5.000"))
                            .setTotalQty(new BigDecimal("5.000"));
                }
                return new ErpStockBatchDO().setId(11L).setProductId(1L).setWarehouseId(2L)
                        .setBatchNo("B001").setAvailableQty(new BigDecimal("3.000"))
                        .setTotalQty(new BigDecimal("3.000"));
            }
            if ("updateQtyDecrement".equals(methodName)) {
                updateArgsRef.set(args);
                return 1;
            }
            return null;
        }));
        setField(service, "stockBatchAdjustmentMapper", createProxy(ErpStockBatchAdjustmentMapper.class, (methodName, args) -> {
            if ("selectByAdjustNo".equals(methodName)) {
                return null;
            }
            if ("insert".equals(methodName)) {
                ErpStockBatchAdjustmentDO adjustment = (ErpStockBatchAdjustmentDO) args[0];
                adjustment.setId(502L);
                insertedAdjustmentRef.set(adjustment);
                return 1;
            }
            if ("updateById".equals(methodName)) {
                updatedAdjustmentRef.set((ErpStockBatchAdjustmentDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "stockBatchRecordService", createProxy(ErpStockBatchRecordService.class, (methodName, args) -> {
            if ("createStockBatchRecord".equals(methodName)) {
                records.add((ErpStockBatchRecordDO) args[0]);
            }
            return null;
        }));

        ErpStockBatchAdjustReqVO reqVO = new ErpStockBatchAdjustReqVO();
        reqVO.setStockBatchId(11L);
        reqVO.setAdjustType(ErpStockBatchAdjustTypeEnum.DECREASE.getType());
        reqVO.setCount(new BigDecimal("2.000"));
        reqVO.setAdjustNo("TZ202604290002");
        reqVO.setRemark("盘点调减");

        ErpStockBatchDO result = service.adjustBatch(reqVO);

        assertThat(result.getAvailableQty()).isEqualByComparingTo("3.000");
        assertThat(updateArgsRef.get()).containsExactly(11L, new BigDecimal("2.000"));
        assertThat(insertedAdjustmentRef.get())
                .extracting(ErpStockBatchAdjustmentDO::getAdjustNo,
                        ErpStockBatchAdjustmentDO::getStockBatchId,
                        ErpStockBatchAdjustmentDO::getBeforeAvailableQty,
                        ErpStockBatchAdjustmentDO::getBeforeTotalQty)
                .containsExactly("TZ202604290002", 11L, new BigDecimal("5.000"), new BigDecimal("5.000"));
        assertThat(updatedAdjustmentRef.get())
                .extracting(ErpStockBatchAdjustmentDO::getAfterAvailableQty,
                        ErpStockBatchAdjustmentDO::getAfterTotalQty)
                .containsExactly(new BigDecimal("3.000"), new BigDecimal("3.000"));
        assertThat(records).hasSize(1);
        assertThat(records.get(0))
                .extracting(ErpStockBatchRecordDO::getBizType, ErpStockBatchRecordDO::getBizId,
                        ErpStockBatchRecordDO::getBizNo, ErpStockBatchRecordDO::getCount,
                        ErpStockBatchRecordDO::getAfterAvailableQty)
                .containsExactly(ErpStockRecordBizTypeEnum.BATCH_ADJUST_OUT.getType(), 502L,
                        "TZ202604290002", new BigDecimal("-2.000"), new BigDecimal("3.000"));
    }

    @Test
    void lockBatch_shouldMoveAvailableQtyToLockedQty() throws Exception {
        ErpStockBatchServiceImpl service = new ErpStockBatchServiceImpl();
        AtomicInteger selectTimes = new AtomicInteger();
        AtomicReference<Object[]> updateArgsRef = new AtomicReference<>();

        setField(service, "stockBatchMapper", createProxy(ErpStockBatchMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                if (selectTimes.getAndIncrement() == 0) {
                    return new ErpStockBatchDO().setId(11L).setProductId(1L).setWarehouseId(2L)
                            .setBatchNo("B001").setAvailableQty(new BigDecimal("5.000"))
                            .setLockedQty(BigDecimal.ZERO).setTotalQty(new BigDecimal("5.000"));
                }
                return new ErpStockBatchDO().setId(11L).setProductId(1L).setWarehouseId(2L)
                        .setBatchNo("B001").setAvailableQty(new BigDecimal("2.000"))
                        .setLockedQty(new BigDecimal("3.000")).setTotalQty(new BigDecimal("5.000"));
            }
            if ("updateLockIncrement".equals(methodName)) {
                updateArgsRef.set(args);
                return 1;
            }
            return null;
        }));

        ErpStockBatchDO result = service.lockBatch(new ErpStockBatchChangeReqBO(
                11L, new BigDecimal("3.000"), ErpStockRecordBizTypeEnum.SALE_OUT.getType(),
                100L, 101L, "XSCK202604300001", "销售出库预占"));

        assertThat(result.getAvailableQty()).isEqualByComparingTo("2.000");
        assertThat(result.getLockedQty()).isEqualByComparingTo("3.000");
        assertThat(updateArgsRef.get()).containsExactly(11L, new BigDecimal("3.000"));
    }

    @Test
    void releaseLockedBatch_shouldMoveLockedQtyBackToAvailableQty() throws Exception {
        ErpStockBatchServiceImpl service = new ErpStockBatchServiceImpl();
        AtomicInteger selectTimes = new AtomicInteger();
        AtomicReference<Object[]> updateArgsRef = new AtomicReference<>();

        setField(service, "stockBatchMapper", createProxy(ErpStockBatchMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                if (selectTimes.getAndIncrement() == 0) {
                    return new ErpStockBatchDO().setId(11L).setProductId(1L).setWarehouseId(2L)
                            .setBatchNo("B001").setAvailableQty(new BigDecimal("2.000"))
                            .setLockedQty(new BigDecimal("3.000")).setTotalQty(new BigDecimal("5.000"));
                }
                return new ErpStockBatchDO().setId(11L).setProductId(1L).setWarehouseId(2L)
                        .setBatchNo("B001").setAvailableQty(new BigDecimal("5.000"))
                        .setLockedQty(BigDecimal.ZERO).setTotalQty(new BigDecimal("5.000"));
            }
            if ("updateLockRelease".equals(methodName)) {
                updateArgsRef.set(args);
                return 1;
            }
            return null;
        }));

        ErpStockBatchDO result = service.releaseLockedBatch(new ErpStockBatchChangeReqBO(
                11L, new BigDecimal("3.000"), ErpStockRecordBizTypeEnum.SALE_OUT.getType(),
                100L, 101L, "XSCK202604300001", "销售出库预占释放"));

        assertThat(result.getAvailableQty()).isEqualByComparingTo("5.000");
        assertThat(result.getLockedQty()).isEqualByComparingTo("0.000");
        assertThat(updateArgsRef.get()).containsExactly(11L, new BigDecimal("3.000"));
    }

    @Test
    void deductLockedBatch_shouldDeductTotalAndLockedQtyAndWriteNegativeRecord() throws Exception {
        ErpStockBatchServiceImpl service = new ErpStockBatchServiceImpl();
        AtomicInteger selectTimes = new AtomicInteger();
        AtomicReference<Object[]> updateArgsRef = new AtomicReference<>();
        List<ErpStockBatchRecordDO> records = new ArrayList<>();

        setField(service, "stockBatchMapper", createProxy(ErpStockBatchMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                if (selectTimes.getAndIncrement() == 0) {
                    return new ErpStockBatchDO().setId(11L).setProductId(1L).setWarehouseId(2L)
                            .setBatchNo("B001").setAvailableQty(new BigDecimal("2.000"))
                            .setLockedQty(new BigDecimal("3.000")).setTotalQty(new BigDecimal("5.000"));
                }
                return new ErpStockBatchDO().setId(11L).setProductId(1L).setWarehouseId(2L)
                        .setBatchNo("B001").setAvailableQty(new BigDecimal("2.000"))
                        .setLockedQty(BigDecimal.ZERO).setTotalQty(new BigDecimal("2.000"));
            }
            if ("updateLockedDeduct".equals(methodName)) {
                updateArgsRef.set(args);
                return 1;
            }
            return null;
        }));
        setField(service, "stockBatchRecordService", createProxy(ErpStockBatchRecordService.class, (methodName, args) -> {
            if ("createStockBatchRecord".equals(methodName)) {
                records.add((ErpStockBatchRecordDO) args[0]);
            }
            return null;
        }));

        ErpStockBatchDO result = service.deductLockedBatch(new ErpStockBatchChangeReqBO(
                11L, new BigDecimal("3.000"), ErpStockRecordBizTypeEnum.SALE_OUT.getType(),
                100L, 101L, "XSCK202604300001", "销售出库实扣"));

        assertThat(result.getTotalQty()).isEqualByComparingTo("2.000");
        assertThat(result.getLockedQty()).isEqualByComparingTo("0.000");
        assertThat(updateArgsRef.get()).containsExactly(11L, new BigDecimal("3.000"));
        assertThat(records).hasSize(1);
        assertThat(records.get(0))
                .extracting(ErpStockBatchRecordDO::getBizType, ErpStockBatchRecordDO::getBizId,
                        ErpStockBatchRecordDO::getBizItemId, ErpStockBatchRecordDO::getCount,
                        ErpStockBatchRecordDO::getAfterAvailableQty)
                .containsExactly(ErpStockRecordBizTypeEnum.SALE_OUT.getType(), 100L, 101L,
                        new BigDecimal("-3.000"), new BigDecimal("2.000"));
    }

    @Test
    void getStockBatchList_shouldReturnEmptyListWhenIdsBlank() throws Exception {
        ErpStockBatchServiceImpl service = new ErpStockBatchServiceImpl();
        AtomicBoolean selectCalled = new AtomicBoolean(false);

        setField(service, "stockBatchMapper", createProxy(ErpStockBatchMapper.class, (methodName, args) -> {
            if ("selectListByIds".equals(methodName)) {
                selectCalled.set(true);
            }
            return null;
        }));

        assertThat(service.getStockBatchList(List.of())).isEmpty();
        assertThat(selectCalled).isFalse();
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
