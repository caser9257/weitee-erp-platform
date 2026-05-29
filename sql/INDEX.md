# SQL 脚本索引

项目使用的数据库为 MySQL 8.x，其他数据库版本的脚本通过工具生成。

## 初始化脚本（按顺序执行）

| 文件名 | 大小 | 说明 |
|--------|------|------|
| `mysql/ruoyi-vue-pro.sql` | 990KB | 基础框架 DDL + 种子数据（48 张表，2764 条 INSERT） |
| `mysql/quartz.sql` | 41KB | Quartz 任务调度表 |

## 增量脚本（按编号顺序执行）

编号范围 04-130，按模块分组。建议按编号升序执行，同编号的脚本没有依赖关系。

### 制造管理（5 个）

| 编号 | 文件 | 行数 | 说明 |
|------|------|------|------|
| 04 | `04-erp-manufacturing-master.sql` | 142 | 工艺路线、工作中心主数据 |
| 05 | `05-erp-manufacturing-execution.sql` | 248 | 制造执行：工单、报工、物料消耗 |
| 08 | `08-erp-manufacturing-alter.sql` | 93 | 制造模块字段变更 |
| 09 | `09-erp-manufacturing-dict-menu.sql` | 136 | 制造模块字典与菜单 |
| 97 | `97-erp-manufacturing-mrp-demo-data.sql` | 195 | 制造 MRP 演示数据 |

### MRP 管理（27 个）

| 编号 | 文件 | 行数 | 说明 |
|------|------|------|------|
| 10 | `10-erp-mrp-tenant-fix.sql` | 54 | 多租户兼容修复 |
| 14 | `14-erp-mrp-menu.sql` | 244 | MRP 菜单 |
| 20 | `20-erp-mrp-suggest-basis.sql` | 190 | 建议依据兼容层 |
| 21 | `21-erp-mrp-plan-menu.sql` | 109 | MRP 计划菜单 |
| 22 | `22-erp-mrp-menu-merge.sql` | 66 | MRP 菜单合并 |
| 22 | `22-erp-mrp-project-reservation.sql` | 140 | 项目预留 |
| 24 | `24-erp-mrp-supply-chain-role.sql` | 435 | 供应链角色 |
| 52 | `52-erp-mrp-business-routing.sql` | 99 | 业务路线 |
| 54 | `54-erp-mrp-menu-information-architecture.sql` | 414 | MRP 菜单信息架构 |
| 56 | `56-erp-mrp-substitute-demo-data.sql` | 272 | 替代料演示数据 |
| 57 | `57-erp-project-role-task-notify.sql` | 26 | 项目角色任务通知 |
| 58 | `58-erp-material-plan-rule-upgrade.sql` | 72 | 物料计划规则升级 |
| 59 | `59-erp-mrp-stock-reservation-summary.sql` | 131 | 库存预留汇总表 |
| 61 | `61-erp-mrp-netting-policy.sql` | 235 | 净需求策略 |
| 62 | `62-erp-mrp-netting-policy-menu.sql` | 227 | 净需求策略菜单 |
| 62 | `62-erp-mrp-result-view.sql` | 172 | MRP 结果视图 |
| 63 | `63-erp-mrp-stale-running-repair.sql` | 82 | 过期运行修复 |
| 64 | `64-erp-mrp-run-bootstrap.sql` | 502 | MRP 运行引导 |
| 65 | `65-erp-mrp-netting-policy-description.sql` | 46 | 策略描述 |
| 66 | `66-erp-mrp-component-role.sql` | 58 | 组件角色 |
| 97 | `97-erp-manufacturing-mrp-demo-data.sql` | 195 | 制造 MRP 演示数据 |
| 98 | `98-erp-mrp-outsource-inbound-menu.sql` | 42 | 委外入库菜单 |
| 99 | `99-erp-mrp-finish-quality-menu.sql` | 91 | 完工质检菜单 |
| 99 | `99-erp-mrp-outsource-issue-menu.sql` | 42 | 委外发料菜单 |
| 100 | `100-erp-mrp-traceable-shortage-notification.sql` | 389 | 可追溯短缺通知 |
| 102 | `102-erp-mrp-trace-branch-demo-data.sql` | 301 | 追溯分支演示数据 |
| 104 | `104-erp-mrp-full-chain-base-data.sql` | 252 | 全链路基础数据 |

### 销售管理（9 个）

| 编号 | 文件 | 行数 | 说明 |
|------|------|------|------|
| 11 | `11-erp-project-sale.sql` | 91 | 销售项目关联 |
| 15 | `15-erp-sale-order-reject.sql` | 100 | 销售订单驳回（含兼容层，合并） |
| 16 | `16-erp-sale-order-audit-log.sql` | 26 | 销售审批日志 |
| 17 | `17-erp-sale-order-bpm-lite.sql` | 42 | 销售订单轻量审批 |
| 19 | `19-erp-sale-order-bpm-test-users.sql` | 211 | 销售审批测试用户 |
| 24 | `24-erp-sale-order-bpm-role-split.sql` | 93 | 销售审批角色拆分 |
| 41 | `41-erp-sale-delivery-ready-light-quality.sql` | 41 | 销售发货就绪轻量质检 |
| 42 | `42-erp-sale-delivery-ready.sql` | 24 | 销售发货就绪 |

### 采购管理（21 个）

| 编号 | 文件 | 行数 | 说明 |
|------|------|------|------|
| 23 | `23-erp-purchase-order-project-backfill.sql` | 131 | 采购订单项目回填 |
| 25 | `25-erp-purchase-order-bpm-lite.sql` | 119 | 采购订单轻量审批 |
| 26 | `26-erp-purchase-order-bpm-users.sql` | 171 | 采购审批测试用户 |
| 27 | `27-erp-purchase-in-bpm-lite.sql` | 90 | 采购入库轻量审批 |
| 28 | `28-erp-purchase-in-bpm-users.sql` | 94 | 采购入库审批用户 |
| 30 | `30-erp-purchase-in-quality-mvp.sql` | 11 | 采购入库质检 MVP |
| 31 | `31-erp-purchase-in-stock-in-confirm.sql` | 10 | 采购入库确认 |
| 32 | `32-erp-purchase-in-quality.sql` | 45 | 采购入库质检 |
| 33 | `33-erp-purchase-in-light-iqc-upgrade.sql` | 248 | 来料检验升级 |
| 34 | `34-erp-purchase-in-quality-menu.sql` | 75 | 质检菜单 |
| 35 | `35-erp-purchase-in-quality-role-permissions.sql` | 368 | 质检角色权限 |
| 36 | `36-erp-purchase-in-quality-assign-checker.sql` | 198 | 质检指派检验员 |
| 39 | `39-erp-purchase-in-quality-permission-repair.sql` | 244 | 质检权限修复 |
| 40 | `40-erp-purchase-in-quality-permission-repair-multi-tenant.sql` | 240 | 多租户质检权限修复 |
| 42 | `42-erp-purchase-in-quality-notify.sql` | 74 | 质检通知 |
| 44 | `44-erp-purchase-in-partial-stock-execute.sql` | 56 | 部分入库执行 |
| 60 | `60-erp-purchase-engineering-fee.sql` | 13 | 采购工程费 |
| 61 | `61-erp-purchase-order-item-delivery-date.sql` | 2 | 采购订单行交期 |
| 67 | `67-erp-purchase-finance-phase1.sql` | 85 | 采购财务一期 |
| 69 | `69-erp-purchase-source-batch-phase1.sql` | 24 | 采购来源批次 |
| 70 | `70-erp-purchase-source-batch-linkage.sql` | 8 | 采购来源批次联动 |

### 财务管理（35 个）

| 编号 | 文件 | 行数 | 说明 |
|------|------|------|------|
| 71 | `71-erp-ap-estimate-phase1.sql` | 66 | AP 暂估一期 |
| 81 | `81-erp-ap-invoice-match-phase1.sql` | 240 | AP 发票匹配 |
| 82 | `82-erp-ap-estimate-closure-phase2.sql` | 62 | 暂估关账二期 |
| 83 | `83-erp-finance-expense-phase1.sql` | 30 | 费用一期 |
| 84 | `84-erp-finance-expense-menu.sql` | 132 | 费用菜单 |
| 85 | `85-erp-finance-expense-phase2.sql` | 51 | 费用二期 |
| 86 | `86-erp-finance-ledger-period-phase1.sql` | 48 | 账套期间 |
| 87 | `87-erp-finance-voucher-phase1.sql` | 87 | 凭证一期 |
| 88 | `88-erp-finance-voucher-phase2.sql` | 172 | 凭证二期 |
| 89 | `89-erp-finance-general-ledger-phase3.sql` | 29 | 总账三期 |
| 90 | `90-erp-finance-subject-report-phase4.sql` | 65 | 科目报表四期 |
| 95 | `95-erp-finance-voucher-amount-source-expansion.sql` | 6 | 凭证金额来源扩展 |
| 96 | `96-erp-finance-secondary-menu-restore.sql` | 136 | 财务二级菜单恢复 |
| 98 | `98-erp-finance-personnel-test-users.sql` | 439 | 财务人事测试用户 |
| 99 | `99-erp-finance-demo-data.sql` | 522 | 财务演示数据 |
| 99 | `99-erp-finance-dual-write-config.sql` | 18 | 财务双写配置 |
| 99 | `99-erp-finance-research-voucher-phase1.sql` | 41 | 财务研发凭证 |
| 100 | `100-erp-finance-dual-write.sql` | 40 | 财务双写 |
| 102 | `102-erp-finance-role-readonly-permission-fix.sql` | 449 | 财务只读角色权限修复 |
| 103 | `103-erp-finance-manager-full-permission-fix.sql` | 366 | 财务经理全权限修复 |
| 104 | `104-erp-finance-manager-account-menu-fix.sql` | 86 | 财务经理科目菜单修复 |
| 105 | `105-erp-finance-manager-permission-complete-fix.sql` | 247 | 财务经理权限完整修复 |
| 106 | `106-erp-ap-statement-permission-seed-and-grant.sql` | 176 | AP 对账单权限 |
| 107 | `107-erp-ap-estimate-and-expense-permission-fix.sql` | 272 | AP 暂估费用权限修复 |
| 108 | `108-erp-finance-asset-phase1.sql` | 181 | 固定资产一期 |
| 111 | `111-erp-finance-asset-source-permission-test-data.sql` | 259 | 资产来源权限测试数据 |
| 112 | `112-erp-finance-expense-bpm-lite.sql` | 42 | 费用轻量审批 |
| 113 | `113-erp-finance-payment-bpm-lite.sql` | 42 | 付款轻量审批 |
| 114 | `114-erp-finance-payment-bpm-permission-fix.sql` | 135 | 付款审批权限修复 |
| 115 | `115-erp-finance-expense-rd-accounting-type.sql` | 28 | 费用研发核算类型 |
| 116 | `116-erp-finance-dual-ledger-config.sql` | 24 | 双账套配置 |
| 117 | `117-erp-finance-dual-ledger-diff-config.sql` | 27 | 双账套差异配置 |
| 118 | `118-erp-finance-ledger-permission.sql` | 70 | 账套权限 |
| 119 | `119-erp-finance-audit-role-seed.sql` | 14 | 财务审计角色种子 |
| 120 | `120-erp-finance-audit-role-permissions.sql` | 21 | 财务审计角色权限 |
| 129 | `129-erp-finance-report-test-data.sql` | 179 | 财务报表测试数据 |
| 130 | `130-erp-finance-report-test-verify.sql` | 249 | 财务报表测试验证 |
| 131 | `131-erp-finance-dual-ledger-config-permission-fix.sql` | 319 | 双账套配置权限补全 |
| 132 | `132-erp-finance-general-ledger-test-data.sql` | 97 | 总账测试数据 |
| 133 | `133-erp-finance-dual-ledger-query-permission-hotfix.sql` | 152 | 双账套查询权限热修复 |

### 库存管理（5 个）

| 编号 | 文件 | 行数 | 说明 |
|------|------|------|------|
| 68 | `68-erp-production-batch-fifo-phase1.sql` | 196 | 生产批次 FIFO |
| 91 | `91-erp-stock-batch-allocation-phase1.sql` | 30 | 批次分配 |
| 92 | `92-erp-stock-batch-adjust-record-phase2.sql` | 59 | 批次调整记录 |
| 93 | `93-erp-stock-batch-adjustment-rebuild-phase3.sql` | 117 | 批次调整重构 |
| 94 | `94-erp-stock-batch-reservation-phase4.sql` | 128 | 批次预留 |

### 委外管理（5 个）

| 编号 | 文件 | 行数 | 说明 |
|------|------|------|------|
| 76 | `76-erp-outsource-phase1.sql` | 183 | 委外一期 |
| 77 | `77-erp-outsource-phase2.sql` | 7 | 委外二期（占位） |
| 78 | `78-erp-outsource-phase3.sql` | 9 | 委外三期 |
| 79 | `79-erp-outsource-phase4.sql` | 25 | 委外四期 |
| 80 | `80-erp-outsource-phase5.sql` | 6 | 委外五期（占位） |

### 项目管理（6 个）

| 编号 | 文件 | 行数 | 说明 |
|------|------|------|------|
| 12 | `12-erp-project-business-types.sql` | 309 | 项目业务类型 |
| 13 | `13-erp-project-stock-quality-enhance.sql` | 444 | 项目库存质量增强 |
| 34 | `34-erp-project-pc-mc-role-mvp.sql` | 317 | 项目 PC/MC 角色 |
| 43 | `43-erp-product-workbench-demo-data.sql` | 46 | 产品工作台演示 |
| 57 | `57-erp-project-role-task-notify.sql` | 26 | 项目角色任务通知 |
| 100 | `100-erp-project-warning-demo-data.sql` | 115 | 项目预警演示数据 |

### 系统管理（8 个）

| 编号 | 文件 | 行数 | 说明 |
|------|------|------|------|
| 46 | `46-system-dept-post-import.sql` | 118 | 部门岗位导入 |
| 47 | `47-system-post-level-dual-entry.sql` | 209 | 岗位级别双录入 |
| 48 | `48-system-dept-post-template-final.sql` | 462 | 部门岗位模板终版 |
| 48 | `48-system-post-level-menu-fix.sql` | 99 | 岗位级别菜单修复 |
| 49 | `49-system-dept-post-template-final-v2.sql` | 331 | 部门岗位模板 V2 |
| 50 | `50-system-post-level-finalize.sql` | 131 | 岗位级别定版 |
| 51 | `51-system-post-level-menu-parent-finalize.sql` | 108 | 岗位级别菜单父节点定版 |
| 98 | `98-system-notice-menu-restore.sql` | 264 | 通知菜单恢复 |

### BPM 工作流（4 个）

| 编号 | 文件 | 行数 | 说明 |
|------|------|------|------|
| 18 | `18-bpm-process-definition-info.sql` | 61 | 流程定义信息 |
| 29 | `29-bpm-super-admin-publish-fix.sql` | 93 | 超级管理员发布修复 |
| 53 | `53-bpm-approval-platform-foundation.sql` | 61 | 审批平台基础 |
| 58 | `58-bpm-notification-policy.sql` | 27 | 通知策略 |

### 微泰定制（3 个）

| 编号 | 文件 | 行数 | 说明 |
|------|------|------|------|
| 41 | `41-weitai-menu-information-architecture-phase1.sql` | 605 | 微泰菜单信息架构 |
| 55 | `55-weitai-final-menu-alignment.sql` | 542 | 微泰最终菜单对齐 |
| 97 | `97-weitai-formal-menu-tree-restore.sql` | 499 | 微泰正式菜单树恢复 |

### 其他

| 编号 | 文件 | 行数 | 说明 |
|------|------|------|------|
| 98 | `98-clean-mall-and-mp-data.sql` | 96 | 商城与公众号数据清理 |
| 110 | `110-finance-asset-expense-source-test-data.sql` | 187 | 资产费用来源测试数据 |
| 113 | `113-erp-production-inbound-phase1.sql` | 38 | 生产入库一期 |
| 114 | `114-erp-mrp-production-inbound-menu.sql` | 114 | 生产入库菜单 |

## 独立脚本

| 文件 | 大小 | 说明 |
|------|------|------|
| `mysql/bpm-2025-03-17-违规查询.sql` | 618KB | BPM 违规数据查询（一次性） |
| `mysql/erp-2024-05-03.sql` | 72KB | ERP 历史数据 |
| `mysql/erp-finance-void-2026-05-22.sql` | <1KB | 财务作废操作 |
| `mysql/erp-mrp-2026-03-31.sql` | 9KB | MRP 历史数据 |
| `mysql/erp_purchase_export_test_data.sql` | 38KB | 采购导出测试数据 |
| `mysql/local-erp-closure-bootstrap-2026-04-21.sql` | 8KB | 本地关账引导数据 |
| `project.sql` | 22KB | 项目管理模块表结构 |
| `project_comment.sql` | 3KB | 项目评论表 |
| `project_notify_template.sql` | 1KB | 项目通知模板 |

## 归档脚本

`handoff-archive/` 目录存放功能交付时产生的 SQL 脚本，供历史追溯，不在常规部署流程中。

| 文件 | 大小 | 说明 |
|------|------|------|
| `apply_latest_menu_assignments.sql` | 4KB | 菜单分配（原 tmp/） |
| `enable_root_menus.sql` | <1KB | 启用根菜单（原 tmp/） |
| `finance-module6-readonly-menu-permission-fix-2026-04-30.sql` | 14KB | 财务模块只读菜单权限修复 |
| `finance-module6-smoke-seed-2026-04-30.sql` | 9KB | 财务模块冒烟测试种子数据 |
| `phase1-mainline-real-acceptance-data-pack-2026-05-20.sql` | 54KB | 一期主链路验收数据包 |
| `project-warning-role-fix.sql` | 1KB | 项目预警角色修复（原 tmp/） |
| `restore_final_roots_visible.sql` | 1KB | 最终根菜单可见性修复（原 tmp/） |
| `seed_purchase_finance_ap_statement.sql` | 2KB | 采购财务 AP 对账单种子数据（原 tmp/） |
| `stock-batch-menu-parent-chain-diagnostics-2026-04-30.sql` | 1KB | 库存批次菜单父链诊断 |
| `stock-batch-permission-diagnostics-2026-04-30.sql` | 2KB | 库存批次权限诊断 |
| `stock-batch-query-menu-permission-2026-04-30.sql` | 2KB | 库存批次查询菜单权限 |
| `stock-batch-scm-menu-diagnostics-2026-04-30.sql` | 1KB | 库存批次 SCM 菜单诊断 |
| `stock-batch-scm-menu-permission-fix-2026-04-30.sql` | 3KB | 库存批次 SCM 权限修复 |
| `stock-scm-base-permission-fix-2026-04-30.sql` | 2KB | SCM 库存基础权限修复 |

## 多数据库版本

| 目录 | 文件 | 大小 | 状态 |
|------|------|------|------|
| `oracle/` | `ruoyi-vue-pro.sql` + `quartz.sql` | 1086KB | 仅含基础框架 48 表，未同步 ERP |
| `postgresql/` | `ruoyi-vue-pro.sql` + `quartz.sql` | 897KB | 同上 |
| `sqlserver/` | `ruoyi-vue-pro.sql` + `quartz.sql` | 1007KB | 同上 |
| `dm/` | `ruoyi-vue-pro.sql` + `quartz.sql` | 905KB | 同上 |
| `kingbase/` | `ruoyi-vue-pro.sql` + `quartz.sql` | 900KB | 同上 |
| `opengauss/` | `ruoyi-vue-pro.sql` + `quartz.sql` | 905KB | 同上 |

> 多数据库版本通过 `tools/convertor.py` 从 MySQL 版生成，当前仅覆盖基础框架。
> ERP 业务表（约 90+ 张）尚未同步到其他数据库版本。

## 已知问题

1. **编号缺口**：脚本从 04 开始，缺少 01-03（对应基础框架，未显式编号）
2. **多数据库失步**：Oracle/PostgreSQL/SQL Server 等版本缺少所有 ERP 业务表
3. **小型脚本**：`72-erp-production-issue-cost-phase1.sql`、`73-erp-finance-receipt-file-url-phase1.sql`、`77-erp-outsource-phase2.sql`、`80-erp-outsource-phase5.sql`、`95-erp-finance-voucher-amount-source-expansion.sql` 体积较小（265-424B），但均含有效 ALTER TABLE DDL，非空占位
4. ~~兼容脚本~~：已全部合并/重命名，`-compatible` 后缀已消除
