package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.route.ErpProcessRouteSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpBomDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProcessRouteDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpWorkCenterDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpBomMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProcessRouteMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProcessRouteStepMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpWorkCenterMapper;
import cn.weitee.erp.module.erp.enums.mrp.ErpProcessRouteStatusEnum;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpProcessRouteServiceImplTest {

    private ErpProcessRouteServiceImpl service;

    private final AtomicReference<ErpProcessRouteDO> insertedRouteRef = new AtomicReference<>();
    private final AtomicReference<ErpProcessRouteDO> selectedRouteRef = new AtomicReference<>();
    private final AtomicReference<ErpProcessRouteDO> existedByCodeRef = new AtomicReference<>();
    private final AtomicReference<List<ErpProcessRouteDO>> insertedStepsRef = new AtomicReference<>();
    private final AtomicInteger bomRouteRefCount = new AtomicInteger();
    private final AtomicInteger orderRouteRefCount = new AtomicInteger();

    @BeforeAll
    static void initTableInfo() {
        // 纯单元测试环境缺少 MyBatis-Plus 表元数据缓存，lambda 包装器需要先注册
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                ErpProcessRouteDO.class);
    }

    @BeforeEach
    void setUp() throws Exception {
        service = new ErpProcessRouteServiceImpl();
        insertedRouteRef.set(null);
        selectedRouteRef.set(null);
        existedByCodeRef.set(null);
        insertedStepsRef.set(null);
        bomRouteRefCount.set(0);
        orderRouteRefCount.set(0);

        setField(service, "routeMapper", createProxy(ErpProcessRouteMapper.class, (methodName, args) -> {
            switch (methodName) {
                case "insert":
                    ErpProcessRouteDO route = (ErpProcessRouteDO) args[0];
                    route.setId(100L);
                    insertedRouteRef.set(route);
                    return 1;
                case "selectById":
                    return selectedRouteRef.get();
                case "selectByRouteCode":
                    return existedByCodeRef.get();
                case "updateById":
                    return 1;
                case "update":
                    return 1;
                case "deleteById":
                    return 1;
                default:
                    return null;
            }
        }));
        setField(service, "stepMapper", createProxy(ErpProcessRouteStepMapper.class, (methodName, args) -> {
            if ("insertBatch".equals(methodName)) {
                insertedStepsRef.set((List<ErpProcessRouteDO>) args[0]);
                return Boolean.TRUE;
            }
            if ("deleteByRouteId".equals(methodName)) {
                return 1;
            }
            return null;
        }));
        setField(service, "workCenterMapper", createProxy(ErpWorkCenterMapper.class, (methodName, args) -> {
            if ("selectByIds".equals(methodName)) {
                java.util.Collection<?> ids = (java.util.Collection<?>) args[0];
                return ids.stream().map(id -> new ErpWorkCenterDO().setId((Long) id)).toList();
            }
            return null;
        }));
        setField(service, "bomMapper", createProxy(ErpBomMapper.class, (methodName, args) -> {
            if ("selectCount".equals(methodName)) {
                return (long) bomRouteRefCount.get();
            }
            return null;
        }));
        setField(service, "productionOrderMapper", createProxy(ErpProductionOrderMapper.class, (methodName, args) -> {
            if ("selectCount".equals(methodName)) {
                return (long) orderRouteRefCount.get();
            }
            return null;
        }));
        setField(service, "productService", createProxy(ErpProductService.class, (methodName, args) -> List.of()));
    }

    @Test
    void create_shouldInsertRouteAndStepsWithDraftStatus() {
        Long id = service.create(buildReqVO());

        assertEquals(100L, id);
        ErpProcessRouteDO inserted = insertedRouteRef.get();
        assertNotNull(inserted);
        assertEquals(ErpProcessRouteStatusEnum.DRAFT.getStatus(), inserted.getStatus());
        assertEquals(Boolean.TRUE, inserted.getDefaultFlag());
        assertEquals(2, insertedStepsRef.get().size());
    }

    @Test
    void create_shouldRejectDuplicateRouteCode() {
        existedByCodeRef.set(new ErpProcessRouteDO().setId(1L));
        ServiceException ex = assertThrows(ServiceException.class, () -> service.create(buildReqVO()));
        assertEquals(1_030_700_043, ex.getCode());
    }

    @Test
    void create_shouldRejectExpireBeforeEffective() {
        ErpProcessRouteSaveReqVO reqVO = buildReqVO();
        reqVO.setEffectiveDate(LocalDate.of(2026, 6, 1));
        reqVO.setExpireDate(LocalDate.of(2026, 5, 1));
        ServiceException ex = assertThrows(ServiceException.class, () -> service.create(reqVO));
        assertEquals(1_030_700_046, ex.getCode());
    }

    @Test
    void create_shouldRejectDuplicateStepNo() {
        ErpProcessRouteSaveReqVO reqVO = buildReqVO();
        reqVO.getSteps().get(1).setStepNo(10);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.create(reqVO));
        assertEquals(1_030_700_045, ex.getCode());
    }

    @Test
    void create_shouldRejectDuplicateStepCode() {
        ErpProcessRouteSaveReqVO reqVO = buildReqVO();
        reqVO.getSteps().get(1).setStepCode("OP-10");
        ServiceException ex = assertThrows(ServiceException.class, () -> service.create(reqVO));
        assertEquals(1_030_700_045, ex.getCode());
    }

    @Test
    void update_shouldRejectNotExists() {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.update(buildReqVO()));
        assertEquals(1_030_700_042, ex.getCode());
    }

    @Test
    void updateStatus_shouldRejectInvalidStatus() {
        selectedRouteRef.set(new ErpProcessRouteDO().setId(100L));
        ServiceException ex = assertThrows(ServiceException.class, () -> service.updateStatus(100L, 99));
        assertEquals(1_030_700_044, ex.getCode());
    }

    @Test
    void updateStatus_shouldAcceptEnabled() {
        selectedRouteRef.set(new ErpProcessRouteDO().setId(100L));
        service.updateStatus(100L, ErpProcessRouteStatusEnum.ENABLED.getStatus());
    }

    @Test
    void delete_shouldRejectWhenReferencedByBom() {
        selectedRouteRef.set(new ErpProcessRouteDO().setId(100L));
        bomRouteRefCount.set(1);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.delete(100L));
        assertEquals(1_030_700_047, ex.getCode());
    }

    @Test
    void delete_shouldRejectWhenReferencedByOrder() {
        selectedRouteRef.set(new ErpProcessRouteDO().setId(100L));
        orderRouteRefCount.set(1);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.delete(100L));
        assertEquals(1_030_700_047, ex.getCode());
    }

    @Test
    void delete_shouldSucceedWhenNotReferenced() {
        selectedRouteRef.set(new ErpProcessRouteDO().setId(100L));
        service.delete(100L);
    }

    @Test
    void get_shouldRejectNotExists() {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.get(100L));
        assertEquals(1_030_700_042, ex.getCode());
    }

    private ErpProcessRouteSaveReqVO buildReqVO() {
        ErpProcessRouteSaveReqVO reqVO = new ErpProcessRouteSaveReqVO();
        reqVO.setRouteCode("RT-001");
        reqVO.setRouteName("装配路线");
        reqVO.setProductId(10L);
        reqVO.setVersion("V1");
        reqVO.setDefaultFlag(true);
        reqVO.setEffectiveDate(LocalDate.of(2026, 1, 1));
        reqVO.setExpireDate(LocalDate.of(2026, 12, 31));

        ErpProcessRouteSaveReqVO.Step step1 = new ErpProcessRouteSaveReqVO.Step();
        step1.setStepNo(10);
        step1.setStepCode("OP-10");
        step1.setStepName("下料");
        step1.setWorkCenterId(1L);
        step1.setPrepareTime(new BigDecimal("0.5"));
        step1.setProcessTime(new BigDecimal("1.5"));
        step1.setReportRequired(true);

        ErpProcessRouteSaveReqVO.Step step2 = new ErpProcessRouteSaveReqVO.Step();
        step2.setStepNo(20);
        step2.setStepCode("OP-20");
        step2.setStepName("装配");
        step2.setQcFlag(true);
        step2.setInspectRequired(true);

        reqVO.setSteps(List.of(step1, step2));
        return reqVO;
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
