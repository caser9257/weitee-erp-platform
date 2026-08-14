package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.warehouse.ErpWarehouseSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockCheckDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockCheckItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockCheckItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockCheckMapper;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.warehouse.ErpWarehouseSaveReqVO;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.enums.stock.ErpStockCheckStatusEnum;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceVoucherService;
import cn.weitee.erp.module.erp.service.mrp.ErpProductionCostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.STOCK_CHECK_VOUCHER_GENERATE_FAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpStockCheckServiceImplTest {

    @Test
    void startCounting_shouldFreezeWarehouseWithCompleteRequiredFields() throws Exception {
        ErpStockCheckServiceImpl service = new ErpStockCheckServiceImpl();
        Long checkId = 1002L;
        Long warehouseId = 4002L;
        ErpStockCheckMapper checkMapper = mock(ErpStockCheckMapper.class);
        ErpStockCheckItemMapper itemMapper = mock(ErpStockCheckItemMapper.class);
        ErpStockCheckSnapshotService snapshotService = mock(ErpStockCheckSnapshotService.class);
        ErpWarehouseService warehouseService = mock(ErpWarehouseService.class);
        ErpWarehouseDO warehouse = new ErpWarehouseDO()
                .setId(warehouseId)
                .setName("测试仓库")
                .setCategoryId(5002L)
                .setSort(10L)
                .setStatus(0)
                .setFrozen(false);

        when(checkMapper.selectById(checkId)).thenReturn(new ErpStockCheckDO()
                .setId(checkId).setStatus(ErpStockCheckStatusEnum.DRAFT.getStatus()));
        when(snapshotService.createSnapshot(checkId)).thenReturn(1);
        when(itemMapper.selectListByCheckId(checkId)).thenReturn(List.of(new ErpStockCheckItemDO()
                .setCheckId(checkId).setWarehouseId(warehouseId)));
        when(warehouseService.validWarehouseList(List.of(warehouseId))).thenReturn(List.of(warehouse));

        setField(service, "erpStockCheckMapper", checkMapper);
        setField(service, "erpStockCheckItemMapper", itemMapper);
        setField(service, "stockCheckSnapshotService", snapshotService);
        setField(service, "warehouseService", warehouseService);

        service.startCounting(checkId);

        ArgumentCaptor<ErpWarehouseSaveReqVO> captor = ArgumentCaptor.forClass(ErpWarehouseSaveReqVO.class);
        verify(warehouseService).updateWarehouse(captor.capture());
        ErpWarehouseSaveReqVO updateReqVO = captor.getValue();
        assertEquals(warehouseId, updateReqVO.getId());
        assertEquals("测试仓库", updateReqVO.getName());
        assertEquals(5002L, updateReqVO.getCategoryId());
        assertEquals(10L, updateReqVO.getSort());
        assertEquals(0, updateReqVO.getStatus());
        assertEquals(true, updateReqVO.getFrozen());
        verify(checkMapper).updateById(any(ErpStockCheckDO.class));
    }

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
    void startCounting_shouldPreserveWarehouseFieldsWhenFreezing() throws Exception {
        ErpStockCheckServiceImpl service = new ErpStockCheckServiceImpl();
        Long checkId = 1002L;
        AtomicReference<ErpWarehouseSaveReqVO> freezeRequestRef = new AtomicReference<>();

        setField(service, "erpStockCheckMapper", createProxy(ErpStockCheckMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpStockCheckDO()
                        .setId(checkId)
                        .setStatus(ErpStockCheckStatusEnum.DRAFT.getStatus());
            }
            return "updateById".equals(methodName) ? 1 : null;
        }));
        setField(service, "erpStockCheckItemMapper", createProxy(ErpStockCheckItemMapper.class,
                (methodName, args) -> "selectListByCheckId".equals(methodName)
                        ? List.of(new ErpStockCheckItemDO().setId(2002L).setCheckId(checkId)
                        .setProductId(3002L).setWarehouseId(4002L)) : null));
        setField(service, "stockCheckSnapshotService", createProxy(ErpStockCheckSnapshotService.class,
                (methodName, args) -> "createSnapshot".equals(methodName) ? 1 : null));
        ErpWarehouseDO warehouse = new ErpWarehouseDO()
                .setId(4002L)
                .setCategoryId(5002L)
                .setName("原料仓")
                .setAddress("A区")
                .setSort(3L)
                .setRemark("盘点仓库")
                .setPrincipal("仓管员")
                .setWarehousePrice(new BigDecimal("10.00"))
                .setTruckagePrice(new BigDecimal("2.00"))
                .setStatus(0)
                .setDefaultStatus(true)
                .setFrozen(false);
        setField(service, "warehouseService", createProxy(ErpWarehouseService.class, (methodName, args) -> {
            if ("validWarehouseList".equals(methodName)) {
                return List.of(warehouse);
            }
            if ("updateWarehouse".equals(methodName)) {
                freezeRequestRef.set((ErpWarehouseSaveReqVO) args[0]);
            }
            return null;
        }));

        service.startCounting(checkId);

        ErpWarehouseSaveReqVO request = freezeRequestRef.get();
        assertEquals(warehouse.getId(), request.getId());
        assertEquals(warehouse.getCategoryId(), request.getCategoryId());
        assertEquals(warehouse.getName(), request.getName());
        assertEquals(warehouse.getSort(), request.getSort());
        assertEquals(warehouse.getStatus(), request.getStatus());
        assertTrue(request.getFrozen());
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
