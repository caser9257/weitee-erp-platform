package cn.weitee.erp.module.bpm.service.approval;

import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalInstanceSnapshotDO;
import cn.weitee.erp.module.bpm.dal.mysql.approval.BpmApprovalInstanceSnapshotMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BpmApprovalInstanceSnapshotServiceImplTest {

    @Test
    void getEffectiveSnapshotBySceneCodeAndBizId_shouldUseEffectiveQuery() throws Exception {
        BpmApprovalInstanceSnapshotServiceImpl service = new BpmApprovalInstanceSnapshotServiceImpl();
        AtomicReference<String> queriedSceneCodeRef = new AtomicReference<>();
        AtomicReference<String> queriedBizIdRef = new AtomicReference<>();

        setField(service, "approvalInstanceSnapshotMapper",
                createProxy(BpmApprovalInstanceSnapshotMapper.class, (methodName, args) -> {
                    if ("selectEffectiveBySceneCodeAndBizId".equals(methodName)) {
                        queriedSceneCodeRef.set((String) args[0]);
                        queriedBizIdRef.set((String) args[1]);
                        return BpmApprovalInstanceSnapshotDO.builder()
                                .id(22L)
                                .sceneCode((String) args[0])
                                .bizId((String) args[1])
                                .status(1)
                                .processInstanceId("PROC-22")
                                .build();
                    }
                    if ("selectBySceneCodeAndBizId".equals(methodName)) {
                        throw new IllegalStateException("raw snapshot query should not be used");
                    }
                    return null;
                }));

        BpmApprovalInstanceSnapshotDO snapshot =
                service.getEffectiveSnapshotBySceneCodeAndBizId("erp.finance.payment.submit", "990001");

        assertNotNull(snapshot);
        assertEquals("erp.finance.payment.submit", queriedSceneCodeRef.get());
        assertEquals("990001", queriedBizIdRef.get());
        assertEquals(22L, snapshot.getId());
    }

    @Test
    void deleteSnapshot_shouldUseHardDeleteInsteadOfLogicDelete() throws Exception {
        BpmApprovalInstanceSnapshotServiceImpl service = new BpmApprovalInstanceSnapshotServiceImpl();
        AtomicBoolean hardDeleteCalledRef = new AtomicBoolean(false);

        setField(service, "approvalInstanceSnapshotMapper",
                createProxy(BpmApprovalInstanceSnapshotMapper.class, (methodName, args) -> {
                    if ("selectById".equals(methodName)) {
                        return BpmApprovalInstanceSnapshotDO.builder()
                                .id((Long) args[0])
                                .sceneCode("erp.finance.payment.submit")
                                .bizId("981506")
                                .build();
                    }
                    if ("deleteById".equals(methodName)) {
                        throw new IllegalStateException("logic delete should not be used");
                    }
                    if ("hardDeleteById".equals(methodName)) {
                        hardDeleteCalledRef.set(true);
                        return 1;
                    }
                    return null;
                }));

        service.deleteSnapshot(16L);

        assertEquals(true, hardDeleteCalledRef.get());
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
        Object handle(String methodName, Object[] args) throws Exception;
    }
}
