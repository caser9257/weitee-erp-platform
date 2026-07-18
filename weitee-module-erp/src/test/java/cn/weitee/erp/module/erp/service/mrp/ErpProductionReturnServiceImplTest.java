package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.returning.ErpProductionReturnCreateReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.returning.ErpProductionReturnPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.*;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.*;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.service.stock.ErpStockBatchService;
import cn.weitee.erp.module.erp.service.stock.ErpStockRecordService;
import cn.weitee.erp.module.erp.service.stock.ErpStockService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpProductionReturnServiceImplTest {

    @Test
    void getProductionReturnPage_shouldDelegateToMapper() throws Exception {
        ErpProductionReturnServiceImpl service = new ErpProductionReturnServiceImpl();
        ErpProductionReturnPageReqVO reqVO = new ErpProductionReturnPageReqVO().setProductionOrderId(1L);
        PageResult<ErpProductionReturnDO> expected = new PageResult<>(List.of(
                new ErpProductionReturnDO().setId(100L)), 1L);
        setField(service, "erpProductionReturnMapper", proxy(ErpProductionReturnMapper.class,
                (name, args) -> "selectPage".equals(name) ? expected : null));

        assertSame(expected, service.getProductionReturnPage(reqVO));
    }

    @Test
    void createProductionReturn_shouldRejectWhenAtomicReturnedQtyUpdateLosesRace() throws Exception {
        ErpProductionReturnServiceImpl service = new ErpProductionReturnServiceImpl();
        AtomicLong id = new AtomicLong(100L);
        setField(service, "productionOrderService", proxy(ErpProductionOrderService.class,
                (name, args) -> "getProductionOrder".equals(name) ? new ErpProductionOrderDO().setId(1L) : null));
        setField(service, "erpProductionMaterialMapper", proxy(ErpProductionMaterialMapper.class, (name, args) -> {
            if ("selectListByIds".equals(name)) {
                return List.of(new ErpProductionMaterialDO().setId(11L).setProductionOrderId(1L)
                        .setMaterialId(1001L).setIssuedQty(BigDecimal.ONE).setReturnedQty(BigDecimal.ZERO));
            }
            if ("updateReturnedQtyIncrement".equals(name)) return 0;
            return null;
        }));
        setField(service, "erpProductionIssueItemMapper", proxy(ErpProductionIssueItemMapper.class,
                (name, args) -> "selectListByProductionMaterialIds".equals(name)
                        ? List.of(new ErpProductionIssueItemDO().setId(21L).setProductionMaterialId(11L)
                        .setMaterialId(1001L).setWarehouseId(2001L)) : null));
        setField(service, "erpProductionIssueBatchMapper", proxy(ErpProductionIssueBatchMapper.class,
                (name, args) -> "selectListForUpdateByIds".equals(name)
                        ? List.of(new ErpProductionIssueBatchDO().setId(31L).setIssueItemId(21L)
                        .setStockBatchId(3001L).setBatchNo("B-001").setIssueQty(BigDecimal.ONE)) : null));
        setField(service, "erpProductionReturnBatchMapper", proxy(ErpProductionReturnBatchMapper.class,
                (name, args) -> "selectListByIssueBatchIds".equals(name) ? List.of() : null));
        setField(service, "erpProductionReturnMapper", proxy(ErpProductionReturnMapper.class, (name, args) -> {
            if ("insert".equals(name)) ((ErpProductionReturnDO) args[0]).setId(id.getAndIncrement());
            return 1;
        }));
        setField(service, "erpProductionReturnItemMapper", proxy(ErpProductionReturnItemMapper.class, (name, args) -> {
            if ("insert".equals(name)) ((ErpProductionReturnItemDO) args[0]).setId(id.getAndIncrement());
            return 1;
        }));
        setField(service, "stockService", proxy(ErpStockService.class, (name, args) ->
                "getStockMapByProductAndWarehouseIds".equals(name)
                        ? Map.of(ErpStockService.buildProductWarehouseKey(1001L, 2001L), new ErpStockDO()) : null));
        setField(service, "stockBatchService", proxy(ErpStockBatchService.class, (name, args) -> null));
        setField(service, "stockRecordService", proxy(ErpStockRecordService.class, (name, args) -> null));
        setField(service, "noRedisDAO", new ErpNoRedisDAO() {
            @Override public String generate(String prefix) { return "SCTL-001"; }
        });

        assertThrows(RuntimeException.class, () -> service.createProductionReturn(new ErpProductionReturnCreateReqVO()
                .setProductionOrderId(1L).setItems(List.of(new ErpProductionReturnCreateReqVO.Item()
                        .setProductionMaterialId(11L).setMaterialId(1001L).setWarehouseId(2001L)
                        .setReturnQty(BigDecimal.ONE).setBatches(List.of(new ErpProductionReturnCreateReqVO.Batch()
                                .setIssueBatchId(31L).setStockBatchId(3001L).setBatchNo("B-001")
                                .setReturnQty(BigDecimal.ONE)))))));
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, Handler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, (proxy, method, args) ->
                method.getDeclaringClass() == Object.class ? null : handler.call(method.getName(), args));
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @FunctionalInterface
    private interface Handler { Object call(String name, Object[] args); }
}
