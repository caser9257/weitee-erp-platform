---
description: 交互式回滚 — 展示最近提交，让用户选择回滚目标
---

## 任务
展示最近 10 个 commit，让用户交互式选择回滚到哪个 commit，执行 `git revert`。

## 步骤
1. 运行 `git log --oneline -10` 和 `git status` 获取当前状态
2. 如果有未提交的变更，警告用户并建议先 stash
3. 以编号列表展示最近 10 个 commit（编号、hash、message、日期）
4. 让用户选择：
   - 输入编号选择要回滚到的 commit
   - `s` 先 stash 再继续
   - `c` 取消
5. 确认时要展示 `git diff <hash>` 让用户了解影响
6. 执行 `git revert <hash>`（不回滚已推送的 commit 时）或 `git reset --soft <hash>`
7. 仅限于未推送的本地 commit 使用 reset，已推送的必须用 revert

## 输出格式
```
最近的 10 个 commit：
[1] a1b2c3d feat: add user avatar (2026-05-24)
[2] e4f5g6h fix: correct login redirect (2026-05-23)
...
    
⚠ 检测到 2 个未提交文件变更。建议先 stash。
输入编号选择回滚目标，s=stash 后继续，c=取消：
```
