package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockOutDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockOutItemDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockOutItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockOutMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.weitee.erp.module.erp.service.stock.ErpStockService;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchAllocateOutboundReqBO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.STOCK_OUT_MANUAL_STATUS_UPDATE_FORBIDDEN;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.STOCK_OUT_STATUS_UPDATE_ILLEGAL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.STOCK_OUT_UPDATE_FAIL_PROCESSING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpStockOutServiceImplTest {

    @Mock
    private ErpStockOutMapper approvalMapper;
    @Mock
    private ErpStockOutItemMapper approvalItemMapper;
    @Mock
    private ErpStockService approvalStockService;
    @Mock
    private ErpStockBatchAllocationService approvalBatchAllocationService;

    @InjectMocks
    private ErpStockOutServiceImpl approvalService;

    @Test
    void updateStockOutStatus_shouldAllocateBatchWhenApprove() throws Exception {
        ErpStockOutServiceImpl service = new ErpStockOutServiceImpl();
        AtomicReference<ErpStockBatchAllocateOutboundReqBO> allocateReqRef = new AtomicReference<>();

        setField(service, "erpStockOutMapper", createProxy(ErpStockOutMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpStockOutDO().setId(200L).setNo("QTCK202604290001")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus());
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(service, "erpStockOutItemMapper", createProxy(ErpStockOutItemMapper.class, (methodName, args) -> {
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
        setField(service, "stockService", createProxy(ErpStockService.class, (methodName, args) -> List.of()));

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

        setField(service, "erpStockOutMapper", createProxy(ErpStockOutMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpStockOutDO().setId(200L).setNo("QTCK202604290001")
                        .setStatus(ErpAuditStatus.APPROVE.getStatus());
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(service, "erpStockOutItemMapper", createProxy(ErpStockOutItemMapper.class, (methodName, args) -> {
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
        setField(service, "stockService", createProxy(ErpStockService.class, (methodName, args) -> List.of()));

        service.updateStockOutStatus(200L, ErpAuditStatus.PROCESS.getStatus());

        assertEquals(ErpStockRecordBizTypeEnum.OTHER_OUT.getType(), rollbackArgsRef.get()[0]);
        assertEquals(200L, rollbackArgsRef.get()[1]);
        assertEquals(ErpStockRecordBizTypeEnum.OTHER_OUT_CANCEL.getType(), rollbackArgsRef.get()[2]);
    }

    @Test
    void updateStockOutStatusManually_shouldThrowForbidden() {
        ErpStockOutServiceImpl service = new ErpStockOutServiceImpl();

        ServiceException ex = assertThrows(ServiceException.class, () ->
                service.updateStockOutStatusManually(9L, ErpAuditStatus.APPROVE.getStatus()));

        assertEquals(STOCK_OUT_MANUAL_STATUS_UPDATE_FORBIDDEN.getCode(), ex.getCode());
    }

    @Test
    void updateStockOutStatusByBpm_shouldRejectMismatchedProcessInstanceId() throws Exception {
        ErpStockOutServiceImpl service = new ErpStockOutServiceImpl();
        setField(service, "erpStockOutMapper", createProxy(ErpStockOutMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpStockOutDO().setId(10L)
                        .setStatus(ErpAuditStatus.PROCESS.getStatus())
                        .setProcessInstanceId("PI-BOUND");
            }
            return null;
        }));

        ServiceException ex = assertThrows(ServiceException.class, () ->
                service.updateStockOutStatusByBpm(10L, "PI-OTHER", ErpAuditStatus.APPROVE.getStatus(), "approved"));

        assertEquals(STOCK_OUT_STATUS_UPDATE_ILLEGAL.getCode(), ex.getCode());
    }

    @Test
    void updateStockOutStatusByBpm_shouldIgnoreLateCallbackWhenOrderAlreadyHandled() throws Exception {
        ErpStockOutServiceImpl service = new ErpStockOutServiceImpl();
        AtomicReference<Boolean> updateCalledRef = new AtomicReference<>(false);
        setField(service, "erpStockOutMapper", createProxy(ErpStockOutMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpStockOutDO().setId(10L)
                        .setStatus(ErpAuditStatus.APPROVE.getStatus())
                        .setProcessInstanceId("PI-BOUND");
            }
            if ("updateByIdStatusAndProcessInstanceId".equals(methodName)) {
                updateCalledRef.set(true);
                return 1;
            }
            return null;
        }));

        service.updateStockOutStatusByBpm(10L, "PI-BOUND", ErpAuditStatus.APPROVE.getStatus(), "approved");

        assertEquals(Boolean.FALSE, updateCalledRef.get());
    }

    @Test
    void updateStockOutStatusByBpm_shouldClearProcessInstanceIdAfterApproval() {
        Long stockOutId = 10L;
        String processInstanceId = "PI-BOUND";
        when(approvalMapper.selectById(stockOutId)).thenReturn(new ErpStockOutDO().setId(stockOutId)
                .setNo("QCKD20260714000002")
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId(processInstanceId));
        when(approvalMapper.updateByIdStatusAndProcessInstanceId(any(), any(), any(), any())).thenReturn(1);
        when(approvalItemMapper.selectListByOutId(stockOutId)).thenReturn(Collections.emptyList());
        when(approvalStockService.getStockListByProductIds(anySet())).thenReturn(Collections.emptyList());

        approvalService.updateStockOutStatusByBpm(stockOutId, processInstanceId,
                ErpAuditStatus.APPROVE.getStatus(), "approved");

        verify(approvalMapper).clearProcessInstanceId(stockOutId, processInstanceId);
    }

    @Test
    void rollbackStockOutStatusToDraftByBpm_shouldIgnoreLateCallbackWhenNotProcessing() throws Exception {
        ErpStockOutServiceImpl service = new ErpStockOutServiceImpl();
        AtomicReference<Boolean> rollbackCalledRef = new AtomicReference<>(false);
        setField(service, "erpStockOutMapper", createProxy(ErpStockOutMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpStockOutDO().setId(10L).setStatus(ErpAuditStatus.DRAFT.getStatus());
            }
            if ("resetStatusToDraftByBpm".equals(methodName)) {
                rollbackCalledRef.set(true);
                return 0;
            }
            return null;
        }));

        service.rollbackStockOutStatusToDraftByBpm(10L, "PI-010", "cancel");

        assertEquals(Boolean.FALSE, rollbackCalledRef.get());
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
