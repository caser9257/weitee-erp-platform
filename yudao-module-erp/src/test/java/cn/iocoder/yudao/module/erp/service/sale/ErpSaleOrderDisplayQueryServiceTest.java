package cn.iocoder.yudao.module.erp.service.sale;

import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErpSaleOrderDisplayQueryServiceTest {

    private final AtomicReference<List<Long>> selectDisplayIdsRef = new AtomicReference<>();

    private ErpSaleOrderServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new ErpSaleOrderServiceImpl();
        selectDisplayIdsRef.set(null);
        setField(service, "saleOrderMapper", createProxy(ErpSaleOrderMapper.class, (methodName, args) -> {
            if ("selectDisplayListByIds".equals(methodName)) {
                @SuppressWarnings("unchecked")
                List<Long> ids = List.copyOf((Collection<Long>) args[0]);
                selectDisplayIdsRef.set(ids);
                return List.of(new ErpSaleOrderDO().setId(1L).setNo("XS001").setSaleUserId(2L).setCreator("3"));
            }
            return null;
        }));
    }

    @Test
    void getSaleOrderDisplayListByIds_shouldUseLightweightMapperQuery() {
        List<ErpSaleOrderDO> result = service.getSaleOrderDisplayListByIds(List.of(1L, 2L));

        assertEquals(List.of(1L, 2L), selectDisplayIdsRef.get());
        assertEquals(1, result.size());
        assertEquals("XS001", result.get(0).getNo());
        assertEquals(2L, result.get(0).getSaleUserId());
        assertEquals("3", result.get(0).getCreator());
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
