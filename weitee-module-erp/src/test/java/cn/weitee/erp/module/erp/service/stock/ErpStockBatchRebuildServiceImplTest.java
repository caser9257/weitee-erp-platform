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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ErpStockBatchRebuildServiceImplTest {

    @Test
    void previewOutbound_shouldBatchLoadStockOutItemsWithoutCallingSingleQuery() throws Exception {
        ErpStockBatchRebuildServiceImpl service = new ErpStockBatchRebuildServiceImpl();
        AtomicReference<Collection<Long>> queriedOutIdsRef = new AtomicReference<>();
        AtomicReference<Boolean> singleQueryCalledRef = new AtomicReference<>(false);

        setField(service, "erpStockOutMapper", createProxy(cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockOutMapper.class,
                (methodName, args) -> {
                    if ("selectApprovedListForBatchRebuild".equals(methodName)) {
                        return List.of(
                                new cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockOutDO()
                                        .setId(201L).setNo("QTCK202607060001")
                                        .setStatus(ErpAuditStatus.APPROVE.getStatus()),
                                new cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockOutDO()
                                        .setId(202L).setNo("QTCK202607060002")
                                        .setStatus(ErpAuditStatus.APPROVE.getStatus()));
                    }
                    return null;
                }));
        setField(service, "erpStockOutItemMapper",
                createProxy(cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockOutItemMapper.class, (methodName, args) -> {
                    if ("selectListByOutId".equals(methodName)) {
                        singleQueryCalledRef.set(true);
                        return List.of();
                    }
                    if ("selectListByOutIds".equals(methodName)) {
                        @SuppressWarnings("unchecked")
                        Collection<Long> outIds = (Collection<Long>) args[0];
                        queriedOutIdsRef.set(outIds);
                        return List.of(
                                new cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockOutItemDO()
                                        .setId(301L).setOutId(201L).setProductId(1L).setWarehouseId(11L)
                                        .setCount(new BigDecimal("3.000")),
                                new cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockOutItemDO()
                                        .setId(302L).setOutId(202L).setProductId(2L).setWarehouseId(12L)
                                        .setCount(new BigDecimal("5.000")));
                    }
                    return null;
                }));
        setField(service, "erpStockBatchAllocationMapper", createProxy(ErpStockBatchAllocationMapper.class, (methodName, args) -> {
            if ("selectListByBizItem".equals(methodName)) {
                return new ArrayList<>();
            }
            return null;
        }));
        setField(service, "productService", createProxy(ErpProductService.class, (methodName, args) -> {
            if ("getProduct".equals(methodName)) {
                Long productId = (Long) args[0];
                return new ErpProductDO().setId(productId).setBatchControlFlag(true);
            }
            return null;
        }));
        setField(service, "stockBatchService", createProxy(ErpStockBatchService.class, (methodName, args) -> {
            if ("getAvailableStockBatchList".equals(methodName)) {
                return List.of(new ErpStockBatchDO().setId(21L).setProductId(1L).setWarehouseId(11L)
                                .setBatchNo("B201").setAvailableQty(new BigDecimal("9.000")),
                        new ErpStockBatchDO().setId(22L).setProductId(2L).setWarehouseId(12L)
                                .setBatchNo("B202").setAvailableQty(new BigDecimal("9.000")));
            }
            return null;
        }));
        setField(service, "stockBatchAllocationService", createProxy(ErpStockBatchAllocationService.class,
                (methodName, args) -> new ArrayList<>()));

        ErpStockBatchRebuildOutboundReqVO reqVO = new ErpStockBatchRebuildOutboundReqVO();
        reqVO.setBizType(ErpStockRecordBizTypeEnum.OTHER_OUT.getType());
        reqVO.setLimit(20);

        ErpStockBatchRebuildOutboundRespVO preview = service.previewOutbound(reqVO);

        assertThat(singleQueryCalledRef.get()).isFalse();
        assertThat(queriedOutIdsRef.get()).containsExactly(201L, 202L);
        assertThat(preview.getDetails()).hasSize(2);
        assertThat(preview.getDetails())
                .extracting(ErpStockBatchRebuildOutboundRespVO.Detail::getBizItemId)
                .containsExactly(301L, 302L);
    }

    @Test
    void previewOutbound_shouldBatchLoadSaleOutItemsWithoutCallingSingleQuery() throws Exception {
        ErpStockBatchRebuildServiceImpl service = new ErpStockBatchRebuildServiceImpl();
        AtomicReference<Collection<Long>> queriedOutIdsRef = new AtomicReference<>();
        AtomicReference<Boolean> singleQueryCalledRef = new AtomicReference<>(false);

        setField(service, "saleOutMapper", createProxy(ErpSaleOutMapper.class, (methodName, args) -> {
            if ("selectApprovedListForBatchRebuild".equals(methodName)) {
                return List.of(
                        new ErpSaleOutDO().setId(101L).setNo("XSCK202607060001")
                                .setStatus(ErpAuditStatus.APPROVE.getStatus()),
                        new ErpSaleOutDO().setId(102L).setNo("XSCK202607060002")
                                .setStatus(ErpAuditStatus.APPROVE.getStatus()));
            }
            return null;
        }));
        setField(service, "saleOutItemMapper", createProxy(ErpSaleOutItemMapper.class, (methodName, args) -> {
            if ("selectListByOutId".equals(methodName)) {
                singleQueryCalledRef.set(true);
                return List.of();
            }
            if ("selectListByOutIds".equals(methodName)) {
                @SuppressWarnings("unchecked")
                Collection<Long> outIds = (Collection<Long>) args[0];
                queriedOutIdsRef.set(outIds);
                return List.of(
                        new ErpSaleOutItemDO().setId(111L).setOutId(101L)
                                .setProductId(1L).setWarehouseId(11L).setCount(new BigDecimal("3.000")),
                        new ErpSaleOutItemDO().setId(112L).setOutId(102L)
                                .setProductId(2L).setWarehouseId(12L).setCount(new BigDecimal("5.000")));
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
                Long productId = (Long) args[0];
                return new ErpProductDO().setId(productId).setBatchControlFlag(true);
            }
            return null;
        }));
        setField(service, "stockBatchService", createProxy(ErpStockBatchService.class, (methodName, args) -> {
            if ("getAvailableStockBatchList".equals(methodName)) {
                return List.of(new ErpStockBatchDO().setId(31L).setProductId(1L).setWarehouseId(11L)
                                .setBatchNo("B101").setAvailableQty(new BigDecimal("9.000")),
                        new ErpStockBatchDO().setId(32L).setProductId(2L).setWarehouseId(12L)
                                .setBatchNo("B102").setAvailableQty(new BigDecimal("9.000")));
            }
            return null;
        }));
        setField(service, "stockBatchAllocationService", createProxy(ErpStockBatchAllocationService.class,
                (methodName, args) -> new ArrayList<>()));

        ErpStockBatchRebuildOutboundReqVO reqVO = new ErpStockBatchRebuildOutboundReqVO();
        reqVO.setBizType(ErpStockRecordBizTypeEnum.SALE_OUT.getType());
        reqVO.setLimit(20);

        ErpStockBatchRebuildOutboundRespVO preview = service.previewOutbound(reqVO);

        assertThat(singleQueryCalledRef.get()).isFalse();
        assertThat(queriedOutIdsRef.get()).containsExactly(101L, 102L);
        assertThat(preview.getDetails()).hasSize(2);
        assertThat(preview.getDetails())
                .extracting(ErpStockBatchRebuildOutboundRespVO.Detail::getBizItemId)
                .containsExactly(111L, 112L);
    }

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
            if ("selectListByOutIds".equals(methodName)) {
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
