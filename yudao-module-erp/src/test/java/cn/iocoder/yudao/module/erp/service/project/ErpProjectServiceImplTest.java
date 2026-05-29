package cn.iocoder.yudao.module.erp.service.project;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.module.erp.controller.admin.project.vo.project.ErpProjectMcConfirmReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.project.vo.project.ErpProjectPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.project.vo.project.ErpProjectPcConfirmReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.project.vo.project.ErpProjectSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpProductionSuggestMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpPurchaseSuggestMapper;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.iocoder.yudao.module.erp.dal.mysql.project.ErpProjectMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpBusinessTypeConstants;
import cn.iocoder.yudao.module.erp.enums.ErpProjectTypeConstants;
import cn.iocoder.yudao.module.erp.enums.mrp.ErpMrpSuggestStatusEnum;
import cn.iocoder.yudao.module.erp.service.sale.ErpCustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PROJECT_DELETE_FAIL_EXISTS_SALE_ORDER;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PROJECT_MC_CONFIRM_FORBIDDEN;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PROJECT_MC_CONFIRM_STATUS_INVALID;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PROJECT_NOT_ENABLE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PROJECT_NO_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PROJECT_PC_CONFIRM_FORBIDDEN;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PROJECT_PC_CONFIRM_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

class ErpProjectServiceImplTest {

    private final AtomicReference<ErpProjectDO> selectByNoResult = new AtomicReference<>();
    private final AtomicReference<ErpProjectDO> selectByIdResult = new AtomicReference<>();
    private final AtomicReference<ErpProjectPageReqVO> selectPageReqVO = new AtomicReference<>();
    private final AtomicLong saleOrderCount = new AtomicLong();
    private final AtomicReference<ErpSaleOrderDO> saleOrderByIdResult = new AtomicReference<>();
    private final AtomicLong pendingPurchaseSuggestCount = new AtomicLong();
    private final AtomicLong pendingProductionSuggestCount = new AtomicLong();
    private final AtomicLong validatedCustomerId = new AtomicLong(-1L);
    private final List<ErpProjectDO> insertedProjects = new ArrayList<>();
    private final List<ErpProjectDO> updatedProjects = new ArrayList<>();
    private final AtomicReference<Long> completedPcProjectId = new AtomicReference<>();
    private final AtomicReference<String> completedPcRemark = new AtomicReference<>();
    private final AtomicReference<Long> completedMcProjectId = new AtomicReference<>();
    private final AtomicReference<String> completedMcRemark = new AtomicReference<>();
    private final AtomicReference<Long> refreshedPcProjectId = new AtomicReference<>();
    private final AtomicReference<Long> refreshedPcSaleOrderId = new AtomicReference<>();
    private final AtomicReference<java.time.LocalDate> refreshedPcDueDate = new AtomicReference<>();
    private final AtomicReference<Long> refreshedMcProjectId = new AtomicReference<>();
    private final AtomicReference<Long> refreshedMcSourceId = new AtomicReference<>();
    private final AtomicReference<String> refreshedMcSummary = new AtomicReference<>();

    private ErpProjectServiceImpl projectService;

    @BeforeEach
    void setUp() throws Exception {
        projectService = new ErpProjectServiceImpl();
        insertedProjects.clear();
        updatedProjects.clear();
        selectByNoResult.set(null);
        selectByIdResult.set(null);
        selectPageReqVO.set(null);
        saleOrderCount.set(0L);
        saleOrderByIdResult.set(null);
        pendingPurchaseSuggestCount.set(0L);
        pendingProductionSuggestCount.set(0L);
        validatedCustomerId.set(-1L);
        completedPcProjectId.set(null);
        completedPcRemark.set(null);
        completedMcProjectId.set(null);
        completedMcRemark.set(null);
        refreshedPcProjectId.set(null);
        refreshedPcSaleOrderId.set(null);
        refreshedPcDueDate.set(null);
        refreshedMcProjectId.set(null);
        refreshedMcSourceId.set(null);
        refreshedMcSummary.set(null);
        setField(projectService, "erpProjectMapper", createProjectMapperProxy());
        setField(projectService, "customerService", createCustomerServiceProxy());
        setField(projectService, "saleOrderMapper", createSaleOrderMapperProxy());
        setField(projectService, "purchaseSuggestMapper", createPurchaseSuggestMapperProxy());
        setField(projectService, "productionSuggestMapper", createProductionSuggestMapperProxy());
        setField(projectService, "projectRoleTaskService", createProjectRoleTaskServiceProxy());
        setField(projectService, "noRedisDAO", new ErpNoRedisDAO() {
            @Override
            public String generate(String prefix) {
                return prefix + "-000001";
            }
        });
    }

    @Test
    void testCreateProjectDuplicateNo() {
        ErpProjectSaveReqVO reqVO = createReqVO();
        selectByNoResult.set(new ErpProjectDO().setId(1L).setNo("PRJ-001"));

        try {
            projectService.createProject(reqVO);
            fail("Expected ServiceException");
        } catch (ServiceException ex) {
            assertServiceException(ex, PROJECT_NO_EXISTS);
        }
    }

    @Test
    void testDeleteProjectExistsSaleOrder() {
        ErpProjectDO project = new ErpProjectDO().setId(1L).setName("project-one")
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        selectByIdResult.set(project);
        saleOrderCount.set(2L);

        try {
            projectService.deleteProject(1L);
            fail("Expected ServiceException");
        } catch (ServiceException ex) {
            assertServiceException(ex, PROJECT_DELETE_FAIL_EXISTS_SALE_ORDER, project.getName());
        }
    }

    @Test
    void testValidateProjectDisable() {
        ErpProjectDO project = new ErpProjectDO().setId(1L).setName("project-one")
                .setStatus(CommonStatusEnum.DISABLE.getStatus());
        selectByIdResult.set(project);

        try {
            projectService.validateProject(1L);
            fail("Expected ServiceException");
        } catch (ServiceException ex) {
            assertServiceException(ex, PROJECT_NOT_ENABLE, project.getName());
        }
    }

    @Test
    void testCreateProjectSuccess() {
        ErpProjectSaveReqVO reqVO = createReqVO();
        reqVO.setProjectType(ErpProjectTypeConstants.RESEARCH);
        reqVO.setBusinessType(ErpBusinessTypeConstants.SELF_RESEARCH);
        reqVO.setCurrentStageCode("INIT");
        reqVO.setRiskLevel("NORMAL");

        Long id = projectService.createProject(reqVO);

        assertEquals(100L, id);
        assertEquals(1L, validatedCustomerId.get());
        assertEquals(1, insertedProjects.size());
        assertEquals("PRJ-001", insertedProjects.get(0).getNo());
        assertEquals(ErpProjectTypeConstants.RESEARCH, insertedProjects.get(0).getProjectType());
        assertEquals(ErpBusinessTypeConstants.SELF_RESEARCH, insertedProjects.get(0).getBusinessType());
        assertEquals("INIT", insertedProjects.get(0).getCurrentStageCode());
        assertEquals("PENDING", insertedProjects.get(0).getPcStatus());
        assertEquals("PENDING", insertedProjects.get(0).getMcStatus());
    }

    @Test
    void testUpdateProjectDuplicateNo() {
        ErpProjectSaveReqVO reqVO = createReqVO();
        reqVO.setId(2L);
        reqVO.setName("project-two");
        selectByIdResult.set(new ErpProjectDO().setId(2L).setNo("PRJ-002").setName("project-two"));
        selectByNoResult.set(new ErpProjectDO().setId(1L).setNo("PRJ-001"));

        try {
            projectService.updateProject(reqVO);
            fail("Expected ServiceException");
        } catch (ServiceException ex) {
            assertServiceException(ex, PROJECT_NO_EXISTS);
        }
    }

    @Test
    void testConfirmPcSuccess() {
        login(12L);
        try {
            selectByIdResult.set(new ErpProjectDO().setId(2L).setPlanCoordinatorId(12L).setPcStatus("PENDING"));
            ErpProjectPcConfirmReqVO reqVO = new ErpProjectPcConfirmReqVO();
            reqVO.setProjectId(2L);
            reqVO.setCurrentStageCode("PLAN_CONFIRMED");
            reqVO.setRemark("pc confirmed");

            projectService.confirmPc(reqVO);

            assertEquals(1, updatedProjects.size());
            ErpProjectDO updatedProject = updatedProjects.get(0);
            assertEquals(2L, updatedProject.getId());
            assertEquals("PLAN_CONFIRMED", updatedProject.getCurrentStageCode());
            assertEquals("DONE", updatedProject.getPcStatus());
            assertEquals("pc confirmed", updatedProject.getPcRemark());
            assertNotNull(updatedProject.getPcConfirmTime());
            assertEquals(2L, completedPcProjectId.get());
            assertEquals("pc confirmed", completedPcRemark.get());
        } finally {
            logout();
        }
    }

    @Test
    void testUpdateProjectRefreshPcTaskWhenCoordinatorAssignedForApprovedSaleOrder() {
        ErpProjectDO currentProject = new ErpProjectDO().setId(6L).setNo("PRJ-006").setName("project-six")
                .setCustomerId(1L).setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setSaleOrderId(18L).setPlanCoordinatorId(null).setPcStatus("PENDING");
        selectByIdResult.set(currentProject);
        saleOrderByIdResult.set(new ErpSaleOrderDO().setId(18L).setProjectId(6L)
                .setStatus(ErpAuditStatus.APPROVE.getStatus())
                .setDeliveryDate(java.time.LocalDate.of(2026, 4, 30)));
        ErpProjectSaveReqVO reqVO = createReqVO();
        reqVO.setId(6L);
        reqVO.setNo("PRJ-006");
        reqVO.setName("project-six");
        reqVO.setPlanCoordinatorId(910203L);

        projectService.updateProject(reqVO);

        assertEquals(1, updatedProjects.size());
        assertEquals(6L, refreshedPcProjectId.get());
        assertEquals(18L, refreshedPcSaleOrderId.get());
        assertEquals(java.time.LocalDate.of(2026, 4, 30), refreshedPcDueDate.get());
    }

    @Test
    void testUpdateProjectRefreshMcTaskWhenControllerAssignedForPendingSuggest() {
        ErpProjectDO currentProject = new ErpProjectDO().setId(6L).setNo("PRJ-006").setName("project-six")
                .setCustomerId(1L).setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setMaterialControllerId(null).setMcStatus("PENDING");
        selectByIdResult.set(currentProject);
        pendingPurchaseSuggestCount.set(1L);
        pendingProductionSuggestCount.set(0L);
        ErpProjectSaveReqVO reqVO = createReqVO();
        reqVO.setId(6L);
        reqVO.setNo("PRJ-006");
        reqVO.setName("project-six");
        reqVO.setMaterialControllerId(920203L);

        projectService.updateProject(reqVO);

        assertEquals(1, updatedProjects.size());
        assertEquals(6L, refreshedMcProjectId.get());
        assertNull(refreshedMcSourceId.get());
        assertEquals("MRP 建议仍待 MC 处理", refreshedMcSummary.get());
    }

    @Test
    void testConfirmPcForbidden() {
        login(99L);
        try {
            selectByIdResult.set(new ErpProjectDO().setId(2L).setPlanCoordinatorId(12L).setPcStatus("PENDING"));
            ErpProjectPcConfirmReqVO reqVO = new ErpProjectPcConfirmReqVO();
            reqVO.setProjectId(2L);
            reqVO.setCurrentStageCode("PLAN_CONFIRMED");
            reqVO.setRemark("pc confirmed");

            try {
                projectService.confirmPc(reqVO);
                fail("Expected ServiceException");
            } catch (ServiceException ex) {
                assertServiceException(ex, PROJECT_PC_CONFIRM_FORBIDDEN);
            }
            assertEquals(0, updatedProjects.size());
            assertNull(completedPcProjectId.get());
        } finally {
            logout();
        }
    }

    @Test
    void testConfirmPcStatusInvalid() {
        login(12L);
        try {
            selectByIdResult.set(new ErpProjectDO().setId(2L).setPlanCoordinatorId(12L).setPcStatus("DONE"));
            ErpProjectPcConfirmReqVO reqVO = new ErpProjectPcConfirmReqVO();
            reqVO.setProjectId(2L);
            reqVO.setCurrentStageCode("PLAN_CONFIRMED");
            reqVO.setRemark("pc confirmed");

            try {
                projectService.confirmPc(reqVO);
                fail("Expected ServiceException");
            } catch (ServiceException ex) {
                assertServiceException(ex, PROJECT_PC_CONFIRM_STATUS_INVALID);
            }
            assertEquals(0, updatedProjects.size());
            assertNull(completedPcProjectId.get());
        } finally {
            logout();
        }
    }

    @Test
    void testConfirmMcSuccess() {
        login(14L);
        try {
            selectByIdResult.set(new ErpProjectDO().setId(3L).setMaterialControllerId(14L).setMcStatus("PENDING"));
            ErpProjectMcConfirmReqVO reqVO = new ErpProjectMcConfirmReqVO();
            reqVO.setProjectId(3L);
            reqVO.setRemark("mc confirmed");

            projectService.confirmMc(reqVO);

            assertEquals(1, updatedProjects.size());
            ErpProjectDO updatedProject = updatedProjects.get(0);
            assertEquals(3L, updatedProject.getId());
            assertEquals("DONE", updatedProject.getMcStatus());
            assertEquals("mc confirmed", updatedProject.getMcRemark());
            assertNotNull(updatedProject.getMcConfirmTime());
            assertEquals(3L, completedMcProjectId.get());
            assertEquals("mc confirmed", completedMcRemark.get());
        } finally {
            logout();
        }
    }

    @Test
    void testConfirmMcForbidden() {
        login(88L);
        try {
            selectByIdResult.set(new ErpProjectDO().setId(3L).setMaterialControllerId(14L).setMcStatus("PENDING"));
            ErpProjectMcConfirmReqVO reqVO = new ErpProjectMcConfirmReqVO();
            reqVO.setProjectId(3L);
            reqVO.setRemark("mc confirmed");

            try {
                projectService.confirmMc(reqVO);
                fail("Expected ServiceException");
            } catch (ServiceException ex) {
                assertServiceException(ex, PROJECT_MC_CONFIRM_FORBIDDEN);
            }
            assertEquals(0, updatedProjects.size());
            assertNull(completedMcProjectId.get());
        } finally {
            logout();
        }
    }

    @Test
    void testConfirmMcStatusInvalid() {
        login(14L);
        try {
            selectByIdResult.set(new ErpProjectDO().setId(3L).setMaterialControllerId(14L).setMcStatus("DONE"));
            ErpProjectMcConfirmReqVO reqVO = new ErpProjectMcConfirmReqVO();
            reqVO.setProjectId(3L);
            reqVO.setRemark("mc confirmed");

            try {
                projectService.confirmMc(reqVO);
                fail("Expected ServiceException");
            } catch (ServiceException ex) {
                assertServiceException(ex, PROJECT_MC_CONFIRM_STATUS_INVALID);
            }
            assertEquals(0, updatedProjects.size());
            assertNull(completedMcProjectId.get());
        } finally {
            logout();
        }
    }

    @Test
    void testCreateDeliveryProjectFromSource() {
        ErpProjectDO sourceProject = new ErpProjectDO().setId(2L).setNo("SRC-001").setName("project-source")
                .setCustomerId(1L).setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setProjectManagerId(11L).setPlanCoordinatorId(12L).setMaterialControllerId(14L).setOwnerDeptId(13L)
                .setRiskLevel("LOW");
        selectByIdResult.set(sourceProject);

        ErpSaleOrderSaveReqVO saleReqVO = new ErpSaleOrderSaveReqVO();
        saleReqVO.setDeliveryDate(LocalDateTime.now().plusDays(5));
        saleReqVO.setRemark("sale delivery");

        Long id = projectService.createDeliveryProjectFromSource(2L, saleReqVO);

        assertEquals(100L, id);
        assertEquals(1, insertedProjects.size());
        ErpProjectDO deliveryProject = insertedProjects.get(0);
        assertEquals(ErpProjectTypeConstants.DELIVERY, deliveryProject.getProjectType());
        assertEquals(ErpBusinessTypeConstants.SELF_RESEARCH, deliveryProject.getBusinessType());
        assertEquals(2L, deliveryProject.getSourceProjectId());
        assertEquals("INIT", deliveryProject.getCurrentStageCode());
        assertEquals(14L, deliveryProject.getMaterialControllerId());
        assertEquals(1L, deliveryProject.getCustomerId());
        assertEquals("PENDING", deliveryProject.getPcStatus());
        assertEquals("PENDING", deliveryProject.getMcStatus());
    }

    @Test
    void testGetAssignedProjectPageForPc() {
        login(9527L);
        try {
            ErpProjectPageReqVO pageReqVO = new ErpProjectPageReqVO();

            PageResult<ErpProjectDO> result = projectService.getAssignedProjectPage("PC", pageReqVO);

            assertEquals(0L, result.getTotal());
            assertEquals(9527L, selectPageReqVO.get().getPlanCoordinatorId());
            assertNull(selectPageReqVO.get().getMaterialControllerId());
        } finally {
            logout();
        }
    }

    @Test
    void testGetAssignedProjectPageForMc() {
        login(9638L);
        try {
            ErpProjectPageReqVO pageReqVO = new ErpProjectPageReqVO();

            PageResult<ErpProjectDO> result = projectService.getAssignedProjectPage("MC", pageReqVO);

            assertEquals(0L, result.getTotal());
            assertNull(selectPageReqVO.get().getPlanCoordinatorId());
            assertEquals(9638L, selectPageReqVO.get().getMaterialControllerId());
        } finally {
            logout();
        }
    }

    private void login(Long userId) {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(userId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, List.of()));
    }

    private void logout() {
        SecurityContextHolder.clearContext();
    }

    private void assertServiceException(ServiceException ex, ErrorCode errorCode, Object... messageParams) {
        assertEquals(errorCode.getCode(), ex.getCode());
        assertEquals(ServiceExceptionUtil.doFormat(errorCode.getCode(), errorCode.getMsg(), messageParams),
                ex.getMessage());
    }

    private ErpProjectSaveReqVO createReqVO() {
        ErpProjectSaveReqVO reqVO = new ErpProjectSaveReqVO();
        reqVO.setNo("PRJ-001");
        reqVO.setName("project-one");
        reqVO.setCustomerId(1L);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return reqVO;
    }

    private ErpProjectMapper createProjectMapperProxy() {
        return createProxy(ErpProjectMapper.class, (methodName, args) -> {
            switch (methodName) {
                case "selectByNo":
                    return selectByNoResult.get();
                case "selectById":
                    return selectByIdResult.get();
                case "selectPage":
                    selectPageReqVO.set((ErpProjectPageReqVO) args[0]);
                    return new PageResult<>(List.of(), 0L);
                case "insert":
                    ErpProjectDO insertProject = (ErpProjectDO) args[0];
                    if (insertProject.getId() == null) {
                        insertProject.setId(100L);
                    }
                    insertedProjects.add(insertProject);
                    return 1;
                case "updateById":
                    updatedProjects.add((ErpProjectDO) args[0]);
                    return 1;
                case "deleteById":
                    return 1;
                default:
                    return null;
            }
        });
    }

    private ErpCustomerService createCustomerServiceProxy() {
        return createProxy(ErpCustomerService.class, (methodName, args) -> {
            if ("validateCustomer".equals(methodName)) {
                validatedCustomerId.set((Long) args[0]);
            }
            return null;
        });
    }

    private ErpSaleOrderMapper createSaleOrderMapperProxy() {
        return createProxy(ErpSaleOrderMapper.class, (methodName, args) -> {
            if ("selectCountByProjectId".equals(methodName)) {
                return saleOrderCount.get();
            }
            if ("selectById".equals(methodName)) {
                return saleOrderByIdResult.get();
            }
            return null;
        });
    }

    private ErpPurchaseSuggestMapper createPurchaseSuggestMapperProxy() {
        return createProxy(ErpPurchaseSuggestMapper.class, (methodName, args) -> {
            if ("selectCountByProjectIdAndStatus".equals(methodName)) {
                assertEquals(ErpMrpSuggestStatusEnum.TO_CONFIRM.getStatus(), args[1]);
                return pendingPurchaseSuggestCount.get();
            }
            return null;
        });
    }

    private ErpProductionSuggestMapper createProductionSuggestMapperProxy() {
        return createProxy(ErpProductionSuggestMapper.class, (methodName, args) -> {
            if ("selectCountByProjectIdAndStatus".equals(methodName)) {
                assertEquals(ErpMrpSuggestStatusEnum.TO_CONFIRM.getStatus(), args[1]);
                return pendingProductionSuggestCount.get();
            }
            return null;
        });
    }

    private ErpProjectRoleTaskService createProjectRoleTaskServiceProxy() {
        return createProxy(ErpProjectRoleTaskService.class, (methodName, args) -> {
            switch (methodName) {
                case "createOrRefreshPcTask":
                    refreshedPcProjectId.set((Long) args[0]);
                    refreshedPcSaleOrderId.set((Long) args[1]);
                    refreshedPcDueDate.set((java.time.LocalDate) args[2]);
                    return null;
                case "createOrRefreshMcTask":
                    refreshedMcProjectId.set((Long) args[0]);
                    refreshedMcSourceId.set((Long) args[1]);
                    refreshedMcSummary.set((String) args[2]);
                    return null;
                case "completePcTask":
                    completedPcProjectId.set((Long) args[0]);
                    completedPcRemark.set((String) args[1]);
                    return null;
                case "completeMcTask":
                    completedMcProjectId.set((Long) args[0]);
                    completedMcRemark.set((String) args[1]);
                    return null;
                default:
                    return null;
            }
        });
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
