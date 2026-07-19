package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.module.erp.controller.admin.stock.vo.batch.ErpStockBatchAdjustReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchAdjustmentDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockBatchAdjustmentMapper;
import cn.weitee.erp.module.erp.enums.stock.ErpStockBatchAdjustTypeEnum;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ErpStockBatchAdjustmentServiceImplTest {

    @Test
    void adjustBatch_shouldSynchronizeStockSummaryWithBatchAdjustment() throws Exception {
        ErpStockBatchAdjustmentServiceImpl service = new ErpStockBatchAdjustmentServiceImpl();
        AtomicReference<Object[]> stockIncrementArgs = new AtomicReference<>();

        setField(service, "stockBatchAdjustmentMapper", createProxy(ErpStockBatchAdjustmentMapper.class, (methodName, args) -> {
            if ("selectByAdjustNo".equals(methodName)) {
                return new ErpStockBatchAdjustmentDO().setId(501L).setAdjustNo("TZ202604290004");
            }
            return null;
        }));
        setField(service, "stockBatchService", createProxy(ErpStockBatchService.class, (methodName, args) -> {
            if ("adjustBatch".equals(methodName)) {
                return new ErpStockBatchDO().setId(11L).setProductId(1L).setWarehouseId(2L);
            }
            return null;
        }));
        trySetField(service, "stockService", createProxy(ErpStockService.class, (methodName, args) -> {
            if ("updateStockCountIncrement".equals(methodName)) {
                stockIncrementArgs.set(args);
                return BigDecimal.ZERO;
            }
            return null;
        }));

        ErpStockBatchAdjustReqVO reqVO = new ErpStockBatchAdjustReqVO();
        reqVO.setStockBatchId(11L);
        reqVO.setAdjustType(ErpStockBatchAdjustTypeEnum.INCREASE.getType());
        reqVO.setCount(new BigDecimal("3.000"));
        reqVO.setAdjustNo("TZ202604290004");

        service.adjustBatch(reqVO);

        assertThat(stockIncrementArgs.get()).containsExactly(1L, 2L, new BigDecimal("3.000"));
    }

    @Test
    void adjustBatch_shouldCreateAdjustmentMasterAndUseAdjustmentIdAsRecordBizId() throws Exception {
        ErpStockBatchAdjustmentServiceImpl service = new ErpStockBatchAdjustmentServiceImpl();
        AtomicReference<ErpStockBatchAdjustReqVO> delegatedReqRef = new AtomicReference<>();

        setField(service, "stockBatchAdjustmentMapper", createProxy(ErpStockBatchAdjustmentMapper.class, (methodName, args) -> {
            if ("selectByAdjustNo".equals(methodName)) {
                return new ErpStockBatchAdjustmentDO().setId(501L).setAdjustNo("TZ202604290003")
                        .setStockBatchId(11L).setBeforeAvailableQty(new BigDecimal("5.000"))
                        .setBeforeTotalQty(new BigDecimal("5.000")).setAfterAvailableQty(new BigDecimal("8.000"))
                        .setAfterTotalQty(new BigDecimal("8.000"));
            }
            return null;
        }));
        setField(service, "stockBatchService", createProxy(ErpStockBatchService.class, (methodName, args) -> {
            if ("adjustBatch".equals(methodName)) {
                delegatedReqRef.set((ErpStockBatchAdjustReqVO) args[0]);
                return new ErpStockBatchDO().setId(11L).setProductId(1L).setWarehouseId(2L);
            }
            return null;
        }));
        setField(service, "stockService", createProxy(ErpStockService.class, (methodName, args) -> {
            if ("updateStockCountIncrement".equals(methodName)) {
                return BigDecimal.ZERO;
            }
            return null;
        }));

        ErpStockBatchAdjustReqVO reqVO = new ErpStockBatchAdjustReqVO();
        reqVO.setStockBatchId(11L);
        reqVO.setAdjustType(ErpStockBatchAdjustTypeEnum.INCREASE.getType());
        reqVO.setCount(new BigDecimal("3.000"));
        reqVO.setAdjustNo("TZ202604290003");
        reqVO.setRemark("期初补录");

        ErpStockBatchAdjustmentDO result = service.adjustBatch(reqVO);

        assertThat(result.getId()).isEqualTo(501L);
        assertThat(delegatedReqRef.get()).isSameAs(reqVO);
        assertThat(result)
                .extracting(ErpStockBatchAdjustmentDO::getStockBatchId,
                        ErpStockBatchAdjustmentDO::getBeforeAvailableQty,
                        ErpStockBatchAdjustmentDO::getBeforeTotalQty)
                .containsExactly(11L, new BigDecimal("5.000"), new BigDecimal("5.000"));
        assertThat(result)
                .extracting(ErpStockBatchAdjustmentDO::getAfterAvailableQty,
                        ErpStockBatchAdjustmentDO::getAfterTotalQty)
                .containsExactly(new BigDecimal("8.000"), new BigDecimal("8.000"));
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

    private void trySetField(Object target, String fieldName, Object value) throws IllegalAccessException {
        try {
            Field field = findField(target.getClass(), fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException ignored) {
            // The red test keeps the dependency optional until production code adds it.
        }
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
