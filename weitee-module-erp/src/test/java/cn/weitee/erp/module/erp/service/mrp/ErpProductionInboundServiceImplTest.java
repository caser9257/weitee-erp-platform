package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostDetailRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionFinishQualityDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionInboundDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionInboundMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.common.ErpBizTypeEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionInboundStatusEnum;
import cn.weitee.erp.module.erp.enums.stock.ErpStockRecordBizTypeEnum;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceBizHookService;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ErpProductionInboundServiceImplTest {

    private final AtomicReference<ErpProductionInboundDO> insertedInboundRef = new AtomicReference<>();
    private final AtomicReference<ErpProductionInboundDO> selectedInboundRef = new AtomicReference<>();
    private final AtomicReference<ErpProductionInboundDO> existingInboundByQualityRef = new AtomicReference<>();
    private final AtomicReference<ErpProductionInboundDO> updatedInboundRef = new AtomicReference<>();
    private final AtomicReference<Long> resetExecutionInfoInboundIdRef = new AtomicReference<>();
    private final AtomicReference<ErpProductionOrderDO> selectedOrderRef = new AtomicReference<>();
    private final AtomicReference<ErpProductionCostDetailRespVO> costDetailRef = new AtomicReference<>();
    private final AtomicReference<ErpStockRecordCreateReqBO> stockRecordReqRef = new AtomicReference<>();
    private final AtomicReference<Integer> approvedBizTypeRef = new AtomicReference<>();
    private final AtomicReference<Long> approvedBizIdRef = new AtomicReference<>();
    private final AtomicReference<LocalDate> approvedBizDateRef = new AtomicReference<>();
    private final AtomicReference<Integer> rollbackBizTypeRef = new AtomicReference<>();
    private final AtomicReference<Long> rollbackBizIdRef = new AtomicReference<>();
    private final AtomicReference<Long> rollbackUserIdRef = new AtomicReference<>();
    private final AtomicReference<String> rollbackRemarkRef = new AtomicReference<>();

    private ErpProductionInboundServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new ErpProductionInboundServiceImpl();
        insertedInboundRef.set(null);
        selectedInboundRef.set(null);
        existingInboundByQualityRef.set(null);
        updatedInboundRef.set(null);
        resetExecutionInfoInboundIdRef.set(null);
        selectedOrderRef.set(null);
        costDetailRef.set(null);
        stockRecordReqRef.set(null);
        approvedBizTypeRef.set(null);
        approvedBizIdRef.set(null);
        approvedBizDateRef.set(null);
        rollbackBizTypeRef.set(null);
        rollbackBizIdRef.set(null);
        rollbackUserIdRef.set(null);
        rollbackRemarkRef.set(null);

        setField(service, "erpProductionInboundMapper", createProxy(ErpProductionInboundMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ErpProductionInboundDO inbound = (ErpProductionInboundDO) args[0];
                inbound.setId(66L);
                insertedInboundRef.set(inbound);
                return 1;
            }
            if ("selectById".equals(methodName)) {
                return selectedInboundRef.get();
            }
            if ("selectByFinishQualityId".equals(methodName)) {
                return existingInboundByQualityRef.get();
            }
            if ("updateById".equals(methodName)) {
                updatedInboundRef.set((ErpProductionInboundDO) args[0]);
                return 1;
            }
            if ("resetExecutionInfoById".equals(methodName)) {
                resetExecutionInfoInboundIdRef.set((Long) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "productionOrderService", createProxyByName(
                "cn.weitee.erp.module.erp.service.mrp.ErpProductionOrderService", (methodName, args) -> {
                    if ("getProductionOrder".equals(methodName)) {
                        return selectedOrderRef.get();
                    }
                    return null;
                }));
        setField(service, "productionCostService", createProxyByName(
                "cn.weitee.erp.module.erp.service.mrp.ErpProductionCostService", (methodName, args) -> {
                    if ("getCostDetail".equals(methodName)) {
                        return costDetailRef.get();
                    }
                    return null;
                }));
        setField(service, "stockBatchService", createProxyByName(
                "cn.weitee.erp.module.erp.service.stock.ErpStockBatchService", (methodName, args) -> {
                    if ("getStockBatchByProductWarehouseAndBatchNo".equals(methodName)) {
                        return new ErpStockBatchDO().setId(701L);
                    }
                    return null;
                }));
        setField(service, "stockRecordService", createProxyByName(
                "cn.weitee.erp.module.erp.service.stock.ErpStockRecordService", (methodName, args) -> {
                    if ("createStockRecord".equals(methodName)) {
                        stockRecordReqRef.set((ErpStockRecordCreateReqBO) args[0]);
                    }
                    return null;
                }));
        setField(service, "financeBizHookService", createProxy(ErpFinanceBizHookService.class, (methodName, args) -> {
            if ("handleApprovedBiz".equals(methodName)) {
                approvedBizTypeRef.set((Integer) args[0]);
                approvedBizIdRef.set((Long) args[1]);
                approvedBizDateRef.set((LocalDate) args[2]);
                return 1L;
            }
            if ("handleRollbackBiz".equals(methodName)) {
                rollbackBizTypeRef.set((Integer) args[0]);
                rollbackBizIdRef.set((Long) args[1]);
                rollbackUserIdRef.set((Long) args[2]);
                rollbackRemarkRef.set((String) args[3]);
            }
            return null;
        }));
        setField(service, "noRedisDAO", new ErpNoRedisDAO() {
            @Override
            public String generate(String prefix) {
                return "ZZRK202605240001";
            }
        });
    }

    @Test
    void createProductionInboundFromQuality_shouldCreatePendingInboundWithCostSnapshot() {
        selectedOrderRef.set(new ErpProductionOrderDO()
                .setId(11L)
                .setOrderNo("SCGD202605240001")
                .setProjectId(21L)
                .setProductId(31L)
                .setWarehouseId(41L));
        costDetailRef.set(new ErpProductionCostDetailRespVO().setUnitCost(new BigDecimal("12.5")));

        Long inboundId = service.createProductionInboundFromQuality(new ErpProductionFinishQualityDO()
                .setId(51L)
                .setNo("CPZJ202605240001")
                .setProductionOrderId(11L)
                .setQualifiedQty(new BigDecimal("5"))
                .setRemark("passed"));

        assertEquals(66L, inboundId);
        assertNotNull(insertedInboundRef.get());
        assertEquals("ZZRK202605240001", insertedInboundRef.get().getNo());
        assertEquals(51L, insertedInboundRef.get().getFinishQualityId());
        assertEquals(41L, insertedInboundRef.get().getWarehouseId());
        assertEquals(new BigDecimal("12.500000"), insertedInboundRef.get().getUnitCost());
        assertEquals(new BigDecimal("62.500000"), insertedInboundRef.get().getTotalCost());
        assertEquals(ErpProductionInboundStatusEnum.PENDING.getStatus(), insertedInboundRef.get().getStatus());
    }

    @Test
    void createProductionInboundFromQuality_shouldReturnExistingInboundIdWhenAlreadyCreated() {
        existingInboundByQualityRef.set(new ErpProductionInboundDO().setId(88L));

        Long inboundId = service.createProductionInboundFromQuality(new ErpProductionFinishQualityDO()
                .setId(51L)
                .setProductionOrderId(11L)
                .setQualifiedQty(new BigDecimal("5")));

        assertEquals(88L, inboundId);
        assertNull(insertedInboundRef.get());
    }

    @Test
    void executeProductionInbound_shouldCreateStockRecordAndMarkExecuted() {
        selectedInboundRef.set(new ErpProductionInboundDO()
                .setId(61L)
                .setNo("ZZRK202605240001")
                .setProductId(31L)
                .setWarehouseId(41L)
                .setInboundQty(new BigDecimal("6"))
                .setStatus(ErpProductionInboundStatusEnum.PENDING.getStatus()));

        service.executeProductionInbound(99L, 61L);

        assertEquals(new BigDecimal("6"), stockRecordReqRef.get().getCount());
        assertEquals(ErpStockRecordBizTypeEnum.PRODUCTION_IN.getType(), stockRecordReqRef.get().getBizType());
        assertEquals(ErpProductionInboundStatusEnum.EXECUTED.getStatus(), updatedInboundRef.get().getStatus());
        assertEquals(99L, updatedInboundRef.get().getExecutedBy());
        assertNotNull(updatedInboundRef.get().getExecutedTime());
        assertNotNull(updatedInboundRef.get().getInboundTime());
        assertEquals(ErpBizTypeEnum.PRODUCTION_INBOUND.getType(), approvedBizTypeRef.get());
        assertEquals(61L, approvedBizIdRef.get());
        assertNotNull(approvedBizDateRef.get());
    }

    @Test
    void cancelProductionInbound_shouldMarkCanceledWhenPending() {
        selectedInboundRef.set(new ErpProductionInboundDO()
                .setId(62L)
                .setStatus(ErpProductionInboundStatusEnum.PENDING.getStatus()));

        service.cancelProductionInbound(62L);

        assertEquals(ErpProductionInboundStatusEnum.CANCELED.getStatus(), updatedInboundRef.get().getStatus());
    }

    @Test
    void revertProductionInbound_shouldCreateNegativeStockRecordAndResetPending() {
        selectedInboundRef.set(new ErpProductionInboundDO()
                .setId(63L)
                .setNo("ZZRK202605240002")
                .setProductId(31L)
                .setWarehouseId(41L)
                .setInboundQty(new BigDecimal("7"))
                .setStatus(ErpProductionInboundStatusEnum.EXECUTED.getStatus()));

        service.revertProductionInbound(100L, 63L);

        assertEquals(new BigDecimal("-7"), stockRecordReqRef.get().getCount());
        assertEquals(ErpStockRecordBizTypeEnum.PRODUCTION_IN_CANCEL.getType(), stockRecordReqRef.get().getBizType());
        assertEquals(ErpProductionInboundStatusEnum.PENDING.getStatus(), updatedInboundRef.get().getStatus());
        assertEquals(63L, resetExecutionInfoInboundIdRef.get());
        assertEquals(ErpBizTypeEnum.PRODUCTION_INBOUND.getType(), rollbackBizTypeRef.get());
        assertEquals(63L, rollbackBizIdRef.get());
        assertEquals(100L, rollbackUserIdRef.get());
        assertEquals("自制入库反执行回滚凭证", rollbackRemarkRef.get());
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

    @SuppressWarnings("unchecked")
    private <T> T createProxyByName(String className, MethodHandler handler) {
        try {
            Class<T> type = (Class<T>) Class.forName(className);
            return createProxy(type, handler);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
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
