package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpWarehouseMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class ErpWarehouseServiceImplTest {

    @Test
    void getWarehouseList_shouldReturnEmptyListWhenIdsBlank() throws Exception {
        ErpWarehouseServiceImpl service = new ErpWarehouseServiceImpl();
        AtomicBoolean selectCalled = new AtomicBoolean(false);

        setField(service, "warehouseMapper", createProxy(ErpWarehouseMapper.class, (methodName, args) -> {
            if ("selectByIds".equals(methodName)) {
                selectCalled.set(true);
            }
            return null;
        }));

        assertThat(service.getWarehouseList(List.of())).isEmpty();
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
        Object handle(String methodName, Object[] args) throws Throwable;
    }

}
