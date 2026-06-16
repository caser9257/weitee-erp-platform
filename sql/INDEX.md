# SQL 目录索引

本目录以 `mysql/` 为主维护来源，其他数据库目录主要由工具从 MySQL 基础脚本转换生成。

## 当前结构

| 路径 | 说明 |
|------|------|
| `mysql/` | 主维护目录，包含基础初始化、编号增量、测试/补数/专项脚本 |
| `tools/` | Docker 启动、跨数据库转换等辅助工具 |
| `oracle/` `postgresql/` `sqlserver/` `dm/` `kingbase/` `opengauss/` | 从 MySQL 基础脚本转换得到的多数据库版本 |
| `handoff-archive/` | 交付过程中的归档 SQL，不纳入常规部署路径 |
| `project.sql` `project_comment.sql` `project_notify_template.sql` | 独立项目模块脚本 |

## MySQL 目录现状

截至 2026-06-16，`sql/mysql/` 目录下共有：

- `193` 个编号增量脚本：`NNN-*.sql`
- `12` 个独立专项脚本：不带编号，通常用于测试数据、补数、历史修复或一次性场景
- `0` 个异常非 `.sql` 文件

详细分类见 [mysql/README.md](/D:/ruoyi-vue-pro/sql/mysql/README.md)。

## 建议使用方式

### 1. 初始化新库

按顺序执行：

1. `mysql/ruoyi-vue-pro.sql`
2. `mysql/quartz.sql`

说明：

- `ruoyi-vue-pro.sql` 是基础框架初始化入口，也是 `sql/tools/convertor.py` 转换其他数据库版本的来源。
- 业务增量脚本并未完全回灌到 `ruoyi-vue-pro.sql`，所以 ERP 功能通常还需要按场景补执行对应增量脚本。

### 2. 执行增量脚本

默认遵循以下约定：

- 优先按业务模块挑选脚本，不要机械地把 `04` 到 `144` 全量重放到任意环境。
- 同一编号可能对应多个模块脚本，例如 `99-*`、`100-*`、`140-*`，不能只靠编号推断单一主题。
- 菜单、权限、测试数据、补数、热修复脚本与 DDL/DML 主链脚本混放在一起，执行前必须先读文件头注释。
- 带 `test-data`、`demo-data`、`sample`、`bootstrap`、`verify`、`void`、`init`、`hotfix`、`repair`、`restore` 等后缀的脚本，默认按“专项脚本”对待，不应直接纳入生产全量升级流程。

### 3. 多数据库版本

多数据库目录当前只适合作为基础框架初始化参考：

- 生成来源：`sql/tools/convertor.py`
- 当前覆盖范围：基础框架表 + Quartz
- 当前限制：大部分 ERP 增量脚本没有同步转换

因此：

- 需要 ERP 业务能力时，不能把其他数据库目录视为与 `mysql/` 完全等价。
- 需要跨库落地时，应先确认目标脚本是否只是基础框架，还是已经包含 ERP 业务表。

## 本次整理结论

这次没有直接移动或重命名 `sql/mysql` 下的现有脚本，原因如下：

- 仓库内已有大量文档、PowerShell 脚本、Docker 初始化配置直接引用 `sql/mysql/*.sql` 的固定路径。
- 直接搬迁或批量改名会引入大面积断链，需要同步修改大量引用点，风险明显高于收益。

因此当前整理策略是：

1. 保留现有路径不变
2. 补齐目录级索引
3. 标记异常文件、危险脚本和命名约定
4. 为后续新增脚本提供统一落位说明

其中 BPM 审批平台脚本已开始收口：

- `140-bpm-approval-schema-consolidation.sql` 现在同时包含最终 schema 与基础 seed
- 老的 `54/55/135` 分片脚本已不再作为首选入口

## 已知问题

### 1. 路径耦合较重

以下位置直接依赖 `sql/mysql` 固定路径：

- `script/docker/docker-compose.yml`
- `sql/tools/docker-compose.yaml`
- `scripts/erp/install-finance-demo-data.ps1`
- 多份 `docs/**`、`openspec/**`、`production/**` 文档与运维脚本

因此目前不建议直接做物理迁移。

### 2. 已清理的重复脚本

- `mysql/42-erpurchase.tmp` 已删除：与 `mysql/42-erp-purchase-in-quality-notify.sql` 内容完全一致，属于重复临时文件。
- `mysql/erp-dept-cost-type-init.sql` 已删除：内容已被 `mysql/erp-finance-year-end-close-data-init.sql` 第 1 节完整覆盖。

### 3. 顶层历史文档曾有失真

此前索引里存在过期描述，例如：

- 写成 `bpm-2025-03-17-违规查询.sql`，而当前实际文件名是 `bpm-2025-03-17-传播违法.sql`
- 未覆盖 `131-144` 区间以及多个 2026-06 独立脚本

本次已改为“现状索引 + 下钻 README”的维护方式，避免继续在单个大表里手工追更 200+ 文件。
