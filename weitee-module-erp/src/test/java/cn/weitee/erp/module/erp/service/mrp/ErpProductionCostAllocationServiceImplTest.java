package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostAllocationSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationResultDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationRuleDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionCostEntryDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionManHourDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionCostAllocationMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionCostAllocationResultMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionCostAllocationRuleMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionCostEntryMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionManHourMapper;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostAllocationBasisTypeEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostAllocationStatusEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostSourceTypeEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostTypeEnum;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsMrpExt.PRODUCTION_COST_ALLOCATION_ALREADY_EXECUTED;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsMrpExt.PRODUCTION_COST_ALLOCATION_DUPLICATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpProductionCostAllocationServiceImplTest {

    @Test
    void createProductionCostAllocation_shouldRejectWhenSameMonthAndCostTypeAlreadyExists() throws Exception {
        ErpProductionCostAllocationServiceImpl service = new ErpProductionCostAllocationServiceImpl();
        ErpProductionCostAllocationRuleDO rule = new ErpProductionCostAllocationRuleDO()
                .setId(11L)
                .setCostType(ErpProductionCostTypeEnum.LABOR.getType())
                .setBasisType(ErpProductionCostAllocationBasisTypeEnum.MAN_HOUR.getType())
                .setStatus(CommonStatusEnum.ENABLE.getStatus());

        setField(service, "erpProductionCostAllocationRuleMapper", createProxy(ErpProductionCostAllocationRuleMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return rule;
            }
            return null;
        }));
        setField(service, "erpProductionCostAllocationMapper", createProxy(ErpProductionCostAllocationMapper.class, (methodName, args) -> {
            if ("selectByAccountingMonthAndCostType".equals(methodName)) {
                return new ErpProductionCostAllocationDO().setId(99L);
            }
            return null;
        }));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.createProductionCostAllocation(new ErpProductionCostAllocationSaveReqVO()
                .setAccountingMonth("2026-04")
                .setCostType(ErpProductionCostTypeEnum.LABOR.getType())
                .setRuleId(11L)
                .setTotalAmount(new BigDecimal("100.00"))
                .setRemark("4月人工分摊")));

        assertEquals(PRODUCTION_COST_ALLOCATION_DUPLICATED.getCode(), ex.getCode());
    }

    @Test
    void executeProductionCostAllocation_shouldAllocateByManHourAndCreateCostEntries() throws Exception {
        ErpProductionCostAllocationServiceImpl service = new ErpProductionCostAllocationServiceImpl();
        ErpProductionCostAllocationDO allocation = new ErpProductionCostAllocationDO()
                .setId(1L)
                .setAllocationNo("CBFT202604280001")
                .setAccountingMonth("2026-04")
                .setCostType(ErpProductionCostTypeEnum.LABOR.getType())
                .setRuleId(11L)
                .setTotalAmount(new BigDecimal("100.00"))
                .setStatus(ErpProductionCostAllocationStatusEnum.DRAFT.getStatus())
                .setRemark("4月人工分摊");
        ErpProductionCostAllocationRuleDO rule = new ErpProductionCostAllocationRuleDO()
                .setId(11L)
                .setRuleName("按人工工时")
                .setCostType(ErpProductionCostTypeEnum.LABOR.getType())
                .setBasisType(ErpProductionCostAllocationBasisTypeEnum.MAN_HOUR.getType())
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        List<ErpProductionManHourDO> manHours = List.of(
                new ErpProductionManHourDO().setId(101L).setProductionOrderId(1001L)
                        .setAccountingMonth("2026-04").setManHour(new BigDecimal("1.00")),
                new ErpProductionManHourDO().setId(102L).setProductionOrderId(1001L)
                        .setAccountingMonth("2026-04").setManHour(new BigDecimal("2.00")),
                new ErpProductionManHourDO().setId(103L).setProductionOrderId(1002L)
                        .setAccountingMonth("2026-04").setManHour(new BigDecimal("2.00")));
        List<ErpProductionCostEntryDO> insertedEntries = new ArrayList<>();
        AtomicReference<List<ErpProductionCostAllocationResultDO>> insertedResultsRef = new AtomicReference<>();
        AtomicReference<ErpProductionCostAllocationDO> updatedAllocationRef = new AtomicReference<>();
        AtomicLong costEntryIdSeq = new AtomicLong(5001L);

        setField(service, "erpProductionCostAllocationMapper", createProxy(ErpProductionCostAllocationMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return allocation;
            }
            if ("updateById".equals(methodName)) {
                updatedAllocationRef.set((ErpProductionCostAllocationDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "erpProductionCostAllocationRuleMapper", createProxy(ErpProductionCostAllocationRuleMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return rule;
            }
            return null;
        }));
        setField(service, "erpProductionManHourMapper", createProxy(ErpProductionManHourMapper.class, (methodName, args) -> {
            if ("selectListByAccountingMonth".equals(methodName)) {
                return manHours;
            }
            return null;
        }));
        setField(service, "erpProductionCostAllocationResultMapper", createProxy(ErpProductionCostAllocationResultMapper.class, (methodName, args) -> {
            if ("selectListByAllocationId".equals(methodName)) {
                return List.of();
            }
            if ("insertBatch".equals(methodName)) {
                insertedResultsRef.set(new ArrayList<>((List<ErpProductionCostAllocationResultDO>) args[0]));
                return true;
            }
            return null;
        }));
        setField(service, "erpProductionCostEntryMapper", createProxy(ErpProductionCostEntryMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ErpProductionCostEntryDO entry = (ErpProductionCostEntryDO) args[0];
                entry.setId(costEntryIdSeq.getAndIncrement());
                insertedEntries.add(entry);
                return 1;
            }
            return null;
        }));
        setField(service, "productionOrderService", createProxy(ErpProductionOrderService.class, (methodName, args) -> {
            if ("getProductionOrderList".equals(methodName)) {
                return List.of(
                        new ErpProductionOrderDO().setId(1001L).setOrderNo("SCGD202604280001").setProjectId(2001L),
                        new ErpProductionOrderDO().setId(1002L).setOrderNo("SCGD202604280002").setProjectId(2002L));
            }
            return null;
        }));

        service.executeProductionCostAllocation(1L);

        assertEquals(2, insertedEntries.size());
        assertEquals(ErpProductionCostSourceTypeEnum.ALLOCATION.getType(), insertedEntries.get(0).getSourceType());
        assertEquals(new BigDecimal("60.000000"), insertedEntries.get(0).getAmount());
        assertEquals(new BigDecimal("40.000000"), insertedEntries.get(1).getAmount());
        assertEquals(2, insertedResultsRef.get().size());
        assertEquals(new BigDecimal("3.000000"), insertedResultsRef.get().get(0).getBasisValue());
        assertEquals(new BigDecimal("0.600000"), insertedResultsRef.get().get(0).getBasisRatio());
        assertEquals(new BigDecimal("60.000000"), insertedResultsRef.get().get(0).getAllocatedAmount());
        assertNotNull(insertedResultsRef.get().get(0).getGeneratedCostEntryId());
        assertEquals(ErpProductionCostAllocationStatusEnum.EXECUTED.getStatus(), updatedAllocationRef.get().getStatus());
        assertNotNull(updatedAllocationRef.get().getExecutedTime());
    }

    @Test
    void executeProductionCostAllocation_shouldUseGeneratedRemarkWhenAllocationRemarkBlank() throws Exception {
        ErpProductionCostAllocationServiceImpl service = new ErpProductionCostAllocationServiceImpl();
        ErpProductionCostAllocationDO allocation = new ErpProductionCostAllocationDO()
                .setId(2L)
                .setAllocationNo("CBFT202604280002")
                .setAccountingMonth("2026-04")
                .setCostType(ErpProductionCostTypeEnum.LABOR.getType())
                .setRuleId(11L)
                .setTotalAmount(new BigDecimal("100.00"))
                .setStatus(ErpProductionCostAllocationStatusEnum.DRAFT.getStatus())
                .setRemark("");
        ErpProductionCostAllocationRuleDO rule = new ErpProductionCostAllocationRuleDO()
                .setId(11L)
                .setRuleName("按人工工时")
                .setCostType(ErpProductionCostTypeEnum.LABOR.getType())
                .setBasisType(ErpProductionCostAllocationBasisTypeEnum.MAN_HOUR.getType())
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        List<ErpProductionManHourDO> manHours = List.of(
                new ErpProductionManHourDO().setId(201L).setProductionOrderId(1001L)
                        .setAccountingMonth("2026-04").setManHour(new BigDecimal("4.00")));
        List<ErpProductionCostEntryDO> insertedEntries = new ArrayList<>();
        AtomicReference<List<ErpProductionCostAllocationResultDO>> insertedResultsRef = new AtomicReference<>();
        AtomicReference<ErpProductionCostAllocationDO> updatedAllocationRef = new AtomicReference<>();
        AtomicLong costEntryIdSeq = new AtomicLong(6001L);

        setField(service, "erpProductionCostAllocationMapper", createProxy(ErpProductionCostAllocationMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return allocation;
            }
            if ("updateById".equals(methodName)) {
                updatedAllocationRef.set((ErpProductionCostAllocationDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(service, "erpProductionCostAllocationRuleMapper", createProxy(ErpProductionCostAllocationRuleMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return rule;
            }
            return null;
        }));
        setField(service, "erpProductionManHourMapper", createProxy(ErpProductionManHourMapper.class, (methodName, args) -> {
            if ("selectListByAccountingMonth".equals(methodName)) {
                return manHours;
            }
            return null;
        }));
        setField(service, "erpProductionCostAllocationResultMapper", createProxy(ErpProductionCostAllocationResultMapper.class, (methodName, args) -> {
            if ("selectListByAllocationId".equals(methodName)) {
                return List.of();
            }
            if ("insertBatch".equals(methodName)) {
                insertedResultsRef.set(new ArrayList<>((List<ErpProductionCostAllocationResultDO>) args[0]));
                return true;
            }
            return null;
        }));
        setField(service, "erpProductionCostEntryMapper", createProxy(ErpProductionCostEntryMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ErpProductionCostEntryDO entry = (ErpProductionCostEntryDO) args[0];
                entry.setId(costEntryIdSeq.getAndIncrement());
                insertedEntries.add(entry);
                return 1;
            }
            return null;
        }));
        setField(service, "productionOrderService", createProxy(ErpProductionOrderService.class, (methodName, args) -> {
            if ("getProductionOrderList".equals(methodName)) {
                return List.of(new ErpProductionOrderDO().setId(1001L).setOrderNo("SCGD202604280001").setProjectId(2001L));
            }
            return null;
        }));

        service.executeProductionCostAllocation(2L);

        assertEquals(1, insertedEntries.size());
        assertEquals("CBFT202604280002-按人工工时", insertedEntries.get(0).getRemark());
        assertEquals(1, insertedResultsRef.get().size());
        assertEquals("CBFT202604280002-按人工工时", insertedResultsRef.get().get(0).getRemark());
        assertEquals(ErpProductionCostAllocationStatusEnum.EXECUTED.getStatus(), updatedAllocationRef.get().getStatus());
    }

    @Test
    void executeProductionCostAllocation_shouldRejectWhenAlreadyExecuted() throws Exception {
        ErpProductionCostAllocationServiceImpl service = new ErpProductionCostAllocationServiceImpl();

        setField(service, "erpProductionCostAllocationMapper", createProxy(ErpProductionCostAllocationMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return new ErpProductionCostAllocationDO()
                        .setId(1L)
                        .setStatus(ErpProductionCostAllocationStatusEnum.EXECUTED.getStatus());
            }
            return null;
        }));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.executeProductionCostAllocation(1L));

        assertEquals(PRODUCTION_COST_ALLOCATION_ALREADY_EXECUTED.getCode(), ex.getCode());
    }

    @Test
    void executeProductionCostAllocation_shouldAllocateByFinishedQtyWhenBasisTypeIsFinishedQty() throws Exception {
        ErpProductionCostAllocationServiceImpl service = new ErpProductionCostAllocationServiceImpl();
        ErpProductionCostAllocationDO allocation = new ErpProductionCostAllocationDO()
                .setId(3L)
                .setAllocationNo("CBFT202604280003")
                .setAccountingMonth("2026-04")
                .setCostType(ErpProductionCostTypeEnum.POWER.getType())
                .setRuleId(31L)
                .setTotalAmount(new BigDecimal("100.00"))
                .setStatus(ErpProductionCostAllocationStatusEnum.DRAFT.getStatus())
                .setRemark("quantity allocation");
        ErpProductionCostAllocationRuleDO rule = new ErpProductionCostAllocationRuleDO()
                .setId(31L)
                .setRuleName("qty allocation")
                .setCostType(ErpProductionCostTypeEnum.POWER.getType())
                .setBasisType(ErpProductionCostAllocationBasisTypeEnum.FINISHED_QTY.getType())
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        List<ErpProductionManHourDO> manHours = List.of(
                new ErpProductionManHourDO().setId(301L).setProductionOrderId(1001L)
                        .setAccountingMonth("2026-04").setManHour(new BigDecimal("1.00")),
                new ErpProductionManHourDO().setId(302L).setProductionOrderId(1002L)
                        .setAccountingMonth("2026-04").setManHour(new BigDecimal("1.00")));
        List<ErpProductionCostEntryDO> insertedEntries = new ArrayList<>();
        AtomicReference<List<ErpProductionCostAllocationResultDO>> insertedResultsRef = new AtomicReference<>();
        AtomicLong costEntryIdSeq = new AtomicLong(7001L);

        setField(service, "erpProductionCostAllocationMapper", createProxy(ErpProductionCostAllocationMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return allocation;
            }
            if ("updateById".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(service, "erpProductionCostAllocationRuleMapper", createProxy(ErpProductionCostAllocationRuleMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return rule;
            }
            return null;
        }));
        setField(service, "erpProductionManHourMapper", createProxy(ErpProductionManHourMapper.class, (methodName, args) -> {
            if ("selectListByAccountingMonth".equals(methodName)) {
                return manHours;
            }
            return null;
        }));
        setField(service, "erpProductionCostAllocationResultMapper", createProxy(ErpProductionCostAllocationResultMapper.class, (methodName, args) -> {
            if ("selectListByAllocationId".equals(methodName)) {
                return List.of();
            }
            if ("insertBatch".equals(methodName)) {
                insertedResultsRef.set(new ArrayList<>((List<ErpProductionCostAllocationResultDO>) args[0]));
                return true;
            }
            return null;
        }));
        setField(service, "erpProductionCostEntryMapper", createProxy(ErpProductionCostEntryMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ErpProductionCostEntryDO entry = (ErpProductionCostEntryDO) args[0];
                entry.setId(costEntryIdSeq.getAndIncrement());
                insertedEntries.add(entry);
                return 1;
            }
            return null;
        }));
        setField(service, "productionOrderService", createProxy(ErpProductionOrderService.class, (methodName, args) -> {
            if ("getProductionOrderList".equals(methodName)) {
                return List.of(
                        new ErpProductionOrderDO().setId(1001L).setProductId(2001L).setFinishedQty(new BigDecimal("6")),
                        new ErpProductionOrderDO().setId(1002L).setProductId(2002L).setFinishedQty(new BigDecimal("4")));
            }
            return null;
        }));

        service.executeProductionCostAllocation(3L);

        assertEquals(2, insertedEntries.size());
        assertEquals(new BigDecimal("60.000000"), insertedEntries.get(0).getAmount());
        assertEquals(new BigDecimal("40.000000"), insertedEntries.get(1).getAmount());
        assertEquals(new BigDecimal("6.000000"), insertedResultsRef.get().get(0).getBasisValue());
        assertEquals(new BigDecimal("4.000000"), insertedResultsRef.get().get(1).getBasisValue());
    }

    @Test
    void executeProductionCostAllocation_shouldAllocateByFinishedWeightWhenBasisTypeIsFinishedWeight() throws Exception {
        ErpProductionCostAllocationServiceImpl service = new ErpProductionCostAllocationServiceImpl();
        ErpProductionCostAllocationDO allocation = new ErpProductionCostAllocationDO()
                .setId(4L)
                .setAllocationNo("CBFT202604280004")
                .setAccountingMonth("2026-04")
                .setCostType(ErpProductionCostTypeEnum.DEPRECIATION.getType())
                .setRuleId(41L)
                .setTotalAmount(new BigDecimal("100.00"))
                .setStatus(ErpProductionCostAllocationStatusEnum.DRAFT.getStatus())
                .setRemark("weight allocation");
        ErpProductionCostAllocationRuleDO rule = new ErpProductionCostAllocationRuleDO()
                .setId(41L)
                .setRuleName("weight allocation")
                .setCostType(ErpProductionCostTypeEnum.DEPRECIATION.getType())
                .setBasisType(ErpProductionCostAllocationBasisTypeEnum.FINISHED_WEIGHT.getType())
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        List<ErpProductionManHourDO> manHours = List.of(
                new ErpProductionManHourDO().setId(401L).setProductionOrderId(1001L)
                        .setAccountingMonth("2026-04").setManHour(new BigDecimal("1.00")),
                new ErpProductionManHourDO().setId(402L).setProductionOrderId(1002L)
                        .setAccountingMonth("2026-04").setManHour(new BigDecimal("1.00")));
        List<ErpProductionCostEntryDO> insertedEntries = new ArrayList<>();
        AtomicReference<List<ErpProductionCostAllocationResultDO>> insertedResultsRef = new AtomicReference<>();
        AtomicLong costEntryIdSeq = new AtomicLong(8001L);

        setField(service, "erpProductionCostAllocationMapper", createProxy(ErpProductionCostAllocationMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return allocation;
            }
            if ("updateById".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(service, "erpProductionCostAllocationRuleMapper", createProxy(ErpProductionCostAllocationRuleMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return rule;
            }
            return null;
        }));
        setField(service, "erpProductionManHourMapper", createProxy(ErpProductionManHourMapper.class, (methodName, args) -> {
            if ("selectListByAccountingMonth".equals(methodName)) {
                return manHours;
            }
            return null;
        }));
        setField(service, "erpProductionCostAllocationResultMapper", createProxy(ErpProductionCostAllocationResultMapper.class, (methodName, args) -> {
            if ("selectListByAllocationId".equals(methodName)) {
                return List.of();
            }
            if ("insertBatch".equals(methodName)) {
                insertedResultsRef.set(new ArrayList<>((List<ErpProductionCostAllocationResultDO>) args[0]));
                return true;
            }
            return null;
        }));
        setField(service, "erpProductionCostEntryMapper", createProxy(ErpProductionCostEntryMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ErpProductionCostEntryDO entry = (ErpProductionCostEntryDO) args[0];
                entry.setId(costEntryIdSeq.getAndIncrement());
                insertedEntries.add(entry);
                return 1;
            }
            return null;
        }));
        setField(service, "productionOrderService", createProxy(ErpProductionOrderService.class, (methodName, args) -> {
            if ("getProductionOrderList".equals(methodName)) {
                return List.of(
                        new ErpProductionOrderDO().setId(1001L).setProductId(2001L).setFinishedQty(new BigDecimal("3")),
                        new ErpProductionOrderDO().setId(1002L).setProductId(2002L).setFinishedQty(new BigDecimal("4")));
            }
            return null;
        }));
        setField(service, "productService", createProxy(ErpProductService.class, (methodName, args) -> {
            if ("getProductVOMap".equals(methodName)) {
                return Map.of(
                        2001L, new cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO()
                                .setId(2001L).setWeight(new BigDecimal("2.00")),
                        2002L, new cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO()
                                .setId(2002L).setWeight(new BigDecimal("1.00")));
            }
            return null;
        }));

        service.executeProductionCostAllocation(4L);

        assertEquals(2, insertedEntries.size());
        assertEquals(new BigDecimal("60.000000"), insertedEntries.get(0).getAmount());
        assertEquals(new BigDecimal("40.000000"), insertedEntries.get(1).getAmount());
        assertEquals(new BigDecimal("6.000000"), insertedResultsRef.get().get(0).getBasisValue());
        assertEquals(new BigDecimal("4.000000"), insertedResultsRef.get().get(1).getBasisValue());
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
