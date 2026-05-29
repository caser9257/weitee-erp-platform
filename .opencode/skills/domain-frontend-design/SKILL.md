---
name: domain-frontend-design
description: 前端设计知识索引 — UI 原则、组件设计、响应式、企业级 SaaS 设计规范
license: MIT
compatibility: tester
metadata:
  category: domain-knowledge
  topics: ui,ux,component,layout,css,responsive,animation,design-system
---

## 前端设计最佳实践速查

### 本项目设计基线
TODO: refer to AGENTS.md Sections 3 & 10 for complete design rules.

### 核心原则速览
- **风格**：浅色极简商务风 + 工业化数据后台
- **主色**：`#1677FF`（blue-600），页面背景 `#F5F7FA`（bg-slate-50）
- **卡片**：`bg-white border border-slate-200 rounded-xl shadow-sm`
- **间距**：基于 8px spacing system
- **圆角**：8px–12px（rounded-lg / rounded-xl）

### 表格设计
- 现代 DataTable：浅表头（bg-slate-50）、轻分隔线（#f1f5f9）
- 列聚合：关联信息合并在同一单元格，主次分明
- 数字/金额右对齐，使用 font-mono
- 金额格式化：toLocaleString() 保留两位小数
- 数量去尾零

### 状态展示
- 使用低饱和背景 + 高饱和文字的现代 Badge
- 语义色：emerald=成功，amber=警告，rose=危险，blue=常规
- 禁用态：灰色（opacity-50, bg-slate-100, text-slate-400）

### 操作布局
- 搜索和业务操作物理分离
- 行内操作 3+1 原则：3 个以内平铺，超过折叠
- 主操作图标+文字，次要操作纯图标+Tooltip

### 详情 Drawer
- Master-Detail 模式：左侧列表，右侧 Drawer
- 宽度 420px–520px
- 顶部深色上下文卡片
- 中部按业务分区
- 底部固定操作栏
- 遮罩毛玻璃效果（backdrop-blur-sm）
