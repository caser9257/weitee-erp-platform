package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceVoucherLogDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpFinanceVoucherLogMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ErpFinanceVoucherLogServiceTest {

    @Test
    void createVoucherLog_shouldInsertLog() throws Exception {
        ErpFinanceVoucherLogServiceImpl service = new ErpFinanceVoucherLogServiceImpl();
        AtomicReference<ErpFinanceVoucherLogDO> capturedRef = new AtomicReference<>();

        setField(service, "voucherLogMapper", createProxy(ErpFinanceVoucherLogMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                capturedRef.set((ErpFinanceVoucherLogDO) args[0]);
                return 1;
            }
            return null;
        }));

        ErpFinanceVoucherLogDO log = new ErpFinanceVoucherLogDO();
        log.setVoucherId(1L);
        log.setOperationType("generate");
        log.setOperationResult("success");
        log.setOperationDetail("自动生成凭证");
        log.setOperator("system");

        service.createVoucherLog(log);

        assertNotNull(capturedRef.get());
        assertEquals(1L, capturedRef.get().getVoucherId());
        assertEquals("generate", capturedRef.get().getOperationType());
        assertEquals("success", capturedRef.get().getOperationResult());
        assertEquals("system", capturedRef.get().getOperator());
    }

    @Test
    void getVoucherLogsByVoucherId_shouldReturnLogs() throws Exception {
        ErpFinanceVoucherLogServiceImpl service = new ErpFinanceVoucherLogServiceImpl();

        ErpFinanceVoucherLogDO log1 = new ErpFinanceVoucherLogDO();
        log1.setId(1L);
        log1.setVoucherId(1L);
        log1.setOperationType("generate");
        log1.setOperationResult("success");

        ErpFinanceVoucherLogDO log2 = new ErpFinanceVoucherLogDO();
        log2.setId(2L);
        log2.setVoucherId(1L);
        log2.setOperationType("approve");
        log2.setOperationResult("success");

        List<ErpFinanceVoucherLogDO> mockLogs = Arrays.asList(log1, log2);

        setField(service, "voucherLogMapper", createProxy(ErpFinanceVoucherLogMapper.class, (methodName, args) -> {
            if ("selectList".equals(methodName)) {
                return mockLogs;
            }
            return null;
        }));

        List<ErpFinanceVoucherLogDO> result = service.getVoucherLogsByVoucherId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getVoucherId());
        assertEquals("generate", result.get(0).getOperationType());
        assertEquals("approve", result.get(1).getOperationType());
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
