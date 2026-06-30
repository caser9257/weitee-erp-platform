package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockInDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockInMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.STOCK_IN_MANUAL_STATUS_UPDATE_FORBIDDEN;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.STOCK_IN_UPDATE_FAIL_PROCESSING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpStockInServiceImplTest {

    @Test
    void updateStockInStatusManually_shouldThrowForbidden() {
        ErpStockInServiceImpl service = new ErpStockInServiceImpl();

        ServiceException ex = assertThrows(ServiceException.class, () ->
                service.updateStockInStatusManually(9L, ErpAuditStatus.APPROVE.getStatus()));

        assertEquals(STOCK_IN_MANUAL_STATUS_UPDATE_FORBIDDEN.getCode(), ex.getCode());
    }

    @Test
    void rollbackStockInStatusToDraftByBpm_shouldThrowWhenNotProcessing() throws Exception {
        ErpStockInServiceImpl service = new ErpStockInServiceImpl();
        setField(service, "erpStockInMapper", createProxy(ErpStockInMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpStockInDO().setId(10L).setStatus(ErpAuditStatus.DRAFT.getStatus());
            }
            if ("resetStatusToDraftByBpm".equals(methodName)) {
                return 0;
            }
            return null;
        }));

        ServiceException ex = assertThrows(ServiceException.class, () ->
                service.rollbackStockInStatusToDraftByBpm(10L, "PI-010", "cancel"));

        assertEquals(STOCK_IN_UPDATE_FAIL_PROCESSING.getCode(), ex.getCode());
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
