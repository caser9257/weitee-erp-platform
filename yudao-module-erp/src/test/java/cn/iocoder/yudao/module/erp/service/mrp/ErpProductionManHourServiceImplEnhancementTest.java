package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionManHourProjectSummaryReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionManHourProjectSummaryRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionCostEntryDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionManHourDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionCostAllocationMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionCostEntryMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionManHourMapper;
import cn.iocoder.yudao.module.erp.service.project.ErpProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstantsMrpExt.PRODUCTION_ACCOUNTING_MONTH_INVALID;
import static cn.iocoder.yudao.module.erp.enums.mrp.ErpProductionCostTypeEnum.LABOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErpProductionManHourServiceImplEnhancementTest {

    @InjectMocks
    private ErpProductionManHourServiceImpl service;

    @Mock
    private ErpProductionManHourMapper erpProductionManHourMapper;
    @Mock
    private ErpProductionCostEntryMapper erpProductionCostEntryMapper;
    @Mock
    private ErpProductionOrderService productionOrderService;
    @Mock
    private ErpProjectService projectService;
    @Mock
    private ErpProductionCostAllocationMapper erpProductionCostAllocationMapper;

    @Test
    void getProductionManHourProjectSummary_shouldAggregateByProjectAndComputeLaborUnitCost() {
        when(erpProductionManHourMapper.selectListByAccountingMonth("2026-04")).thenReturn(List.of(
                manHour(101L, 1001L, LocalDate.of(2026, 4, 1), new BigDecimal("2.00")),
                manHour(102L, 1001L, LocalDate.of(2026, 4, 2), new BigDecimal("3.00")),
                manHour(103L, 1002L, LocalDate.of(2026, 4, 3), new BigDecimal("4.00")),
                manHour(104L, 1003L, LocalDate.of(2026, 4, 5), new BigDecimal("6.00"))
        ));
        when(productionOrderService.getProductionOrderList(anyCollection())).thenReturn(List.of(
                order(1001L, 2001L),
                order(1002L, 2001L),
                order(1003L, 2002L)
        ));
        when(erpProductionCostEntryMapper.selectListByAccountingMonth("2026-04")).thenReturn(List.of(
                costEntry(201L, 1001L, new BigDecimal("50.00")),
                costEntry(202L, 1002L, new BigDecimal("40.00")),
                costEntry(203L, 1003L, new BigDecimal("30.00")),
                costEntry(204L, 1003L, new BigDecimal("5.00"), LABOR.getType() + 100)
        ));
        when(projectService.getProjectMap(anyCollection())).thenReturn(Map.of(
                2001L, project(2001L, "XM202604280001", "新产品项目A"),
                2002L, project(2002L, "XM202604280002", "新产品项目B")
        ));

        List<ErpProductionManHourProjectSummaryRespVO> summaryList = service.getProductionManHourProjectSummaryList(
                new ErpProductionManHourProjectSummaryReqVO().setAccountingMonth("2026-04"));

        assertEquals(2, summaryList.size());
        ErpProductionManHourProjectSummaryRespVO projectA = summaryList.get(0);
        ErpProductionManHourProjectSummaryRespVO projectB = summaryList.get(1);
        assertEquals(2001L, projectA.getProjectId());
        assertEquals("XM202604280001", projectA.getProjectNo());
        assertEquals("新产品项目A", projectA.getProjectName());
        assertEquals(2, projectA.getProductionOrderCount());
        assertEquals(new BigDecimal("9.000000"), projectA.getManHour());
        assertEquals(new BigDecimal("90.000000"), projectA.getLaborCost());
        assertEquals(new BigDecimal("10.000000"), projectA.getLaborUnitCost());
        assertEquals(LocalDate.of(2026, 4, 3), projectA.getLastWorkDate());
        assertEquals(2002L, projectB.getProjectId());
        assertEquals(1, projectB.getProductionOrderCount());
        assertEquals(new BigDecimal("6.000000"), projectB.getManHour());
        assertEquals(new BigDecimal("30.000000"), projectB.getLaborCost());
        assertEquals(new BigDecimal("5.000000"), projectB.getLaborUnitCost());
        assertEquals(LocalDate.of(2026, 4, 5), projectB.getLastWorkDate());
    }

    @Test
    void getProductionManHourProjectSummary_shouldRejectInvalidAccountingMonth() {
        ServiceException ex = assertThrows(ServiceException.class, () ->
                service.getProductionManHourProjectSummaryList(
                        new ErpProductionManHourProjectSummaryReqVO().setAccountingMonth("2026/04")));

        assertEquals(PRODUCTION_ACCOUNTING_MONTH_INVALID.getCode(), ex.getCode());
    }

    private ErpProductionManHourDO manHour(Long id, Long productionOrderId, LocalDate workDate, BigDecimal manHour) {
        return new ErpProductionManHourDO()
                .setId(id)
                .setProductionOrderId(productionOrderId)
                .setAccountingMonth("2026-04")
                .setWorkDate(workDate)
                .setManHour(manHour);
    }

    private ErpProductionOrderDO order(Long id, Long projectId) {
        return new ErpProductionOrderDO()
                .setId(id)
                .setOrderNo("SCGD" + id)
                .setProjectId(projectId);
    }

    private ErpProductionCostEntryDO costEntry(Long id, Long productionOrderId, BigDecimal amount) {
        return costEntry(id, productionOrderId, amount, LABOR.getType());
    }

    private ErpProductionCostEntryDO costEntry(Long id, Long productionOrderId, BigDecimal amount, Integer costType) {
        return new ErpProductionCostEntryDO()
                .setId(id)
                .setProductionOrderId(productionOrderId)
                .setCostType(costType)
                .setAccountingMonth("2026-04")
                .setAmount(amount);
    }

    private ErpProjectDO project(Long id, String no, String name) {
        return new ErpProjectDO()
                .setId(id)
                .setNo(no)
                .setName(name);
    }

}
