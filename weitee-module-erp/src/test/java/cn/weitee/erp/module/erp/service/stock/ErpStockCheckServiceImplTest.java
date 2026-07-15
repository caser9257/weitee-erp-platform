package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.warehouse.ErpWarehouseSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockCheckDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockCheckItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockCheckItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockCheckMapper;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.enums.stock.ErpStockCheckStatusEnum;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceVoucherService;
import cn.weitee.erp.module.erp.service.mrp.ErpProductionCostService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.STOCK_CHECK_VOUCHER_GENERATE_FAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpStockCheckServiceImplTest {

    @Test
    void updateStockCheckStatus_whenVoucherGenerationFails_shouldThrowAndNotWriteClosed() throws Exception {
        ErpStockCheckServiceImpl service = new ErpStockCheckServiceImpl();
        Long checkId = 1001L;
        AtomicReference<Integer> lastWrittenStatus = new AtomicReference<>();

        setField(service, "erpStockCheckMapper", createProxy(ErpStockCheckMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpStockCheckDO()
                        .setId(checkId)
                        .setNo("PD20260707001")
                        .setStatus(ErpStockCheckStatusEnum.REVIEWING.getStatus());
            }
            if ("updateById".equals(methodName)) {
                ErpStockCheckDO updateObj = (ErpStockCheckDO) args[0];
                lastWrittenStatus.set(updateObj.getStatus());
                return 1;
            }
            return null;
        }));
        setField(service, "erpStockCheckItemMapper", createProxy(ErpStockCheckItemMapper.class, (methodName, args) -> {
            if ("selectListByCheckId".equals(methodName)) {
                return List.of(new ErpStockCheckItemDO()
                        .setId(2001L)
                        .setCheckId(checkId)
                        .setProductId(3001L)
                        .setWarehouseId(4001L)
                        .setCount(BigDecimal.ZERO)
                        .setProductPrice(BigDecimal.TEN));
            }
            return null;
        }));
        setField(service, "stockService", createProxy(ErpStockService.class, (methodName, args) -> {
            if ("getStockListByProductAndWarehouseIds".equals(methodName)) {
                return List.of();
            }
            return null;
        }));
        setField(service, "stockRecordService", createProxy(ErpStockRecordService.class, (methodName, args) -> null));
        setField(service, "productionCostService", createProxy(ErpProductionCostService.class, (methodName, args) -> null));
        setField(service, "voucherService", createProxy(ErpFinanceVoucherService.class, (methodName, args) -> {
            if ("autoGenerateVoucher".equals(methodName)) {
                assertEquals(ErpBizTypeEnum.STOCK_CHECK.getType(), args[0]);
                assertEquals(checkId, args[1]);
                throw new RuntimeException("voucher rule missing");
            }
            return null;
        }));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.updateStockCheckStatus(checkId, ErpStockCheckStatusEnum.APPROVED.getStatus()));

        assertEquals(STOCK_CHECK_VOUCHER_GENERATE_FAIL.getCode(), ex.getCode());
        assertEquals(ErpStockCheckStatusEnum.APPROVED.getStatus(), lastWrittenStatus.get());
    }

    @Test
    void startCounting_shouldFreezeWarehouseWithCompleteSaveRequest() throws Exception {
        ErpStockCheckServiceImpl service = new ErpStockCheckServiceImpl();
        Long checkId = 1002L;
        Long warehouseId = 4002L;
        AtomicReference<ErpWarehouseSaveReqVO> frozenRequest = new AtomicReference<>();

        setField(service, "erpStockCheckMapper", createProxy(ErpStockCheckMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpStockCheckDO().setId(checkId)
                        .setStatus(ErpStockCheckStatusEnum.DRAFT.getStatus());
            }
            if ("updateById".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(service, "erpStockCheckItemMapper", createProxy(ErpStockCheckItemMapper.class, (methodName, args) -> {
            if ("selectListByCheckId".equals(methodName)) {
                return List.of(new ErpStockCheckItemDO().setWarehouseId(warehouseId));
            }
            return null;
        }));
        setField(service, "stockCheckSnapshotService", createProxy(ErpStockCheckSnapshotService.class,
                (methodName, args) -> "createSnapshot".equals(methodName) ? 1 : null));
        setField(service, "warehouseService", createProxy(ErpWarehouseService.class, (methodName, args) -> {
            if ("getWarehouse".equals(methodName)) {
                return ErpWarehouseDO.builder().id(warehouseId).name("验收仓")
                        .categoryId(10L).sort(1L).status(0).frozen(false).build();
            }
            if ("updateWarehouse".equals(methodName)) {
                frozenRequest.set((ErpWarehouseSaveReqVO) args[0]);
            }
            return null;
        }));

        service.startCounting(checkId);

        assertEquals(warehouseId, frozenRequest.get().getId());
        assertEquals("验收仓", frozenRequest.get().getName());
        assertEquals(10L, frozenRequest.get().getCategoryId());
        assertEquals(1L, frozenRequest.get().getSort());
        assertEquals(0, frozenRequest.get().getStatus());
        assertEquals(true, frozenRequest.get().getFrozen());
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
