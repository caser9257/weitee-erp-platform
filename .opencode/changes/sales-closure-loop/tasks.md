# Tasks: 市场部销售执行闭环系统

---

## P0：核心链路（19 人天）

### Task 1: 合同商业条款字段扩展

**状态**：pending  
**工时**：1d  
**前置依赖**：无  
**描述**：在 CrmContractDO 中新增 10 个商业条款字段

**验收标准**：
- [ ] CrmContractDO.java 新增 10 个字段
- [ ] 字段注释完整
- [ ] DDL 脚本可执行

**涉及文件**：
- `yudao-module-crm/src/main/java/cn/iocoder/yudao/module/crm/dal/dataobject/contract/CrmContractDO.java`

---

### Task 2: 合同商业条款枚举定义

**状态**：pending  
**工时**：0.5d  
**前置依赖**：无  
**描述**：定义 ShipmentReleaseRule、InvoiceTrigger、CollectionRule 枚举

**验收标准**：
- [ ] ShipmentReleaseRule.java 创建完成
- [ ] InvoiceTrigger.java 创建完成
- [ ] CollectionRule.java 创建完成

**涉及文件**：
- `yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/enums/ShipmentReleaseRule.java`
- `yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/enums/InvoiceTrigger.java`
- `yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/enums/CollectionRule.java`

---

### Task 3: 合同表单增加商业条款 Tab

**状态**：pending  
**工时**：1.5d  
**前置依赖**：Task 1, Task 2  
**描述**：在 ContractForm.vue 中新增"商业条款"Tab 页

**验收标准**：
- [ ] Tab 页显示正常
- [ ] 放行规则、开票触发、收款规则可选择
- [ ] 预付款金额/比例条件显示
- [ ] 表单提交和回显正常

**涉及文件**：
- `yudao-ui-admin-vue3/src/views/crm/contract/ContractForm.vue`

---

### Task 4: 销售订单字段扩展

**状态**：pending  
**工时**：0.5d  
**前置依赖**：无  
**描述**：在 ErpSaleOrderDO 中新增 6 个字段

**验收标准**：
- [ ] ErpSaleOrderDO.java 新增 6 个字段
- [ ] 字段注释完整
- [ ] DDL 脚本可执行

**涉及文件**：
- `yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/dal/dataobject/sale/ErpSaleOrderDO.java`

---

### Task 5: 销售订单表单增加合同选择

**状态**：pending  
**工时**：1.5d  
**前置依赖**：Task 4  
**描述**：在 SaleOrderForm.vue 中增加合同选择联动和条款回显

**验收标准**：
- [ ] 客户选择后可筛选该客户的合同
- [ ] 合同选择后自动回显合同条款摘要
- [ ] 表单提交包含合同关联信息

**涉及文件**：
- `yudao-ui-admin-vue3/src/views/erp/sale/order/SaleOrderForm.vue`

---

### Task 6: 发货放行校验服务实现

**状态**：pending  
**工时**：3d  
**前置依赖**：Task 1, Task 4  
**描述**：实现 ErpShipmentReleaseService 接口

**验收标准**：
- [ ] ErpShipmentReleaseService 接口定义完成
- [ ] ErpShipmentReleaseServiceImpl 实现完成
- [ ] 4 种放行规则校验逻辑正确
- [ ] 通用校验（合同状态、订单状态、财务审核）正确
- [ ] 单元测试覆盖率 ≥ 80%

**涉及文件**：
- `yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/service/sale/ErpShipmentReleaseService.java`
- `yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/service/sale/ErpShipmentReleaseServiceImpl.java`
- `yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/controller/admin/sale/vo/ShipmentReleaseCheckReqVO.java`
- `yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/controller/admin/sale/vo/ShipmentReleaseResultVO.java`

---

### Task 7: 发货放行审核接口实现

**状态**：pending  
**工时**：1d  
**前置依赖**：Task 6  
**描述**：实现放行校验、财务审核接口

**验收标准**：
- [ ] POST /api/erp/shipment-release/check 接口可用
- [ ] POST /api/erp/shipment-release/approve 接口可用
- [ ] POST /api/erp/shipment-release/reject 接口可用
- [ ] 接口文档更新

**涉及文件**：
- `yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/controller/admin/sale/ErpShipmentReleaseController.java`

---

### Task 8: 发货放行审核前端页面

**状态**：pending  
**工时**：2d  
**前置依赖**：Task 7  
**描述**：新增 shipment-release/index.vue 页面

**验收标准**：
- [ ] 页面路由配置正确
- [ ] 筛选条件功能正常
- [ ] 待审核列表显示正确
- [ ] 审核操作（通过、驳回）功能正常
- [ ] 菜单配置完成

**涉及文件**：
- `yudao-ui-admin-vue3/src/views/erp/sale/shipment-release/index.vue`
- 路由配置文件
- 菜单配置

---

### Task 9: 市场执行台账后端服务

**状态**：pending  
**工时**：3d  
**前置依赖**：Task 1, Task 4, Task 6  
**描述**：实现 ErpMarketExecutionLedgerService 接口

**验收标准**：
- [ ] ErpMarketExecutionLedgerService 接口定义完成
- [ ] ErpMarketExecutionLedgerServiceImpl 实现完成
- [ ] 分页查询接口正确聚合项目、合同、订单、收款、发货、开票信息
- [ ] 统计接口返回正确的 KPI 数据
- [ ] 单元测试覆盖率 ≥ 80%

**涉及文件**：
- `yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/service/sale/ErpMarketExecutionLedgerService.java`
- `yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/service/sale/ErpMarketExecutionLedgerServiceImpl.java`
- `yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/controller/admin/sale/vo/MarketLedgerPageReqVO.java`
- `yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/controller/admin/sale/vo/MarketLedgerVO.java`
- `yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/controller/admin/sale/vo/MarketLedgerStatsVO.java`

---

### Task 10: 市场执行台账前端页面

**状态**：pending  
**工时**：3d  
**前置依赖**：Task 9  
**描述**：新增 market-ledger/index.vue 页面

**验收标准**：
- [ ] 页面路由配置正确
- [ ] 筛选条件功能正常（基础 + 高级折叠）
- [ ] KPI 统计卡片显示正确
- [ ] 台账列表显示正确（列聚合）
- [ ] 详情 Drawer 功能正常
- [ ] 菜单配置完成

**涉及文件**：
- `yudao-ui-admin-vue3/src/views/erp/sale/market-ledger/index.vue`
- 路由配置文件
- 菜单配置

---

### Task 11: 项目生命周期字段扩展

**状态**：pending  
**工时**：0.5d  
**前置依赖**：无  
**描述**：在 pmo_project 表增加生命周期字段

**验收标准**：
- [ ] PmoProjectDO.java 新增 7 个字段
- [ ] 字段注释完整
- [ ] DDL 脚本可执行

**涉及文件**：
- `yudao-module-pmo/src/main/java/cn/iocoder/yudao/module/pmo/dal/dataobject/project/PmoProjectDO.java`

---

### Task 12: 项目生命周期时间线表

**状态**：pending  
**工时**：1d  
**前置依赖**：无  
**描述**：创建 pmo_project_lifecycle_timeline 表

**验收标准**：
- [ ] PmoProjectLifecycleTimelineDO.java 创建完成
- [ ] PmoProjectLifecycleTimelineMapper.java 创建完成
- [ ] DDL 脚本可执行

**涉及文件**：
- `yudao-module-pmo/src/main/java/cn/iocoder/yudao/module/pmo/dal/dataobject/project/PmoProjectLifecycleTimelineDO.java`
- `yudao-module-pmo/src/main/java/cn/iocoder/yudao/module/pmo/dal/mysql/project/PmoProjectLifecycleTimelineMapper.java`

---

### Task 13: 项目生命周期服务实现

**状态**：pending  
**工时**：2d  
**前置依赖**：Task 11, Task 12  
**描述**：实现 PmoProjectLifecycleService 接口

**验收标准**：
- [ ] PmoProjectLifecycleService 接口定义完成
- [ ] PmoProjectLifecycleServiceImpl 实现完成
- [ ] 生命周期阶段更新逻辑正确
- [ ] 时间线记录逻辑正确
- [ ] 项目聚合状态刷新逻辑正确
- [ ] 单元测试覆盖率 ≥ 80%

**涉及文件**：
- `yudao-module-pmo/src/main/java/cn/iocoder/yudao/module/pmo/service/project/PmoProjectLifecycleService.java`
- `yudao-module-pmo/src/main/java/cn/iocoder/yudao/module/pmo/service/project/PmoProjectLifecycleServiceImpl.java`

---

### Task 14: 项目生命周期前端页面

**状态**：pending  
**工时**：2d  
**前置依赖**：Task 13  
**描述**：新增 lifecycle/index.vue 页面

**验收标准**：
- [ ] 页面路由配置正确
- [ ] 生命周期阶段流转图显示正确
- [ ] 时间线记录显示正确
- [ ] 菜单配置完成

**涉及文件**：
- `yudao-ui-admin-vue3/src/views/pmo/project/lifecycle/index.vue`
- 路由配置文件
- 菜单配置

---

## P1：功能完善（12 人天）

### Task 15: 销项开票模块

**状态**：pending  
**工时**：5d  
**前置依赖**：P0 完成  
**描述**：新增 erp_invoice 表和相关服务

**验收标准**：
- [ ] erp_invoice 表创建完成
- [ ] 开票服务实现完成
- [ ] 开票状态同步逻辑正确
- [ ] 前端页面完成

---

### Task 16: 生命周期细化节点

**状态**：pending  
**工时**：2d  
**前置依赖**：Task 14  
**描述**：时间线展示、节点详情

**验收标准**：
- [ ] 时间线展示优化
- [ ] 节点详情查看功能
- [ ] 阶段流转动画效果

---

### Task 17: 市场预警与统计

**状态**：pending  
**工时**：3d  
**前置依赖**：P0 完成  
**描述**：预警规则、统计报表

**验收标准**：
- [ ] 预警规则配置功能
- [ ] 预警事件触发和通知
- [ ] 统计报表展示

---

### Task 18: 合同导入增强

**状态**：pending  
**工时**：2d  
**前置依赖**：Task 3  
**描述**：合同批量导入、模板下载

**验收标准**：
- [ ] 合同导入模板下载
- [ ] 合同批量导入功能
- [ ] 导入结果反馈

---

## 里程碑

| 里程碑 | 完成标准 | 预计时间 |
|--------|----------|----------|
| M1: 数据模型完成 | Task 1, 2, 4, 11, 12 完成 | Week 1 |
| M2: 放行校验完成 | Task 6, 7, 8 完成 | Week 2 |
| M3: 市场台账完成 | Task 9, 10 完成 | Week 3 |
| M4: 生命周期完成 | Task 13, 14 完成 | Week 3 |
| M5: P0 全部完成 | Task 1-14 全部完成 | Week 4 |
