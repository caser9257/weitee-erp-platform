# Tester 扩展 — CCG 移植增强模块

本目录存放从 [ccg-workflow](https://github.com/fengshao1227/ccg-workflow) 移植的开发效率增强模块，全部通过 `.tester/` 扩展体系实现，不修改 Tester 自身安装文件。

## 快速开始

### 自定义命令（Slash Commands）

在聊天中输入以下命令使用：

| 命令 | 用途 |
|------|------|
| `/git:commit` | 分析暂存变更，生成 conventional commit 信息 |
| `/git:rollback` | 展示最近 commit，交互式回滚 |
| `/git:clean-branches` | 清理已合并的本地分支 |
| `/git:worktree` | 管理 git worktree（list / add / remove） |
| `/enhance` | 将模糊需求转为结构化任务描述 |
| `/style:engineer` | 切换到专业工程师风格 |
| `/style:brief` | 切换到冷刃简报风格 |
| `/style:command` | 切换到铁律军令风格 |
| `/style:default` | 重置为默认风格 |

### 专家 Agent（@ 提及）

在提示中使用 `@agent-name` 直接调用：

| Agent | 角色 | 权限 | 典型用途 |
|-------|------|------|---------|
| `@analyzer` | 代码分析 | read-only | 技术研究、影响评估 |
| `@architect` | 架构设计 | read-only | 系统设计、技术选型 |
| `@debugger` | 问题诊断 | full | 定位 Bug、修复 |
| `@reviewer` | 代码审查 | read-only | 审查变更、安全检查 |
| `@tester` | 测试生成 | full | 单元测试、覆盖率分析 |
| `@optimizer` | 性能优化 | read-only | 性能瓶颈分析 |
| `@frontend` | 前端开发 | full | UI/UX 开发、组件设计 |

### 质量关卡 Skill

在涉及以下场景时，通过 `skill(name="verify-xxx")` 加载：

- **verify-security**：处理认证、授权、支付、用户数据时
- **verify-quality**：新增文件或较大改动时
- **verify-change**：修改 API、数据库 schema、共享接口时

### 域知识 Skill

检测到关键词时自动加载（或通过 `skill()` 手动加载）：

| 关键词 | Skill 文件 |
|--------|-----------|
| auth, encryption, xss, cors, jwt, csrf | `skills/domain-security/SKILL.md` |
| microservices, ddd, caching, mq | `skills/domain-architecture/SKILL.md` |
| typescript, java, spring, testing | `skills/domain-development/SKILL.md` |
| ui, ux, component, design system | `skills/domain-frontend-design/SKILL.md` |

### 任务持久化

跨多文件复杂任务，使用 `task-manager` 工具管理状态：

```bash
# 文件存储在 .tester/tasks/<task-name>/task.json
# 通过 task-manager tool 操作：create / get / update / list
```

## 目录结构

```
.tester/
├── agents/            # 专家 Agent 配置（7 个）
│   ├── analyzer.md
│   ├── architect.md
│   ├── debugger.md
│   ├── frontend.md
│   ├── optimizer.md
│   ├── reviewer.md
│   └── tester.md
├── commands/          # Slash 命令（9 个新增 + 4 个预装）
│   ├── enhance.md
│   ├── git-clean-branches.md
│   ├── git-commit.md
│   ├── git-rollback.md
│   ├── git-worktree.md
│   ├── style-brief.md
│   ├── style-command.md
│   ├── style-default.md
│   └── style-engineer.md
├── skills/            # Skill 知识库（7 个新增 + 4 个预装）
│   ├── domain-architecture/
│   ├── domain-development/
│   ├── domain-frontend-design/
│   ├── domain-security/
│   ├── verify-change/
│   ├── verify-quality/
│   └── verify-security/
├── tools/             # Custom Tool
│   └── task-manager.ts
├── tasks/             # 任务持久化目录
└── README.md          # 本文件
```

## 移植来源

本模块移植自 ccg-workflow v3.1.1（[GitHub](https://github.com/fengshao1227/ccg-workflow)）。

### 已移植的能力
- Git 智能命令（commit / rollback / clean-branches / worktree）
- Prompt 增强（enhance）
- 专家 Agent 集（分析/架构/调试/审查/测试/优化/前端）
- 输出风格切换（engineer / brief / command / default）
- 质量关卡（security / quality / change）
- 域知识库（security / architecture / development / frontend-design）
- 任务持久化（task-manager）

### 未移植的能力
- 多模型编排引擎（Tester 原生多 provider 已覆盖）
- Hook 引擎（依赖 Claude Code 专有事件 API）
- Agent Teams 并行执行（依赖 Claude Code 专有 API）

## AGENTS.md 规则

相关使用规则已在 `AGENTS.md` 第 13 节中定义，包括命令速查表、质量关卡自动触发规则、域知识按需加载规则、任务持久化约定和输出风格说明。
