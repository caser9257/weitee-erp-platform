# MySQL 脚本整理说明

`sql/mysql/` 是当前项目 SQL 维护的主目录。当前整理目标已经收紧为：主目录只保留基础初始化、现役功能增量、菜单权限和字段迁移；测试数据、演示数据、验证脚本、本地引导和历史查询包不再保留在这里。

## 快速结论

- 基础初始化脚本：保留
- 现役编号增量脚本：保留
- 测试 / 演示 / 验证 / 本地引导脚本：已从主目录删除
- 异常非 `.sql` 文件：`0` 个

## 脚本类型

| 类型 | 文件特征 | 说明 | 默认处理方式 |
|------|----------|------|--------------|
| 基础初始化 | `ruoyi-vue-pro.sql`、`quartz.sql` | 新库初始化入口 | 可以作为常规初始化入口 |
| 编号增量 | `NNN-*.sql` | 业务功能、菜单权限、字段变更 | 按场景挑选，执行前先读文件头 |
| 独立专项 | 不带编号的 `.sql` | 多为补数、历史修复、一次性场景 | 不默认纳入全量升级 |
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
- `NNN-模块-功能-test-data.sql`（此类已不建议留在主目录）

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
- `100-erp-finance-dual-write.sql`
- `146` 双账套整合脚本
- `140` 产品成本报表菜单与相关迁移脚本
- `erp-finance-year-end-close-gaps-2026-06-09.sql`
- `erp-finance-year-end-close-data-init.sql`

### 库存 / 批次 / 盘点

代表文件：

- `68-erp-production-batch-fifo-phase1.sql`
- `91-94` 批次分配、调整、预留
- `100-erp-stock-analysis-menu.sql`
- `142-erp-stock-check-year-end-close.sql`

### BPM / 审批平台

代表文件：

- `18-bpm-process-definition-info.sql`
- `53-bpm-approval-platform-foundation.sql`
- `54-bpm-approval-platform-incremental.sql`
- `54-bpm-approval-platform-incremental-v2.sql`
- `140` 审批平台最终整合脚本

### 系统 / 菜单 / 权限 / 租户处理

代表文件：

- `46-51` 岗位与菜单相关脚本
- `137-tenant-id-merge-and-drop.sql`
- `147-erp-finance-menu-permission-consolidation.sql`
- `143-erp-cost-product-trend-menu.sql`

## 近期新增且需要重点关注的脚本

`137-147` 区间和以下独立脚本当前尚未沉淀进旧索引，后续排查问题时应优先留意：

- `137-tenant-id-merge-and-drop.sql`
- `138-erp-expense-type-hybrid.sql`
- `139-sales-closure-menu.sql`
- `140-bpm-approval-schema-consolidation.sql`
- `140-erp-finance-cost-report-menu.sql`
- `140-erp-sale-order-contract-fields.sql`
- `141-erp-project-lifecycle-fields.sql`
- `142-erp-stock-check-year-end-close.sql`
- `143-erp-cost-product-trend-menu.sql`
- `146-erp-finance-dual-ledger-consolidation.sql`
- `147-erp-finance-menu-permission-consolidation.sql`
- `erp-finance-year-end-close-data-init.sql`
- `erp-finance-year-end-close-gaps-2026-06-09.sql`
- `erp-market-alert-rule-2026-06-15.sql`

## 独立专项脚本

当前不带编号、需要单独判断是否执行的脚本包括：

- `erp-finance-year-end-close-data-init.sql`
- `erp-finance-year-end-close-gaps-2026-06-09.sql`
- `erp-market-alert-rule-2026-06-15.sql`
- `quartz.sql`
- `ruoyi-vue-pro.sql`

其中：

- `ruoyi-vue-pro.sql`、`quartz.sql` 仍属于基础初始化。
- 其他脚本默认按“专项用途”理解。
- 测试、演示、验证、本地引导脚本已经不再保留在主目录。

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

#### `99-erp-finance-dual-write-config.sql`

- 已删除
- `erp_finance_dual_write_config` 建表逻辑已被 `100-erp-finance-dual-write.sql` 明确覆盖
- 且 `99` 与 `100` 对同表结构口径并不一致，主目录只保留后续主链脚本，避免双份来源继续并存

#### `59-bpm-admin-role-permissions.sql` / `63-bpm-portal-menu.sql` / `64-bpm-portal-menu-simple.sql`

- 已删除
- BPM 审批平台菜单、审批门户、审批模板、审批委托和审批统计入口已统一并入 `140-bpm-approval-schema-consolidation.sql`
- `64` 是 `63` 的简化子集，`59` 仍使用旧组件路径，继续保留只会制造多份 BPM 菜单来源

#### `999-remove-tenant-tables.sql`

- 已删除
- 其“tenant 合并 + 删索引 + 删列”的去租户动作已被 `137-tenant-id-merge-and-drop.sql` 更完整覆盖
- 主目录不再保留第二份简化版去租户删除脚本

#### `29-bpm-super-admin-publish-fix.sql`

- 已删除
- 这是面向特定账号和特定环境快照的权限热修脚本，不属于通用迁移链
- 继续保留会误导为现役升级入口

#### `98-clean-mall-and-mp-data.sql`

- 已删除
- 这是商城 / 公众号模块剔除后的环境清理脚本，不属于现役业务迁移
- 相关背景仍保留在 `docs/商城与公众号模块剔除清单-2026-05-07.md`

### 2. `137-tenant-id-merge-and-drop.sql`

这是高风险脚本，包含：

- 大范围租户数据合并
- 索引处理
- 删列动作

使用要求：

- 不要在不了解当前环境租户状态的情况下执行
- 只能作为明确的去租户化迁移脚本使用

### 3. `140-bpm-approval-schema-consolidation.sql`

文件头已明确声明其意图是整合并替代旧脚本。处理 BPM 审批平台问题时，应优先把它视为最终单文件入口。

本次整理后，`140-bpm-approval-schema-consolidation.sql` 已额外并入：

- 流程定义 `notification_policy_setting` 通知策略兼容字段
- 审批记录 / 催办记录的 BaseDO 补列兼容
- 审批场景 / 审批方案 / 审批快照的历史数据回填
- 审批统计视图与用户审批统计视图
- 审批模板初始化 seed
- 审批站内信模板初始化 seed
- 审批场景 / 审批方案菜单与按钮权限
- 审批门户（待我审批 / 我发起的 / 抄送我的 / 我已审批）菜单
- 审批模板、审批委托、审批统计看板菜单
- 模板 `flowConfig` 兼容修复

因此它现在承担的是“BPM 审批平台最终单文件”的角色。

原 `57-bpm-urge-record-add-base-fields.sql`、`58-bpm-record-add-base-fields.sql`、`58-bpm-notification-policy.sql`、`60-bpm-historical-data-compatibility.sql` 已删除，不再作为独立补丁保留。

### 4. `146-erp-finance-dual-ledger-consolidation.sql`

- 已新增
- 统一承接双账套建表、旧环境补列、菜单和权限
- 后续处理双账套环境初始化或补齐时，应优先执行该脚本，而不是再分别挑选 `116/117/123/125/131/133`

### 5. `108-erp-finance-asset-phase1.sql`

- 已并入原 `145-erp-asset-depreciation-voucher-template.sql` 的资产补列、折旧/摊销凭证模板和相关科目初始化
- 已补入研发无形资产摊销模板（`biz_type=72`）以及 `5301` 研发支出科目
- 已并入原 `98-system-notice-menu-restore.sql` 的固定资产页面菜单、按钮权限和财务主管授权
- 后续处理固定资产与折旧初始化时，应优先把 `108` 视为资产主链脚本，而不是再单独执行 `145`

### 6. `147-erp-finance-menu-permission-consolidation.sql`

- 已新增
- 统一承接财务菜单恢复、只读 query 权限、应付台账、暂估/费用单、付款审批/作废、预付款、凭证模板等菜单与按钮权限修复
- 后续处理财务菜单树或财务角色授权问题时，应优先执行该脚本，而不是再分别挑选 `96/102/106/107/114/119/134`

### 7. `118-erp-finance-ledger-permission.sql`

- 当前保留为财务账簿权限 schema 入口
- 负责 `erp_finance_ledger_role`、`erp_finance_audit_operation_log`、`erp_finance_dual_ledger_amount_diff_log` 三张现役表
- 原 `119-erp-finance-audit-role-seed.sql` 与 `120-erp-finance-audit-role-permissions.sql` 使用写死角色 ID、假菜单 ID、假账簿 ID，属于环境占位脚本，已从主目录删除

### 8. 主目录清理策略

以下类型脚本已经从 `sql/mysql/` 主目录移除：

- `demo-data`
- `test-data`
- `verify`
- `sample`
- `local bootstrap`
- 一次性历史查询包

当前主目录定位为“现役初始化 + 现役迁移”，不再承担测试数据仓库职责。

### 9. 历史文档可能残留旧路径

由于这轮是激进清理，部分 `docs/**`、`plans/**`、`handoff/**` 里仍可能残留对已删测试脚本或历史查询包的引用。

处理原则：

- 以当前 `sql/mysql/` 实际文件为准
- 历史文档里的旧路径仅作为交付记录，不再作为主目录脚本清单依据

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
