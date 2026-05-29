---
description: 管理 git worktree — list / add / remove
---

## 任务
管理 git worktree，支持列表查看、新增、删除。

## 用法
- `/git:worktree list` — 列出所有 worktree
- `/git:worktree add <分支名> [路径]` — 新增 worktree
- `/git:worktree remove <路径>` — 删除 worktree

## 步骤
1. 解析参数，判断操作类型
2. `list`：执行 `git worktree list`，展示路径、分支、commit
3. `add`：检查分支是否存在，执行 `git worktree add <路径> <分支>`
4. `remove`：检查 worktree 是否干净，执行 `git worktree remove <路径>`
5. 确保所有 git 命令从项目根目录执行（非子目录）
