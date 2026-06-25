package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.security.core.LoginUser;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpMrpPlanDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpMrpPlanMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionSuggestMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpPurchaseSuggestMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.mrp.ErpMrpPlanStatusEnum;
import cn.weitee.erp.module.erp.service.project.ErpProjectRoleTaskService;
import cn.weitee.erp.module.erp.service.sale.ErpSaleOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpSaleOrderMrpFlowServiceImplTest {

    @Test
    void createAutoPlanForSaleOrder_shouldBuildDraftPlanFromSaleOrder() throws Exception {
        ErpMrpPlanServiceImpl mrpPlanService = new ErpMrpPlanServiceImpl();
        AtomicReference<ErpMrpPlanDO> insertedPlanRef = new AtomicReference<>();
        AtomicLong idGenerator = new AtomicLong(100L);

        setField(mrpPlanService, "planMapper", createProxy(ErpMrpPlanMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                ErpMrpPlanDO plan = (ErpMrpPlanDO) args[0];
                plan.setId(idGenerator.incrementAndGet());
                insertedPlanRef.set(plan);
                return 1;
            }
            return null;
        }));
        ErpNoRedisDAO noRedisDAO = new ErpNoRedisDAO() {
            @Override
            public String generate(String prefix) {
                assertEquals(ErpNoRedisDAO.MRP_PLAN_NO_PREFIX, prefix);
                return "MRP202604030001";
            }
        };
        setField(mrpPlanService, "noRedisDAO", noRedisDAO);

        ErpSaleOrderDO saleOrder = new ErpSaleOrderDO()
                .setId(11L)
                .setNo("SO-2026-001")
                .setOrderTime(LocalDateTime.of(2026, 4, 3, 9, 30))
                .setDeliveryDate(LocalDate.of(2026, 4, 8));

        LoginUser loginUser = new LoginUser();
        loginUser.setId(9527L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, List.of()));
        try {
            Long planId = mrpPlanService.createAutoPlanForSaleOrder(saleOrder);

            assertEquals(101L, planId);
            ErpMrpPlanDO insertedPlan = insertedPlanRef.get();
            assertNotNull(insertedPlan);
            assertEquals("MRP202604030001", insertedPlan.getPlanNo());
            assertEquals("AUTO-SO-SO-2026-001", insertedPlan.getPlanName());
            assertEquals(LocalDate.of(2026, 4, 8), insertedPlan.getPlanStartDate());
            assertEquals(LocalDate.of(2026, 4, 8), insertedPlan.getPlanEndDate());
            assertEquals(ErpMrpPlanStatusEnum.DRAFT.getStatus(), insertedPlan.getStatus());
            assertEquals(9527L, insertedPlan.getOperatorId());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void createAutoPlanForSaleOrder_shouldFailFastWhenDemandDateMissing() throws Exception {
        ErpMrpPlanServiceImpl mrpPlanService = new ErpMrpPlanServiceImpl();
        AtomicReference<ErpMrpPlanDO> insertedPlanRef = new AtomicReference<>();

        setField(mrpPlanService, "planMapper", createProxy(ErpMrpPlanMapper.class, (methodName, args) -> {
            if ("insert".equals(methodName)) {
                insertedPlanRef.set((ErpMrpPlanDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(mrpPlanService, "noRedisDAO", new ErpNoRedisDAO() {
            @Override
            public String generate(String prefix) {
                return "MRP202604030002";
            }
        });

        ErpSaleOrderDO saleOrder = new ErpSaleOrderDO()
                .setId(12L)
                .setNo("SO-2026-002");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> mrpPlanService.createAutoPlanForSaleOrder(saleOrder));

        assertEquals("Sale order 12 must have deliveryDate or orderTime before auto plan creation",
                exception.getMessage());
        assertNull(insertedPlanRef.get());
    }

    @Test
    void releaseApprovedSaleOrder_shouldCreateAutoPlanAndRunOnlyCurrentOrder() throws Exception {
        ErpSaleOrderMrpFlowServiceImpl flowService = new ErpSaleOrderMrpFlowServiceImpl();
        AtomicReference<ErpSaleOrderDO> validatedSaleOrderRef = new AtomicReference<>();
        AtomicReference<ErpSaleOrderDO> createdPlanSaleOrderRef = new AtomicReference<>();
        List<ErpMrpPlanDO> updatedPlans = new ArrayList<>();
        AtomicReference<Long> runPlanIdRef = new AtomicReference<>();
        AtomicReference<List<ErpSaleOrderDO>> runSaleOrdersRef = new AtomicReference<>();
        AtomicReference<Long> pcTaskProjectIdRef = new AtomicReference<>();
        AtomicReference<Long> pcTaskSaleOrderIdRef = new AtomicReference<>();
        AtomicReference<LocalDate> pcTaskDueDateRef = new AtomicReference<>();
        AtomicReference<Long> mcTaskProjectIdRef = new AtomicReference<>();
        AtomicReference<Long> mcTaskSourceIdRef = new AtomicReference<>();
        AtomicReference<String> mcTaskSummaryRef = new AtomicReference<>();

        ErpSaleOrderDO saleOrder = new ErpSaleOrderDO()
                .setId(22L)
                .setNo("SO-APPROVED-22")
                .setProjectId(3001L)
                .setOrderTime(LocalDateTime.of(2026, 4, 3, 10, 0))
                .setDeliveryDate(LocalDate.of(2026, 4, 9));

        setField(flowService, "saleOrderService", createProxy(ErpSaleOrderService.class, (methodName, args) -> {
            if ("validateSaleOrder".equals(methodName)) {
                assertEquals(22L, args[0]);
                validatedSaleOrderRef.set(saleOrder);
                return saleOrder;
            }
            return null;
        }));
        setField(flowService, "mrpPlanService", createProxy(ErpMrpPlanService.class, (methodName, args) -> {
            if ("createAutoPlanForSaleOrder".equals(methodName)) {
                ErpSaleOrderDO currentSaleOrder = (ErpSaleOrderDO) args[0];
                createdPlanSaleOrderRef.set(currentSaleOrder);
                return 7001L;
            }
            return null;
        }));
        setField(flowService, "planMapper", createProxy(ErpMrpPlanMapper.class, (methodName, args) -> {
            if ("updateById".equals(methodName)) {
                updatedPlans.add((ErpMrpPlanDO) args[0]);
                return 1;
            }
            return null;
        }));
        setField(flowService, "mrpCalcService", createProxy(ErpMrpCalcService.class, (methodName, args) -> {
            if ("runForSaleOrders".equals(methodName)) {
                runPlanIdRef.set((Long) args[0]);
                runSaleOrdersRef.set(List.copyOf((List<ErpSaleOrderDO>) args[1]));
            }
            return null;
        }));
        setField(flowService, "projectRoleTaskService", createProxy(ErpProjectRoleTaskService.class, (methodName, args) -> {
            if ("createOrRefreshPcTask".equals(methodName)) {
                pcTaskProjectIdRef.set((Long) args[0]);
                pcTaskSaleOrderIdRef.set((Long) args[1]);
                pcTaskDueDateRef.set((LocalDate) args[2]);
                return null;
            }
            if ("createOrRefreshMcTask".equals(methodName)) {
                mcTaskProjectIdRef.set((Long) args[0]);
                mcTaskSourceIdRef.set((Long) args[1]);
                mcTaskSummaryRef.set((String) args[2]);
                return null;
            }
            return null;
        }));
        setField(flowService, "purchaseSuggestMapper", createProxy(ErpPurchaseSuggestMapper.class, (methodName, args) -> {
            if ("selectCountByPlanIdAndProjectId".equals(methodName)) {
                assertEquals(7001L, args[0]);
                assertEquals(3001L, args[1]);
                return 2L;
            }
            return null;
        }));
        setField(flowService, "productionSuggestMapper", createProxy(ErpProductionSuggestMapper.class, (methodName, args) -> {
            if ("selectCountByPlanIdAndProjectId".equals(methodName)) {
                assertEquals(7001L, args[0]);
                assertEquals(3001L, args[1]);
                return 1L;
            }
            return null;
        }));
        setField(flowService, "transactionManager", createProxy(PlatformTransactionManager.class, (methodName, args) -> {
            if ("getTransaction".equals(methodName)) {
                return new SimpleTransactionStatus();
            }
            if ("commit".equals(methodName) || "rollback".equals(methodName)) {
                return null;
            }
            return null;
        }));

        flowService.releaseApprovedSaleOrder(22L);

        assertSame(saleOrder, validatedSaleOrderRef.get());
        assertSame(saleOrder, createdPlanSaleOrderRef.get());
        assertEquals(7001L, runPlanIdRef.get());
        assertEquals(1, runSaleOrdersRef.get().size());
        assertSame(saleOrder, runSaleOrdersRef.get().get(0));
        assertEquals(2, updatedPlans.size());
        assertEquals(ErpMrpPlanStatusEnum.RUNNING.getStatus(), updatedPlans.get(0).getStatus());
        assertEquals(7001L, updatedPlans.get(0).getId());
        assertNotNull(updatedPlans.get(0).getRunTime());
        assertEquals(ErpMrpPlanStatusEnum.FINISHED.getStatus(), updatedPlans.get(1).getStatus());
        assertEquals(7001L, updatedPlans.get(1).getId());
        assertEquals(3001L, pcTaskProjectIdRef.get());
        assertEquals(22L, pcTaskSaleOrderIdRef.get());
        assertEquals(LocalDate.of(2026, 4, 9), pcTaskDueDateRef.get());
        assertEquals(3001L, mcTaskProjectIdRef.get());
        assertEquals(7001L, mcTaskSourceIdRef.get());
        assertEquals("MRP 已生成 2 条采购建议、1 条生产建议，请 MC 确认物料准备策略", mcTaskSummaryRef.get());
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
