---
name: verify-security
description: 安全质量关卡 — 检查代码变更中的安全漏洞（注入、认证、数据暴露、依赖漏洞）
license: MIT
compatibility: tester
metadata:
  category: quality-gate
  severity: critical
---

## 检查清单

### 注入攻击（Injection）
- [ ] SQL 注入：检查 MyBatis XML 和原生 SQL 中是否存在字符串拼接
- [ ] 命令注入：检查 `Runtime.exec()`、`ProcessBuilder` 等调用
- [ ] XSS：检查前端模板中是否未转义用户输入
- [ ] NoSQL 注入：检查 MongoDB 等查询条件

### 认证与授权（Authentication & Authorization）
- [ ] 硬编码凭据：检查代码中是否有 API Key、密码、Token 硬编码
- [ ] 权限校验：检查新增接口是否有 `@PreAuthorize` 或等效权限注解
- [ ] Session 管理：检查 Token 生成和验证逻辑

### 数据保护（Data Protection）
- [ ] 敏感数据日志：检查 `log.info()` 等是否打印了敏感字段
- [ ] 传输安全：检查 API 是否强制 HTTPS
- [ ] 数据脱敏：检查手机号、身份证等字段是否脱敏输出

### 依赖安全（Dependency Security）
- [ ] 已知漏洞依赖：检查 pom.xml / package.json 中是否有已知漏洞版本
- [ ] 不必要的依赖：检查是否引入了不需要的包

## 输出格式

```
## Security Audit Report

### CRITICAL
- [CRITICAL] SQL 注入风险 - src/main/xxx.xml:42 - 使用 #{} 替代 ${}

### HIGH
- [HIGH] 缺少权限校验 - src/main/xxx.java:85 - 添加 @PreAuthorize

### MEDIUM
...

### LOW
...
```
