package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockMapper;
import cn.weitee.erp.module.erp.service.product.ErpProductQuantityPrecisionService;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpStockServiceImplTest {

    @Test
    void updateStockCountIncrement_shouldDeclareTransactionalBoundary() throws Exception {
        Method method = ErpStockServiceImpl.class.getMethod("updateStockCountIncrement",
                Long.class, Long.class, java.math.BigDecimal.class);
        assertTrue(method.isAnnotationPresent(Transactional.class),
                "updateStockCountIncrement should declare @Transactional");
    }

    @Test
    void updateStockCountIncrement_shouldRejectInvalidQuantityPrecisionBeforeWritingStock() throws Exception {
        ErpStockServiceImpl service = new ErpStockServiceImpl();
        AtomicBoolean insertCalled = new AtomicBoolean(false);
        AtomicBoolean updateCalled = new AtomicBoolean(false);

        setField(service, "productQuantityPrecisionService", (ErpProductQuantityPrecisionService) (productId, quantity) -> {
            throw new ServiceException(1_030_502_003, "产品【测试产品】数量最多允许 0 位小数");
        });
        setField(service, "erpStockMapper", createProxy(ErpStockMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertCalled.set(true);
            }
            if ("updateCountIncrement".equals(methodName)) {
                updateCalled.set(true);
            }
            if ("selectByProductIdAndWarehouseId".equals(methodName)) {
                return new ErpStockDO().setId(1L).setProductId(10L).setWarehouseId(20L)
                        .setCount(new BigDecimal("5.000"));
            }
            return null;
        }));

        assertThrows(ServiceException.class,
                () -> service.updateStockCountIncrement(10L, 20L, new BigDecimal("0.6")));
        assertTrue(!insertCalled.get(), "invalid precision should not insert stock");
        assertTrue(!updateCalled.get(), "invalid precision should not update stock count");
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
