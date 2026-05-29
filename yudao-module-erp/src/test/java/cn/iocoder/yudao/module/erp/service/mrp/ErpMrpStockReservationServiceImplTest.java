package cn.iocoder.yudao.module.erp.service.mrp;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationProjectSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationSummaryPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.mrp.vo.stockreservation.ErpMrpStockReservationSummaryRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpPlanDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpStockReservationDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpStockReservationSummaryDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpMrpPlanMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpMrpStockReservationMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpMrpStockReservationSummaryMapper;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.project.ErpProjectService;
import cn.iocoder.yudao.module.erp.service.sale.ErpSaleOrderService;
import cn.iocoder.yudao.module.erp.service.stock.ErpStockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ErpMrpStockReservationServiceImplTest {

    private static final Long PLAN_ID = 11L;
    private static final Long PRODUCT_ID = 31L;
    private static final Long PROJECT_A_ID = 21L;
    private static final Long PROJECT_B_ID = 22L;
    private static final Long PROJECT_C_ID = 23L;
    private static final Long ORDER_A_ID = 41L;
    private static final Long ORDER_B_ID = 42L;
    private static final Long ORDER_C_ID = 43L;
    private static final Long ORDER_D_ID = 44L;

    private final AtomicReference<PageResult<ErpMrpStockReservationDO>> pageResultRef = new AtomicReference<>();
    private final AtomicReference<PageResult<ErpMrpStockReservationSummaryDO>> summaryPageResultRef = new AtomicReference<>();
    private final AtomicReference<List<ErpMrpStockReservationDO>> activeReservationsRef = new AtomicReference<>();

    private ErpMrpStockReservationServiceImpl service;

    private Map<Long, ErpMrpPlanDO> planRegistry;
    private Map<Long, ErpProjectDO> projectRegistry;
    private Map<Long, ErpProductRespVO> productRegistry;
    private Map<Long, ErpSaleOrderDO> saleOrderRegistry;
    private Map<Long, BigDecimal> stockQtyRegistry;

    @BeforeEach
    void setUp() throws Exception {
        service = new ErpMrpStockReservationServiceImpl();
        pageResultRef.set(new PageResult<>(Collections.emptyList(), 0L));
        summaryPageResultRef.set(new PageResult<>(Collections.emptyList(), 0L));
        activeReservationsRef.set(Collections.emptyList());

        planRegistry = Map.of(
                PLAN_ID, new ErpMrpPlanDO().setId(PLAN_ID).setPlanNo("MRP-001")
        );
        projectRegistry = Map.of(
                PROJECT_A_ID, new ErpProjectDO().setId(PROJECT_A_ID).setNo("PRJ-001").setName("Project A"),
                PROJECT_B_ID, new ErpProjectDO().setId(PROJECT_B_ID).setNo("PRJ-002").setName("Project B"),
                PROJECT_C_ID, new ErpProjectDO().setId(PROJECT_C_ID).setNo("PRJ-003").setName("Project C")
        );
        productRegistry = Map.of(
                PRODUCT_ID, new ErpProductRespVO().setId(PRODUCT_ID).setName("Material A")
        );
        saleOrderRegistry = Map.of(
                ORDER_A_ID, new ErpSaleOrderDO().setId(ORDER_A_ID).setNo("SO-001"),
                ORDER_B_ID, new ErpSaleOrderDO().setId(ORDER_B_ID).setNo("SO-002"),
                ORDER_C_ID, new ErpSaleOrderDO().setId(ORDER_C_ID).setNo("SO-003"),
                ORDER_D_ID, new ErpSaleOrderDO().setId(ORDER_D_ID).setNo("SO-004")
        );
        stockQtyRegistry = Map.of(
                PRODUCT_ID, new BigDecimal("48.50")
        );

        setField(service, "stockReservationMapper", createProxy(ErpMrpStockReservationMapper.class, (methodName, args) -> {
            if ("selectPage".equals(methodName)) {
                return pageResultRef.get();
            }
            if ("selectActiveListByProductIds".equals(methodName)) {
                Collection<Long> productIds = toLongCollection(args[0]);
                return activeReservationsRef.get().stream()
                        .filter(item -> productIds.contains(item.getProductId()))
                        .collect(Collectors.toList());
            }
            return null;
        }));
        setField(service, "stockReservationSummaryMapper", createProxy(ErpMrpStockReservationSummaryMapper.class,
                (methodName, args) -> "selectPage".equals(methodName) ? summaryPageResultRef.get() : null));
        setField(service, "planMapper", createProxy(ErpMrpPlanMapper.class, (methodName, args) -> {
            if ("selectByIds".equals(methodName)) {
                return toLongCollection(args[0]).stream()
                        .map(planRegistry::get)
                        .collect(Collectors.toList());
            }
            return null;
        }));
        setField(service, "projectService", createProxy(ErpProjectService.class, (methodName, args) -> {
            if ("getProjectMap".equals(methodName)) {
                return toLongCollection(args[0]).stream()
                        .filter(projectRegistry::containsKey)
                        .collect(Collectors.toMap(id -> id, projectRegistry::get));
            }
            return null;
        }));
        setField(service, "productService", createProxy(ErpProductService.class, (methodName, args) -> {
            if ("getProductVOMap".equals(methodName)) {
                return toLongCollection(args[0]).stream()
                        .filter(productRegistry::containsKey)
                        .collect(Collectors.toMap(id -> id, productRegistry::get));
            }
            return null;
        }));
        setField(service, "saleOrderService", createProxy(ErpSaleOrderService.class, (methodName, args) -> {
            if ("getSaleOrderListByIds".equals(methodName)) {
                return toLongCollection(args[0]).stream()
                        .map(saleOrderRegistry::get)
                        .collect(Collectors.toList());
            }
            return null;
        }));
        setField(service, "stockService", createProxy(ErpStockService.class, (methodName, args) -> {
            if ("getStockCountMap".equals(methodName)) {
                return toLongCollection(args[0]).stream()
                        .filter(stockQtyRegistry::containsKey)
                        .collect(Collectors.toMap(id -> id, stockQtyRegistry::get));
            }
            return null;
        }));
    }

    @Test
    void getStockReservationPage_shouldFillDisplayNames() {
        pageResultRef.set(new PageResult<>(List.of(
                new ErpMrpStockReservationDO()
                        .setId(1L)
                        .setPlanId(PLAN_ID)
                        .setProjectId(PROJECT_A_ID)
                        .setProductId(PRODUCT_ID)
                        .setSourceOrderId(ORDER_A_ID)
                        .setReservedQty(new BigDecimal("12.50"))
                        .setStatus(0)
        ), 1L));

        PageResult<ErpMrpStockReservationRespVO> page =
                service.getStockReservationPage(new ErpMrpStockReservationPageReqVO());

        assertThat(page.getList()).hasSize(1);
        ErpMrpStockReservationRespVO row = page.getList().get(0);
        assertThat(row.getPlanNo()).isEqualTo("MRP-001");
        assertThat(row.getProjectName()).isEqualTo("Project A");
        assertThat(row.getProductName()).isEqualTo("Material A");
        assertThat(row.getSourceOrderNo()).isEqualTo("SO-001");
    }

    @Test
    void getStockReservationSummaryPage_shouldFillOverviewFields() {
        summaryPageResultRef.set(new PageResult<>(List.of(
                new ErpMrpStockReservationSummaryDO()
                        .setProductId(PRODUCT_ID)
                        .setActiveReservedQty(new BigDecimal("12.50"))
                        .setActiveProjectCount(3)
                        .setActiveReservationCount(4)
        ), 1L));
        activeReservationsRef.set(List.of(
                newReservation(PRODUCT_ID, PROJECT_A_ID, ORDER_A_ID, "8.00", "2026-04-20T10:00:00"),
                newReservation(PRODUCT_ID, PROJECT_B_ID, ORDER_B_ID, "3.00", "2026-04-20T11:00:00"),
                newReservation(PRODUCT_ID, PROJECT_C_ID, ORDER_C_ID, "1.50", "2026-04-20T09:30:00")
        ));

        PageResult<ErpMrpStockReservationSummaryRespVO> page =
                service.getStockReservationSummaryPage(new ErpMrpStockReservationSummaryPageReqVO());

        assertThat(page.getList()).hasSize(1);
        ErpMrpStockReservationSummaryRespVO row = page.getList().get(0);
        assertThat(row.getProductName()).isEqualTo("Material A");
        assertThat(row.getStockQty()).isEqualByComparingTo("48.50");
        assertThat(row.getAvailableQty()).isEqualByComparingTo("36.00");
        assertThat(row.getProjectDistributionItems()).containsExactly("Project A 8.00", "Project B 3.00");
        assertThat(row.getProjectDistributionMoreCount()).isEqualTo(1);
    }

    @Test
    void getStockReservationProjectSummaryList_shouldAggregateByProject() {
        activeReservationsRef.set(List.of(
                newReservation(PRODUCT_ID, PROJECT_B_ID, ORDER_A_ID, "5.00", "2026-04-20T12:00:00"),
                newReservation(PRODUCT_ID, PROJECT_B_ID, ORDER_A_ID, "2.00", "2026-04-20T11:00:00"),
                newReservation(PRODUCT_ID, PROJECT_A_ID, ORDER_B_ID, "7.00", "2026-04-20T10:00:00"),
                newReservation(PRODUCT_ID, PROJECT_A_ID, ORDER_C_ID, "1.00", "2026-04-20T13:00:00"),
                newReservation(PRODUCT_ID, PROJECT_C_ID, ORDER_D_ID, "8.00", "2026-04-20T12:30:00")
        ));

        List<ErpMrpStockReservationProjectSummaryRespVO> result =
                service.getStockReservationProjectSummaryList(PRODUCT_ID);

        assertThat(result).hasSize(3);

        ErpMrpStockReservationProjectSummaryRespVO first = result.get(0);
        assertThat(first.getProjectId()).isEqualTo(PROJECT_A_ID);
        assertThat(first.getProjectName()).isEqualTo("Project A");
        assertThat(first.getActiveReservedQty()).isEqualByComparingTo("8.00");
        assertThat(first.getActiveReservationCount()).isEqualTo(2);
        assertThat(first.getSourceOrderCount()).isEqualTo(2);
        assertThat(first.getLastReservedTime()).isEqualTo(LocalDateTime.parse("2026-04-20T13:00:00"));

        ErpMrpStockReservationProjectSummaryRespVO second = result.get(1);
        assertThat(second.getProjectId()).isEqualTo(PROJECT_C_ID);
        assertThat(second.getActiveReservedQty()).isEqualByComparingTo("8.00");
        assertThat(second.getLastReservedTime()).isEqualTo(LocalDateTime.parse("2026-04-20T12:30:00"));

        ErpMrpStockReservationProjectSummaryRespVO third = result.get(2);
        assertThat(third.getProjectId()).isEqualTo(PROJECT_B_ID);
        assertThat(third.getActiveReservedQty()).isEqualByComparingTo("7.00");
        assertThat(third.getActiveReservationCount()).isEqualTo(2);
        assertThat(third.getSourceOrderCount()).isEqualTo(1);
    }

    private ErpMrpStockReservationDO newReservation(Long productId, Long projectId, Long sourceOrderId,
                                                    String reservedQty, String createTime) {
        ErpMrpStockReservationDO reservation = new ErpMrpStockReservationDO()
                .setProductId(productId)
                .setProjectId(projectId)
                .setSourceOrderId(sourceOrderId)
                .setReservedQty(new BigDecimal(reservedQty))
                .setStatus(0);
        reservation.setCreateTime(LocalDateTime.parse(createTime));
        return reservation;
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

    @SuppressWarnings("unchecked")
    private Collection<Long> toLongCollection(Object arg) {
        if (arg == null) {
            return Collections.emptyList();
        }
        return (Collection<Long>) arg;
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
