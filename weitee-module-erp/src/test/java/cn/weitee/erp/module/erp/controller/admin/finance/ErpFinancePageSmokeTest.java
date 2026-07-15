package cn.weitee.erp.module.erp.controller.admin.finance;

import cn.weitee.erp.framework.common.pojo.CommonResult;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.payment.ErpFinancePaymentPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.payment.ErpFinancePaymentRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apestimate.ErpApEstimatePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.apestimate.ErpApEstimateRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.receipt.ErpFinanceReceiptPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.receipt.ErpFinanceReceiptRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpAccountDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePaymentDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinancePaymentItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceReceiptDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceReceiptItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApEstimateDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApEstimateItemDO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpApStatementDO;
import cn.weitee.erp.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpSupplierDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpCustomerDO;
import cn.weitee.erp.module.erp.service.finance.ErpAccountService;
import cn.weitee.erp.module.erp.service.finance.ErpFinancePaymentService;
import cn.weitee.erp.module.erp.service.finance.ErpFinanceReceiptService;
import cn.weitee.erp.module.erp.service.purchase.ErpSupplierService;
import cn.weitee.erp.module.erp.service.sale.ErpCustomerService;
import cn.weitee.erp.module.system.api.user.AdminUserApi;
import cn.weitee.erp.module.system.api.user.dto.AdminUserRespDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class ErpFinancePageSmokeTest {

    @Test
    void accountController_shouldKeepLegacyFinanceAccountPath() {
        RequestMapping mapping = ErpAccountController.class.getAnnotation(RequestMapping.class);

        assertThat(mapping.value()).contains("/erp/account", "/erp/finance-account");
    }

    @Test
    void getFinancePaymentPage_shouldKeepPageOrderAndIgnoreBlankCreatorLookup() throws Exception {
        ErpFinancePaymentController controller = new ErpFinancePaymentController();
        AtomicBoolean userLookupCalled = new AtomicBoolean(false);

        setField(controller, "financePaymentService", createFinancePaymentServiceProxy());
        setField(controller, "supplierService", createSupplierServiceProxy());
        setField(controller, "accountService", createAccountServiceProxy());
        setField(controller, "apStatementService", createApEstimateStatementServiceProxy());
        setField(controller, "adminUserApi", createAdminUserApiProxy(userLookupCalled));

        CommonResult<PageResult<ErpFinancePaymentRespVO>> result =
                controller.getFinancePaymentPage(new ErpFinancePaymentPageReqVO());

        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getData().getTotal()).isEqualTo(2L);
        assertThat(result.getData().getList()).extracting(ErpFinancePaymentRespVO::getNo,
                ErpFinancePaymentRespVO::getSupplierName, ErpFinancePaymentRespVO::getAccountName,
                ErpFinancePaymentRespVO::getCreatorName, ErpFinancePaymentRespVO::getFinanceUserName)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("FK202605060001", "供应商A", "结算账簿A", "制单A", "财务A"),
                        org.assertj.core.groups.Tuple.tuple("FK202605060002", "供应商B", "结算账簿B", "制单B", "财务B"));
        assertThat(userLookupCalled).isTrue();
    }

    @Test
    void getFinancePaymentPage_shouldHandleBlankCreatorWithout500() throws Exception {
        ErpFinancePaymentController controller = new ErpFinancePaymentController();
        AtomicBoolean userLookupCalled = new AtomicBoolean(false);

        setField(controller, "financePaymentService", createFinancePaymentServiceWithBlankCreatorProxy());
        setField(controller, "supplierService", createSupplierServiceProxy());
        setField(controller, "accountService", createAccountServiceProxy());
        setField(controller, "apStatementService", createApStatementServiceProxy());
        setField(controller, "adminUserApi", createAdminUserApiProxy(userLookupCalled));

        CommonResult<PageResult<ErpFinancePaymentRespVO>> result =
                controller.getFinancePaymentPage(new ErpFinancePaymentPageReqVO());

        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getData().getTotal()).isEqualTo(1L);
        assertThat(result.getData().getList()).extracting(ErpFinancePaymentRespVO::getNo,
                ErpFinancePaymentRespVO::getCreatorName, ErpFinancePaymentRespVO::getFinanceUserName)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("FK202605060003", null, "财务C"));
        assertThat(userLookupCalled).isTrue();
    }

    @Test
    void getFinanceReceiptPage_shouldKeepPageOrderAndIgnoreBlankCreatorLookup() throws Exception {
        ErpFinanceReceiptController controller = new ErpFinanceReceiptController();
        AtomicBoolean userLookupCalled = new AtomicBoolean(false);

        setField(controller, "financeReceiptService", createFinanceReceiptServiceProxy());
        setField(controller, "customerService", createCustomerServiceProxy());
        setField(controller, "accountService", createAccountServiceProxy());
        setField(controller, "adminUserApi", createAdminUserApiProxy(userLookupCalled));

        CommonResult<PageResult<ErpFinanceReceiptRespVO>> result =
                controller.getFinanceReceiptPage(new ErpFinanceReceiptPageReqVO());

        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getData().getTotal()).isEqualTo(2L);
        assertThat(result.getData().getList()).extracting(ErpFinanceReceiptRespVO::getNo,
                ErpFinanceReceiptRespVO::getCustomerName, ErpFinanceReceiptRespVO::getAccountName,
                ErpFinanceReceiptRespVO::getCreatorName, ErpFinanceReceiptRespVO::getFinanceUserName)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("SK202605060001", "客户A", "结算账簿A", "制单A", "财务A"),
                        org.assertj.core.groups.Tuple.tuple("SK202605060002", "客户B", "结算账簿B", "制单B", "财务B"));
        assertThat(userLookupCalled).isTrue();
    }

    @Test
    void getFinanceReceiptPage_shouldHandleBlankCreatorWithout500() throws Exception {
        ErpFinanceReceiptController controller = new ErpFinanceReceiptController();
        AtomicBoolean userLookupCalled = new AtomicBoolean(false);

        setField(controller, "financeReceiptService", createFinanceReceiptServiceWithBlankCreatorProxy());
        setField(controller, "customerService", createCustomerServiceProxy());
        setField(controller, "accountService", createAccountServiceProxy());
        setField(controller, "adminUserApi", createAdminUserApiProxy(userLookupCalled));

        CommonResult<PageResult<ErpFinanceReceiptRespVO>> result =
                controller.getFinanceReceiptPage(new ErpFinanceReceiptPageReqVO());

        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getData().getTotal()).isEqualTo(1L);
        assertThat(result.getData().getList()).extracting(ErpFinanceReceiptRespVO::getNo,
                ErpFinanceReceiptRespVO::getCreatorName, ErpFinanceReceiptRespVO::getFinanceUserName)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("SK202605060003", null, "财务C"));
        assertThat(userLookupCalled).isTrue();
    }

    @Test
    void getApEstimatePage_shouldHandleDirtyCreatorWithout500() throws Exception {
        ErpApEstimateController controller = new ErpApEstimateController();
        AtomicBoolean userLookupCalled = new AtomicBoolean(false);

        setField(controller, "apEstimateService", createApEstimateServiceWithDirtyCreatorProxy());
        setField(controller, "apStatementService", createApStatementServiceProxy());
        setField(controller, "supplierService", createSupplierServiceProxy());
        setField(controller, "accountService", createAccountServiceProxy());
        setField(controller, "productService", createProductServiceProxy());
        setField(controller, "warehouseService", createWarehouseServiceProxy());
        setField(controller, "projectService", createProjectServiceProxy());
        setField(controller, "adminUserApi", createAdminUserApiProxy(userLookupCalled));

        CommonResult<PageResult<ErpApEstimateRespVO>> result =
                controller.getApEstimatePage(new ErpApEstimatePageReqVO());

        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getData().getTotal()).isEqualTo(1L);
        assertThat(result.getData().getList()).extracting(ErpApEstimateRespVO::getEstimateNo,
                ErpApEstimateRespVO::getCreatorName, ErpApEstimateRespVO::getSupplierName,
                ErpApEstimateRespVO::getAccountName)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("YS202605060001", null, "供应商A", "结算账簿A"));
        assertThat(userLookupCalled).isFalse();
    }

    private ErpFinancePaymentService createFinancePaymentServiceProxy() {
        return (ErpFinancePaymentService) Proxy.newProxyInstance(
                ErpFinancePaymentService.class.getClassLoader(),
                new Class<?>[]{ErpFinancePaymentService.class},
                (proxy, method, args) -> {
                    if ("getFinancePaymentPage".equals(method.getName())) {
                        return new PageResult<>(List.of(
                                new ErpFinancePaymentDO().setId(1L).setNo("FK202605060001").setSupplierId(11L)
                                        .setAccountId(21L).setFinanceUserId(31L).setCreator("51").setCreateTime(LocalDateTime.of(2026, 5, 6, 9, 0)),
                                new ErpFinancePaymentDO().setId(2L).setNo("FK202605060002").setSupplierId(12L)
                                        .setAccountId(22L).setFinanceUserId(32L).setCreator("52").setCreateTime(LocalDateTime.of(2026, 5, 6, 10, 0))), 2L);
                    }
                    if ("getFinancePaymentItemListByPaymentIds".equals(method.getName())) {
                        return List.of(
                                new ErpFinancePaymentItemDO().setId(101L).setPaymentId(1L).setBizType(100).setBizId(1001L).setBizNo("CG-001")
                                        .setPaymentPrice(new BigDecimal("100.00")),
                                new ErpFinancePaymentItemDO().setId(102L).setPaymentId(2L).setBizType(100).setBizId(1002L).setBizNo("CG-002")
                                        .setPaymentPrice(new BigDecimal("200.00")));
                    }
                    return null;
                });
    }

    private ErpFinanceReceiptService createFinanceReceiptServiceProxy() {
        return (ErpFinanceReceiptService) Proxy.newProxyInstance(
                ErpFinanceReceiptService.class.getClassLoader(),
                new Class<?>[]{ErpFinanceReceiptService.class},
                (proxy, method, args) -> {
                    if ("getFinanceReceiptPage".equals(method.getName())) {
                        return new PageResult<>(List.of(
                                new ErpFinanceReceiptDO().setId(1L).setNo("SK202605060001").setCustomerId(11L)
                                        .setAccountId(21L).setFinanceUserId(31L).setCreator("51").setCreateTime(LocalDateTime.of(2026, 5, 6, 9, 0)),
                                new ErpFinanceReceiptDO().setId(2L).setNo("SK202605060002").setCustomerId(12L)
                                        .setAccountId(22L).setFinanceUserId(32L).setCreator("52").setCreateTime(LocalDateTime.of(2026, 5, 6, 10, 0))), 2L);
                    }
                    if ("getFinanceReceiptItemListByReceiptIds".equals(method.getName())) {
                        return List.of(
                                new ErpFinanceReceiptItemDO().setId(101L).setReceiptId(1L).setBizType(200).setBizId(2001L).setBizNo("XS-001")
                                        .setReceiptPrice(new BigDecimal("100.00")),
                                new ErpFinanceReceiptItemDO().setId(102L).setReceiptId(2L).setBizType(200).setBizId(2002L).setBizNo("XS-002")
                                        .setReceiptPrice(new BigDecimal("200.00")));
                    }
                    return null;
                });
    }

    private ErpFinancePaymentService createFinancePaymentServiceWithBlankCreatorProxy() {
        return (ErpFinancePaymentService) Proxy.newProxyInstance(
                ErpFinancePaymentService.class.getClassLoader(),
                new Class<?>[]{ErpFinancePaymentService.class},
                (proxy, method, args) -> {
                    if ("getFinancePaymentPage".equals(method.getName())) {
                        return new PageResult<>(List.of(
                                new ErpFinancePaymentDO().setId(3L).setNo("FK202605060003").setSupplierId(13L)
                                        .setAccountId(23L).setFinanceUserId(33L).setCreator(null)
                                        .setCreateTime(LocalDateTime.of(2026, 5, 6, 11, 0))), 1L);
                    }
                    if ("getFinancePaymentItemListByPaymentIds".equals(method.getName())) {
                        return List.of(
                                new ErpFinancePaymentItemDO().setId(103L).setPaymentId(3L).setBizType(100)
                                        .setBizId(1003L).setBizNo("CG-003").setPaymentPrice(new BigDecimal("300.00")));
                    }
                    return null;
                });
    }

    private ErpFinanceReceiptService createFinanceReceiptServiceWithBlankCreatorProxy() {
        return (ErpFinanceReceiptService) Proxy.newProxyInstance(
                ErpFinanceReceiptService.class.getClassLoader(),
                new Class<?>[]{ErpFinanceReceiptService.class},
                (proxy, method, args) -> {
                    if ("getFinanceReceiptPage".equals(method.getName())) {
                        return new PageResult<>(List.of(
                                new ErpFinanceReceiptDO().setId(3L).setNo("SK202605060003").setCustomerId(13L)
                                        .setAccountId(23L).setFinanceUserId(33L).setCreator(null)
                                        .setCreateTime(LocalDateTime.of(2026, 5, 6, 11, 0))), 1L);
                    }
                    if ("getFinanceReceiptItemListByReceiptIds".equals(method.getName())) {
                        return List.of(
                                new ErpFinanceReceiptItemDO().setId(103L).setReceiptId(3L).setBizType(200)
                                        .setBizId(2003L).setBizNo("XS-003").setReceiptPrice(new BigDecimal("300.00")));
                    }
                    return null;
                });
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

    private ErpCustomerService createCustomerServiceProxy() {
        return (ErpCustomerService) Proxy.newProxyInstance(
                ErpCustomerService.class.getClassLoader(),
                new Class<?>[]{ErpCustomerService.class},
                (proxy, method, args) -> "getCustomerMap".equals(method.getName())
                        ? Map.of(11L, new ErpCustomerDO().setId(11L).setName("客户A"),
                        12L, new ErpCustomerDO().setId(12L).setName("客户B"))
                        : null);
    }

    private ErpAccountService createAccountServiceProxy() {
        return (ErpAccountService) Proxy.newProxyInstance(
                ErpAccountService.class.getClassLoader(),
                new Class<?>[]{ErpAccountService.class},
                (proxy, method, args) -> "getAccountMap".equals(method.getName())
                        ? Map.of(21L, new ErpAccountDO().setId(21L).setName("结算账簿A"),
                        22L, new ErpAccountDO().setId(22L).setName("结算账簿B"))
                        : null);
    }

    private Object createApStatementServiceProxy() {
        return Proxy.newProxyInstance(
                ErpFinancePaymentController.class.getClassLoader(),
                new Class<?>[]{cn.weitee.erp.module.erp.service.finance.ErpApStatementService.class},
                (proxy, method, args) -> List.of());
    }

    private Object createApEstimateServiceWithDirtyCreatorProxy() {
        return Proxy.newProxyInstance(
                ErpApEstimateController.class.getClassLoader(),
                new Class<?>[]{cn.weitee.erp.module.erp.service.finance.ErpApEstimateService.class},
                (proxy, method, args) -> {
                    if ("getApEstimatePage".equals(method.getName())) {
                        ErpApEstimateDO estimate = new ErpApEstimateDO().setId(1L).setEstimateNo("YS202605060001")
                                .setSupplierId(11L).setAccountId(21L)
                                .setSourceBizType(100).setSourceBizId(1001L);
                        estimate.setCreator("tester");
                        return new PageResult<>(List.of(estimate), 1L);
                    }
                    if ("getApEstimateItemListByEstimateIds".equals(method.getName())) {
                        return List.of(
                                new ErpApEstimateItemDO().setId(101L).setEstimateId(1L).setProductId(1001L)
                                        .setWarehouseId(2001L).setProjectId(3001L));
                    }
                    return null;
                });
    }

    private Object createApEstimateStatementServiceProxy() {
        return Proxy.newProxyInstance(
                ErpApEstimateController.class.getClassLoader(),
                new Class<?>[]{cn.weitee.erp.module.erp.service.finance.ErpApStatementService.class},
                (proxy, method, args) -> {
                    if ("getApStatementListByBizTypeAndBizIds".equals(method.getName())) {
                        return List.of(new ErpApStatementDO().setBizId(1001L));
                    }
                    return List.of();
                });
    }

    private Object createProductServiceProxy() {
        return Proxy.newProxyInstance(
                ErpApEstimateController.class.getClassLoader(),
                new Class<?>[]{cn.weitee.erp.module.erp.service.product.ErpProductService.class},
                (proxy, method, args) -> "getProductVOMap".equals(method.getName())
                        ? Map.of(1001L, new ErpProductRespVO().setId(1001L).setName("物料A"))
                        : null);
    }

    private Object createWarehouseServiceProxy() {
        return Proxy.newProxyInstance(
                ErpApEstimateController.class.getClassLoader(),
                new Class<?>[]{cn.weitee.erp.module.erp.service.stock.ErpWarehouseService.class},
                (proxy, method, args) -> "getWarehouseMap".equals(method.getName())
                        ? Map.of(2001L, new ErpWarehouseDO().setId(2001L).setName("仓库A"))
                        : null);
    }

    private Object createProjectServiceProxy() {
        return Proxy.newProxyInstance(
                ErpApEstimateController.class.getClassLoader(),
                new Class<?>[]{cn.weitee.erp.module.erp.service.project.ErpProjectService.class},
                (proxy, method, args) -> "getProjectMap".equals(method.getName())
                        ? Map.of(3001L, new ErpProjectDO().setId(3001L).setName("项目A"))
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
                                51L, new AdminUserRespDTO().setNickname("制单A"),
                                52L, new AdminUserRespDTO().setNickname("制单B"),
                                31L, new AdminUserRespDTO().setNickname("财务A"),
                                32L, new AdminUserRespDTO().setNickname("财务B"),
                                33L, new AdminUserRespDTO().setNickname("财务C"));
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
