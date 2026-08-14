package cn.weitee.erp.module.mes.service;

import cn.weitee.erp.framework.common.exception.ServiceException;
import cn.weitee.erp.module.mes.controller.admin.vo.sop.MesSopDocumentSaveReqVO;
import cn.weitee.erp.module.mes.dal.dataobject.MesSopDocumentDO;
import cn.weitee.erp.module.mes.dal.dataobject.MesSopStepBindingDO;
import cn.weitee.erp.module.mes.dal.mysql.MesSopDocumentMapper;
import cn.weitee.erp.module.mes.dal.mysql.MesSopStepBindingMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MesSopServiceImplTest {

    private final AtomicReference<MesSopDocumentDO> insertedRef = new AtomicReference<>();
    private final AtomicReference<MesSopDocumentDO> sopRef = new AtomicReference<>();
    private final List<MesSopStepBindingDO> insertedBindings = new java.util.ArrayList<>();
    private final AtomicReference<MesSopDocumentDO> updatedRef = new AtomicReference<>();

    private MesSopServiceImpl sopService;

    @BeforeEach
    void setUp() throws Exception {
        sopService = new MesSopServiceImpl();
        insertedRef.set(null);
        sopRef.set(null);
        insertedBindings.clear();
        updatedRef.set(null);

        setField(sopService, "mesSopDocumentMapper", createProxy(MesSopDocumentMapper.class, (m, a) -> {
            if ("insert".equals(m)) {
                insertedRef.set((MesSopDocumentDO) a[0]);
                return 1;
            }
            if ("selectById".equals(m)) return sopRef.get();
            if ("updateById".equals(m)) {
                updatedRef.set((MesSopDocumentDO) a[0]);
                return 1;
            }
            if ("deleteById".equals(m)) return 1;
            if ("selectByIds".equals(m)) return sopRef.get() == null ? List.of() : List.of(sopRef.get());
            return null;
        }));
        setField(sopService, "mesSopStepBindingMapper", createProxy(MesSopStepBindingMapper.class, (m, a) -> {
            if ("insertBatch".equals(m)) {
                insertedBindings.addAll((List<MesSopStepBindingDO>) a[0]);
                return true;
            }
            if ("selectListBySopId".equals(m)) return List.of();
            if ("selectListByStepId".equals(m)) return List.of();
            if ("deleteBySopId".equals(m)) return 1;
            return null;
        }));
    }

    private MesSopDocumentSaveReqVO buildReqVO() {
        MesSopDocumentSaveReqVO reqVO = new MesSopDocumentSaveReqVO();
        reqVO.setSopNo("SOP-001");
        reqVO.setTitle("装配作业指导");
        reqVO.setVersion("V1.0");
        reqVO.setRouteStepIds(List.of(101L, 102L));
        return reqVO;
    }

    @Test
    void createSop_shouldCreateDraftAndBindSteps() {
        Long id = sopService.createSop(buildReqVO());

        assertEquals(0, insertedRef.get().getStatus()); // 草稿态
        assertEquals("SOP-001", insertedRef.get().getSopNo());
        assertEquals(2, insertedBindings.size());
        assertEquals(101L, insertedBindings.get(0).getRouteStepId());
    }

    @Test
    void updateSop_shouldRejectWhenPublished() {
        sopRef.set(new MesSopDocumentDO().setId(1L).setStatus(1));
        MesSopDocumentSaveReqVO reqVO = buildReqVO();
        reqVO.setId(1L);

        assertThrows(ServiceException.class, () -> sopService.updateSop(reqVO));
    }

    @Test
    void updateSop_shouldRebindWhenDraft() {
        sopRef.set(new MesSopDocumentDO().setId(1L).setStatus(0));
        MesSopDocumentSaveReqVO reqVO = buildReqVO();
        reqVO.setId(1L);

        sopService.updateSop(reqVO);

        assertEquals(2, insertedBindings.size());
    }

    @Test
    void updateStatus_shouldRejectInvalidStatus() {
        sopRef.set(new MesSopDocumentDO().setId(1L).setStatus(0));
        assertThrows(ServiceException.class, () -> sopService.updateStatus(1L, 9));
    }

    @Test
    void updateStatus_shouldAllowPublish() {
        sopRef.set(new MesSopDocumentDO().setId(1L).setStatus(0));
        sopService.updateStatus(1L, 1);
        assertEquals(1, updatedRef.get().getStatus());
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> type, MethodHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        if ("toString".equals(method.getName())) return type.getSimpleName() + "Proxy";
                        if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
                        if ("equals".equals(method.getName())) return proxy == args[0];
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
