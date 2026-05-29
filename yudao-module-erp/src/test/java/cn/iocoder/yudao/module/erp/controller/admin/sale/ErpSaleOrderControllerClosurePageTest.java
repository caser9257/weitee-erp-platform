package cn.iocoder.yudao.module.erp.controller.admin.sale;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderClosurePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderClosurePageRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.service.project.ErpProjectService;
import cn.iocoder.yudao.module.erp.service.sale.ErpCustomerService;
import cn.iocoder.yudao.module.erp.service.sale.ErpSaleOrderClosureService;
import cn.iocoder.yudao.module.erp.service.sale.ErpSaleOrderService;
import cn.iocoder.yudao.module.erp.service.sale.bo.ErpSaleOrderClosureSummaryBO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ErpSaleOrderControllerClosurePageTest {

    @Test
    void getSaleOrderClosureSummaryPage_shouldReturnPageWithSummary() throws Exception {
        ErpSaleOrderController controller = new ErpSaleOrderController();
        setField(controller, "saleOrderService", createSaleOrderServiceProxy());
        setField(controller, "saleOrderClosureService", createClosureServiceProxy());
        setField(controller, "customerService", createCustomerServiceProxy());
        setField(controller, "projectService", createProjectServiceProxy());
        setField(controller, "adminUserApi", createAdminUserApiProxy());

        CommonResult<PageResult<ErpSaleOrderClosurePageRespVO>> result =
                controller.getSaleOrderClosureSummaryPage(new ErpSaleOrderClosurePageReqVO());

        assertNotNull(result.getData());
        assertEquals(1, result.getData().getList().size());
        ErpSaleOrderClosurePageRespVO row = result.getData().getList().get(0);
        assertEquals("SO-001", row.getNo());
        assertEquals("测试客户", row.getCustomerName());
        assertEquals("测试项目", row.getProjectName());
        assertEquals("销售员A", row.getSaleUserName());
        assertEquals("WAIT_PURCHASE_IQC", row.getClosureSummary().getClosureStage());
    }

    private ErpSaleOrderService createSaleOrderServiceProxy() {
        return (ErpSaleOrderService) Proxy.newProxyInstance(
                ErpSaleOrderService.class.getClassLoader(),
                new Class<?>[]{ErpSaleOrderService.class},
                (proxy, method, args) -> {
                    if ("getSaleOrderPage".equals(method.getName())) {
                        ErpSaleOrderDO saleOrder = new ErpSaleOrderDO()
                                .setId(1L)
                                .setNo("SO-001")
                                .setCustomerId(11L)
                                .setProjectId(21L)
                                .setSaleUserId(31L)
                                .setOrderTime(LocalDateTime.of(2026, 4, 21, 10, 0))
                                .setDeliveryDate(LocalDate.of(2026, 4, 30));
                        return new PageResult<>(List.of(saleOrder), 1L);
                    }
                    return null;
                });
    }

    private ErpSaleOrderClosureService createClosureServiceProxy() {
        return (ErpSaleOrderClosureService) Proxy.newProxyInstance(
                ErpSaleOrderClosureService.class.getClassLoader(),
                new Class<?>[]{ErpSaleOrderClosureService.class},
                (proxy, method, args) -> {
                    if ("getClosureSummaryMap".equals(method.getName())) {
                        ErpSaleOrderClosureSummaryBO summary = new ErpSaleOrderClosureSummaryBO();
                        summary.setSaleOrderId(1L);
                        summary.setSaleOrderNo("SO-001");
                        summary.setRemainingShipQty(new BigDecimal("6"));
                        summary.setClosureStage("WAIT_PURCHASE_IQC");
                        return Map.of(1L, summary);
                    }
                    return null;
                });
    }

    private ErpCustomerService createCustomerServiceProxy() {
        return (ErpCustomerService) Proxy.newProxyInstance(
                ErpCustomerService.class.getClassLoader(),
                new Class<?>[]{ErpCustomerService.class},
                (proxy, method, args) -> {
                    if ("getCustomerMap".equals(method.getName())) {
                        return Map.of(11L, new ErpCustomerDO().setId(11L).setName("测试客户"));
                    }
                    return null;
                });
    }

    private ErpProjectService createProjectServiceProxy() {
        return (ErpProjectService) Proxy.newProxyInstance(
                ErpProjectService.class.getClassLoader(),
                new Class<?>[]{ErpProjectService.class},
                (proxy, method, args) -> {
                    if ("getProjectMap".equals(method.getName())) {
                        return Map.of(21L, new ErpProjectDO().setId(21L).setName("测试项目"));
                    }
                    return null;
                });
    }

    private AdminUserApi createAdminUserApiProxy() {
        return (AdminUserApi) Proxy.newProxyInstance(
                AdminUserApi.class.getClassLoader(),
                new Class<?>[]{AdminUserApi.class},
                (proxy, method, args) -> {
                    if ("getUserMap".equals(method.getName())) {
                        return Map.of(31L, new AdminUserRespDTO().setId(31L).setNickname("销售员A"));
                    }
                    return null;
                });
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
