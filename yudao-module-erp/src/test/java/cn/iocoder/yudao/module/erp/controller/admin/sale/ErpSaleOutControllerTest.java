package cn.iocoder.yudao.module.erp.controller.admin.sale;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.out.ErpSaleOutPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.out.ErpSaleOutRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOutItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.sale.ErpCustomerService;
import cn.iocoder.yudao.module.erp.service.sale.ErpSaleOutService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

class ErpSaleOutControllerTest {

    @Test
    void getSaleOutPage_shouldSkipBlankCreatorWithoutThrowing() throws Exception {
        ErpSaleOutController controller = new ErpSaleOutController();
        AtomicBoolean userLookupCalled = new AtomicBoolean(false);

        setField(controller, "saleOutService", createSaleOutServiceProxy());
        setField(controller, "stockService", createStockServiceProxy());
        setField(controller, "productService", createProductServiceProxy());
        setField(controller, "customerService", createCustomerServiceProxy());
        setField(controller, "adminUserApi", createAdminUserApiProxy(userLookupCalled));

        CommonResult<PageResult<ErpSaleOutRespVO>> result = controller.getSaleOutPage(new ErpSaleOutPageReqVO());

        Assertions.assertEquals(0, result.getCode());
        Assertions.assertEquals(1L, result.getData().getTotal());
        Assertions.assertEquals(1, result.getData().getList().size());
        Assertions.assertNull(result.getData().getList().get(0).getCreatorName());
        Assertions.assertFalse(userLookupCalled.get());
    }

    private ErpSaleOutService createSaleOutServiceProxy() {
        return (ErpSaleOutService) Proxy.newProxyInstance(
                ErpSaleOutService.class.getClassLoader(),
                new Class<?>[]{ErpSaleOutService.class},
                (proxy, method, args) -> {
                    if ("getSaleOutPage".equals(method.getName())) {
                        return new PageResult<>(List.of(new ErpSaleOutDO()
                                .setId(1L)
                                .setNo("XSCK20260430000001")
                                .setCustomerId(2L)
                                .setCreator("")
                                .setCreateTime(LocalDateTime.of(2026, 4, 30, 10, 0))), 1L);
                    }
                    if ("getSaleOutItemListByOutIds".equals(method.getName())) {
                        return List.of(new ErpSaleOutItemDO()
                                .setId(11L)
                                .setOutId(1L)
                                .setProductId(3L)
                                .setWarehouseId(4L)
                                .setCount(new BigDecimal("2"))
                                .setProductPrice(new BigDecimal("50.00"))
                                .setTotalPrice(new BigDecimal("100.00")));
                    }
                    return null;
                });
    }

    private ErpStockService createStockServiceProxy() {
        return (ErpStockService) Proxy.newProxyInstance(
                ErpStockService.class.getClassLoader(),
                new Class<?>[]{ErpStockService.class},
                (proxy, method, args) -> {
                    if ("getStock".equals(method.getName())) {
                        return new ErpStockDO();
                    }
                    return null;
                });
    }

    private ErpProductService createProductServiceProxy() {
        return (ErpProductService) Proxy.newProxyInstance(
                ErpProductService.class.getClassLoader(),
                new Class<?>[]{ErpProductService.class},
                (proxy, method, args) -> {
                    if ("getProductVOMap".equals(method.getName())) {
                        return Map.of();
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
                        return Map.of();
                    }
                    return null;
                });
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
