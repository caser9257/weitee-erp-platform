package cn.weitee.erp.module.bpm.service.approval;

import cn.weitee.erp.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalInstanceSnapshotDO;
import cn.weitee.erp.module.bpm.enums.approval.BpmApprovalInstanceSnapshotStatusEnum;
import cn.weitee.erp.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.weitee.erp.module.bpm.service.approval.BpmApprovalRecordService;
import cn.weitee.erp.module.bpm.service.approval.handler.ApprovalResultHandler;
import cn.weitee.erp.module.bpm.service.message.BpmMessageService;
import cn.weitee.erp.module.bpm.service.message.dto.BpmMessageSendWhenProcessInstanceApproveReqDTO;
import cn.weitee.erp.module.bpm.service.message.dto.BpmMessageSendWhenProcessInstanceRejectReqDTO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 审批事件分发器单元测试
 */
class BpmApprovalEventDispatcherTest {

    // ========== handleApprove 测试 ==========

    @Test
    void handleApprove_handlerFails_snapshotShouldNotBeTerminal() throws Exception {
        // 模拟 handler.onApprove 抛异常
        AtomicInteger snapshotUpdateCount = new AtomicInteger(0);
        AtomicReference<Integer> snapshotUpdatedStatus = new AtomicReference<>();

        BpmApprovalEventDispatcher dispatcher = new BpmApprovalEventDispatcher();

        // Mock snapshotService
        setField(dispatcher, "approvalInstanceSnapshotService", createProxy(
                BpmApprovalInstanceSnapshotService.class,
                (methodName, args) -> {
                    if ("getSnapshotByProcessInstanceId".equals(methodName)) {
                        return BpmApprovalInstanceSnapshotDO.builder()
                                .id(1L).sceneCode("test.scene").bizId("100")
                                .processInstanceId("proc-1").status(BpmApprovalInstanceSnapshotStatusEnum.PROCESSING.getStatus())
                                .build();
                    }
                    if ("updateSnapshotStatus".equals(methodName)) {
                        snapshotUpdateCount.incrementAndGet();
                        snapshotUpdatedStatus.set((Integer) args[1]);
                        return null;
                    }
                    return null;
                }));

        // Mock handler：onApprove 抛异常
        setField(dispatcher, "resultHandlerMap", Map.of("test.scene",
                createProxy(ApprovalResultHandler.class, (methodName, args) -> {
                    if ("onApprove".equals(methodName)) {
                        throw new RuntimeException("业务处理失败");
                    }
                    return null;
                })));

        // Mock messageService
        setField(dispatcher, "messageService", createProxy(BpmMessageService.class,
                (methodName, args) -> null));

        // 构造 APPROVE 事件
        BpmProcessInstanceStatusEvent event = new BpmProcessInstanceStatusEvent(this);
        event.setId("proc-1");
        event.setProcessDefinitionKey("test-process");
        event.setStatus(BpmProcessInstanceStatusEnum.APPROVE.getStatus());

        // 执行：handler 异常被内部捕获，不会抛出
        dispatcher.onApplicationEvent(event);

        // 关键验证：snapshot 不应该被更新为 APPROVE 终态
        assertEquals(0, snapshotUpdateCount.get(), "handler 失败时 snapshot 不应被更新");
    }

    @Test
    void handleApprove_handlerSucceeds_snapshotUpdatedToApprove() throws Exception {
        AtomicInteger snapshotUpdateCount = new AtomicInteger(0);
        AtomicReference<Integer> snapshotUpdatedStatus = new AtomicReference<>();

        BpmApprovalEventDispatcher dispatcher = new BpmApprovalEventDispatcher();

        setField(dispatcher, "approvalInstanceSnapshotService", createProxy(
                BpmApprovalInstanceSnapshotService.class,
                (methodName, args) -> {
                    if ("getSnapshotByProcessInstanceId".equals(methodName)) {
                        return BpmApprovalInstanceSnapshotDO.builder()
                                .id(1L).sceneCode("test.scene").bizId("100")
                                .processInstanceId("proc-1").status(BpmApprovalInstanceSnapshotStatusEnum.PROCESSING.getStatus())
                                .build();
                    }
                    if ("updateSnapshotStatus".equals(methodName)) {
                        snapshotUpdateCount.incrementAndGet();
                        snapshotUpdatedStatus.set((Integer) args[1]);
                        return null;
                    }
                    return null;
                }));

        setField(dispatcher, "resultHandlerMap", Map.of("test.scene",
                createProxy(ApprovalResultHandler.class, (methodName, args) -> null)));

        setField(dispatcher, "messageService", createProxy(BpmMessageService.class,
                (methodName, args) -> null));

        BpmProcessInstanceStatusEvent event = new BpmProcessInstanceStatusEvent(this);
        event.setId("proc-1");
        event.setProcessDefinitionKey("test-process");
        event.setStatus(BpmProcessInstanceStatusEnum.APPROVE.getStatus());

        dispatcher.onApplicationEvent(event);

        assertEquals(1, snapshotUpdateCount.get(), "handler 成功后 snapshot 应被更新");
        assertEquals(BpmApprovalInstanceSnapshotStatusEnum.APPROVE.getStatus(), snapshotUpdatedStatus.get());
    }

    // ========== handleReject 测试 ==========

    @Test
    void handleReject_handlerFails_snapshotShouldNotBeTerminal() throws Exception {
        AtomicInteger snapshotUpdateCount = new AtomicInteger(0);

        BpmApprovalEventDispatcher dispatcher = new BpmApprovalEventDispatcher();

        setField(dispatcher, "approvalInstanceSnapshotService", createProxy(
                BpmApprovalInstanceSnapshotService.class,
                (methodName, args) -> {
                    if ("getSnapshotByProcessInstanceId".equals(methodName)) {
                        return BpmApprovalInstanceSnapshotDO.builder()
                                .id(1L).sceneCode("test.scene").bizId("100")
                                .processInstanceId("proc-1").status(BpmApprovalInstanceSnapshotStatusEnum.PROCESSING.getStatus())
                                .build();
                    }
                    if ("updateSnapshotStatus".equals(methodName)) {
                        snapshotUpdateCount.incrementAndGet();
                        return null;
                    }
                    return null;
                }));

        setField(dispatcher, "resultHandlerMap", Map.of("test.scene",
                createProxy(ApprovalResultHandler.class, (methodName, args) -> {
                    if ("onReject".equals(methodName)) {
                        throw new RuntimeException("业务处理失败");
                    }
                    return null;
                })));

        setField(dispatcher, "messageService", createProxy(BpmMessageService.class,
                (methodName, args) -> null));

        BpmProcessInstanceStatusEvent event = new BpmProcessInstanceStatusEvent(this);
        event.setId("proc-1");
        event.setProcessDefinitionKey("test-process");
        event.setStatus(BpmProcessInstanceStatusEnum.REJECT.getStatus());

        // handler 异常被内部捕获，不会抛出
        dispatcher.onApplicationEvent(event);
        assertEquals(0, snapshotUpdateCount.get(), "handler 失败时 snapshot 不应被更新");
    }

    // ========== handleCancel 幂等测试 ==========

    @Test
    void handleCancel_alreadyCancelled_shouldSkip() throws Exception {
        AtomicInteger handlerCallCount = new AtomicInteger(0);

        BpmApprovalEventDispatcher dispatcher = new BpmApprovalEventDispatcher();

        // 快照已经是 CANCEL 状态
        setField(dispatcher, "approvalInstanceSnapshotService", createProxy(
                BpmApprovalInstanceSnapshotService.class,
                (methodName, args) -> {
                    if ("getSnapshotByProcessInstanceId".equals(methodName)) {
                        return BpmApprovalInstanceSnapshotDO.builder()
                                .id(1L).sceneCode("test.scene").bizId("100")
                                .processInstanceId("proc-1").status(BpmApprovalInstanceSnapshotStatusEnum.CANCEL.getStatus())
                                .build();
                    }
                    return null;
                }));

        setField(dispatcher, "resultHandlerMap", Map.of("test.scene",
                createProxy(ApprovalResultHandler.class, (methodName, args) -> {
                    handlerCallCount.incrementAndGet();
                    return null;
                })));

        setField(dispatcher, "messageService", createProxy(BpmMessageService.class,
                (methodName, args) -> null));

        BpmProcessInstanceStatusEvent event = new BpmProcessInstanceStatusEvent(this);
        event.setId("proc-1");
        event.setProcessDefinitionKey("test-process");
        event.setStatus(BpmProcessInstanceStatusEnum.CANCEL.getStatus());

        dispatcher.onApplicationEvent(event);

        // 幂等：快照已终态时，handler 不应被调用
        assertEquals(0, handlerCallCount.get(), "快照已 CANCEL 时，handler 不应被调用");
    }

    @Test
    void handleCancel_processing_shouldUpdateAndCallHandler() throws Exception {
        AtomicInteger handlerCallCount = new AtomicInteger(0);
        AtomicInteger snapshotUpdateCount = new AtomicInteger(0);

        BpmApprovalEventDispatcher dispatcher = new BpmApprovalEventDispatcher();

        setField(dispatcher, "approvalInstanceSnapshotService", createProxy(
                BpmApprovalInstanceSnapshotService.class,
                (methodName, args) -> {
                    if ("getSnapshotByProcessInstanceId".equals(methodName)) {
                        return BpmApprovalInstanceSnapshotDO.builder()
                                .id(1L).sceneCode("test.scene").bizId("100")
                                .processInstanceId("proc-1").status(BpmApprovalInstanceSnapshotStatusEnum.PROCESSING.getStatus())
                                .build();
                    }
                    if ("updateSnapshotStatus".equals(methodName)) {
                        snapshotUpdateCount.incrementAndGet();
                        return null;
                    }
                    return null;
                }));

        setField(dispatcher, "resultHandlerMap", Map.of("test.scene",
                createProxy(ApprovalResultHandler.class, (methodName, args) -> {
                    handlerCallCount.incrementAndGet();
                    return null;
                })));

        setField(dispatcher, "messageService", createProxy(BpmMessageService.class,
                (methodName, args) -> null));

        setField(dispatcher, "approvalRecordService", createProxy(
                BpmApprovalRecordService.class, (methodName, args) -> null));

        BpmProcessInstanceStatusEvent event = new BpmProcessInstanceStatusEvent(this);
        event.setId("proc-1");
        event.setProcessDefinitionKey("test-process");
        event.setStatus(BpmProcessInstanceStatusEnum.CANCEL.getStatus());

        dispatcher.onApplicationEvent(event);

        assertEquals(1, handlerCallCount.get(), "handler 应被调用一次");
        assertEquals(1, snapshotUpdateCount.get(), "快照应从 PROCESSING 更新为 CANCEL");
    }

    // ========== 辅助方法 ==========

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
