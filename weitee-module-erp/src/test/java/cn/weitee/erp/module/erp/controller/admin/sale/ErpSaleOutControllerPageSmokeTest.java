package cn.weitee.erp.module.erp.controller.admin.sale;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.out.ErpSaleOutPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.out.ErpSaleOutRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOutItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import cn.weitee.erp.module.erp.service.sale.ErpCustomerService;
import cn.weitee.erp.module.erp.service.sale.ErpSaleOutService;
import cn.weitee.erp.module.erp.service.stock.ErpStockService;
import cn.weitee.erp.module.system.api.user.AdminUserApi;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class ErpSaleOutControllerPageSmokeTest {

    @Test
    void getSaleOutPage_shouldKeepPageOrderAndIgnoreBlankCreatorLookup() throws Exception {
        ErpSaleOutController controller = new ErpSaleOutController();
        AtomicBoolean userLookupCalled = new AtomicBoolean(false);

        setField(controller, "saleOutService", createSaleOutServiceProxy());
        setField(controller, "stockService", createStockServiceProxy());
        setField(controller, "productService", createProductServiceProxy());
        setField(controller, "customerService", createCustomerServiceProxy());
        setField(controller, "adminUserApi", createAdminUserApiProxy(userLookupCalled));

        CommonResult<PageResult<ErpSaleOutRespVO>> result = controller.getSaleOutPage(new ErpSaleOutPageReqVO());

        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getData().getTotal()).isEqualTo(2L);
        assertThat(result.getData().getList()).extracting(ErpSaleOutRespVO::getNo, ErpSaleOutRespVO::getProductNames,
                ErpSaleOutRespVO::getCustomerName, ErpSaleOutRespVO::getCreatorName)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("XS202605060001", "产品A", "客户A", null),
                        org.assertj.core.groups.Tuple.tuple("XS202605060002", "产品B", "客户B", null));
        assertThat(userLookupCalled).isFalse();
    }

    private ErpSaleOutService createSaleOutServiceProxy() {
        return (ErpSaleOutService) Proxy.newProxyInstance(
                ErpSaleOutService.class.getClassLoader(),
                new Class<?>[]{ErpSaleOutService.class},
                (proxy, method, args) -> {
                    if ("getSaleOutPage".equals(method.getName())) {
                        return new PageResult<>(List.of(
                                new ErpSaleOutDO().setId(1L).setNo("XS202605060001").setCustomerId(11L)
                                        .setCreator("").setCreateTime(LocalDateTime.of(2026, 5, 6, 9, 0)),
                                new ErpSaleOutDO().setId(2L).setNo("XS202605060002").setCustomerId(12L)
                                        .setCreator("").setCreateTime(LocalDateTime.of(2026, 5, 6, 10, 0))), 2L);
                    }
                    if ("getSaleOutItemListByOutIds".equals(method.getName())) {
                        return List.of(
                                new ErpSaleOutItemDO().setId(101L).setOutId(1L).setProductId(101L)
                                        .setWarehouseId(201L).setCount(new BigDecimal("2")).setProductPrice(new BigDecimal("50.00")),
                                new ErpSaleOutItemDO().setId(102L).setOutId(2L).setProductId(102L)
                                        .setWarehouseId(202L).setCount(new BigDecimal("3")).setProductPrice(new BigDecimal("60.00")));
                    }
                    return null;
                });
    }

    private ErpStockService createStockServiceProxy() {
        return (ErpStockService) Proxy.newProxyInstance(
                ErpStockService.class.getClassLoader(),
                new Class<?>[]{ErpStockService.class},
                (proxy, method, args) -> "getStock".equals(method.getName())
                        ? new ErpStockDO().setCount(new BigDecimal("99.000"))
                        : null);
    }

    private ErpProductService createProductServiceProxy() {
        return (ErpProductService) Proxy.newProxyInstance(
                ErpProductService.class.getClassLoader(),
                new Class<?>[]{ErpProductService.class},
                (proxy, method, args) -> {
                    if ("getProductVOMap".equals(method.getName())) {
                        return Map.of(
                                101L, new ErpProductRespVO().setName("产品A").setBarCode("A-001").setUnitName("件"),
                                102L, new ErpProductRespVO().setName("产品B").setBarCode("B-001").setUnitName("箱"));
                    }
                    return null;
                });
    }

    private ErpCustomerService createCustomerServiceProxy() {
        return (ErpCustomerService) Proxy.newProxyInstance(
                ErpCustomerService.class.getClassLoader(),
                new Class<?>[]{ErpCustomerService.class},
                (proxy, method, args) -> "getCustomerMap".equals(method.getName())
                        ? Map.of(11L, new ErpCustomerDO().setId(11L).setName("客户A"),
                        12L, new ErpCustomerDO().setId(12L).setName("客户B"))
                        : null);
    }

    private AdminUserApi createAdminUserApiProxy(AtomicBoolean userLookupCalled) {
        return (AdminUserApi) Proxy.newProxyInstance(
                AdminUserApi.class.getClassLoader(),
                new Class<?>[]{AdminUserApi.class},
                (proxy, method, args) -> {
                    if ("getUserMap".equals(method.getName())) {
                        userLookupCalled.set(true);
                        throw new AssertionError("blank creator should not trigger admin user lookup");
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
