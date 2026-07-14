package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockInDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockInItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockInMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.STOCK_IN_MANUAL_STATUS_UPDATE_FORBIDDEN;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.STOCK_IN_STATUS_UPDATE_ILLEGAL;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.STOCK_IN_UPDATE_FAIL_PROCESSING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpStockInServiceImplTest {

    @Mock
    private ErpStockInMapper approvalMapper;
    @Mock
    private ErpStockInItemMapper approvalItemMapper;
    @Mock
    private ErpStockService approvalStockService;

    @InjectMocks
    private ErpStockInServiceImpl approvalService;

    @Test
    void updateStockInStatusManually_shouldThrowForbidden() {
        ErpStockInServiceImpl service = new ErpStockInServiceImpl();

        ServiceException ex = assertThrows(ServiceException.class, () ->
                service.updateStockInStatusManually(9L, ErpAuditStatus.APPROVE.getStatus()));

        assertEquals(STOCK_IN_MANUAL_STATUS_UPDATE_FORBIDDEN.getCode(), ex.getCode());
    }

    @Test
    void updateStockInStatusByBpm_shouldRejectMismatchedProcessInstanceId() throws Exception {
        ErpStockInServiceImpl service = new ErpStockInServiceImpl();
        setField(service, "erpStockInMapper", createProxy(ErpStockInMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpStockInDO().setId(10L)
                        .setStatus(ErpAuditStatus.PROCESS.getStatus())
                        .setProcessInstanceId("PI-BOUND");
            }
            return null;
        }));

        ServiceException ex = assertThrows(ServiceException.class, () ->
                service.updateStockInStatusByBpm(10L, "PI-OTHER", ErpAuditStatus.APPROVE.getStatus(), "approved"));

        assertEquals(STOCK_IN_STATUS_UPDATE_ILLEGAL.getCode(), ex.getCode());
    }

    @Test
    void updateStockInStatusByBpm_shouldIgnoreLateCallbackWhenOrderAlreadyHandled() throws Exception {
        ErpStockInServiceImpl service = new ErpStockInServiceImpl();
        AtomicReference<Boolean> updateCalledRef = new AtomicReference<>(false);
        setField(service, "erpStockInMapper", createProxy(ErpStockInMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpStockInDO().setId(10L)
                        .setStatus(ErpAuditStatus.APPROVE.getStatus())
                        .setProcessInstanceId("PI-BOUND");
            }
            if ("updateByIdStatusAndProcessInstanceId".equals(methodName)) {
                updateCalledRef.set(true);
                return 1;
            }
            return null;
        }));

        service.updateStockInStatusByBpm(10L, "PI-BOUND", ErpAuditStatus.APPROVE.getStatus(), "approved");

        assertEquals(Boolean.FALSE, updateCalledRef.get());
    }

    @Test
    void updateStockInStatusByBpm_shouldClearProcessInstanceIdAfterApproval() {
        Long stockInId = 10L;
        String processInstanceId = "PI-BOUND";
        when(approvalMapper.selectById(stockInId)).thenReturn(new ErpStockInDO().setId(stockInId)
                .setNo("QTRK20260714000001")
                .setStatus(ErpAuditStatus.PROCESS.getStatus())
                .setProcessInstanceId(processInstanceId));
        when(approvalMapper.updateByIdStatusAndProcessInstanceId(any(), any(), any(), any())).thenReturn(1);
        when(approvalItemMapper.selectListByInId(stockInId)).thenReturn(Collections.emptyList());
        when(approvalStockService.getStockListByProductIds(anySet())).thenReturn(Collections.emptyList());

        approvalService.updateStockInStatusByBpm(stockInId, processInstanceId,
                ErpAuditStatus.APPROVE.getStatus(), "approved");

        verify(approvalMapper).clearProcessInstanceId(stockInId, processInstanceId);
    }

    @Test
    void rollbackStockInStatusToDraftByBpm_shouldIgnoreLateCallbackWhenNotProcessing() throws Exception {
        ErpStockInServiceImpl service = new ErpStockInServiceImpl();
        AtomicReference<Boolean> rollbackCalledRef = new AtomicReference<>(false);
        setField(service, "erpStockInMapper", createProxy(ErpStockInMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpStockInDO().setId(10L).setStatus(ErpAuditStatus.DRAFT.getStatus());
            }
            if ("resetStatusToDraftByBpm".equals(methodName)) {
                rollbackCalledRef.set(true);
                return 0;
            }
            return null;
        }));

        service.rollbackStockInStatusToDraftByBpm(10L, "PI-010", "cancel");

        assertEquals(Boolean.FALSE, rollbackCalledRef.get());
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> type, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        if ("toString".equals(method.getName())) return type.getSimpleName() + "Proxy";
                        if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
                        if ("equals".equals(method.getName())) return proxy == args[0];
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
