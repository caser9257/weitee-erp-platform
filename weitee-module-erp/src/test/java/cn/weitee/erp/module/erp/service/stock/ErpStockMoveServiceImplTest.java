package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockMoveDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockMoveItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockMoveMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ErpStockMoveServiceImplTest {

    @Test
    void updateStockMoveStatusByBpm_shouldApproveOnlyMatchingProcess() throws Exception {
        ErpStockMoveServiceImpl service = new ErpStockMoveServiceImpl();
        setField(service, "erpStockMoveMapper", proxy(ErpStockMoveMapper.class, (name, args) -> {
            if ("selectById".equals(name)) {
                return new ErpStockMoveDO().setId(1L).setNo("DB001").setStatus(10).setProcessInstanceId("process-1");
            }
            if ("updateByIdStatusAndProcessInstanceId".equals(name)) {
                return 1;
            }
            return null;
        }));
        setField(service, "erpStockMoveItemMapper", proxy(ErpStockMoveItemMapper.class,
                (name, args) -> "selectListByMoveId".equals(name) ? Collections.emptyList() : null));
        setField(service, "stockService", proxy(ErpStockService.class,
                (name, args) -> "getStockMapByProductAndWarehouseIds".equals(name) ? Collections.emptyMap() : null));
        setField(service, "stockRecordService", proxy(ErpStockRecordService.class, (name, args) -> null));

        assertDoesNotThrow(() -> service.updateStockMoveStatusByBpm(1L, "process-1", 20, "通过"));
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, Handler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> handler.call(method.getName(), args));
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @FunctionalInterface
    private interface Handler {
        Object call(String name, Object[] args);
    }
}
