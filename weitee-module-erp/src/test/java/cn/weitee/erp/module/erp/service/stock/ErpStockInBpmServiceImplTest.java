package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockInDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockInMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpStockInBpmServiceImplTest {

    // ========== submit tests ==========

    @Test
    void submitStockIn_shouldStartProcessAndBindProcessInstance() throws Exception {
        Object service = createService();
        AtomicReference<ErpStockInDO> entityRef = new AtomicReference<>(
                new ErpStockInDO()
                        .setId(1L).setNo("SI-001")
                        .setStatus(ErpAuditStatus.DRAFT.getStatus()));
        List<int[]> updates = new ArrayList<>();
        AtomicReference<String> submittedSceneCode = new AtomicReference<>();

        injectField(service, "erpStockInMapper", createMapperProxy(entityRef, updates));
        injectField(service, "stockInService", createServiceProxy());
        injectField(service, "approvalRuntimeService", createApprovalProxy(submittedSceneCode));

        Object reqVO = createSubmitReqVO(1L, Map.of("task_1", List.of(7L)));
        Method method = service.getClass().getMethod("submitStockIn", Long.class, reqVO.getClass());
        Object result = method.invoke(service, 9527L, reqVO);

        assertNull(result); // afterCommit returns null
        assertEquals(1, updates.get(0)[0]); // First update: set PROCESS
        assertEquals(1, updates.get(1)[0]); // Second update: set processInstanceId
        assertEquals("erp.stock.in.submit", submittedSceneCode.get());
    }

    @Test
    void submitStockIn_shouldThrowWhenAlreadyApproved() throws Exception {
        Object service = createService();
        AtomicReference<ErpStockInDO> entityRef = new AtomicReference<>(
                new ErpStockInDO()
                        .setId(2L).setNo("SI-002")
                        .setStatus(ErpAuditStatus.APPROVE.getStatus()));

        injectField(service, "erpStockInMapper", createMapperProxy(entityRef, new ArrayList<>()));
        injectField(service, "stockInService", createServiceProxy());
        injectField(service, "approvalRuntimeService", createApprovalProxy(null));

        Object reqVO = createSubmitReqVO(2L, Map.of());
        Method method = service.getClass().getMethod("submitStockIn", Long.class, reqVO.getClass());
        assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> method.invoke(service, 9527L, reqVO));
    }

    @Test
    void submitStockIn_shouldThrowWhenAlreadyProcessingWithProcessInstanceId() throws Exception {
        Object service = createService();
        ErpStockInDO entity = new ErpStockInDO()
                .setId(3L).setNo("SI-003")
                .setStatus(ErpAuditStatus.PROCESS.getStatus());
        entity.setProcessInstanceId("existing-pi-id");
        AtomicReference<ErpStockInDO> entityRef = new AtomicReference<>(entity);

        injectField(service, "erpStockInMapper", createMapperProxy(entityRef, new ArrayList<>()));
        injectField(service, "stockInService", createServiceProxy());
        injectField(service, "approvalRuntimeService", createApprovalProxy(null));

        Object reqVO = createSubmitReqVO(3L, Map.of());
        Method method = service.getClass().getMethod("submitStockIn", Long.class, reqVO.getClass());
        assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> method.invoke(service, 9527L, reqVO));
    }

    // ========== cancel tests ==========

    @Test
    void cancelStockInApproval_shouldCancelProcess() throws Exception {
        Object service = createService();
        ErpStockInDO entity = new ErpStockInDO()
                .setId(4L).setNo("SI-004")
                .setStatus(ErpAuditStatus.PROCESS.getStatus());
        entity.setProcessInstanceId("PI-TO-CANCEL");
        AtomicReference<ErpStockInDO> entityRef = new AtomicReference<>(entity);
        AtomicReference<String> cancelSceneCode = new AtomicReference<>();

        injectField(service, "erpStockInMapper", createMapperProxy(entityRef, new ArrayList<>()));
        injectField(service, "stockInService", createServiceProxy());
        injectField(service, "approvalRuntimeService", createCancelProxy(cancelSceneCode));

        Object reqVO = createCancelReqVO(4L, "cancel test");
        Method method = service.getClass().getMethod("cancelStockInApproval", Long.class, reqVO.getClass());
        method.invoke(service, 9527L, reqVO);

        assertEquals("erp.stock.in.submit", cancelSceneCode.get());
    }

    @Test
    void cancelStockInApproval_shouldThrowWhenNotProcessing() throws Exception {
        Object service = createService();
        AtomicReference<ErpStockInDO> entityRef = new AtomicReference<>(
                new ErpStockInDO()
                        .setId(5L).setNo("SI-005")
                        .setStatus(ErpAuditStatus.DRAFT.getStatus()));

        injectField(service, "erpStockInMapper", createMapperProxy(entityRef, new ArrayList<>()));
        injectField(service, "stockInService", createServiceProxy());
        injectField(service, "approvalRuntimeService", createCancelProxy(null));

        Object reqVO = createCancelReqVO(5L, "reason");
        Method method = service.getClass().getMethod("cancelStockInApproval", Long.class, reqVO.getClass());
        assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> method.invoke(service, 9527L, reqVO));
    }

    @Test
    void cancelStockInApproval_shouldThrowWhenProcessInstanceIdBlank() throws Exception {
        Object service = createService();
        AtomicReference<ErpStockInDO> entityRef = new AtomicReference<>(
                new ErpStockInDO()
                        .setId(6L).setNo("SI-006")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus()));
        // processInstanceId is null

        injectField(service, "erpStockInMapper", createMapperProxy(entityRef, new ArrayList<>()));
        injectField(service, "stockInService", createServiceProxy());
        injectField(service, "approvalRuntimeService", createCancelProxy(null));

        Object reqVO = createCancelReqVO(6L, "reason");
        Method method = service.getClass().getMethod("cancelStockInApproval", Long.class, reqVO.getClass());
        assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> method.invoke(service, 9527L, reqVO));
    }

    // ========== handleProcessInstanceResult test ==========

    @Test
    void handleProcessInstanceResult_shouldBeNoOp() throws Exception {
        Object service = createService();
        Method method = service.getClass().getMethod("handleProcessInstanceResult",
                Long.class, String.class, Integer.class, String.class);
        method.invoke(service, 10L, "PI-123", 20, "ok");
    }

    // ========== helpers ==========

    private Object createService() throws Exception {
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.service.stock.ErpStockInBpmServiceImpl");
        return clazz.getDeclaredConstructor().newInstance();
    }

    private Object createSubmitReqVO(Long id, Map<String, List<Long>> assignees) throws Exception {
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.controller.admin.stock.vo.in.ErpStockInSubmitReqVO");
        Object reqVO = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("setId", Long.class).invoke(reqVO, id);
        clazz.getMethod("setStartUserSelectAssignees", Map.class).invoke(reqVO, assignees);
        return reqVO;
    }

    private Object createCancelReqVO(Long id, String reason) throws Exception {
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.controller.admin.stock.vo.in.ErpStockInCancelApprovalReqVO");
        Object reqVO = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("setId", Long.class).invoke(reqVO, id);
        clazz.getMethod("setReason", String.class).invoke(reqVO, reason);
        return reqVO;
    }

    private Object createMapperProxy(AtomicReference<ErpStockInDO> entityRef, List<int[]> updates) {
        return createProxy(ErpStockInMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) return entityRef.get();
            if ("updateById".equals(methodName)) {
                ErpStockInDO doObj = (ErpStockInDO) args[0];
                updates.add(new int[]{doObj.getId().intValue()});
                return 1;
            }
            if ("clearProcessInstanceId".equals(methodName)) return 1;
            return null;
        });
    }

    private Object createServiceProxy() {
        return createProxy(ErpStockInService.class, (methodName, args) -> null);
    }

    private Object createApprovalProxy(AtomicReference<String> sceneCodeRef) {
        return createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> {
            if ("submit".equals(methodName)) {
                if (sceneCodeRef != null) sceneCodeRef.set((String) args[0]);
                return "SI-PI-20260629-001";
            }
            if ("cancel".equals(methodName)) {
                if (sceneCodeRef != null) sceneCodeRef.set((String) args[0]);
            }
            return null;
        });
    }

    private Object createCancelProxy(AtomicReference<String> sceneCodeRef) {
        return createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> {
            if ("cancel".equals(methodName)) {
                if (sceneCodeRef != null) sceneCodeRef.set((String) args[0]);
            }
            return null;
        });
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

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @FunctionalInterface
    private interface MethodHandler {
        Object handle(String methodName, Object[] args) throws Exception;
    }
}
