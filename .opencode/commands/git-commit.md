---
description: 智能 conventional commit — 分析暂存变更，生成规范提交信息
---

## 任务
分析当前 git diff --cached，生成 conventional commit 信息，确认后提交。

## 步骤
1. 运行 `git diff --cached` 读取暂存的变更内容
2. 如果没有暂存变更，提示用户并建议 `git add` 再试
3. 根据变更内容选择合适的 conventional commit 类型（feat/fix/chore/refactor/docs/style/test/perf/ci/build/revert）
4. 推断合适的 scope（模块/范围）
5. 生成完整的 commit message：`<type>(<scope>): <简短描述>`
6. 展示 message 给用户确认
7. 用户确认后执行 `git commit -m "<message>"`
8. 如果用户拒绝，允许重新输入 message 或取消

## 输出格式
```
检测到以下变更文件：
- src/xxx.java (修改)
- src/yyy.java (新增)

建议提交信息：feat(user): add user avatar upload support

确认提交？(y/n)
```
