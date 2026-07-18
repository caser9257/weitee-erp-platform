package cn.weitee.erp.module.erp.service.stock;

import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRuntimeService;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockOutDO;
import cn.weitee.erp.module.erp.dal.mysql.stock.ErpStockOutMapper;
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

class ErpStockOutBpmServiceImplTest {

    // ========== submit tests ==========

    @Test
    void submitStockOut_shouldStartProcessAndBindProcessInstance() throws Exception {
        Object service = createService();
        AtomicReference<ErpStockOutDO> entityRef = new AtomicReference<>(
                new ErpStockOutDO()
                        .setId(1L).setNo("SO-001")
                        .setStatus(ErpAuditStatus.DRAFT.getStatus()));
        List<int[]> updates = new ArrayList<>();
        AtomicReference<String> submittedSceneCode = new AtomicReference<>();

        injectField(service, "erpStockOutMapper", createMapperProxy(entityRef, updates));
        injectField(service, "stockOutService", createServiceProxy());
        injectField(service, "approvalRuntimeService", createApprovalProxy(submittedSceneCode));

        Object reqVO = createSubmitReqVO(1L, Map.of("task_1", List.of(7L)));
        Method method = service.getClass().getMethod("submitStockOut", Long.class, reqVO.getClass());
        Object result = method.invoke(service, 9527L, reqVO);

        assertNull(result);
        assertEquals(1, updates.get(0)[0]);
        assertEquals(1, updates.get(1)[0]);
        assertEquals("erp.stock.out.submit", submittedSceneCode.get());
    }

    @Test
    void submitStockOut_shouldMarkFailedWhenBpmSubmitThrows() throws Exception {
        Object service = createService();
        AtomicReference<ErpStockOutDO> entityRef = new AtomicReference<>(
                new ErpStockOutDO()
                        .setId(8L).setNo("SO-008")
                        .setStatus(ErpAuditStatus.DRAFT.getStatus()));
        List<ErpStockOutDO> updateObjects = new ArrayList<>();

        injectField(service, "erpStockOutMapper", createMapperObjectProxy(entityRef, updateObjects));
        injectField(service, "stockOutService", createServiceProxy());
        injectField(service, "approvalRuntimeService", createApprovalFailureProxy());

        Object reqVO = createSubmitReqVO(8L, Map.of());
        Method method = service.getClass().getMethod("submitStockOut", Long.class, reqVO.getClass());
        Object result = method.invoke(service, 9527L, reqVO);

        assertNull(result);
        assertEquals(2, updateObjects.size());
        assertEquals(ErpAuditStatus.PROCESS.getStatus(), updateObjects.get(0).getStatus());
        assertNull(updateObjects.get(0).getProcessInstanceId());
        assertEquals(ErpAuditStatus.FAILED.getStatus(), updateObjects.get(1).getStatus());
        assertNull(updateObjects.get(1).getProcessInstanceId());
    }

    @Test
    void submitStockOut_shouldThrowWhenAlreadyApproved() throws Exception {
        Object service = createService();
        AtomicReference<ErpStockOutDO> entityRef = new AtomicReference<>(
                new ErpStockOutDO()
                        .setId(2L).setNo("SO-002")
                        .setStatus(ErpAuditStatus.APPROVE.getStatus()));

        injectField(service, "erpStockOutMapper", createMapperProxy(entityRef, new ArrayList<>()));
        injectField(service, "stockOutService", createServiceProxy());
        injectField(service, "approvalRuntimeService", createApprovalProxy(null));

        Object reqVO = createSubmitReqVO(2L, Map.of());
        Method method = service.getClass().getMethod("submitStockOut", Long.class, reqVO.getClass());
        assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> method.invoke(service, 9527L, reqVO));
    }

    @Test
    void submitStockOut_shouldThrowWhenAlreadyProcessingWithProcessInstanceId() throws Exception {
        Object service = createService();
        ErpStockOutDO entity = new ErpStockOutDO()
                .setId(3L).setNo("SO-003")
                .setStatus(ErpAuditStatus.PROCESS.getStatus());
        entity.setProcessInstanceId("existing-pi-id");
        AtomicReference<ErpStockOutDO> entityRef = new AtomicReference<>(entity);

        injectField(service, "erpStockOutMapper", createMapperProxy(entityRef, new ArrayList<>()));
        injectField(service, "stockOutService", createServiceProxy());
        injectField(service, "approvalRuntimeService", createApprovalProxy(null));

        Object reqVO = createSubmitReqVO(3L, Map.of());
        Method method = service.getClass().getMethod("submitStockOut", Long.class, reqVO.getClass());
        assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> method.invoke(service, 9527L, reqVO));
    }

    @Test
    void submitStockOut_shouldThrowWhenDraftIsAlreadyClaimedByAnotherRequest() throws Exception {
        Object service = createService();
        AtomicReference<ErpStockOutDO> entityRef = new AtomicReference<>(
                new ErpStockOutDO().setId(9L).setNo("SO-009")
                        .setStatus(ErpAuditStatus.DRAFT.getStatus()));
        AtomicReference<String> submittedSceneCode = new AtomicReference<>();

        injectField(service, "erpStockOutMapper", createMapperClaimConflictProxy(entityRef));
        injectField(service, "stockOutService", createServiceProxy());
        injectField(service, "approvalRuntimeService", createApprovalProxy(submittedSceneCode));

        Object reqVO = createSubmitReqVO(9L, Map.of());
        Method method = service.getClass().getMethod("submitStockOut", Long.class, reqVO.getClass());

        assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> method.invoke(service, 9527L, reqVO));
        assertNull(submittedSceneCode.get());
    }

    // ========== cancel tests ==========

    @Test
    void cancelStockOutApproval_shouldCancelProcess() throws Exception {
        Object service = createService();
        ErpStockOutDO entity = new ErpStockOutDO()
                .setId(4L).setNo("SO-004")
                .setStatus(ErpAuditStatus.PROCESS.getStatus());
        entity.setProcessInstanceId("PI-TO-CANCEL");
        AtomicReference<ErpStockOutDO> entityRef = new AtomicReference<>(entity);
        AtomicReference<String> cancelSceneCode = new AtomicReference<>();

        injectField(service, "erpStockOutMapper", createMapperProxy(entityRef, new ArrayList<>()));
        injectField(service, "stockOutService", createServiceProxy());
        injectField(service, "approvalRuntimeService", createCancelProxy(cancelSceneCode));

        Object reqVO = createCancelReqVO(4L, "cancel test");
        Method method = service.getClass().getMethod("cancelStockOutApproval", Long.class, reqVO.getClass());
        method.invoke(service, 9527L, reqVO);

        assertEquals("erp.stock.out.submit", cancelSceneCode.get());
    }

    @Test
    void cancelStockOutApproval_shouldPropagateBpmCancelFailure() throws Exception {
        Object service = createService();
        ErpStockOutDO entity = new ErpStockOutDO()
                .setId(7L).setNo("SO-007")
                .setStatus(ErpAuditStatus.PROCESS.getStatus());
        entity.setProcessInstanceId("PI-CANCEL-FAILED");
        AtomicReference<ErpStockOutDO> entityRef = new AtomicReference<>(entity);

        injectField(service, "erpStockOutMapper", createMapperProxy(entityRef, new ArrayList<>()));
        injectField(service, "stockOutService", createServiceProxy());
        injectField(service, "approvalRuntimeService", createCancelFailureProxy());

        Object reqVO = createCancelReqVO(7L, "cancel failed");
        Method method = service.getClass().getMethod("cancelStockOutApproval", Long.class, reqVO.getClass());
        assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> method.invoke(service, 9527L, reqVO));
    }

    @Test
    void cancelStockOutApproval_shouldThrowWhenNotProcessing() throws Exception {
        Object service = createService();
        AtomicReference<ErpStockOutDO> entityRef = new AtomicReference<>(
                new ErpStockOutDO()
                        .setId(5L).setNo("SO-005")
                        .setStatus(ErpAuditStatus.DRAFT.getStatus()));

        injectField(service, "erpStockOutMapper", createMapperProxy(entityRef, new ArrayList<>()));
        injectField(service, "stockOutService", createServiceProxy());
        injectField(service, "approvalRuntimeService", createCancelProxy(null));

        Object reqVO = createCancelReqVO(5L, "reason");
        Method method = service.getClass().getMethod("cancelStockOutApproval", Long.class, reqVO.getClass());
        assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> method.invoke(service, 9527L, reqVO));
    }

    @Test
    void cancelStockOutApproval_shouldThrowWhenProcessInstanceIdBlank() throws Exception {
        Object service = createService();
        AtomicReference<ErpStockOutDO> entityRef = new AtomicReference<>(
                new ErpStockOutDO()
                        .setId(6L).setNo("SO-006")
                        .setStatus(ErpAuditStatus.PROCESS.getStatus()));

        injectField(service, "erpStockOutMapper", createMapperProxy(entityRef, new ArrayList<>()));
        injectField(service, "stockOutService", createServiceProxy());
        injectField(service, "approvalRuntimeService", createCancelProxy(null));

        Object reqVO = createCancelReqVO(6L, "reason");
        Method method = service.getClass().getMethod("cancelStockOutApproval", Long.class, reqVO.getClass());
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
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.service.stock.ErpStockOutBpmServiceImpl");
        return clazz.getDeclaredConstructor().newInstance();
    }

    private Object createSubmitReqVO(Long id, Map<String, List<Long>> assignees) throws Exception {
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.controller.admin.stock.vo.out.ErpStockOutSubmitReqVO");
        Object reqVO = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("setId", Long.class).invoke(reqVO, id);
        clazz.getMethod("setStartUserSelectAssignees", Map.class).invoke(reqVO, assignees);
        return reqVO;
    }

    private Object createCancelReqVO(Long id, String reason) throws Exception {
        Class<?> clazz = Class.forName("cn.weitee.erp.module.erp.controller.admin.stock.vo.out.ErpStockOutCancelApprovalReqVO");
        Object reqVO = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("setId", Long.class).invoke(reqVO, id);
        clazz.getMethod("setReason", String.class).invoke(reqVO, reason);
        return reqVO;
    }

    private Object createMapperProxy(AtomicReference<ErpStockOutDO> entityRef, List<int[]> updates) {
        return createProxy(ErpStockOutMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) return entityRef.get();
            if ("updateById".equals(methodName)) {
                ErpStockOutDO doObj = (ErpStockOutDO) args[0];
                updates.add(new int[]{doObj.getId().intValue()});
                return 1;
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                updates.add(new int[]{((Long) args[0]).intValue()});
                return 1;
            }
            if ("clearProcessInstanceId".equals(methodName)) return 1;
            return null;
        });
    }

    private Object createMapperObjectProxy(AtomicReference<ErpStockOutDO> entityRef, List<ErpStockOutDO> updates) {
        return createProxy(ErpStockOutMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) return entityRef.get();
            if ("updateById".equals(methodName)) {
                updates.add((ErpStockOutDO) args[0]);
                return 1;
            }
            if ("updateByIdAndStatus".equals(methodName)) {
                updates.add((ErpStockOutDO) args[2]);
                return 1;
            }
            if ("clearProcessInstanceId".equals(methodName)) return 1;
            return null;
        });
    }

    private Object createMapperClaimConflictProxy(AtomicReference<ErpStockOutDO> entityRef) {
        return createProxy(ErpStockOutMapper.class, (methodName, args) -> {
            if ("selectById".equals(methodName)) return entityRef.get();
            if ("updateByIdAndStatus".equals(methodName)) return 0;
            return null;
        });
    }

    private Object createServiceProxy() {
        return createProxy(ErpStockOutService.class, (methodName, args) -> null);
    }

    private Object createApprovalProxy(AtomicReference<String> sceneCodeRef) {
        return createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> {
            if ("submit".equals(methodName)) {
                if (sceneCodeRef != null) sceneCodeRef.set((String) args[0]);
                return "SO-PI-20260629-001";
            }
            if ("cancel".equals(methodName)) {
                if (sceneCodeRef != null) sceneCodeRef.set((String) args[0]);
            }
            return null;
        });
    }

    private Object createApprovalFailureProxy() {
        return createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> {
            if ("submit".equals(methodName)) {
                throw new IllegalStateException("flowable submit failed");
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

    private Object createCancelFailureProxy() {
        return createProxy(BpmApprovalRuntimeService.class, (methodName, args) -> {
            if ("cancel".equals(methodName)) {
                throw new IllegalStateException("flowable cancel failed");
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
