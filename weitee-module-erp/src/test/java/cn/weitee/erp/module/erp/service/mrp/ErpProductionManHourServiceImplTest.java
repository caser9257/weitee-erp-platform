package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionManHourSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionManHourDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionCostAllocationMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionManHourMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsMrpExt.PRODUCTION_ACCOUNTING_MONTH_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsMrpExt.PRODUCTION_MAN_HOUR_MONTH_LOCKED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpProductionManHourServiceImplTest {

    @Test
    void createProductionManHour_shouldCreateWhenValid() throws Exception {
        ErpProductionManHourServiceImpl service = new ErpProductionManHourServiceImpl();
        AtomicReference<ErpProductionManHourDO> insertedRef = new AtomicReference<>();

        setField(service, "productionOrderService", createProxy(ErpProductionOrderService.class, (methodName, args) -> {
            if ("getProductionOrder".equals(methodName)) {
                return new ErpProductionOrderDO().setId(1L).setOrderNo("SCGD202604280001");
            }
            return null;
        }));
        setField(service, "productionManHourMapper", createProxy(ErpProductionManHourMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ErpProductionManHourDO manHour = (ErpProductionManHourDO) args[0];
                manHour.setId(9001L);
                insertedRef.set(manHour);
                return 1;
            }
            return null;
        }));
        setField(service, "productionCostAllocationMapper", createProxy(ErpProductionCostAllocationMapper.class, (methodName, args) -> {
            if ("selectCountExecutedByAccountingMonth".equals(methodName)) {
                return 0L;
            }
            return null;
        }));

        Long id = service.createProductionManHour(new ErpProductionManHourSaveReqVO()
                .setProductionOrderId(1L)
                .setAccountingMonth("2026-04")
                .setWorkDate(LocalDate.of(2026, 4, 20))
                .setManHour(new BigDecimal("8.50"))
                .setRemark("4 月总装工时"));

        assertEquals(9001L, id);
        assertEquals(1L, insertedRef.get().getProductionOrderId());
        assertEquals("2026-04", insertedRef.get().getAccountingMonth());
        assertEquals(LocalDate.of(2026, 4, 20), insertedRef.get().getWorkDate());
        assertEquals(new BigDecimal("8.50"), insertedRef.get().getManHour());
        assertEquals("4 月总装工时", insertedRef.get().getRemark());
    }

    @Test
    void createProductionManHour_shouldRejectWhenAccountingMonthLocked() throws Exception {
        ErpProductionManHourServiceImpl service = new ErpProductionManHourServiceImpl();

        setField(service, "productionOrderService", createProxy(ErpProductionOrderService.class, (methodName, args) -> {
            if ("getProductionOrder".equals(methodName)) {
                return new ErpProductionOrderDO().setId(1L);
            }
            return null;
        }));
        setField(service, "productionManHourMapper", createProxy(ErpProductionManHourMapper.class, (methodName, args) -> null));
        setField(service, "productionCostAllocationMapper", createProxy(ErpProductionCostAllocationMapper.class, (methodName, args) -> {
            if ("selectCountExecutedByAccountingMonth".equals(methodName)) {
                return 1L;
            }
            return null;
        }));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createProductionManHour(new ErpProductionManHourSaveReqVO()
                .setProductionOrderId(1L)
                .setAccountingMonth("2026-04")
                .setWorkDate(LocalDate.of(2026, 4, 20))
                .setManHour(new BigDecimal("8.00"))));

        assertEquals(PRODUCTION_MAN_HOUR_MONTH_LOCKED.getCode(), ex.getCode());
    }

    @Test
    void updateProductionManHour_shouldRejectWhenWorkDateNotInAccountingMonth() throws Exception {
        ErpProductionManHourServiceImpl service = new ErpProductionManHourServiceImpl();
        ErpProductionManHourDO stored = new ErpProductionManHourDO()
                .setId(1001L)
                .setProductionOrderId(1L)
                .setAccountingMonth("2026-04")
                .setWorkDate(LocalDate.of(2026, 4, 20))
                .setManHour(new BigDecimal("6.00"));

        setField(service, "productionOrderService", createProxy(ErpProductionOrderService.class, (methodName, args) -> {
            if ("getProductionOrder".equals(methodName)) {
                return new ErpProductionOrderDO().setId(1L);
            }
            return null;
        }));
        setField(service, "productionManHourMapper", createProxy(ErpProductionManHourMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return stored;
            }
            return null;
        }));
        setField(service, "productionCostAllocationMapper", createProxy(ErpProductionCostAllocationMapper.class, (methodName, args) -> {
            if ("selectCountExecutedByAccountingMonth".equals(methodName)) {
                return 0L;
            }
            return null;
        }));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateProductionManHour(new ErpProductionManHourSaveReqVO()
                .setId(1001L)
                .setProductionOrderId(1L)
                .setAccountingMonth("2026-04")
                .setWorkDate(LocalDate.of(2026, 5, 1))
                .setManHour(new BigDecimal("6.50"))));

        assertEquals(PRODUCTION_ACCOUNTING_MONTH_INVALID.getCode(), ex.getCode());
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
        Field field = getDeclaredField(target, fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field getDeclaredField(Object target, String fieldName) throws NoSuchFieldException {
        try {
            return target.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            return target.getClass().getDeclaredField(mapFieldName(fieldName));
        }
    }

    private String mapFieldName(String fieldName) {
        if (fieldName.endsWith("Mapper")) {
            return "erp" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        }
        return fieldName;
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args);
    }

}
