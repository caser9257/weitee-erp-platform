package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.module.erp.controller.admin.stock.vo.batch.ErpStockBatchRebuildOutboundReqVO;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.batch.ErpStockBatchRebuildOutboundRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOutItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOutItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOutMapper;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockBatchAllocationMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchAllocateOutboundReqBO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ErpStockBatchRebuildServiceImplTest {

    @Test
    void rebuildOutbound_shouldPreviewAndExecuteMissingApprovedSaleOutAllocation() throws Exception {
        ErpStockBatchRebuildServiceImpl service = new ErpStockBatchRebuildServiceImpl();
        AtomicReference<ErpStockBatchAllocateOutboundReqBO> allocateReqRef = new AtomicReference<>();

        setField(service, "saleOutMapper", createProxy(ErpSaleOutMapper.class, (methodName, args) -> {
            if ("selectApprovedListForBatchRebuild".equals(methodName)) {
                return List.of(new ErpSaleOutDO().setId(100L).setNo("XSCK202604290001")
                        .setStatus(ErpAuditStatus.APPROVE.getStatus()));
            }
            return null;
        }));
        setField(service, "saleOutItemMapper", createProxy(ErpSaleOutItemMapper.class, (methodName, args) -> {
            if ("selectListByOutId".equals(methodName)) {
                return List.of(new ErpSaleOutItemDO().setId(101L).setOutId(100L)
                        .setProductId(1L).setWarehouseId(2L).setCount(new BigDecimal("7.000")));
            }
            return null;
        }));
        setField(service, "stockBatchAllocationMapper", createProxy(ErpStockBatchAllocationMapper.class, (methodName, args) -> {
            if ("selectListByBizItem".equals(methodName)) {
                return new ArrayList<>();
            }
            return null;
        }));
        setField(service, "productService", createProxy(ErpProductService.class, (methodName, args) -> {
            if ("getProduct".equals(methodName)) {
                return new ErpProductDO().setId(1L).setBatchControlFlag(true);
            }
            return null;
        }));
        setField(service, "stockBatchService", createProxy(ErpStockBatchService.class, (methodName, args) -> {
            if ("getAvailableStockBatchList".equals(methodName)) {
                return List.of(new ErpStockBatchDO().setId(11L).setProductId(1L).setWarehouseId(2L)
                        .setBatchNo("B001").setAvailableQty(new BigDecimal("10.000")));
            }
            return null;
        }));
        setField(service, "stockBatchAllocationService", createProxy(ErpStockBatchAllocationService.class, (methodName, args) -> {
            if ("allocateOutbound".equals(methodName)) {
                allocateReqRef.set((ErpStockBatchAllocateOutboundReqBO) args[0]);
                return new ArrayList<>();
            }
            return null;
        }));

        ErpStockBatchRebuildOutboundReqVO reqVO = new ErpStockBatchRebuildOutboundReqVO();
        reqVO.setBizType(ErpStockRecordBizTypeEnum.SALE_OUT.getType());
        reqVO.setLimit(10);

        ErpStockBatchRebuildOutboundRespVO preview = service.previewOutbound(reqVO);
        assertThat(preview.getReadyCount()).isEqualTo(1);
        assertThat(preview.getDetails()).hasSize(1);
        assertThat(preview.getDetails().get(0).getResult()).isEqualTo("READY");

        reqVO.setConfirm(true);
        ErpStockBatchRebuildOutboundRespVO rebuild = service.rebuildOutbound(reqVO);

        assertThat(rebuild.getRebuiltCount()).isEqualTo(1);
        assertThat(allocateReqRef.get())
                .extracting(ErpStockBatchAllocateOutboundReqBO::getBizType,
                        ErpStockBatchAllocateOutboundReqBO::getBizId,
                        ErpStockBatchAllocateOutboundReqBO::getBizItemId,
                        ErpStockBatchAllocateOutboundReqBO::getBizNo,
                        ErpStockBatchAllocateOutboundReqBO::getProductId,
                        ErpStockBatchAllocateOutboundReqBO::getWarehouseId,
                        ErpStockBatchAllocateOutboundReqBO::getCount)
                .containsExactly(ErpStockRecordBizTypeEnum.SALE_OUT.getType(), 100L, 101L,
                        "XSCK202604290001", 1L, 2L, new BigDecimal("7.000"));
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
