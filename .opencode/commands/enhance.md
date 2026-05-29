---
description: Prompt 增强 — 将模糊需求转化为结构化任务描述
---

## 任务
将用户输入的模糊需求转化为结构化的任务描述，包含明确的 Goal、Scope、Requirements、Edge Cases、Verification 等部分。

## 步骤
1. 读取 $ARGUMENTS 或提示用户输入描述
2. 分析描述的领域和技术栈
3. 输出结构化内容：

```
## Goal
<!-- 一句话描述目标 -->

## Scope
<!-- 涉及的文件/模块 -->
- xxx

## Requirements
1. xxx
2. xxx

## Edge Cases
- xxx

## Verification Steps
- [ ] xxx
```

4. 如果任务涉及 3+ 文件或步骤，额外输出实施步骤分解
