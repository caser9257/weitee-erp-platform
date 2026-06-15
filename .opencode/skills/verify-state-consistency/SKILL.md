---
name: verify-state-consistency
description: 状态一致性关卡 — 检查事务边界、终态写入顺序、失败补偿、幂等设计、单一路径
license: MIT
compatibility: tester
metadata:
  category: quality-gate
  severity: critical
---

## 触发条件

当代码变更涉及以下任意关键词时，**必须**加载并执行本 skill：

- 审批、审批流、BPM、流程实例、审批结果
- 状态流转、状态机、状态变更、状态回写
- 事务、@Transactional、事务边界、事务编排
- 提交/撤回/通过/驳回等业务动作
- 跨系统调用（ERP ↔ BPM、ERP ↔ 支付、ERP ↔ 外部 API）
- 事件分发、事件监听、Spring Event、MQ 消费
- 快照、补偿、重试、幂等

## 检查清单

### 1. 事务边界（Transaction Boundary）

- [ ] 事务内是否只做本地操作（DB 读写、本地状态变更）
- [ ] 外部系统调用（HTTP/RPC/MQ）是否在事务外（afterCommit / 异步）
- [ ] 事务内是否存在可能导致回滚的外部调用
- [ ] `@Transactional` 是否指定了 `rollbackFor = Exception.class`
- [ ] 是否存在事务失效场景：同类内部调用、try-catch 吞异常

**检查方法**：
```
grep -n "@Transactional" <文件>
grep -n "afterCommit\|TransactionSynchronization" <文件>
grep -n "http\|rpc\|send\|publish\|api\." <文件>  # 查找外部调用
```

### 2. 终态写入顺序（Terminal State Order）

- [ ] 是否先调业务 handler，后落终态状态
- [ ] 终态写入是否在业务操作成功之后
- [ ] 是否存在"先落终态再调业务"的反序模式
- [ ] 业务 handler 失败时，状态是否保持可重试

**反模式识别**：
```java
// ❌ 反模式：先落终态
updateStatus(APPROVE);   // 终态已落
handler.onApprove(bizId); // 业务可能失败

// ✅ 正确：先业务后终态
handler.onApprove(bizId); // 业务先执行
updateStatus(APPROVE);    // 成功后才落终态
```

### 3. 单一路径（Single Path）

- [ ] 终态写入是否只有一个入口
- [ ] 业务回调（handler）是否只有一个调用点
- [ ] 是否存在多个方法都写同一终态的情况
- [ ] 是否存在多个方法都调同一 handler 的情况

**检查方法**：
```bash
# 搜索所有写终态的地方
grep -rn "updateStatus\|setStatus.*APPROVE\|setStatus.*REJECT\|setStatus.*CANCEL" <目录>
# 搜索所有调 handler 的地方
grep -rn "handler\.on\|resultHandler\.on" <目录>
```

### 4. 失败补偿（Failure Compensation）

- [ ] 外部调用失败后，本地状态是否标记为 FAILED/待重试
- [ ] 是否有定时重试机制或手动重试入口
- [ ] 失败后是否留下不可恢复的脏状态（如 PROCESSING + 无 processInstanceId）
- [ ] 补偿操作是否幂等

**脏状态识别**：
```java
// ❌ 脏状态：PROCESSING 但没有 processInstanceId
snapshot.setStatus(PROCESSING);
snapshot.setProcessInstanceId(null);  // BPM 创建失败
// snapshot 已提交到数据库，无法回滚

// ✅ 安全状态：标记失败
snapshot.setStatus(FAILED);
snapshot.setFailReason(e.getMessage());
```

### 5. 幂等设计（Idempotency）

- [ ] 幂等判断是否会阻断失败后的重试
- [ ] 是否区分"已完成的终态"和"handler 失败的伪终态"
- [ ] 重复事件是否被正确忽略
- [ ] 是否有 bizCompleted 标记区分真正完成

**阻断重试识别**：
```java
// ❌ 阻断重试：handler 失败后重试会被拦住
if (snapshot.getStatus() == APPROVE) {
    return;  // 如果是 handler 失败后重试，这里会拦住
}

// ✅ 安全判断：区分真正完成
if (snapshot.getStatus() == APPROVE && snapshot.getBizCompleted()) {
    return;
}
```

### 6. 调用方同步（Caller Sync）

- [ ] 接口返回值语义是否与调用方期望一致
- [ ] 返回类型变更（如 String → void）后，调用方是否同步修改
- [ ] 异常传播策略是否与调用方一致
- [ ] 是否存在调用方依赖未定义行为的情况

### 7. 测试覆盖（Test Coverage）

- [ ] 是否有单元测试覆盖正常路径
- [ ] 是否有单元测试覆盖 handler 失败路径
- [ ] 是否有单元测试覆盖幂等路径
- [ ] 是否有单元测试覆盖外部调用失败路径
- [ ] 测试是否验证了状态变更顺序

## 输出格式

```
## State Consistency Report
Score: PASS / WARN / FAIL

### TRANSACTION_BOUNDARY
- [PASS/WARN/FAIL] 具体发现

### TERMINAL_STATE_ORDER
- [PASS/WARN/FAIL] 具体发现

### SINGLE_PATH
- [PASS/WARN/FAIL] 具体发现

### FAILURE_COMPENSATION
- [PASS/WARN/FAIL] 具体发现

### IDEMPOTENCY
- [PASS/WARN/FAIL] 具体发现

### CALLER_SYNC
- [PASS/WARN/FAIL] 具体发现

### TEST_COVERAGE
- [PASS/WARN/FAIL] 具体发现

### MUST_FIX (如有)
1. 问题描述 + 文件位置 + 修复建议

### RECOMMENDED (如有)
1. 建议描述
```

## 历史教训

本 skill 的检查项来源于以下真实问题：

1. **submit() 返回契约不清**：`afterCommit()` 中创建 BPM 实例，返回值语义不明确，调用方无法确定是否成功
2. **cancel() 双写终态**：`cancel()` 和 `handleCancel()` 都写终态、都调 handler，导致重复执行
3. **handleApprove() 先落终态**：先更新 snapshot 为 APPROVE，再调 handler，handler 失败时无法恢复
4. **幂等阻断重试**：终态已落时直接 return，handler 失败后重试被拦住
5. **脏状态残留**：PROCESSING + processInstanceId 为空，无法恢复
