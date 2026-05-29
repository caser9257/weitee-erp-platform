package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostDetailRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostEntryPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostProjectSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.cost.ErpProductionCostSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionCostAllocationResultDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionCostEntryDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionIssueDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionManHourDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionCostAllocationMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionCostAllocationResultMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionCostEntryMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionIssueMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionManHourMapper;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpProductionCostSourceTypeEnum;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.project.ErpProjectService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ErpProductionCostServiceImplTest {

    @Test
    void getCostDetail_shouldAggregateMaterialAndManualCosts() throws Exception {
        ErpProductionCostServiceImpl service = new ErpProductionCostServiceImpl();
        setField(service, "productionOrderService", createProxy(ErpProductionOrderService.class, (methodName, args) -> {
            if ("getProductionOrder".equals(methodName)) {
                return new ErpProductionOrderDO()
                        .setId(1L)
                        .setOrderNo("SCGD202604280001")
                        .setProjectId(2001L)
                        .setFinishedQty(new BigDecimal("20"));
            }
            return null;
        }));
        setField(service, "erpProductionIssueMapper", createProxy(ErpProductionIssueMapper.class, (methodName, args) -> {
            if ("selectListByProductionOrderId".equals(methodName)) {
                return List.of(
                        new ErpProductionIssueDO().setId(11L).setIssueNo("SCLL202604280001").setProductionOrderId(1L)
                                .setIssueTime(LocalDateTime.of(2026, 4, 28, 9, 0)).setIssueAmount(new BigDecimal("100.00")),
                        new ErpProductionIssueDO().setId(12L).setIssueNo("SCLL202604280002").setProductionOrderId(1L)
                                .setIssueTime(LocalDateTime.of(2026, 4, 28, 13, 0)).setIssueAmount(new BigDecimal("20.00")));
            }
            return null;
        }));
        setField(service, "erpProductionCostEntryMapper", createProxy(ErpProductionCostEntryMapper.class, (methodName, args) -> {
            if ("selectListByProductionOrderId".equals(methodName)) {
                return List.of(
                        new ErpProductionCostEntryDO().setId(101L).setProductionOrderId(1L).setCostType(20)
                                .setAccountingMonth("2026-04").setAmount(new BigDecimal("30.00")).setRemark("manual"),
                        new ErpProductionCostEntryDO().setId(102L).setProductionOrderId(1L).setCostType(30)
                                .setAccountingMonth("2026-04").setAmount(new BigDecimal("10.00")).setRemark("depreciation"),
                        new ErpProductionCostEntryDO().setId(103L).setProductionOrderId(1L).setCostType(40)
                                .setAccountingMonth("2026-04").setAmount(new BigDecimal("5.00")).setRemark("power"),
                        new ErpProductionCostEntryDO().setId(104L).setProductionOrderId(1L).setCostType(50)
                                .setAccountingMonth("2026-04").setAmount(new BigDecimal("2.00")).setRemark("other"));
            }
            return null;
        }));
        setField(service, "erpProductionCostAllocationResultMapper", createProxy(ErpProductionCostAllocationResultMapper.class, (methodName, args) -> List.of()));
        setField(service, "erpProductionCostAllocationMapper", createProxy(ErpProductionCostAllocationMapper.class, (methodName, args) -> List.of()));

        ErpProductionCostDetailRespVO detail = service.getCostDetail(1L);

        assertEquals(new BigDecimal("120.00"), detail.getMaterialCost().setScale(2));
        assertEquals(new BigDecimal("30.00"), detail.getLaborCost().setScale(2));
        assertEquals(new BigDecimal("10.00"), detail.getDepreciationCost().setScale(2));
        assertEquals(new BigDecimal("5.00"), detail.getPowerCost().setScale(2));
        assertEquals(new BigDecimal("2.00"), detail.getOtherCost().setScale(2));
        assertEquals(new BigDecimal("167.00"), detail.getTotalCost().setScale(2));
        assertEquals(new BigDecimal("8.350000"), detail.getUnitCost());
        assertNull(detail.getCostSnapshotTime());
        assertEquals(2, detail.getMaterialDetails().size());
        assertEquals(4, detail.getCostEntries().size());
    }

    @Test
    void getCostDetail_shouldResolveAllocationSourceTypeName() throws Exception {
        ErpProductionCostServiceImpl service = new ErpProductionCostServiceImpl();
        setField(service, "productionOrderService", createProxy(ErpProductionOrderService.class, (methodName, args) -> {
            if ("getProductionOrder".equals(methodName)) {
                return new ErpProductionOrderDO()
                        .setId(1L)
                        .setOrderNo("SCGD202604280001")
                        .setFinishedQty(new BigDecimal("10"));
            }
            return null;
        }));
        setField(service, "erpProductionIssueMapper", createProxy(ErpProductionIssueMapper.class, (methodName, args) -> List.of()));
        setField(service, "erpProductionCostEntryMapper", createProxy(ErpProductionCostEntryMapper.class, (methodName, args) -> {
            if ("selectListByProductionOrderId".equals(methodName)) {
                return List.of(new ErpProductionCostEntryDO().setId(201L).setProductionOrderId(1L).setCostType(20)
                        .setSourceType(ErpProductionCostSourceTypeEnum.ALLOCATION.getType())
                        .setAccountingMonth("2026-04").setAmount(new BigDecimal("12.50")).setRemark("allocation generated"));
            }
            return null;
        }));
        setField(service, "erpProductionCostAllocationResultMapper", createProxy(ErpProductionCostAllocationResultMapper.class, (methodName, args) -> List.of()));
        setField(service, "erpProductionCostAllocationMapper", createProxy(ErpProductionCostAllocationMapper.class, (methodName, args) -> List.of()));

        ErpProductionCostDetailRespVO detail = service.getCostDetail(1L);

        assertEquals(1, detail.getCostEntries().size());
        assertEquals(ErpProductionCostSourceTypeEnum.resolveName(ErpProductionCostSourceTypeEnum.ALLOCATION.getType()),
                detail.getCostEntries().get(0).getSourceTypeName());
    }

    @Test
    void getCostDetail_shouldTraceAllocationSourceFields() throws Exception {
        ErpProductionCostServiceImpl service = new ErpProductionCostServiceImpl();
        setField(service, "productionOrderService", createProxy(ErpProductionOrderService.class, (methodName, args) -> {
            if ("getProductionOrder".equals(methodName)) {
                return new ErpProductionOrderDO()
                        .setId(1L)
                        .setOrderNo("SCGD202604280001")
                        .setFinishedQty(new BigDecimal("10"));
            }
            return null;
        }));
        setField(service, "erpProductionIssueMapper", createProxy(ErpProductionIssueMapper.class, (methodName, args) -> List.of()));
        setField(service, "erpProductionCostEntryMapper", createProxy(ErpProductionCostEntryMapper.class, (methodName, args) -> {
            if ("selectListByProductionOrderId".equals(methodName)) {
                return List.of(new ErpProductionCostEntryDO().setId(301L).setProductionOrderId(1L).setCostType(20)
                        .setSourceType(ErpProductionCostSourceTypeEnum.ALLOCATION.getType())
                        .setAccountingMonth("2026-04").setAmount(new BigDecimal("12.50")).setRemark("generated"));
            }
            return null;
        }));
        setField(service, "erpProductionCostAllocationResultMapper", createProxy(ErpProductionCostAllocationResultMapper.class, (methodName, args) -> {
            if ("selectListByGeneratedCostEntryIds".equals(methodName)) {
                return List.of(new ErpProductionCostAllocationResultDO().setId(401L).setAllocationId(501L)
                        .setProductionOrderId(1L).setGeneratedCostEntryId(301L).setBasisValue(new BigDecimal("4.00"))
                        .setBasisRatio(new BigDecimal("0.250000")).setAllocatedAmount(new BigDecimal("12.50")));
            }
            return null;
        }));
        setField(service, "erpProductionCostAllocationMapper", createProxy(ErpProductionCostAllocationMapper.class, (methodName, args) -> {
            if ("selectByIds".equals(methodName)) {
                return List.of(new ErpProductionCostAllocationDO().setId(501L).setAllocationNo("CBFT202604280001"));
            }
            return null;
        }));

        ErpProductionCostDetailRespVO detail = service.getCostDetail(1L);

        assertEquals(1, detail.getCostEntries().size());
        assertEquals(401L, detail.getCostEntries().get(0).getSourceAllocationResultId());
        assertEquals(501L, detail.getCostEntries().get(0).getSourceAllocationId());
        assertEquals("CBFT202604280001", detail.getCostEntries().get(0).getSourceAllocationNo());
        assertEquals(new BigDecimal("12.50"), detail.getCostEntries().get(0).getAmount());
        assertEquals(301L, detail.getCostEntries().get(0).getId());
    }

    @Test
    void getCostDetail_shouldExposeProjectAndProductDimensions() throws Exception {
        ErpProductionCostServiceImpl service = new ErpProductionCostServiceImpl();
        setField(service, "productionOrderService", createProxy(ErpProductionOrderService.class, (methodName, args) -> {
            if ("getProductionOrder".equals(methodName)) {
                return new ErpProductionOrderDO().setId(1L).setOrderNo("SCGD202604280001").setProductId(1001L)
                        .setProjectId(2001L).setFinishedQty(new BigDecimal("10"));
            }
            return null;
        }));
        setField(service, "productService", createProxy(ErpProductService.class, (methodName, args) -> {
            if ("getProductVOMap".equals(methodName)) {
                return Map.of(1001L, new ErpProductRespVO().setId(1001L).setName("产品A").setMaterialCode("MAT-1001"));
            }
            return null;
        }));
        setField(service, "projectService", createProxy(ErpProjectService.class, (methodName, args) -> {
            if ("getProjectMap".equals(methodName)) {
                return Map.of(2001L, new ErpProjectDO().setId(2001L).setNo("XM202604280001").setName("新产品项目"));
            }
            return null;
        }));
        setField(service, "erpProductionIssueMapper", createProxy(ErpProductionIssueMapper.class, (methodName, args) -> List.of()));
        setField(service, "erpProductionCostEntryMapper", createProxy(ErpProductionCostEntryMapper.class, (methodName, args) -> {
            if ("selectListByProductionOrderId".equals(methodName)) {
                return List.of();
            }
            return null;
        }));
        setField(service, "erpProductionCostAllocationResultMapper", createProxy(ErpProductionCostAllocationResultMapper.class, (methodName, args) -> List.of()));
        setField(service, "erpProductionCostAllocationMapper", createProxy(ErpProductionCostAllocationMapper.class, (methodName, args) -> List.of()));

        ErpProductionCostDetailRespVO detail = service.getCostDetail(1L);

        assertEquals(1001L, detail.getProductId());
        assertEquals("产品A", detail.getProductName());
        assertEquals(2001L, detail.getProjectId());
        assertEquals("XM202604280001", detail.getProjectNo());
        assertEquals("新产品项目", detail.getProjectName());
    }

    @Test
    void getProjectSummary_shouldAggregateProjectManHourAndLaborCost() throws Exception {
        ErpProductionCostServiceImpl service = new ErpProductionCostServiceImpl();
        setField(service, "erpProductionCostEntryMapper", createProxy(ErpProductionCostEntryMapper.class, (methodName, args) -> {
            if ("selectListByAccountingMonth".equals(methodName)) {
                return List.of(
                        new ErpProductionCostEntryDO().setId(1L).setProductionOrderId(101L).setCostType(20)
                                .setAccountingMonth("2026-04").setAmount(new BigDecimal("30.00")),
                        new ErpProductionCostEntryDO().setId(2L).setProductionOrderId(102L).setCostType(20)
                                .setAccountingMonth("2026-04").setAmount(new BigDecimal("20.00")),
                        new ErpProductionCostEntryDO().setId(3L).setProductionOrderId(103L).setCostType(30)
                                .setAccountingMonth("2026-04").setAmount(new BigDecimal("18.00")));
            }
            return List.of();
        }));
        setField(service, "erpProductionManHourMapper", createProxy(ErpProductionManHourMapper.class, (methodName, args) -> {
            if ("selectListByAccountingMonth".equals(methodName)) {
                return List.of(
                        new ErpProductionManHourDO().setId(11L).setProductionOrderId(101L).setAccountingMonth("2026-04")
                                .setManHour(new BigDecimal("3.50")),
                        new ErpProductionManHourDO().setId(12L).setProductionOrderId(102L).setAccountingMonth("2026-04")
                                .setManHour(new BigDecimal("2.50")),
                        new ErpProductionManHourDO().setId(13L).setProductionOrderId(103L).setAccountingMonth("2026-04")
                                .setManHour(new BigDecimal("1.00")));
            }
            return List.of();
        }));
        setField(service, "productionOrderService", createProxy(ErpProductionOrderService.class, (methodName, args) -> {
            if ("getProductionOrderList".equals(methodName)) {
                return List.of(
                        new ErpProductionOrderDO().setId(101L).setProjectId(2001L).setProductId(1001L).setOrderNo("GD-101"),
                        new ErpProductionOrderDO().setId(102L).setProjectId(2001L).setProductId(1002L).setOrderNo("GD-102"),
                        new ErpProductionOrderDO().setId(103L).setProjectId(2002L).setProductId(1003L).setOrderNo("GD-103"));
            }
            return null;
        }));
        setField(service, "projectService", createProxy(ErpProjectService.class, (methodName, args) -> {
            if ("getProjectMap".equals(methodName)) {
                return Map.of(
                        2001L, new ErpProjectDO().setId(2001L).setNo("XM-01").setName("项目一"),
                        2002L, new ErpProjectDO().setId(2002L).setNo("XM-02").setName("项目二"));
            }
            return Map.of();
        }));
        setField(service, "productService", createProxy(ErpProductService.class, (methodName, args) -> {
            if ("getProductVOMap".equals(methodName)) {
                return Map.of(
                        1001L, new ErpProductRespVO().setId(1001L).setName("产品A"),
                        1002L, new ErpProductRespVO().setId(1002L).setName("产品B"),
                        1003L, new ErpProductRespVO().setId(1003L).setName("产品C"));
            }
            return Map.of();
        }));

        List<ErpProductionCostProjectSummaryRespVO> summaryList = service.getProjectSummary("2026-04");

        assertEquals(2, summaryList.size());
        assertEquals(2001L, summaryList.get(0).getProjectId());
        assertEquals(new BigDecimal("6.000000"), summaryList.get(0).getTotalManHour());
        assertEquals(new BigDecimal("50.00"), summaryList.get(0).getLaborCost().setScale(2));
        assertEquals(2, summaryList.get(0).getProductionOrderCount());
        assertEquals(2002L, summaryList.get(1).getProjectId());
        assertEquals(new BigDecimal("1.000000"), summaryList.get(1).getTotalManHour());
        assertEquals(new BigDecimal("0.00"), summaryList.get(1).getLaborCost().setScale(2));
    }

    @Test
    void getProductionCostSummaryPage_shouldSeparateDifferentAccountingMonths() throws Exception {
        ErpProductionCostServiceImpl service = new ErpProductionCostServiceImpl();
        setField(service, "erpProductionCostEntryMapper", createProxy(ErpProductionCostEntryMapper.class, (methodName, args) -> {
            if ("selectListByPageReqVO".equals(methodName)) {
                return List.of(
                        new ErpProductionCostEntryDO().setId(1L).setProductionOrderId(101L).setCostType(20)
                                .setAccountingMonth("2026-04").setAmount(new BigDecimal("30.00")).setRemark("m04"),
                        new ErpProductionCostEntryDO().setId(2L).setProductionOrderId(101L).setCostType(20)
                                .setAccountingMonth("2026-05").setAmount(new BigDecimal("20.00")).setRemark("m05"));
            }
            return List.of();
        }));
        setField(service, "productionOrderService", createProxy(ErpProductionOrderService.class, (methodName, args) -> {
            if ("getProductionOrderList".equals(methodName)) {
                return List.of(new ErpProductionOrderDO().setId(101L).setProjectId(2001L).setProductId(1001L)
                        .setOrderNo("GD-101"));
            }
            return List.of();
        }));
        setField(service, "projectService", createProxy(ErpProjectService.class, (methodName, args) -> {
            if ("getProjectMap".equals(methodName)) {
                return Map.of(2001L, new ErpProjectDO().setId(2001L).setNo("XM-01").setName("项目一"));
            }
            return Map.of();
        }));
        setField(service, "productService", createProxy(ErpProductService.class, (methodName, args) -> {
            if ("getProductVOMap".equals(methodName)) {
                return Map.of(1001L, new ErpProductRespVO().setId(1001L).setName("产品A"));
            }
            return Map.of();
        }));
        setField(service, "adminUserApi", createProxy(AdminUserApi.class, (methodName, args) -> Map.of()));

        ErpProductionCostEntryPageReqVO reqVO = new ErpProductionCostEntryPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);

        var pageResult = service.getProductionCostSummaryPage(reqVO);

        assertNotNull(pageResult);
        assertEquals(2, pageResult.getTotal().intValue());
        assertEquals(2, pageResult.getList().size());
        Map<String, ErpProductionCostSummaryRespVO> summaryMap = pageResult.getList().stream()
                .collect(java.util.stream.Collectors.toMap(
                        ErpProductionCostSummaryRespVO::getAccountingMonth,
                        item -> item,
                        (left, right) -> left));
        assertEquals(new BigDecimal("30.00"), summaryMap.get("2026-04").getTotalAmount());
        assertEquals(new BigDecimal("20.00"), summaryMap.get("2026-05").getTotalAmount());
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
