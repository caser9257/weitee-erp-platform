package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockOutDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockOutItemDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockOutItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockOutMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchAllocateOutboundReqBO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErpStockOutServiceImplTest {

    @Test
    void updateStockOutStatus_shouldAllocateBatchWhenApprove() throws Exception {
        ErpStockOutServiceImpl service = new ErpStockOutServiceImpl();
        AtomicReference<ErpStockBatchAllocateOutboundReqBO> allocateReqRef = new AtomicReference<>();

        setField(service, "stockOutMapper", createProxy(ErpStockOutMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpStockOutDO().setId(200L).setNo("QTCK202604290001")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus());
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(service, "stockOutItemMapper", createProxy(ErpStockOutItemMapper.class, (methodName, args) -> {
            if ("selectListByOutId".equals(methodName)) {
                return List.of(new ErpStockOutItemDO().setId(201L).setOutId(200L)
                        .setProductId(1L).setWarehouseId(2L).setCount(new BigDecimal("3.000")));
            }
            return null;
        }));
        setField(service, "stockBatchAllocationService", createProxy(ErpStockBatchAllocationService.class, (methodName, args) -> {
            if ("allocateOutbound".equals(methodName)) {
                allocateReqRef.set((ErpStockBatchAllocateOutboundReqBO) args[0]);
                return List.of();
            }
            return null;
        }));
        setField(service, "stockRecordService", createProxy(ErpStockRecordService.class, (methodName, args) -> null));

        service.updateStockOutStatus(200L, ErpAuditStatus.APPROVE.getStatus());

        assertEquals(ErpStockRecordBizTypeEnum.OTHER_OUT.getType(), allocateReqRef.get().getBizType());
        assertEquals(200L, allocateReqRef.get().getBizId());
        assertEquals(201L, allocateReqRef.get().getBizItemId());
        assertEquals(new BigDecimal("3.000"), allocateReqRef.get().getCount());
    }

    @Test
    void updateStockOutStatus_shouldRollbackBatchWhenProcess() throws Exception {
        ErpStockOutServiceImpl service = new ErpStockOutServiceImpl();
        AtomicReference<Object[]> rollbackArgsRef = new AtomicReference<>();

        setField(service, "stockOutMapper", createProxy(ErpStockOutMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpStockOutDO().setId(200L).setNo("QTCK202604290001")
                        .setStatus(ErpAuditStatus.APPROVE.getStatus());
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(service, "stockOutItemMapper", createProxy(ErpStockOutItemMapper.class, (methodName, args) -> {
            if ("selectListByOutId".equals(methodName)) {
                return List.of(new ErpStockOutItemDO().setId(201L).setOutId(200L)
                        .setProductId(1L).setWarehouseId(2L).setCount(new BigDecimal("3.000")));
            }
            return null;
        }));
        setField(service, "stockBatchAllocationService", createProxy(ErpStockBatchAllocationService.class, (methodName, args) -> {
            if ("rollbackOutbound".equals(methodName)) {
                rollbackArgsRef.set(args);
            }
            return null;
        }));
        setField(service, "stockRecordService", createProxy(ErpStockRecordService.class, (methodName, args) -> null));

        service.updateStockOutStatus(200L, ErpAuditStatus.PROCESS.getStatus());

        assertEquals(ErpStockRecordBizTypeEnum.OTHER_OUT.getType(), rollbackArgsRef.get()[0]);
        assertEquals(200L, rollbackArgsRef.get()[1]);
        assertEquals(ErpStockRecordBizTypeEnum.OTHER_OUT_CANCEL.getType(), rollbackArgsRef.get()[2]);
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
