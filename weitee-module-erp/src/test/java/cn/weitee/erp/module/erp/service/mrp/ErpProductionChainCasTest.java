package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.issue.ErpProductionIssueCreateReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationRuleDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionCostEntryDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionFinishQualityDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionInboundDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionIssueDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionIssueItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionManHourDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionMaterialDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockBatchDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionCostAllocationMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionCostAllocationResultMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionCostAllocationRuleMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionCostEntryMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionFinishQualityMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionInboundMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionIssueBatchMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionIssueItemMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionIssueMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionManHourMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionMaterialMapper;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseInItemMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.ErpQaStatusEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionCostAllocationStatusEnum;
import cn.weitee.erp.module.erp.enums.mrp.ErpProductionInboundStatusEnum;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceBizHookService;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.stock.ErpStockBatchService;
import cn.weitee.erp.module.erp.service.stock.ErpStockRecordService;
import cn.weitee.erp.module.erp.service.stock.ErpStockService;
import cn.weitee.erp.module.erp.service.stock.ErpWarehouseService;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchChangeReqBO;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockBatchInboundReqBO;
import cn.weitee.erp.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import cn.weitee.erp.module.system.api.user.AdminUserApi;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PRODUCTION_MATERIAL_QTY_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstantsMrpExt.PRODUCTION_COST_ALLOCATION_ALREADY_EXECUTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * S3 生产链闭环：并发/重复提交的 CAS 拒绝路径单测。
 * 断言状态 CAS 失败时，库存、流水、成本条目和下游 hook 均不被触发。
 */
@ExtendWith(MockitoExtension.class)
class ErpProductionChainCasTest {

    @Nested
    class InboundCasTest {

        @Mock
        private ErpProductionInboundMapper erpProductionInboundMapper;
        @Mock
        private ErpProductionOrderService productionOrderService;
        @Mock
        private ErpProductionCostService productionCostService;
        @Mock
        private ErpNoRedisDAO noRedisDAO;
        @Mock
        private ErpStockBatchService stockBatchService;
        @Mock
        private ErpStockRecordService stockRecordService;
        @Mock
        private ErpFinanceBizHookService financeBizHookService;

        @InjectMocks
        private ErpProductionInboundServiceImpl service;

        @Test
        void executeProductionInbound_shouldNotTouchStockWhenCasFails() {
            when(erpProductionInboundMapper.selectById(61L)).thenReturn(new ErpProductionInboundDO()
                    .setId(61L)
                    .setNo("ZZRK20260903000001")
                    .setProductId(31L)
                    .setWarehouseId(41L)
                    .setInboundQty(new BigDecimal("6"))
                    .setStatus(ErpProductionInboundStatusEnum.PENDING.getStatus()));
            when(erpProductionInboundMapper.updateByIdAndStatus(eq(61L),
                    eq(ErpProductionInboundStatusEnum.PENDING.getStatus()),
                    any(ErpProductionInboundDO.class))).thenReturn(0);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> service.executeProductionInbound(99L, 61L));

            assertEquals(1_030_700_041, ex.getCode());
            verify(stockBatchService, never()).createOrIncreaseBatch(any(ErpStockBatchInboundReqBO.class));
            verify(stockRecordService, never()).createStockRecord(any(ErpStockRecordCreateReqBO.class));
            verify(financeBizHookService, never()).handleApprovedBiz(any(), any(), any());
        }

        @Test
        void revertProductionInbound_shouldNotDecreaseStockWhenCasFails() {
            when(erpProductionInboundMapper.selectById(63L)).thenReturn(new ErpProductionInboundDO()
                    .setId(63L)
                    .setNo("ZZRK20260903000002")
                    .setProductId(31L)
                    .setWarehouseId(41L)
                    .setInboundQty(new BigDecimal("7"))
                    .setStatus(ErpProductionInboundStatusEnum.EXECUTED.getStatus()));
            when(stockBatchService.getStockBatchByProductWarehouseAndBatchNo(31L, 41L, "ZZRK20260903000002"))
                    .thenReturn(new ErpStockBatchDO().setId(701L));
            when(erpProductionInboundMapper.updateByIdAndStatus(eq(63L),
                    eq(ErpProductionInboundStatusEnum.EXECUTED.getStatus()),
                    any(ErpProductionInboundDO.class))).thenReturn(0);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> service.revertProductionInbound(100L, 63L));

            assertEquals(1_030_700_041, ex.getCode());
            verify(stockBatchService, never()).decreaseBatch(any(ErpStockBatchChangeReqBO.class));
            verify(stockRecordService, never()).createStockRecord(any(ErpStockRecordCreateReqBO.class));
            verify(financeBizHookService, never()).handleRollbackBiz(any(), any(), any(), any());
        }

        @Test
        void cancelProductionInbound_shouldRejectWhenCasFails() {
            when(erpProductionInboundMapper.selectById(62L)).thenReturn(new ErpProductionInboundDO()
                    .setId(62L)
                    .setStatus(ErpProductionInboundStatusEnum.PENDING.getStatus()));
            when(erpProductionInboundMapper.updateByIdAndStatus(eq(62L),
                    eq(ErpProductionInboundStatusEnum.PENDING.getStatus()),
                    any(ErpProductionInboundDO.class))).thenReturn(0);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> service.cancelProductionInbound(62L));

            assertEquals(1_030_700_041, ex.getCode());
        }
    }

    @Nested
    class CostAllocationCasTest {

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
        private ErpProductService productService;
        @Mock
        private cn.weitee.erp.module.erp.service.project.ErpProjectService projectService;
        @Mock
        private ErpNoRedisDAO noRedisDAO;

        @InjectMocks
        private ErpProductionCostAllocationServiceImpl service;

        @Test
        void executeProductionCostAllocation_shouldNotInsertCostEntriesWhenCasFails() {
            ErpProductionCostAllocationDO allocation = new ErpProductionCostAllocationDO()
                    .setId(1L)
                    .setAllocationNo("CBFT20260903000001")
                    .setAccountingMonth("2026-09")
                    .setCostType(20)
                    .setRuleId(11L)
                    .setTotalAmount(new BigDecimal("100.00"))
                    .setStatus(ErpProductionCostAllocationStatusEnum.DRAFT.getStatus());
            when(erpProductionCostAllocationMapper.selectById(1L)).thenReturn(allocation);
            when(erpProductionCostAllocationResultMapper.selectListByAllocationId(1L)).thenReturn(List.of());
            when(erpProductionCostAllocationRuleMapper.selectById(11L)).thenReturn(
                    new ErpProductionCostAllocationRuleDO()
                            .setId(11L)
                            .setCostType(20)
                            .setBasisType(20)
                            .setStatus(0));
            when(erpProductionManHourMapper.selectListByAccountingMonth("2026-09")).thenReturn(List.of(
                    new ErpProductionManHourDO().setId(101L).setProductionOrderId(1001L)
                            .setAccountingMonth("2026-09").setManHour(new BigDecimal("1.00"))));
            when(productionOrderService.getProductionOrderList(anyCollection())).thenReturn(List.of(
                    new ErpProductionOrderDO().setId(1001L).setFinishedQty(new BigDecimal("10"))));
            when(erpProductionCostAllocationMapper.updateByIdAndStatus(eq(1L),
                    eq(ErpProductionCostAllocationStatusEnum.DRAFT.getStatus()),
                    any(ErpProductionCostAllocationDO.class))).thenReturn(0);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> service.executeProductionCostAllocation(1L));

            assertEquals(PRODUCTION_COST_ALLOCATION_ALREADY_EXECUTED.getCode(), ex.getCode());
            verify(erpProductionCostEntryMapper, never()).insert(any(ErpProductionCostEntryDO.class));
            verify(erpProductionCostAllocationResultMapper, never()).insertBatch(any());
        }
    }

    @Nested
    class FinishQualityCasTest {

        @Mock
        private ErpProductionFinishQualityMapper erpProductionFinishQualityMapper;
        @Mock
        private ErpNoRedisDAO noRedisDAO;
        @Mock
        private ApplicationEventPublisher eventPublisher;
        @Mock
        private ErpProductionInboundService productionInboundService;

        @InjectMocks
        private ErpProductionFinishQualityServiceImpl service;

        @Test
        void submitQuality_shouldNotCreateInboundOrPublishEventWhenCasFails() {
            when(erpProductionFinishQualityMapper.selectById(1L)).thenReturn(new ErpProductionFinishQualityDO()
                    .setId(1L)
                    .setProductionOrderId(11L)
                    .setReportQty(new BigDecimal("10"))
                    .setSourceOrderId(30L)
                    .setStatus(ErpQaStatusEnum.TO_INSPECT.getStatus()));
            when(erpProductionFinishQualityMapper.updateByIdAndStatus(eq(1L),
                    eq(ErpQaStatusEnum.TO_INSPECT.getStatus()),
                    any(ErpProductionFinishQualityDO.class))).thenReturn(0);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> service.submitQuality(1L, 2L, new BigDecimal("10"), BigDecimal.ZERO, "all passed"));

            assertEquals(1_030_700_015, ex.getCode());
            verify(productionInboundService, never()).createProductionInboundFromQuality(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    class MaterialMapperGuardTest {

        @Test
        @SuppressWarnings("unchecked")
        void updateIssuedQtyIncrement_shouldGuardWithNetQtySoReissueAfterReturnIsAllowed() {
            // LambdaUpdateWrapper 解析列名依赖 MP 的 TableInfo 缓存，纯单测需手动初始化
            com.baomidou.mybatisplus.core.metadata.TableInfoHelper.initTableInfo(
                    new org.apache.ibatis.builder.MapperBuilderAssistant(
                            new com.baomidou.mybatisplus.core.MybatisConfiguration(), ""),
                    ErpProductionMaterialDO.class);
            ErpProductionMaterialMapper mapper = mock(ErpProductionMaterialMapper.class, CALLS_REAL_METHODS);

            mapper.updateIssuedQtyIncrement(1L, new BigDecimal("5"));

            ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.Wrapper<ErpProductionMaterialDO>> captor =
                    ArgumentCaptor.forClass(com.baomidou.mybatisplus.core.conditions.Wrapper.class);
            verify(mapper).update(org.mockito.ArgumentMatchers.isNull(), captor.capture());
            com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ErpProductionMaterialDO> wrapper =
                    (com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ErpProductionMaterialDO>) captor.getValue();
            // 守卫必须是净额口径（issued - returned + qty <= required），
            // 否则"退料后重新领料"会被误拒，而静默失败又会漏记 issued_qty
            org.assertj.core.api.Assertions.assertThat(wrapper.getSqlSegment())
                    .contains("issued_qty - returned_qty + ");
            org.assertj.core.api.Assertions.assertThat(wrapper.getSqlSet())
                    .contains("issued_qty = issued_qty + 5");
        }
    }

    @Nested
    class IssueCasTest {

        @Mock
        private ErpProductionMaterialService productionMaterialService;
        @Mock
        private ErpProductionOrderService productionOrderService;
        @Mock
        private ErpProductionMaterialMapper erpProductionMaterialMapper;
        @Mock
        private ErpProductionIssueMapper erpProductionIssueMapper;
        @Mock
        private ErpProductionIssueItemMapper erpProductionIssueItemMapper;
        @Mock
        private ErpProductionIssueBatchMapper erpProductionIssueBatchMapper;
        @Mock
        private ErpPurchaseInItemMapper erpPurchaseInItemMapper;
        @Mock
        private ErpStockBatchService stockBatchService;
        @Mock
        private ErpStockRecordService stockRecordService;
        @Mock
        private ErpStockService stockService;
        @Mock
        private ErpProductService productService;
        @Mock
        private ErpWarehouseService warehouseService;
        @Mock
        private AdminUserApi adminUserApi;
        @Mock
        private ErpNoRedisDAO noRedisDAO;
        @Mock
        private ErpProductionIssueVoucherService productionIssueVoucherService;
        @Mock
        private ErpProductionIqcStockService iqcStockService;

        @InjectMocks
        private ErpProductionIssueServiceImpl service;

        @Test
        void createProductionIssue_shouldRollbackWhenIssuedQtyCasFails() {
            when(productionOrderService.getProductionOrder(1L)).thenReturn(new ErpProductionOrderDO()
                    .setId(1L).setOrderNo("SCGD20260903000001").setStatus(10));
            when(erpProductionMaterialMapper.selectListByIds(any())).thenReturn(List.of(
                    new ErpProductionMaterialDO()
                            .setId(11L)
                            .setProductionOrderId(1L)
                            .setMaterialId(1001L)
                            .setRequiredQty(new BigDecimal("20"))
                            .setIssuedQty(BigDecimal.ZERO)
                            .setReturnedQty(BigDecimal.ZERO)));
            when(stockBatchService.getStockBatchList(anyCollection())).thenReturn(List.of(
                    new ErpStockBatchDO()
                            .setId(9001L)
                            .setProductId(1001L)
                            .setWarehouseId(2001L)
                            .setBatchNo("B202609030001")));
            when(erpPurchaseInItemMapper.selectListByIds(any())).thenReturn(List.of());
            when(stockService.getStockMapByProductAndWarehouseIds(anyCollection(), anyCollection()))
                    .thenReturn(Map.of());
            when(noRedisDAO.generate("SCLL")).thenReturn("SCLL20260903000001");
            doAnswer(invocation -> {
                ((ErpProductionIssueDO) invocation.getArgument(0)).setId(101L);
                return 1;
            }).when(erpProductionIssueMapper).insert(any(ErpProductionIssueDO.class));
            doAnswer(invocation -> {
                ((ErpProductionIssueItemDO) invocation.getArgument(0)).setId(10001L);
                return 1;
            }).when(erpProductionIssueItemMapper).insert(any(ErpProductionIssueItemDO.class));
            // 并发超领：前置校验通过后，另一事务已把 issued_qty 抬高，CAS 守卫返回 0
            when(erpProductionMaterialMapper.updateIssuedQtyIncrement(eq(11L), any(BigDecimal.class)))
                    .thenReturn(0);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> service.createProductionIssue(new ErpProductionIssueCreateReqVO()
                            .setProductionOrderId(1L)
                            .setItems(List.of(new ErpProductionIssueCreateReqVO.Item()
                                    .setProductionMaterialId(11L)
                                    .setMaterialId(1001L)
                                    .setWarehouseId(2001L)
                                    .setIssueQty(new BigDecimal("10"))
                                    .setBatches(List.of(new ErpProductionIssueCreateReqVO.Batch()
                                            .setStockBatchId(9001L)
                                            .setBatchNo("B202609030001")
                                            .setIssueQty(new BigDecimal("10"))))))));

            assertEquals(PRODUCTION_MATERIAL_QTY_INVALID.getCode(), ex.getCode());
            verify(productionIssueVoucherService, never()).createVoucher(any(), any());
            verify(iqcStockService, never()).deductStockForProduction(any(), any(), any(), any());
        }
    }

}
