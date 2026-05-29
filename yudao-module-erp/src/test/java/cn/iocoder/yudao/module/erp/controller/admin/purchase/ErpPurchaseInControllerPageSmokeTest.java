package cn.iocoder.yudao.module.erp.controller.admin.purchase;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.in.ErpPurchaseInRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseInItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseSourceBatchDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpStockDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseInService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpPurchaseSourceBatchService;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class ErpPurchaseInControllerPageSmokeTest {

    @Test
    void getPurchaseInPage_shouldKeepPageOrderAndIgnoreBlankCreatorLookup() throws Exception {
        ErpPurchaseInController controller = new ErpPurchaseInController();
        AtomicBoolean userLookupCalled = new AtomicBoolean(false);

        setField(controller, "purchaseInService", createPurchaseInServiceProxy());
        setField(controller, "stockService", createStockServiceProxy());
        setField(controller, "productService", createProductServiceProxy());
        setField(controller, "supplierService", createSupplierServiceProxy());
        setField(controller, "purchaseSourceBatchService", createPurchaseSourceBatchServiceProxy());
        setField(controller, "adminUserApi", createAdminUserApiProxy(userLookupCalled));
        setField(controller, "bpmTaskService", createBpmTaskServiceProxy());

        CommonResult<PageResult<ErpPurchaseInRespVO>> result = controller.getPurchaseInPage(new ErpPurchaseInPageReqVO());

        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getData().getTotal()).isEqualTo(2L);
        assertThat(result.getData().getList()).extracting(ErpPurchaseInRespVO::getNo, ErpPurchaseInRespVO::getProductNames,
                ErpPurchaseInRespVO::getSupplierName, ErpPurchaseInRespVO::getCreatorName)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("CG202605060001", "产品A", "供应商A", "采购员A"),
                        org.assertj.core.groups.Tuple.tuple("CG202605060002", "产品B", "供应商B", "采购员B"));
        assertThat(userLookupCalled).isTrue();
    }

    private ErpPurchaseInService createPurchaseInServiceProxy() {
        return (ErpPurchaseInService) Proxy.newProxyInstance(
                ErpPurchaseInService.class.getClassLoader(),
                new Class<?>[]{ErpPurchaseInService.class},
                (proxy, method, args) -> {
                    if ("getPurchaseInPage".equals(method.getName())) {
                        return new PageResult<>(List.of(
                                new ErpPurchaseInDO().setId(1L).setNo("CG202605060001").setSupplierId(11L)
                                        .setCreator("41").setCreateTime(LocalDateTime.of(2026, 5, 6, 9, 0)),
                                new ErpPurchaseInDO().setId(2L).setNo("CG202605060002").setSupplierId(12L)
                                        .setCreator("42").setCreateTime(LocalDateTime.of(2026, 5, 6, 10, 0))), 2L);
                    }
                    if ("getPurchaseInItemListByInIds".equals(method.getName())) {
                        return List.of(
                                new ErpPurchaseInItemDO().setId(101L).setInId(1L).setProductId(101L).setWarehouseId(201L)
                                        .setPurchaseSourceBatchId(301L).setCount(new BigDecimal("2")),
                                new ErpPurchaseInItemDO().setId(102L).setInId(2L).setProductId(102L).setWarehouseId(202L)
                                        .setPurchaseSourceBatchId(302L).setCount(new BigDecimal("3")));
                    }
                    return null;
                });
    }

    private ErpStockService createStockServiceProxy() {
        return (ErpStockService) Proxy.newProxyInstance(
                ErpStockService.class.getClassLoader(),
                new Class<?>[]{ErpStockService.class},
                (proxy, method, args) -> "getStock".equals(method.getName())
                        ? new ErpStockDO().setCount(new BigDecimal("88.000"))
                        : null);
    }

    private ErpProductService createProductServiceProxy() {
        return (ErpProductService) Proxy.newProxyInstance(
                ErpProductService.class.getClassLoader(),
                new Class<?>[]{ErpProductService.class},
                (proxy, method, args) -> "getProductVOMap".equals(method.getName())
                        ? Map.of(
                        101L, new ErpProductRespVO().setName("产品A").setBarCode("A-001").setUnitName("件"),
                        102L, new ErpProductRespVO().setName("产品B").setBarCode("B-001").setUnitName("箱"))
                        : null);
    }

    private ErpSupplierService createSupplierServiceProxy() {
        return (ErpSupplierService) Proxy.newProxyInstance(
                ErpSupplierService.class.getClassLoader(),
                new Class<?>[]{ErpSupplierService.class},
                (proxy, method, args) -> "getSupplierMap".equals(method.getName())
                        ? Map.of(11L, new ErpSupplierDO().setId(11L).setName("供应商A"),
                        12L, new ErpSupplierDO().setId(12L).setName("供应商B"))
                        : null);
    }

    private ErpPurchaseSourceBatchService createPurchaseSourceBatchServiceProxy() {
        return (ErpPurchaseSourceBatchService) Proxy.newProxyInstance(
                ErpPurchaseSourceBatchService.class.getClassLoader(),
                new Class<?>[]{ErpPurchaseSourceBatchService.class},
                (proxy, method, args) -> "getPurchaseSourceBatchMap".equals(method.getName())
                        ? Map.of(301L, new ErpPurchaseSourceBatchDO().setId(301L).setBatchNo("CGLY-001"),
                        302L, new ErpPurchaseSourceBatchDO().setId(302L).setBatchNo("CGLY-002"))
                        : null);
    }

    private AdminUserApi createAdminUserApiProxy(AtomicBoolean userLookupCalled) {
        return (AdminUserApi) Proxy.newProxyInstance(
                AdminUserApi.class.getClassLoader(),
                new Class<?>[]{AdminUserApi.class},
                (proxy, method, args) -> {
                    if ("getUserMap".equals(method.getName())) {
                        userLookupCalled.set(true);
                        return Map.of(
                                41L, new AdminUserRespDTO().setNickname("采购员A"),
                                42L, new AdminUserRespDTO().setNickname("采购员B"));
                    }
                    return null;
                });
    }

    private Object createBpmTaskServiceProxy() {
        return Proxy.newProxyInstance(
                ErpPurchaseInController.class.getClassLoader(),
                new Class<?>[]{cn.iocoder.yudao.module.bpm.service.task.BpmTaskService.class},
                (proxy, method, args) -> List.of());
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
