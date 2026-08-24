package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.device.ErpDeviceSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpDeviceDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpWorkCenterDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpDeviceMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderStepMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpWorkCenterMapper;
import cn.weitee.erp.module.erp.enums.mrp.ErpDeviceStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpDeviceServiceImplTest {

    private ErpDeviceServiceImpl service;

    private final AtomicReference<ErpDeviceDO> insertedRef = new AtomicReference<>();
    private final AtomicReference<ErpDeviceDO> selectedRef = new AtomicReference<>();
    private final AtomicReference<ErpDeviceDO> existedByCodeRef = new AtomicReference<>();
    private final AtomicReference<ErpWorkCenterDO> centerRef = new AtomicReference<>();
    private final AtomicInteger orderStepCount = new AtomicInteger();

    @BeforeEach
    void setUp() throws Exception {
        service = new ErpDeviceServiceImpl();
        insertedRef.set(null);
        selectedRef.set(null);
        existedByCodeRef.set(null);
        centerRef.set(null);
        orderStepCount.set(0);

        setField(service, "deviceMapper", createProxy(ErpDeviceMapper.class, (methodName, args) -> {
            switch (methodName) {
                case "insert":
                    ErpDeviceDO device = (ErpDeviceDO) args[0];
                    device.setId(300L);
                    insertedRef.set(device);
                    return 1;
                case "selectById":
                    return selectedRef.get();
                case "selectByDeviceCode":
                    return existedByCodeRef.get();
                case "updateById":
                    return 1;
                case "deleteById":
                    return 1;
                default:
                    return null;
            }
        }));
        setField(service, "workCenterMapper", createProxy(ErpWorkCenterMapper.class, (methodName, args) -> centerRef.get()));
        setField(service, "productionOrderStepMapper", createProxy(ErpProductionOrderStepMapper.class, (methodName, args) -> {
            if ("selectCount".equals(methodName)) {
                return (long) orderStepCount.get();
            }
            return null;
        }));
    }

    @Test
    void create_shouldDefaultStatusToIdle() {
        centerRef.set(new ErpWorkCenterDO().setId(1L));
        ErpDeviceSaveReqVO reqVO = buildReqVO();
        reqVO.setDeviceStatus(null);
        Long id = service.create(reqVO);
        assertEquals(300L, id);
        assertNotNull(insertedRef.get());
        assertEquals(ErpDeviceStatusEnum.IDLE.getStatus(), insertedRef.get().getDeviceStatus());
    }

    @Test
    void create_shouldRejectDuplicateCode() {
        existedByCodeRef.set(new ErpDeviceDO().setId(1L));
        ServiceException ex = assertThrows(ServiceException.class, () -> service.create(buildReqVO()));
        assertEquals(1_030_700_051, ex.getCode());
    }

    @Test
    void create_shouldRejectInvalidStatus() {
        ErpDeviceSaveReqVO reqVO = buildReqVO();
        reqVO.setWorkCenterId(null);
        reqVO.setDeviceStatus(99);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.create(reqVO));
        assertEquals(1_030_700_052, ex.getCode());
    }

    @Test
    void create_shouldRejectDisabledDeviceWithWorkCenter() {
        centerRef.set(new ErpWorkCenterDO().setId(1L));
        ErpDeviceSaveReqVO reqVO = buildReqVO();
        reqVO.setDeviceStatus(ErpDeviceStatusEnum.DISABLED.getStatus());
        ServiceException ex = assertThrows(ServiceException.class, () -> service.create(reqVO));
        assertEquals(1_030_700_052, ex.getCode());
    }

    @Test
    void create_shouldRejectWorkCenterNotExists() {
        ErpDeviceSaveReqVO reqVO = buildReqVO();
        ServiceException ex = assertThrows(ServiceException.class, () -> service.create(reqVO));
        assertEquals(1_030_700_048, ex.getCode());
    }

    @Test
    void create_shouldSucceedWithEnabledCenter() {
        centerRef.set(new ErpWorkCenterDO().setId(1L));
        Long id = service.create(buildReqVO());
        assertEquals(300L, id);
    }

    @Test
    void update_shouldRejectNotExists() {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.update(buildReqVO()));
        assertEquals(1_030_700_050, ex.getCode());
    }

    @Test
    void delete_shouldRejectWhenReferencedByOrderStep() {
        selectedRef.set(new ErpDeviceDO().setId(300L));
        orderStepCount.set(1);
        ServiceException ex = assertThrows(ServiceException.class, () -> service.delete(300L));
        assertEquals(1_030_700_053, ex.getCode());
    }

    @Test
    void delete_shouldSucceedWhenNotReferenced() {
        selectedRef.set(new ErpDeviceDO().setId(300L));
        service.delete(300L);
    }

    @Test
    void get_shouldRejectNotExists() {
        ServiceException ex = assertThrows(ServiceException.class, () -> service.get(300L));
        assertEquals(1_030_700_050, ex.getCode());
    }

    private ErpDeviceSaveReqVO buildReqVO() {
        ErpDeviceSaveReqVO reqVO = new ErpDeviceSaveReqVO();
        reqVO.setDeviceCode("EQ-001");
        reqVO.setDeviceName("数控机床");
        reqVO.setWorkCenterId(1L);
        reqVO.setSpecification("CNC-500");
        reqVO.setDeviceStatus(ErpDeviceStatusEnum.IDLE.getStatus());
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
