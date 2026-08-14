package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.workcenter.ErpWorkCenterSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpWorkCenterDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpDeviceMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProcessRouteStepMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpWorkCenterMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpWorkCenterServiceImplTest {

    private ErpWorkCenterServiceImpl service;

    private final AtomicReference<ErpWorkCenterDO> insertedRef = new AtomicReference<>();
    private final AtomicReference<ErpWorkCenterDO> selectedRef = new AtomicReference<>();
    private final AtomicReference<ErpWorkCenterDO> existedByCodeRef = new AtomicReference<>();
    private final AtomicInteger deviceCount = new AtomicInteger();
    private final AtomicInteger stepCount = new AtomicInteger();

    @BeforeEach
    void setUp() throws Exception {
        service = new ErpWorkCenterServiceImpl();
        insertedRef.set(null);
        selectedRef.set(null);
        existedByCodeRef.set(null);
        deviceCount.set(0);
        stepCount.set(0);

        setField(service, "workCenterMapper", createProxy(ErpWorkCenterMapper.class, (methodName, args) -> {
            switch (methodName) {
                case "insert":
                    ErpWorkCenterDO workCenter = (ErpWorkCenterDO) args[0];
                    workCenter.setId(200L);
                    insertedRef.set(workCenter);
                    return 1;
                case "selectById":
                    return selectedRef.get();
                case "selectByCenterCode":
                    return existedByCodeRef.get();
                case "updateById":
                    return 1;
                case "deleteById":
                    return 1;
                default:
                    return null;
            }
        }));
        setField(service, "deviceMapper", createProxy(ErpDeviceMapper.class, (methodName, args) -> {
            if ("selectCount".equals(methodName)) {
                return (long) deviceCount.get();
            }
            return null;
        }));
        setField(service, "routeStepMapper", createProxy(ErpProcessRouteStepMapper.class, (methodName, args) -> {
            if ("selectCount".equals(methodName)) {
                return (long) stepCount.get();
            }
            return null;
        }));
    }

    @Test
    void create_shouldInsertWorkCenter() {
        Long id = service.create(buildReqVO());
        assertEquals(200L, id);
        ErpWorkCenterDO inserted = insertedRef.get();
        assertNotNull(inserted);
        assertEquals(Boolean.TRUE, inserted.getEnableDeviceDispatch());
    }

    @Test
    void create_shouldRejectDuplicateCode() {
        existedByCodeRef.set(new ErpWorkCenterDO().setId(1L));
        ServiceException ex = assertThrows(ServiceException.class, () -> service.create(buildReqVO()));
        assertEquals(1_030_700_049, ex.getCode());
    }

    @Test
    void update_shouldRejectNotExists() {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.update(buildReqVO()));
        assertEquals(1_030_700_048, ex.getCode());
    }

    @Test
    void update_shouldSucceed() {
        selectedRef.set(new ErpWorkCenterDO().setId(200L));
        service.update(buildReqVO().setId(200L));
    }

    @Test
    void delete_shouldRejectWhenDeviceExists() {
        selectedRef.set(new ErpWorkCenterDO().setId(200L));
        deviceCount.set(1);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.delete(200L));
        assertEquals(1_030_700_054, ex.getCode());
    }

    @Test
    void delete_shouldRejectWhenRouteStepExists() {
        selectedRef.set(new ErpWorkCenterDO().setId(200L));
        stepCount.set(1);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.delete(200L));
        assertEquals(1_030_700_054, ex.getCode());
    }

    @Test
    void delete_shouldSucceedWhenNotReferenced() {
        selectedRef.set(new ErpWorkCenterDO().setId(200L));
        service.delete(200L);
    }

    @Test
    void get_shouldRejectNotExists() {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.get(200L));
        assertEquals(1_030_700_048, ex.getCode());
    }

    private ErpWorkCenterSaveReqVO buildReqVO() {
        ErpWorkCenterSaveReqVO reqVO = new ErpWorkCenterSaveReqVO();
        reqVO.setCenterCode("WC-001");
        reqVO.setCenterName("装配车间");
        reqVO.setDeptId(10L);
        reqVO.setEnableDeviceDispatch(true);
        reqVO.setStatus(1);
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
