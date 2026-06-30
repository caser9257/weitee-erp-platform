package cn.weitee.erp.module.erp.service.purchase;

import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.weitee.erp.module.erp.dal.dataobject.purchase.ErpPurchaseReturnDO;
import cn.weitee.erp.module.erp.dal.mysql.purchase.ErpPurchaseReturnMapper;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErpPurchaseReturnBpmServiceImplTest {

    // ========== submit tests ==========

    @Test
    void submitPurchaseReturn_shouldStartProcessAndBindProcessInstance() throws Exception {
        Object service = createService();
        AtomicReference<ErpPurchaseReturnDO> entityRef = new AtomicReference<>(
                new ErpPurchaseReturnDO()
                        .setId(1L).setNo("PR-001")
                        .setStatus(ErpAuditStatus.DRAFT.getStatus()));
        List<int[]> updates = new java.util.ArrayList<>();
        AtomicReference<String> submittedSceneCode = new AtomicReference<>();

        injectField(service, "erpPurchaseReturnMapper", createMapperProxy(entityRef, updates));
        injectField(service, "purchaseReturnService", createServiceProxy());
        injectField(service, "approvalRuntimeService", createApprovalProxy(submittedSceneCode));

        Object reqVO = createSubmitReqVO(1L, Map.of("task_1", List.of(7L)));
        Method method = service.getClass().getMethod("submitPurchaseReturn", Long.class, reqVO.getClass());
        Object result = method.invoke(service, 9527L, reqVO);

        assertNull(result); // afterCommit returns null
        // First update: set PROCESS status
        assertEquals(1, updates.get(0)[0]); // id
        // Second update (afterCommit): set processInstanceId
        assertEquals(1, updates.get(1)[0]); // id
        assertEquals("erp.purchase.return.submit", submittedSceneCode.get());
    }

    @Test
    void submitPurchaseReturn_shouldThrowWhenAlreadyApproved() throws Exception {
        Object service = createService();
        AtomicReference<ErpPurchaseReturnDO> entityRef = new AtomicReference<>(
                new ErpPurchaseReturnDO()
                        .setId(2L).setNo("PR-002")
                        .setStatus(ErpAuditStatus.APPROVE.getStatus()));

        injectField(service, "erpPurchaseReturnMapper", createMapperProxy(entityRef, new java.util.ArrayList<>()));
        injectField(service, "purchaseReturnService", createServiceProxy());
        injectField(service, "approvalRuntimeService", createApprovalProxy(null));

        Object reqVO = createSubmitReqVO(2L, Map.of());
        Method method = service.getClass().getMethod("submitPurchaseReturn", Long.class, reqVO.getClass());
        assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> method.invoke(service, 9527L, reqVO));
    }

    @Test
    void submitPurchaseReturn_shouldThrowWhenAlreadyProcessingWithProcessInstanceId() throws Exception {
        Object service = createService();
        ErpPurchaseReturnDO entity = new ErpPurchaseReturnDO()
                .setId(3L).setNo("PR-003")
                .setStatus(ErpAuditStatus.PROCESS.getStatus());
        entity.setProcessInstanceId("existing-pi-id");
        AtomicReference<ErpPurchaseReturnDO> entityRef = new AtomicReference<>(entity);

        injectField(service, "erpPurchaseReturnMapper", createMapperProxy(entityRef, new java.util.ArrayList<>()));
        injectField(service, "purchaseReturnService", createServiceProxy());
        injectField(service, "approvalRuntimeService", createApprovalProxy(null));

        Object reqVO = createSubmitReqVO(3L, Map.of());
        Method method = service.getClass().getMethod("submitPurchaseReturn", Long.class, reqVO.getClass());
        assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> method.invoke(service, 9527L, reqVO));
    }

    // ========== cancel tests ==========

    @Test
    void cancelPurchaseReturnApproval_shouldCancelProcessAndClearBinding() throws Exception {
        Object service = createService();
        ErpPurchaseReturnDO entity = new ErpPurchaseReturnDO()
                .setId(4L).setNo("PR-004")
                .setStatus(ErpAuditStatus.PROCESS.getStatus());
        entity.setProcessInstanceId("PI-TO-CANCEL");
        AtomicReference<ErpPurchaseReturnDO> entityRef = new AtomicReference<>(entity);
        AtomicReference<String> cancelSceneCode = new AtomicReference<>();

        injectField(service, "erpPurchaseReturnMapper", createMapperProxy(entityRef, new java.util.ArrayList<>()));
        injectField(service, "purchaseReturnService", createServiceProxy());
        injectField(service, "approvalRuntimeService", createCancelProxy(cancelSceneCode));

        Object reqVO = createCancelReqVO(4L, "cancel test");
        Method method = service.getClass().getMethod("cancelPurchaseReturnApproval", Long.class, reqVO.getClass());
        method.invoke(service, 9527L, reqVO);

        assertEquals("erp.purchase.return.submit", cancelSceneCode.get());
    }

    @Test
    void cancelPurchaseReturnApproval_shouldThrowWhenNotProcessing() throws Exception {
        Object service = createService();
        AtomicReference<ErpPurchaseReturnDO> entityRef = new AtomicReference<>(
                new ErpPurchaseReturnDO()
                        .setId(5L).setNo("PR-005")
                        .setStatus(ErpAuditStatus.DRAFT.getStatus()));

        injectField(service, "erpPurchaseReturnMapper", createMapperProxy(entityRef, new java.util.ArrayList<>()));
        injectField(service, "purchaseReturnService", createServiceProxy());
        injectField(service, "approvalRuntimeService", createCancelProxy(null));

        Object reqVO = createCancelReqVO(5L, "reason");
        Method method = service.getClass().getMethod("cancelPurchaseReturnApproval", Long.class, reqVO.getClass());
        assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> method.invoke(service, 9527L, reqVO));
    }

    @Test
    void cancelPurchaseReturnApproval_shouldThrowWhenProcessInstanceIdBlank() throws Exception {
        Object service = createService();
        AtomicReference<ErpPurchaseReturnDO> entityRef = new AtomicReference<>(
                new ErpPurchaseReturnDO()
                        .setId(6L).setNo("PR-006")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus()));
        // processInstanceId is null (blank)

        injectField(service, "erpPurchaseReturnMapper", createMapperProxy(entityRef, new java.util.ArrayList<>()));
        injectField(service, "purchaseReturnService", createServiceProxy());
        injectField(service, "approvalRuntimeService", createCancelProxy(null));

        Object reqVO = createCancelReqVO(6L, "reason");
        Method method = service.getClass().getMethod("cancelPurchaseReturnApproval", Long.class, reqVO.getClass());
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
        // No exception = pass
    }

    // ========== helpers ==========

    private Object createService() throws Exception {
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.service.purchase.ErpPurchaseReturnBpmServiceImpl");
        return clazz.getDeclaredConstructor().newInstance();
    }

    private Object createSubmitReqVO(Long id, Map<String, List<Long>> assignees) throws Exception {
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnSubmitReqVO");
        Object reqVO = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("setId", Long.class).invoke(reqVO, id);
        clazz.getMethod("setStartUserSelectAssignees", Map.class).invoke(reqVO, assignees);
        return reqVO;
    }

    private Object createCancelReqVO(Long id, String reason) throws Exception {
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.controller.admin.purchase.vo.returns.ErpPurchaseReturnCancelApprovalReqVO");
        Object reqVO = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("setId", Long.class).invoke(reqVO, id);
        clazz.getMethod("setReason", String.class).invoke(reqVO, reason);
        return reqVO;
    }

    private Object createMapperProxy(AtomicReference<ErpPurchaseReturnDO> entityRef, List<int[]> updates) {
        return createProxy(ErpPurchaseReturnMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) {
                return entityRef.get();
            }
            if ("updateById".equals(methodName)) {
                ErpPurchaseReturnDO doObj = (ErpPurchaseReturnDO) args[0];
                updates.add(new int[]{doObj.getId().intValue()});
                return 1;
            }
            if ("clearProcessInstanceId".equals(methodName)) {
                return 1;
            }
            return null;
        });
    }

    private Object createServiceProxy() {
        return createProxy(ErpPurchaseReturnService.class, (methodName, args) -> null);
    }

    private Object createApprovalProxy(AtomicReference<String> sceneCodeRef) {
        return createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> {
            if ("submit".equals(methodName)) {
                if (sceneCodeRef != null) {
                    sceneCodeRef.set((String) args[0]);
                }
                return "PR-PI-20260629-001";
            }
            if ("cancel".equals(methodName)) {
                if (sceneCodeRef != null) {
                    sceneCodeRef.set((String) args[0]);
                }
            }
            return null;
        });
    }

    private Object createCancelProxy(AtomicReference<String> sceneCodeRef) {
        return createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> {
            if ("cancel".equals(methodName)) {
                if (sceneCodeRef != null) {
                    sceneCodeRef.set((String) args[0]);
                }
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
