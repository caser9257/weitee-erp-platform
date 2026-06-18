# 后端代码生成检查清单

## 触发条件
当生成或修改以下类型的后端代码时，必须加载此 skill：
- Service 实现类（*ServiceImpl.java）
- 包含 `@Transactional` 注解的方法
- 循环中调用 Mapper/Service 查询的代码
- 调用外部 API（其他模块 Service、远程调用）的代码

## 强制检查清单（HARD STOP）

在输出任何后端代码之前，必须逐项检查：

### ✅ 检查项 1：事务边界内的外部调用

**规则**：`@Transactional` 方法内禁止调用以下类型的外部操作：
- 其他模块的 Service 方法（跨模块调用）
- 远程 API 调用（HTTP、RPC、gRPC）
- 消息队列发送（MQ、Spring Event）
- 文件操作（读写外部存储）
- 缓存操作（Redis，除非是本地事务的一部分）

**正确模式**：
```java
@Transactional(rollbackFor = Exception.class)
public void bizOperation() {
    // ✅ 事务内：只做本地数据库操作
    localMapper.insert(record);
    localService.updateStatus(id, status);
    
    // ✅ 事务外：外部调用放在 afterCommit
    TransactionSynchronization.afterCommit(() -> {
        try {
            externalApi.call(record);
        } catch (Exception e) {
            log.error("[bizOperation] 外部调用失败", e);
            // 补偿逻辑
        }
    });
}
```

**错误模式（必须拒绝）**：
```java
@Transactional(rollbackFor = Exception.class)
public void bizOperation() {
    localMapper.insert(record);
    externalApi.call(record);  // ❌ 事务内调外部 API
}
```

### ✅ 检查项 2：循环内的查询操作（N+1 预防）

**规则**：禁止在 `for`/`forEach`/`stream` 循环内调用以下查询方法：
- Mapper 的 select 方法
- Service 的 get/query 方法
- 任何返回 List/DO/VO 的查询

**正确模式**：
```java
// ✅ 循环外批量查询
Set<Long> ids = items.stream()
    .map(Item::getRefId)
    .collect(Collectors.toSet());
Map<Long, RefDO> refMap = refMapper.selectBatchIds(ids).stream()
    .collect(Collectors.toMap(RefDO::getId, Function.identity()));

// 循环内使用 Map
for (Item item : items) {
    RefDO ref = refMap.get(item.getRefId());
    // 处理逻辑
}
```

**错误模式（必须拒绝）**：
```java
for (Item item : items) {
    RefDO ref = refMapper.selectById(item.getRefId());  // ❌ N+1 查询
}
```

### ✅ 检查项 3：批量查询方法签名

**规则**：新增查询方法时，如果预期会被循环调用，必须同时提供批量版本：

```java
// 单个查询
ErpFinanceAssetDepreciationDO selectByAssetIdAndPeriod(Long assetId, String period);

// 批量查询（配套提供）
List<ErpFinanceAssetDepreciationDO> selectListByPeriod(String period);
Map<Long, ErpFinanceAssetDepreciationDO> selectMapByAssetIdsAndPeriod(
    Collection<Long> assetIds, String period);
```

### ✅ 检查项 4：异常处理

**规则**：
- `catch` 块必须记录日志（log.warn 或 log.error）
- 不允许静默吞掉异常（catch 后 return null 无日志）
- 外部调用失败必须有补偿机制

```java
// ✅ 正确
try {
    externalApi.call();
} catch (Exception e) {
    log.error("[method] 外部调用失败，id={}", id, e);
    markAsFailed(id);  // 补偿
}

// ❌ 错误
try {
    externalApi.call();
} catch (Exception e) {
    return null;  // 静默失败
}
```

## 代码生成流程

当 AI 生成后端代码时，必须：

1. **识别事务边界**：标记所有 `@Transactional` 方法
2. **标记外部调用**：识别事务内的外部 API 调用
3. **标记循环查询**：识别循环内的数据库查询
4. **应用正确模式**：将外部调用移到 afterCommit，将循环查询改为批量
5. **输出检查结果**：在代码前输出检查清单的通过/失败状态

## 输出格式示例

```markdown
## 代码生成检查清单

### ✅ 检查项 1：事务边界内的外部调用
- [x] 事务内无外部 API 调用
- [x] 外部调用已移至 afterCommit

### ✅ 检查项 2：循环内的查询操作
- [x] 循环内无 N+1 查询
- [x] 已使用批量查询 + Map

### ✅ 检查项 3：批量查询方法
- [x] 已提供 selectListByPeriod 批量方法

### ✅ 检查项 4：异常处理
- [x] catch 块已记录日志
- [x] 外部调用失败有补偿机制

## 生成的代码
[代码内容]
```

## 参考文档
- AGENTS.md 14.3 铁律 1：事务内禁止调外部系统
- AGENTS.md 14.7 防重模式库：模式 A（外部调用失败后的 FAILED 状态标记）
- AGENTS.md 14.7 防重模式库：模式 B（循环查询改为批量查询）
