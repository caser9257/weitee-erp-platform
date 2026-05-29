package cn.iocoder.yudao.module.erp.controller.admin.stock;

import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.batch.ErpStockBatchRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.stock.ErpWarehouseService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ErpStockBatchControllerTest {

    @Test
    void buildStockBatchVO_shouldSkipLookupWhenIdsMissing() throws Exception {
        ErpStockBatchController controller = new ErpStockBatchController();
        AtomicBoolean productLookupCalled = new AtomicBoolean(false);
        AtomicBoolean warehouseLookupCalled = new AtomicBoolean(false);
        AtomicReference<ErpStockBatchDO> stockBatchRef = new AtomicReference<>(new ErpStockBatchDO()
                .setId(11L)
                .setBatchNo("B001")
                .setAvailableQty(new BigDecimal("5.000"))
                .setTotalQty(new BigDecimal("5.000"))
                .setLockedQty(BigDecimal.ZERO)
                .setInboundTime(LocalDateTime.of(2026, 4, 30, 9, 0)));

        setField(controller, "productService", createProxy(ErpProductService.class, (methodName, args) -> {
            if ("getProductVOMap".equals(methodName)) {
                productLookupCalled.set(true);
            }
            return Collections.emptyMap();
        }));
        setField(controller, "warehouseService", createProxy(ErpWarehouseService.class, (methodName, args) -> {
            if ("getWarehouseMap".equals(methodName)) {
                warehouseLookupCalled.set(true);
            }
            return Collections.emptyMap();
        }));

        ErpStockBatchDO stockBatch = stockBatchRef.get();
        stockBatch.setProductId(null);
        stockBatch.setWarehouseId(null);

        ErpStockBatchRespVO respVO = invokeBuildStockBatchVO(controller, stockBatch);

        assertThat(respVO.getId()).isEqualTo(11L);
        assertThat(respVO.getBatchNo()).isEqualTo("B001");
        assertThat(respVO.getProductName()).isNull();
        assertThat(respVO.getWarehouseName()).isNull();
        assertThat(productLookupCalled).isFalse();
        assertThat(warehouseLookupCalled).isFalse();
    }

    @Test
    void buildStockBatchVOPageResult_shouldKeepPageOrderAndEnrichBatchFields() throws Exception {
        ErpStockBatchController controller = new ErpStockBatchController();
        AtomicInteger productLookupTimes = new AtomicInteger();
        AtomicInteger warehouseLookupTimes = new AtomicInteger();
        AtomicReference<List<ErpStockBatchDO>> pageRef = new AtomicReference<>(List.of(
                stockBatch(21L, 101L, 201L, "SMOKE-BATCH-20260506-A", "2026-05-06T09:00:00"),
                stockBatch(22L, 102L, 202L, "SMOKE-BATCH-20260506-B", "2026-05-06T09:00:00"),
                stockBatch(23L, 103L, 201L, "SMOKE-BATCH-20260506-C", "2026-05-06T10:00:00")));

        setField(controller, "productService", createProxy(ErpProductService.class, (methodName, args) -> {
            if ("getProductVOMap".equals(methodName)) {
                productLookupTimes.incrementAndGet();
                return Map.of(
                        101L, new ErpProductRespVO().setName("产品A").setMaterialCode("MAT-A").setUnitName("PCS"),
                        102L, new ErpProductRespVO().setName("产品B").setMaterialCode("MAT-B").setUnitName("KG"),
                        103L, new ErpProductRespVO().setName("产品C").setMaterialCode("MAT-C").setUnitName("BOX"));
            }
            return Collections.emptyMap();
        }));
        setField(controller, "warehouseService", createProxy(ErpWarehouseService.class, (methodName, args) -> {
            if ("getWarehouseMap".equals(methodName)) {
                warehouseLookupTimes.incrementAndGet();
                return Map.of(
                        201L, new ErpWarehouseDO().setId(201L).setName("主仓"),
                        202L, new ErpWarehouseDO().setId(202L).setName("二号仓"));
            }
            return Collections.emptyMap();
        }));

        var method = ErpStockBatchController.class.getDeclaredMethod("buildStockBatchVOPageResult", cn.iocoder.yudao.framework.common.pojo.PageResult.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        cn.iocoder.yudao.framework.common.pojo.PageResult<ErpStockBatchRespVO> result =
                (cn.iocoder.yudao.framework.common.pojo.PageResult<ErpStockBatchRespVO>) method.invoke(controller,
                        new cn.iocoder.yudao.framework.common.pojo.PageResult<>(pageRef.get(), 3L));

        assertThat(result.getTotal()).isEqualTo(3L);
        assertThat(result.getList()).extracting(ErpStockBatchRespVO::getId, ErpStockBatchRespVO::getBatchNo,
                ErpStockBatchRespVO::getProductName, ErpStockBatchRespVO::getMaterialCode,
                ErpStockBatchRespVO::getUnitName, ErpStockBatchRespVO::getWarehouseName)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(21L, "SMOKE-BATCH-20260506-A", "产品A", "MAT-A", "PCS", "主仓"),
                        org.assertj.core.groups.Tuple.tuple(22L, "SMOKE-BATCH-20260506-B", "产品B", "MAT-B", "KG", "二号仓"),
                        org.assertj.core.groups.Tuple.tuple(23L, "SMOKE-BATCH-20260506-C", "产品C", "MAT-C", "BOX", "主仓"));
        assertThat(productLookupTimes).hasValue(1);
        assertThat(warehouseLookupTimes).hasValue(1);
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> type, MethodHandler handler) {
        return (T) java.lang.reflect.Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
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

    private ErpStockBatchDO stockBatch(Long id, Long productId, Long warehouseId, String batchNo, String inboundTime) {
        return new ErpStockBatchDO()
                .setId(id)
                .setProductId(productId)
                .setWarehouseId(warehouseId)
                .setBatchNo(batchNo)
                .setInboundTime(LocalDateTime.parse(inboundTime))
                .setAvailableQty(new BigDecimal("5.000"))
                .setTotalQty(new BigDecimal("5.000"))
                .setLockedQty(BigDecimal.ZERO);
    }

    private ErpStockBatchRespVO invokeBuildStockBatchVO(ErpStockBatchController controller, ErpStockBatchDO stockBatch)
            throws Exception {
        var method = ErpStockBatchController.class.getDeclaredMethod("buildStockBatchVO", ErpStockBatchDO.class);
        method.setAccessible(true);
        return (ErpStockBatchRespVO) method.invoke(controller, stockBatch);
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
