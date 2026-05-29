package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostDetailRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationResultDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionCostEntryDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionCostAllocationMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionCostAllocationResultMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionCostEntryMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionIssueMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCTION_ORDER_NOT_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.mrp.ErpProductionCostTypeEnum.DEPRECIATION;
import static cn.iocoder.yudao.module.erp.enums.mrp.ErpProductionCostTypeEnum.LABOR;
import static cn.iocoder.yudao.module.erp.enums.mrp.ErpProductionCostTypeEnum.OTHER;
import static cn.iocoder.yudao.module.erp.enums.mrp.ErpProductionCostTypeEnum.POWER;
import static cn.iocoder.yudao.module.erp.enums.mrp.ErpProductionCostTypeEnum.resolveName;
import static cn.iocoder.yudao.module.erp.enums.mrp.ErpProductionCostSourceTypeEnum.MANUAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpProductionCostServiceImplEnhancementTest {

    @InjectMocks
    private ErpProductionCostServiceImpl service;

    @Mock
    private ErpProductionCostEntryMapper erpProductionCostEntryMapper;
    @Mock
    private ErpProductionIssueMapper erpProductionIssueMapper;
    @Mock
    private ErpProductionCostAllocationResultMapper erpProductionCostAllocationResultMapper;
    @Mock
    private ErpProductionCostAllocationMapper erpProductionCostAllocationMapper;
    @Mock
    private ErpProductionOrderService productionOrderService;
    @Mock
    private cn.iocoder.yudao.module.erp.service.product.ErpProductService productService;
    @Mock
    private cn.iocoder.yudao.module.erp.service.project.ErpProjectService projectService;

    @Test
    void getCostDetail_shouldSplitDetailsAndKeepTotalsConsistent() {
        when(productionOrderService.getProductionOrder(1L)).thenReturn(order(1L, 1001L, 2001L, new BigDecimal("20")));
        when(erpProductionIssueMapper.selectListByProductionOrderId(1L)).thenReturn(List.of(
                issue(11L, "SCLL202604280001", new BigDecimal("100.00"), LocalDateTime.of(2026, 4, 28, 9, 0)),
                issue(12L, "SCLL202604280002", new BigDecimal("20.00"), LocalDateTime.of(2026, 4, 28, 13, 0))
        ));
        when(erpProductionCostEntryMapper.selectListByProductionOrderId(1L)).thenReturn(List.of(
                entry(101L, LABOR.getType(), new BigDecimal("30.00")),
                entry(102L, DEPRECIATION.getType(), new BigDecimal("10.00")),
                entry(103L, POWER.getType(), new BigDecimal("5.00")),
                entry(104L, OTHER.getType(), new BigDecimal("2.00"))
        ));
        when(erpProductionCostAllocationResultMapper.selectListByGeneratedCostEntryIds(anyCollection())).thenReturn(List.of());
        when(productService.getProductVOMap(anyCollection())).thenReturn(Map.of(
                1001L, product(1001L, "精密组件A", new BigDecimal("1.20"))));
        when(projectService.getProjectMap(anyCollection())).thenReturn(Map.of(
                2001L, project(2001L, "XM202604280001", "新产品项目")));

        ErpProductionCostDetailRespVO detail = service.getCostDetail(1L);

        assertEquals(1L, detail.getProductionOrderId());
        assertEquals("SCGD202604280001", detail.getProductionOrderNo());
        assertEquals(1001L, detail.getProductId());
        assertEquals("精密组件A", detail.getProductName());
        assertEquals(2001L, detail.getProjectId());
        assertEquals("XM202604280001", detail.getProjectNo());
        assertEquals("新产品项目", detail.getProjectName());
        assertEquals(new BigDecimal("120.00"), detail.getMaterialCost().setScale(2));
        assertEquals(new BigDecimal("30.00"), detail.getLaborCost().setScale(2));
        assertEquals(new BigDecimal("10.00"), detail.getDepreciationCost().setScale(2));
        assertEquals(new BigDecimal("5.00"), detail.getPowerCost().setScale(2));
        assertEquals(new BigDecimal("2.00"), detail.getOtherCost().setScale(2));
        assertEquals(new BigDecimal("167.00"), detail.getTotalCost().setScale(2));
        assertEquals(new BigDecimal("8.350000"), detail.getUnitCost());
        assertEquals(2, detail.getMaterialDetails().size());
        assertEquals(4, detail.getCostEntries().size());
        assertEquals(1, detail.getLaborDetails().size());
        assertEquals(LABOR.getType(), detail.getLaborDetails().get(0).getCostType());
        assertEquals(resolveName(LABOR.getType()), detail.getLaborDetails().get(0).getCostTypeName());
        assertEquals(3, detail.getManufacturingCostDetails().size());
        assertTrue(detail.getManufacturingCostDetails().stream()
                .map(ErpProductionCostDetailRespVO.CostEntry::getCostType)
                .allMatch(type -> type != null && !LABOR.getType().equals(type)));
        assertEquals(detail.getCostEntries().size(),
                detail.getLaborDetails().size() + detail.getManufacturingCostDetails().size());
    }

    @Test
    void getCostDetail_shouldRejectWhenProductionOrderMissing() {
        when(productionOrderService.getProductionOrder(1L)).thenReturn(null);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.getCostDetail(1L));

        assertEquals(PRODUCTION_ORDER_NOT_EXISTS.getCode(), ex.getCode());
    }

    private ErpProductionOrderDO order(Long id, Long productId, Long projectId, BigDecimal finishedQty) {
        return new ErpProductionOrderDO()
                .setId(id)
                .setOrderNo("SCGD202604280001")
                .setProductId(productId)
                .setProjectId(projectId)
                .setFinishedQty(finishedQty);
    }

    private ErpProductionIssueDO issue(Long id, String issueNo, BigDecimal issueAmount, LocalDateTime issueTime) {
        return new ErpProductionIssueDO()
                .setId(id)
                .setIssueNo(issueNo)
                .setProductionOrderId(1L)
                .setIssueTime(issueTime)
                .setIssueAmount(issueAmount);
    }

    private ErpProductionCostEntryDO entry(Long id, Integer costType, BigDecimal amount) {
        return new ErpProductionCostEntryDO()
                .setId(id)
                .setProductionOrderId(1L)
                .setCostType(costType)
                .setSourceType(MANUAL.getType())
                .setAccountingMonth("2026-04")
                .setAmount(amount)
                .setRemark("测试分录");
    }

    private ErpProductRespVO product(Long id, String name, BigDecimal weight) {
        return new ErpProductRespVO()
                .setId(id)
                .setName(name)
                .setWeight(weight);
    }

    private ErpProjectDO project(Long id, String no, String name) {
        return new ErpProjectDO()
                .setId(id)
                .setNo(no)
                .setName(name);
    }

}
