# MySQL 脚本整理说明

`sql/mysql/` 是当前项目 SQL 维护的主目录。这里同时承载了基础初始化、功能增量、菜单权限、测试数据、环境补数和一次性修复脚本，因此需要按“脚本类型”而不是只按文件名排序来理解。

## 快速结论

- 基础初始化脚本：`2` 个
- 编号增量脚本：`193` 个
- 独立专项脚本：`12` 个
- 异常非 `.sql` 文件：`0` 个

## 脚本类型

| 类型 | 文件特征 | 说明 | 默认处理方式 |
|------|----------|------|--------------|
| 基础初始化 | `ruoyi-vue-pro.sql`、`quartz.sql` | 新库初始化入口 | 可以作为常规初始化入口 |
| 编号增量 | `NNN-*.sql` | 业务功能、菜单权限、字段变更、测试数据混合存在 | 按场景挑选，执行前先读文件头 |
| 独立专项 | 不带编号的 `.sql` | 多为补数、测试、历史修复、一次性场景 | 不默认纳入全量升级 |
| 异常文件 | 非 `.sql` | 临时文件或待转正内容 | 单独处理，不纳入常规执行链 |

## 基础初始化脚本

| 文件 | 用途 | 备注 |
|------|------|------|
| `ruoyi-vue-pro.sql` | 基础框架 DDL + 种子数据 | 多数据库转换的输入来源 |
| `quartz.sql` | Quartz 调度表 | 通常紧跟基础框架执行 |

说明：

- `ruoyi-vue-pro.sql` 目前仍是基础平台初始化主入口。
- 大量 ERP 业务增量并没有统一回灌到这个基础脚本里。

## 编号增量脚本的阅读方式

### 命名约定

常见格式：

- `NNN-模块-功能.sql`
- `NNN-模块-功能-phaseX.sql`
- `NNN-模块-功能-menu.sql`
- `NNN-模块-功能-test-data.sql`

不要做的假设：

- 不要假设同编号只有一个脚本。
- 不要假设同编号脚本之间一定无关。
- 不要假设所有 `phase` 脚本都能直接串成完整升级链。

当前重复编号较多，典型示例：

- `54-*` 有 `3` 个脚本
- `58-*` 有 `3` 个脚本
- `61-*` 有 `3` 个脚本
- `62-*` 有 `3` 个脚本
- `99-*` 有 `5` 个脚本
- `100-*` 有 `5` 个脚本
- `140-*` 有 `3` 个脚本

结论：

- “编号”更像时间片或提交批次，不是严格的一维迁移版本号。
- 执行脚本时必须结合模块和文件头说明一起判断。

## 主要业务分组

下面不是穷举清单，而是按维护视角给出主要分组和代表文件。

### 制造 / 生产

代表文件：

- `04-erp-manufacturing-master.sql`
- `05-erp-manufacturing-execution.sql`
- `08-erp-manufacturing-alter.sql`
- `09-erp-manufacturing-dict-menu.sql`
- `113-erp-production-inbound-phase1.sql`
- `142-erp-stock-check-year-end-close.sql`

### MRP / 供应链计划

代表文件：

- `10-erp-mrp-tenant-fix.sql`
- `14-erp-mrp-menu.sql`
- `20-erp-mrp-suggest-basis.sql`
- `54-erp-mrp-menu-information-architecture.sql`
- `61-erp-mrp-netting-policy.sql`
- `64-erp-mrp-run-bootstrap.sql`
- `100-erp-mrp-traceable-shortage-notification.sql`

### 采购

代表文件：

- `23-erp-purchase-order-project-backfill.sql`
- `25-erp-purchase-order-bpm-lite.sql`
- `30-erp-purchase-in-quality-mvp.sql`
- `33-erp-purchase-in-light-iqc-upgrade.sql`
- `67-erp-purchase-finance-phase1.sql`
- `69-erp-purchase-source-batch-phase1.sql`

### 销售 / 市场

代表文件：

- `11-erp-project-sale.sql`
- `15-erp-sale-order-reject.sql`
- `17-erp-sale-order-bpm-lite.sql`
- `41-erp-sale-delivery-ready-light-quality.sql`
- `139-sales-closure-menu.sql`
- `erp-market-alert-rule-2026-06-15.sql`

### 财务

代表文件：

- `71-erp-ap-estimate-phase1.sql`
- `81-erp-ap-invoice-match-phase1.sql`
- `83-90` 区间财务一期到四期脚本
- `99-erp-finance-demo-data.sql`
- `100-erp-finance-dual-write.sql`
- `116-117` 双账套配置脚本
- `123-133` 双账套、总账、报表、权限修复
- `140-141` 产品成本报表相关脚本
- `erp-finance-year-end-close-gaps-2026-06-09.sql`
- `erp-finance-year-end-close-data-init.sql`

### 库存 / 批次 / 盘点

代表文件：

- `68-erp-production-batch-fifo-phase1.sql`
- `91-94` 批次分配、调整、预留
- `100-erp-stock-analysis-menu.sql`
- `142-erp-stock-check-year-end-close.sql`
- `erp-stock-check-test-data.sql`

### BPM / 审批平台

代表文件：

- `18-bpm-process-definition-info.sql`
- `53-bpm-approval-platform-foundation.sql`
- `54-bpm-approval-platform-incremental.sql`
- `54-bpm-approval-platform-incremental-v2.sql`
- `135-140` 审批场景、审批方案、模板格式、Schema 整合脚本

### 系统 / 菜单 / 权限 / 租户处理

代表文件：

- `46-51` 岗位与菜单相关脚本
- `98-system-notice-menu-restore.sql`
- `137-tenant-id-merge-and-drop.sql`
- `134-erp-finance-voucher-template-menu-permission-fix.sql`
- `143-erp-cost-product-trend-menu.sql`

## 近期新增且需要重点关注的脚本

`131-144` 区间和以下独立脚本当前尚未沉淀进旧索引，后续排查问题时应优先留意：

- `131-erp-finance-dual-ledger-config-permission-fix.sql`
- `132-erp-finance-general-ledger-test-data.sql`
- `133-erp-finance-dual-ledger-query-permission-hotfix.sql`
- `134-erp-finance-voucher-template-menu-permission-fix.sql`
- `135-bpm-approval-scene-and-snapshot.sql`
- `136-bpm-approval-scene-menu-permission.sql`
- `137-bpm-approval-scheme-menu-permission.sql`
- `137-tenant-id-merge-and-drop.sql`
- `138-bpm-approval-template-flowconfig-fix.sql`
- `138-erp-expense-type-hybrid.sql`
- `139-sales-closure-menu.sql`
- `140-bpm-approval-schema-consolidation.sql`
- `140-erp-finance-cost-report-menu.sql`
- `140-erp-sale-order-contract-fields.sql`
- `141-erp-finance-cost-report-test-data.sql`
- `141-erp-project-lifecycle-fields.sql`
- `142-erp-stock-check-year-end-close.sql`
- `143-erp-cost-product-trend-menu.sql`
- `144-erp-cost-report-simple-test-data.sql`
- `erp-finance-void-2026-05-22.sql`
- `erp-finance-year-end-close-data-init.sql`
- `erp-finance-year-end-close-gaps-2026-06-09.sql`
- `erp-market-alert-rule-2026-06-15.sql`
- `erp-stock-check-test-data.sql`
- `local-erp-closure-bootstrap-2026-04-21.sql`

## 独立专项脚本

当前不带编号、需要单独判断是否执行的脚本包括：

- `bpm-2025-03-17-传播违法.sql`
- `erp-2024-05-03.sql`
- `erp-finance-void-2026-05-22.sql`
- `erp-finance-year-end-close-data-init.sql`
- `erp-finance-year-end-close-gaps-2026-06-09.sql`
- `erp-market-alert-rule-2026-06-15.sql`
- `erp-mrp-2026-03-31.sql`
- `erp_purchase_export_test_data.sql`
- `erp-stock-check-test-data.sql`
- `local-erp-closure-bootstrap-2026-04-21.sql`
- `quartz.sql`
- `ruoyi-vue-pro.sql`

其中：

- `ruoyi-vue-pro.sql`、`quartz.sql` 仍属于基础初始化。
- 其他脚本默认按“专项用途”理解。

## 异常项与风险项

### 1. 已删除的重复/子集脚本

#### `42-erpurchase.tmp`

- 已删除
- 与 `42-erp-purchase-in-quality-notify.sql` 的 SHA256 完全一致
- 属于重复临时文件，不应继续保留

#### `erp-dept-cost-type-init.sql`

- 已删除
- 其全部 `system_dept.cost_type` 初始化逻辑已被 `erp-finance-year-end-close-data-init.sql` 第 1 节完整覆盖
- 后续如果只需要初始化部门成本类型，也应直接从 `erp-finance-year-end-close-data-init.sql` 取对应片段，而不是维护第二份副本

### 2. `137-tenant-id-merge-and-drop.sql`

这是高风险脚本，包含：

- 大范围租户数据合并
- 索引处理
- 删列动作

使用要求：

- 不要在不了解当前环境租户状态的情况下执行
- 只能作为明确的去租户化迁移脚本使用

### 3. `140-bpm-approval-schema-consolidation.sql`

文件头已明确声明其意图是整合并替代旧脚本。处理 BPM 审批平台建表问题时，应优先把它和以下脚本一起看：

- `54-bpm-approval-platform-incremental.sql`
- `54-bpm-approval-platform-incremental-v2.sql`
- `55-bpm-approval-add-missing-columns.sql`
- `135-bpm-approval-scene-and-snapshot.sql`

本次整理后，`140-bpm-approval-schema-consolidation.sql` 已额外并入：

- 审批模板初始化 seed
- 审批站内信模板初始化 seed

因此它现在承担的是“BPM 审批平台最终单文件”的角色。

### 4. 文件名与历史文档不一致

当前实际文件名为：

- `bpm-2025-03-17-传播违法.sql`

如果历史文档里还写着其他名称，应以目录实际文件名为准。

## 为什么这次没有直接搬目录

仓库内已有大量位置直接引用 `sql/mysql/*.sql` 的固定路径，包括：

- Docker 初始化配置
- PowerShell 安装脚本
- 生产切换脚本
- 设计文档、测试文档、交付说明

因此这次整理采用的是：

1. 保持原路径
2. 补充索引
3. 明确脚本类型
4. 标注风险脚本

如果后续确实要物理重构目录，建议分两步做：

1. 先建立兼容索引和软迁移规则
2. 再统一修正文档、脚本和 Docker 引用
