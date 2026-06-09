# 审批平台与双账套成本变更存在阻断问题，暂不建议合并

## 背景

- 分支：`tester/backend-frontend-fusion`
- 审查范围：`abb02e7288eeba85683f2931f777e9400e18a87e..8b9cf5593db4275def0d4f42b2fe410ffcb6aac3`
- 相关提交：`feat(erp): add approval platform foundation and dual cost flows`

## 阻断问题

### 1. 审批提交接口返回的流程实例 ID 基本为 null

- 文件：`yudao-module-bpm/src/main/java/cn/iocoder/yudao/module/bpm/service/approval/BpmApprovalRuntimeServiceImpl.java:176-186`
- 问题：`submit()` 在 `afterCommit()` 中异步创建流程实例，却在方法结束时立即返回 `processInstanceIdRef.get()`。
- 影响：调用方拿不到有效流程实例 ID；若后续逻辑依赖返回值，会直接失效。
- 建议：改为同步创建流程实例，或返回本地快照 ID / 明确定义返回值语义，不要返回几乎必定为空的值。

### 2. 审批快照状态先落库、业务处理后执行，且不在同一事务内

- 文件：`yudao-module-bpm/src/main/java/cn/iocoder/yudao/module/bpm/service/approval/BpmApprovalEventDispatcher.java:104-112,128-135`
- 问题：先更新快照状态，再调用业务结果处理器；两者未纳入同一事务。
- 影响：若业务 handler 抛错，会出现“快照已完成、业务未处理”的分叉状态；后续事件重放还会被幂等判断跳过，难以恢复。
- 建议：先保证业务处理成功再更新快照，或将快照更新与业务处理纳入同一事务，并设计失败重试。

### 3. 产品双账套单产品重算未按产品维度过滤

- 文件：`yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/service/finance/ErpFinanceDualProductCostServiceImpl.java:127-217`
- 问题：`rebuildProductDualCost()` 查询期间内所有自制/委外入库凭证，没有按 `productId / productionOrderId / productBatchNo` 过滤。
- 影响：单产品重算会混入同期间其他产品数据，导致计算结果错误并污染结果表。
- 建议：源数据查询必须严格按重算范围过滤，并保证结果维度与查询条件一致。

### 4. 产品双账套批量重算逻辑错误地使用 null/0 占位产品 ID

- 文件：`yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/service/finance/ErpFinanceDualProductCostServiceImpl.java:275-287`
- 问题：`rebuildBatchByPeriod()` 先把产品 ID 映射成 `null`，再塞一个 `0L` 触发重跑。
- 影响：批量重算基本失效，且可能生成错误的 `productId = 0` 成本结果。
- 建议：从真实源数据中提取产品 ID，禁止使用 `null/0` 作为占位键。

## 重要问题

### 5. BPM 撤回失败后仍继续执行业务撤回

- 文件：`yudao-module-bpm/src/main/java/cn/iocoder/yudao/module/bpm/service/approval/BpmApprovalRuntimeServiceImpl.java:231-253`
- 问题：`cancel()` 中 BPM 撤回失败只记录日志，仍继续执行 `handler.onCancel()`。
- 影响：BPM 实例和业务单据状态可能分叉。
- 建议：撤回失败应中断流程或进入补偿/重试，而不是继续业务侧取消。

### 6. 双账结果重算编排缺少事务边界

- 文件：`yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/service/finance/ErpFinanceDualLedgerResultServiceImpl.java:111-114`
- 问题：先重算凭证，再重算双账结果，但方法缺少统一事务或补偿策略。
- 影响：中途失败会留下半更新数据，导致凭证与结果状态不一致。
- 建议：增加事务边界，或设计为可恢复的编排流程。

## 建议补测

- `submit()` 的返回值与 `afterCommit` 异常路径
- 审批事件分发时 handler 异常、重试与幂等场景
- 产品双账套单产品重算的过滤条件
- 批量重算空集合、错误范围和重复执行场景
- `cancel()` 在 BPM 撤回失败时的状态一致性

## 结论

当前变更存在会导致结果错误或状态分叉的阻断缺陷，暂不建议合并，需修复后再复审。
