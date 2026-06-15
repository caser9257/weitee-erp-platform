---
name: domain-state-machine
description: 状态机与事务设计知识 — 状态机建模、事务编排、跨系统一致性、事件驱动架构
license: MIT
compatibility: tester
metadata:
  category: domain-knowledge
  topics: state-machine,transaction,event-driven,saga,compensation,idempotency
---

## 状态机建模

### 状态定义原则

```
状态枚举设计：
├── 每个状态必须有明确的业务含义
├── 状态值必须是有限集合，不允许运行时动态生成
├── 终态（Terminal State）必须与中间态明确区分
└── 异常状态（FAILED/ERROR）必须独立于业务状态

示例：
enum SnapshotStatus {
    PROCESSING(1, "审批中"),    // 中间态
    APPROVE(2, "已通过"),       // 终态
    REJECT(3, "已驳回"),        // 终态
    CANCEL(4, "已撤回"),        // 终态
    FAILED(5, "处理失败");      // 异常态（可重试）
}
```

### 状态流转图

```
标准审批状态机：
                    ┌─────────────┐
                    │   INIT      │
                    └──────┬──────┘
                           │ submit()
                           ▼
                    ┌─────────────┐
          ┌────────│ PROCESSING  │────────┐
          │        └──────┬──────┘        │
          │               │               │
    cancel()         approve()       reject()
          │               │               │
          ▼               ▼               ▼
    ┌─────────┐    ┌─────────┐    ┌─────────┐
    │ CANCEL  │    │ APPROVE │    │ REJECT  │
    └─────────┘    └─────────┘    └─────────┘
    (终态)         (终态)         (终态)

异常路径：
    PROCESSING ──失败──→ FAILED ──重试──→ PROCESSING
```

### 状态流转约束

```java
// 状态转换矩阵
Map<Status, Set<Status>> transitions = Map.of(
    INIT,       Set.of(PROCESSING),
    PROCESSING, Set.of(APPROVE, REJECT, CANCEL, FAILED),
    FAILED,     Set.of(PROCESSING),  // 允许重试
    APPROVE,    Set.of(),            // 终态，不允许转出
    REJECT,     Set.of(),            // 终态，不允许转出
    CANCEL,     Set.of()             // 终态，不允许转出
);

// 校验方法
public boolean canTransition(Status from, Status to) {
    return transitions.getOrDefault(from, Set.of()).contains(to);
}
```

## 事务编排模式

### 模式 1：本地事务 + 异步外部调用

**适用场景**：本地状态变更 + 调外部系统（BPM、支付、通知）

```java
@Transactional(rollbackFor = Exception.class)
public void submit(Long bizId) {
    // 事务内：只做本地操作
    validatePreConditions(bizId);
    SnapshotDO snapshot = buildSnapshot(bizId);
    snapshot.setStatus(PROCESSING);
    snapshotService.create(snapshot);
    auditLogService.log(bizId, "SUBMIT");

    // 事务外：调外部系统
    TransactionSynchronization.afterCommit(() -> {
        try {
            String instanceId = bpmApi.create(snapshot);
            snapshotService.updateInstanceId(snapshot.getId(), instanceId);
        } catch (Exception e) {
            snapshotService.markFailed(snapshot.getId(), e.getMessage());
        }
    });
}
```

### 模式 2：事件驱动 + 统一分发

**适用场景**：多个业务需要响应同一事件

```java
// 事件发布方
@Transactional(rollbackFor = Exception.class)
public void approve(Long bizId) {
    handler.onApprove(bizId);  // 先执行业务
    snapshotService.updateStatus(APPROVE);  // 后落终态
}

// 事件消费方（统一分发器）
@Component
public class EventDispatcher implements ApplicationListener<StatusEvent> {
    @Override
    public void onApplicationEvent(StatusEvent event) {
        SnapshotDO snapshot = snapshotService.getByInstanceId(event.getInstanceId());

        // 幂等检查
        if (snapshot.getStatus() == translateStatus(event.getStatus())
            && snapshot.getBizCompleted()) {
            return;  // 真正完成才跳过
        }

        // 先业务后终态
        ResultHandler handler = handlerMap.get(snapshot.getSceneCode());
        handler.onResult(snapshot.getBizId(), event);
        snapshotService.updateStatus(snapshot.getId(), translateStatus(event.getStatus()));
        snapshotService.markBizCompleted(snapshot.getId());
    }
}
```

### 模式 3：补偿事务（Saga Pattern）

**适用场景**：跨多个服务的长事务

```java
// 编排型事务
public void executeSaga(Long bizId) {
    String compensationId = UUID.randomUUID().toString();

    try {
        // Step 1：扣库存
        inventoryService.deduct(bizId);
        compensationLog.log(compensationId, "INVENTORY", "DEDUCT");

        // Step 2：创建订单
        orderService.create(bizId);
        compensationLog.log(compensationId, "ORDER", "CREATE");

        // Step 3：调支付
        paymentService.pay(bizId);
        compensationLog.log(compensationId, "PAYMENT", "PAY");

    } catch (Exception e) {
        // 补偿：按逆序回滚
        compensate(compensationId);
        throw e;
    }
}

private void compensate(String compensationId) {
    List<CompensationRecord> records = compensationLog.getByCompensationId(compensationId);
    for (CompensationRecord record : records.reverse()) {
        try {
            switch (record.getAction()) {
                case "DEDUCT" -> inventoryService.restore(record.getBizId());
                case "CREATE" -> orderService.cancel(record.getBizId());
                case "PAY"    -> paymentService.refund(record.getBizId());
            }
        } catch (Exception e) {
            log.error("补偿失败：{}", record, e);
            // 补偿失败需要人工介入
        }
    }
}
```

## 跨系统一致性

### 本地消息表模式

**适用场景**：保证本地事务 + 消息发送的一致性

```java
@Transactional(rollbackFor = Exception.class)
public void submit(Long bizId) {
    // 1. 本地业务操作
    orderService.updateStatus(bizId, PROCESSING);

    // 2. 写本地消息表（同一事务）
    outboxService.save(new OutboxMessage()
        .setTopic("order.submitted")
        .setPayload(JSON.toJSONString(bizId))
        .setStatus(PENDING));
}

// 定时任务扫描 outbox 表，发送消息
@Scheduled(fixedDelay = 5000)
public void processOutbox() {
    List<OutboxMessage> messages = outboxService.findByStatus(PENDING);
    for (OutboxMessage msg : messages) {
        try {
            mq.send(msg.getTopic(), msg.getPayload());
            outboxService.updateStatus(msg.getId(), SENT);
        } catch (Exception e) {
            outboxService.updateStatus(msg.getId(), FAILED);
        }
    }
}
```

### 幂等消费

```java
@Component
public class OrderEventListener {

    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void onOrderSubmitted(OrderSubmittedEvent event) {
        // 幂等检查
        if (processedEventService.exists(event.getEventId())) {
            return;  // 已处理，跳过
        }

        // 业务处理
        inventoryService.reserve(event.getOrderId());

        // 记录已处理（同一事务）
        processedEventService.save(event.getEventId());
    }
}
```

## 设计模式速查

### 状态机模式

```java
// 使用 Spring StateMachine 或手写状态机
public class StateMachine<S, E> {
    private S currentState;
    private Map<S, Map<E, Transition<S, E>>> transitions;

    public S fire(E event) {
        Transition<S, E> transition = transitions
            .get(currentState)
            .get(event);
        if (transition == null) {
            throw new IllegalStateException(
                "No transition from " + currentState + " on " + event);
        }
        transition.getAction().execute();
        currentState = transition.getTarget();
        return currentState;
    }
}
```

### 策略模式（审批结果处理）

```java
// 统一接口
public interface ApprovalResultHandler {
    String getSceneCode();
    void onApprove(Long bizId, String instanceId, String reason);
    void onReject(Long bizId, String instanceId, String reason);
    void onCancel(Long bizId, String instanceId, String reason);
}

// 策略注册
@Resource
public void setResultHandlers(List<ApprovalResultHandler> handlers) {
    this.handlerMap = CollectionUtils.convertMap(handlers, ApprovalResultHandler::getSceneCode);
}

// 策略使用
ApprovalResultHandler handler = handlerMap.get(sceneCode);
if (handler != null) {
    handler.onApprove(bizId, instanceId, reason);
}
```

### 模板方法模式（通用审批流程）

```java
public abstract class AbstractApprovalService {

    // 模板方法
    public final void submit(Long bizId, Long userId) {
        validatePreConditions(bizId);          // 钩子：子类实现
        ApprovalContext context = buildContext(bizId);  // 钩子：子类实现
        SnapshotDO snapshot = createSnapshot(context);
        afterSnapshotCreated(snapshot);        // 钩子：子类实现
        TransactionSynchronization.afterCommit(() -> {
            createBpmInstance(snapshot);
        });
    }

    protected abstract void validatePreConditions(Long bizId);
    protected abstract ApprovalContext buildContext(Long bizId);
    protected void afterSnapshotCreated(SnapshotDO snapshot) {
        // 默认空实现，子类可选覆盖
    }
}
```

## 常见陷阱

### 陷阱 1：事务内调外部系统

```java
// ❌ 事务内调 BPM，BPM 超时导致事务回滚
@Transactional
public void submit() {
    snapshotService.create(snapshot);
    bpmApi.createProcessInstance(...);  // 可能超时
}

// ✅ 事务外调用
@Transactional
public void submit() {
    snapshotService.create(snapshot);
    TransactionSynchronization.afterCommit(() -> {
        bpmApi.createProcessInstance(...);
    });
}
```

### 陷阱 2：先落终态再调业务

```java
// ❌ 终态先落，业务失败无法恢复
public void handleApprove() {
    snapshotService.updateStatus(APPROVE);
    handler.onApprove(bizId);  // 可能失败
}

// ✅ 先业务后终态
public void handleApprove() {
    handler.onApprove(bizId);
    snapshotService.updateStatus(APPROVE);
}
```

### 陷阱 3：幂等阻断重试

```java
// ❌ 终态已落直接 return，handler 失败后重试被拦住
if (snapshot.getStatus() == APPROVE) {
    return;
}

// ✅ 区分真正完成
if (snapshot.getStatus() == APPROVE && snapshot.getBizCompleted()) {
    return;
}
```

### 陷阱 4：吞异常导致事务不回滚

```java
// ❌ catch 吞异常，事务不回滚
@Transactional
public void submit() {
    try {
        doSomething();
    } catch (Exception e) {
        log.error("error", e);  // 事务不回滚！
    }
}

// ✅ 重新抛出或标记回滚
@Transactional(rollbackFor = Exception.class)
public void submit() {
    try {
        doSomething();
    } catch (Exception e) {
        log.error("error", e);
        throw e;  // 触发回滚
    }
}
```
