package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
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
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsMrpExt.PRODUCTION_COST_ALLOCATION_BASIS_EMPTY;
import static cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostAllocationBasisTypeEnum.OUTPUT;
import static cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostAllocationBasisTypeEnum.WEIGHT;
import static cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostAllocationStatusEnum.DRAFT;
import static cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostAllocationStatusEnum.EXECUTED;
import static cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostSourceTypeEnum.ALLOCATION;
import static cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostTypeEnum.LABOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpProductionCostAllocationServiceImplEnhancementTest {

    @InjectMocks
    private ErpProductionCostAllocationServiceImpl service;

    @Mock
    private ErpProductionCostAllocationMapper erpProductionCostAllocationMapper;
    @Mock
    private ErpProductionCostAllocationRuleMapper erpProductionCostAllocationRuleMapper;
    @Mock
    private ErpProductionCostAllocationResultMapper erpProductionCostAllocationResultMapper;
    @Mock
    private ErpProductionManHourMapper erpProductionManHourMapper;
    @Mock
    private ErpProductionCostEntryMapper erpProductionCostEntryMapper;
    @Mock
    private ErpProductionOrderService productionOrderService;
    @Mock
    private cn.weitee.erp.module.erp.service.product.ErpProductService productService;
    @Mock
    private cn.weitee.erp.module.erp.service.project.ErpProjectService projectService;
    @Mock
    private ErpNoRedisDAO noRedisDAO;

    @Test
    void executeProductionCostAllocation_shouldAllocateByOutputBasis() {
        ErpProductionCostAllocationDO allocation = allocation(1L, "CBFT202604280001", "2026-04", 100.00, OUTPUT.getType());
        ErpProductionCostAllocationRuleDO rule = rule(11L, "按产量", OUTPUT.getType());

        when(erpProductionCostAllocationMapper.selectById(1L)).thenReturn(allocation);
        when(erpProductionCostAllocationResultMapper.selectListByAllocationId(1L)).thenReturn(List.of());
        when(erpProductionCostAllocationRuleMapper.selectById(11L)).thenReturn(rule);
        when(erpProductionManHourMapper.selectListByAccountingMonth("2026-04")).thenReturn(List.of(
                manHour(101L, 1001L, new BigDecimal("1.00")),
                manHour(102L, 1002L, new BigDecimal("1.00"))
        ));
        when(productionOrderService.getProductionOrderList(anyCollection())).thenReturn(List.of(
                order(1001L, null, new BigDecimal("10")),
                order(1002L, null, new BigDecimal("30"))
        ));

        List<ErpProductionCostEntryDO> insertedEntries = new ArrayList<>();
        AtomicReference<List<ErpProductionCostAllocationResultDO>> capturedResults = new AtomicReference<>();
        AtomicReference<ErpProductionCostAllocationDO> capturedAllocationUpdate = new AtomicReference<>();
        AtomicLong costEntryIdSeq = new AtomicLong(5001L);
        doAnswer(invocation -> {
            ErpProductionCostEntryDO entry = invocation.getArgument(0);
            entry.setId(costEntryIdSeq.getAndIncrement());
            insertedEntries.add(entry);
            return 1;
        }).when(erpProductionCostEntryMapper).insert(any(ErpProductionCostEntryDO.class));
        doAnswer(invocation -> {
            capturedResults.set(new ArrayList<>(invocation.getArgument(0)));
            return true;
        }).when(erpProductionCostAllocationResultMapper).insertBatch(anyList());
        doAnswer(invocation -> {
            capturedAllocationUpdate.set(invocation.getArgument(0));
            return 1;
        }).when(erpProductionCostAllocationMapper).updateById(any(ErpProductionCostAllocationDO.class));

        service.executeProductionCostAllocation(1L);

        assertEquals(2, insertedEntries.size());
        assertEquals(ALLOCATION.getType(), insertedEntries.get(0).getSourceType());
        assertEquals(new BigDecimal("25.000000"), insertedEntries.get(0).getAmount());
        assertEquals(new BigDecimal("75.000000"), insertedEntries.get(1).getAmount());
        assertEquals(2, capturedResults.get().size());
        assertEquals(new BigDecimal("10.000000"), capturedResults.get().get(0).getBasisValue());
        assertEquals(new BigDecimal("30.000000"), capturedResults.get().get(1).getBasisValue());
        assertEquals(new BigDecimal("0.250000"), capturedResults.get().get(0).getBasisRatio());
        assertEquals(new BigDecimal("0.750000"), capturedResults.get().get(1).getBasisRatio());
        assertNotNull(capturedResults.get().get(0).getGeneratedCostEntryId());
        assertEquals(EXECUTED.getStatus(), capturedAllocationUpdate.get().getStatus());
        assertNotNull(capturedAllocationUpdate.get().getExecutedTime());
    }

    @Test
    void executeProductionCostAllocation_shouldAllocateByWeightBasis() {
        ErpProductionCostAllocationDO allocation = allocation(2L, "CBFT202604280002", "2026-04", 90.00, WEIGHT.getType());
        ErpProductionCostAllocationRuleDO rule = rule(12L, "按重量", WEIGHT.getType());

        when(erpProductionCostAllocationMapper.selectById(2L)).thenReturn(allocation);
        when(erpProductionCostAllocationResultMapper.selectListByAllocationId(2L)).thenReturn(List.of());
        when(erpProductionCostAllocationRuleMapper.selectById(12L)).thenReturn(rule);
        when(erpProductionManHourMapper.selectListByAccountingMonth("2026-04")).thenReturn(List.of(
                manHour(201L, 2001L, new BigDecimal("1.00")),
                manHour(202L, 2002L, new BigDecimal("1.00"))
        ));
        when(productionOrderService.getProductionOrderList(anyCollection())).thenReturn(List.of(
                order(2001L, 3001L, new BigDecimal("10")),
                order(2002L, 3002L, new BigDecimal("4"))
        ));
        when(productService.getProductVOMap(anyCollection())).thenReturn(Map.of(
                3001L, product(3001L, new BigDecimal("2.00")),
                3002L, product(3002L, new BigDecimal("5.00"))
        ));

        List<ErpProductionCostEntryDO> insertedEntries = new ArrayList<>();
        AtomicReference<List<ErpProductionCostAllocationResultDO>> capturedResults = new AtomicReference<>();
        AtomicLong costEntryIdSeq = new AtomicLong(6001L);
        doAnswer(invocation -> {
            ErpProductionCostEntryDO entry = invocation.getArgument(0);
            entry.setId(costEntryIdSeq.getAndIncrement());
            insertedEntries.add(entry);
            return 1;
        }).when(erpProductionCostEntryMapper).insert(any(ErpProductionCostEntryDO.class));
        doAnswer(invocation -> {
            capturedResults.set(new ArrayList<>(invocation.getArgument(0)));
            return true;
        }).when(erpProductionCostAllocationResultMapper).insertBatch(anyList());
        doAnswer(invocation -> 1).when(erpProductionCostAllocationMapper).updateById(any(ErpProductionCostAllocationDO.class));

        service.executeProductionCostAllocation(2L);

        assertEquals(2, insertedEntries.size());
        assertEquals(new BigDecimal("45.000000"), insertedEntries.get(0).getAmount());
        assertEquals(new BigDecimal("45.000000"), insertedEntries.get(1).getAmount());
        assertEquals(new BigDecimal("20.000000"), capturedResults.get().get(0).getBasisValue());
        assertEquals(new BigDecimal("20.000000"), capturedResults.get().get(1).getBasisValue());
        assertEquals(new BigDecimal("0.500000"), capturedResults.get().get(0).getBasisRatio());
        assertEquals(new BigDecimal("0.500000"), capturedResults.get().get(1).getBasisRatio());
    }

    @Test
    void executeProductionCostAllocation_shouldRejectWhenBasisDataMissing() {
        ErpProductionCostAllocationDO allocation = allocation(3L, "CBFT202604280003", "2026-04", 100.00, OUTPUT.getType());
        ErpProductionCostAllocationRuleDO rule = rule(13L, "按产量", OUTPUT.getType());

        when(erpProductionCostAllocationMapper.selectById(3L)).thenReturn(allocation);
        when(erpProductionCostAllocationResultMapper.selectListByAllocationId(3L)).thenReturn(List.of());
        when(erpProductionCostAllocationRuleMapper.selectById(13L)).thenReturn(rule);
        when(erpProductionManHourMapper.selectListByAccountingMonth("2026-04")).thenReturn(List.of());

        ServiceException ex = assertThrows(ServiceException.class, () -> service.executeProductionCostAllocation(3L));

        assertEquals(PRODUCTION_COST_ALLOCATION_BASIS_EMPTY.getCode(), ex.getCode());
    }

    private ErpProductionCostAllocationDO allocation(Long id, String allocationNo, String accountingMonth,
                                                      double totalAmount, Integer basisType) {
        return new ErpProductionCostAllocationDO()
                .setId(id)
                .setAllocationNo(allocationNo)
                .setAccountingMonth(accountingMonth)
                .setCostType(LABOR.getType())
                .setRuleId(id + 10)
                .setTotalAmount(BigDecimal.valueOf(totalAmount))
                .setStatus(DRAFT.getStatus())
                .setRemark("测试分摊单");
    }

    private ErpProductionCostAllocationRuleDO rule(Long id, String ruleName, Integer basisType) {
        return new ErpProductionCostAllocationRuleDO()
                .setId(id)
                .setRuleName(ruleName)
                .setCostType(LABOR.getType())
                .setBasisType(basisType)
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    private ErpProductionManHourDO manHour(Long id, Long productionOrderId, BigDecimal manHour) {
        return new ErpProductionManHourDO()
                .setId(id)
                .setProductionOrderId(productionOrderId)
                .setAccountingMonth("2026-04")
                .setManHour(manHour);
    }

    private ErpProductionOrderDO order(Long id, Long productId, BigDecimal finishedQty) {
        return new ErpProductionOrderDO()
                .setId(id)
                .setOrderNo("SCGD" + id)
                .setProductId(productId)
                .setFinishedQty(finishedQty);
    }

    private ErpProductRespVO product(Long id, BigDecimal weight) {
        return new ErpProductRespVO()
                .setId(id)
                .setName("产品" + id)
                .setWeight(weight);
    }

}
