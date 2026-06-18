# 后端代码生成防护体系

## 问题背景

代码审查反复发现的两个 Critical 问题：
1. **事务内调用外部 API** - 违反 AGENTS.md 铁律 1
2. **循环内 N+1 查询** - 违反 AGENTS.md 模式 B

## 解决方案

本方案通过**四层防护**确保这两个问题在代码生成和提交时被自动拦截：

### 第一层：AI 代码生成检查（Skill）

**文件**: `.tester/skills/backend-code-generation-checklist/SKILL.md`

当 AI 生成后端代码时，会自动加载此 skill 并执行强制检查清单：
- ✅ 事务边界内的外部调用检查
- ✅ 循环内的 N+1 查询检查
- ✅ 批量查询方法签名检查
- ✅ 异常处理检查

**输出格式**：AI 在生成代码前会输出检查清单的通过/失败状态。

### 第二层：静态分析脚本

**文件**: `scripts/check-backend-patterns.ps1`

PowerShell 脚本，可手动运行或集成到 CI/CD：
```powershell
# 检查整个项目
.\scripts\check-backend-patterns.ps1 -Path "."

# 检查特定目录
.\scripts\check-backend-patterns.ps1 -Path "yudao-module-erp/src/main/java"
```

检测能力：
- 事务方法内的 `xxxApi.method()` 调用
- 事务方法内的 `restTemplate`/`webClient` 调用
- 循环内的 `Mapper.select*` 查询
- 循环内的 `Service.get*` 查询

### 第三层：Git Pre-commit Hook

**文件**: `.git/hooks/pre-commit`

每次 `git commit` 时自动运行：
- 检测暂存的 Java 文件
- 阻止包含问题的代码提交
- 输出修复建议

```bash
# 正常提交（会触发检查）
git commit -m "feat: add new feature"

# 跳过检查（不推荐）
git commit --no-verify -m "feat: add new feature"
```

### 第四层：AGENTS.md 规则引用

**文件**: `AGENTS.md` 第 14.3 节

已更新，引用了上述工具，确保 AI 在代码生成时自动遵循。

## 使用指南

### 对于 AI 代码生成

当生成包含以下特征的后端代码时，AI 会自动执行检查：
- `@Transactional` 注解的方法
- 循环中调用 Mapper/Service 的代码
- 调用其他模块 Service 的代码

### 对于人工开发

1. **代码生成后**：运行脚本检查
   ```powershell
   .\scripts\check-backend-patterns.ps1
   ```

2. **提交前**：Git hook 会自动检查

3. **CI/CD**：将脚本集成到构建流程

### 对于代码审查

审查时可参考检查清单：
- [ ] 事务内无外部 API 调用
- [ ] 循环内无 N+1 查询
- [ ] 外部调用使用 afterCommit
- [ ] 查询使用批量方法

## 修复模式

### 模式 1：事务内外部调用

```java
// ❌ 错误
@Transactional
public void process() {
    saveToLocal();
    externalApi.call();  // 事务内调外部
}

// ✅ 正确
@Transactional
public void process() {
    saveToLocal();
    TransactionSynchronization.afterCommit(() -> {
        try {
            externalApi.call();
        } catch (Exception e) {
            log.error("[process] 外部调用失败", e);
            markAsFailed();  // 补偿
        }
    });
}
```

### 模式 2：循环内 N+1 查询

```java
// ❌ 错误
for (Item item : items) {
    RefDO ref = refMapper.selectById(item.getRefId());
}

// ✅ 正确
Set<Long> refIds = items.stream()
    .map(Item::getRefId)
    .collect(Collectors.toSet());
Map<Long, RefDO> refMap = refMapper.selectBatchIds(refIds).stream()
    .collect(Collectors.toMap(RefDO::getId, Function.identity()));

for (Item item : items) {
    RefDO ref = refMap.get(item.getRefId());
}
```

## 相关文档

- AGENTS.md 14.3 铁律 1：事务内禁止调外部系统
- AGENTS.md 14.7 防重模式库：模式 A、模式 B
- Issue #16: 事务内调用外部API（deptApi.getDept）
- Issue #17: 折旧循环内逐条查询—N+1查询
