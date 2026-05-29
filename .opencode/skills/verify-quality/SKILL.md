---
name: verify-quality
description: 代码质量关卡 — 检查类型安全、错误处理、代码风格、测试覆盖
license: MIT
compatibility: tester
metadata:
  category: quality-gate
  severity: high
---

## 检查清单

### 类型安全（Type Safety）
- [ ] 是否存在 `as any` / `@ts-ignore` / `@ts-expect-error` 等类型逃逸
- [ ] 是否存在隐式 `any` 类型
- [ ] 泛型是否使用了合适的约束

### 错误处理（Error Handling）
- [ ] 是否存在空的 catch 块 `catch (e) {}`
- [ ] 异常是否有业务友好的错误消息
- [ ] 外部调用是否有超时和重试机制
- [ ] 资源（IO、DB 连接）是否在 finally 中释放

### 代码风格（Code Style）
- [ ] 命名是否遵循项目约定（camelCase / PascalCase / kebab-case）
- [ ] 是否存在过长方法（> 50 行）
- [ ] 是否存在重复代码片段
- [ ] 注释是否跟上了代码变更

### 测试覆盖（Test Coverage）
- [ ] 新增代码是否有对应的单元测试
- [ ] 测试是否覆盖了边界条件和异常路径
- [ ] 测试是否为可重复的（无外部依赖）

## 输出格式

```
## Quality Report
Score: PASS / WARN / FAIL

### BUG_RISK
- ...

### MAINTAINABILITY
- ...

### PERFORMANCE
- ...

### STYLE
- ...
```
