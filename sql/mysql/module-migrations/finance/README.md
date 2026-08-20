# 财务模块增量迁移目录

本目录只保存后续新增的、可追踪的财务模块增量 SQL，不复制已有的 `sql/mysql/finance/` 历史脚本。

## 命名

```text
001-finance-<功能>.sql
002-finance-<功能>-menu.sql
003-finance-<功能>-data.sql
```

## 规则

- 文件名递增，按文件名执行。
- 已执行脚本禁止修改，内容变化必须新增文件。
- 菜单和权限种子必须幂等。
- 不新增 `tenant_id`。
- 涉及金额、凭证、期间、付款或审批状态时，先补充 `docs/modules/finance/` 中的变更和回滚说明。
