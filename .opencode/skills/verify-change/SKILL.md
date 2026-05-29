---
name: verify-change
description: 变更影响分析关卡 — 分析变更影响范围、API 变更、数据库迁移、破坏性变更
license: MIT
compatibility: tester
metadata:
  category: quality-gate
  severity: high
---

## 检查清单

### API 变更
- [ ] 是否修改了已有的 REST API 路径或方法
- [ ] 是否修改了请求/响应字段名或类型
- [ ] 是否修改了 RPC/Feign 接口签名
- [ ] 是否修改了前端 API SDK 调用

### 数据库变更
- [ ] 是否新增/修改了数据库表结构
- [ ] 是否需要数据迁移脚本
- [ ] 是否有 MyBatis XML 变更
- [ ] 变更是否向后兼容

### 破坏性变更
- [ ] 是否删除了公开方法或类
- [ ] 是否修改了已有方法的参数列表
- [ ] 是否修改了配置项或环境变量
- [ ] 是否影响了序列化/反序列化格式

### 受影响列表
- [ ] 列出所有受影响的调用方
- [ ] 列出迁移步骤（如果有）
- [ ] 标注是否需要灰度发布

## 输出格式

```
## Change Impact Report

### Breaking Changes
- ...

### Migration Required
- ...

### Affected Consumers
- module-xxx: 方法 A、B、C
- module-yyy: 接口 X、Y

### Backward Compatible
- ✅ / ❌
```
