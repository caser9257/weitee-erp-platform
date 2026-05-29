# hm-MES 前后端联动开发顺序与接口对照表

## 1. 文档目标

本文档用于指导 hm-MES 一期开发时，前后端如何按阶段联动推进，减少互相等待、返工和接口反复调整。

文档目标：

- 明确前后端推荐开发顺序
- 明确每个阶段的依赖关系
- 明确后端接口与前端页面的对照关系
- 明确每个阶段的最小联调闭环

适用范围：

- 后端：`D:\ruoyi-vue-pro\yudao-module-erp`
- 前端：`D:\ruoyi-vue-pro\yudao-ui\yudao-ui-admin-vue3`

---

## 2. 联动原则

### 2.1 总体原则

- 先后端定义数据模型，再做前端页面
- 先交付 simple list / 基础 CRUD，再做复杂联动页面
- 先完成主流程链路，再做聚合看板和增强功能
- 前端优先对接稳定接口，不在业务未定时先做复杂交互

### 2.2 推荐节奏

每个阶段按照以下顺序推进：

1. 后端表结构与 DO/VO 定稿
2. 后端基础接口完成
3. 前端 API 类型定义完成
4. 前端页面开发
5. 前后端联调
6. 补查询增强和体验优化

### 2.3 联调策略

建议每一批开发都至少保证一个最小闭环，例如：

- 项目创建 -> 销售订单绑定项目
- MRP 创建 -> 运行 -> 查看结果
- 建议转单 -> 工单 -> 完工

---

## 3. 总体开发顺序

推荐总顺序如下：

1. 项目化销售
2. 制造主数据补强
3. BOM / MRP 交期驱动与项目追溯
4. 建议单转采购单 / 工单
5. 工单与完工闭环
6. 采购追溯与库存增强
7. 工艺管理
8. 文件管理
9. 项目看板

原因：

- `项目` 和 `交期` 是 MRP 的需求入口
- `产品制造属性` 和 `计划参数` 是 MRP 的规则输入
- `MRP 建议` 是采购和生产执行的上游
- `工单与完工` 是生产闭环的最小落地点
- `工艺、文件、看板` 更适合作为主链路稳定后的增强模块

---

## 4. 阶段联动开发顺序

## S1 项目化销售

### 4.1 本阶段目标

打通：

`项目 -> 销售订单`

### 4.2 后端先做

数据库：

- 新增 `erp_project`
- `erp_sale_order` 增加 `project_id`
- `erp_sale_order` 增加 `delivery_date`

后端类：

- `ErpProjectDO`
- `ErpProjectMapper`
- `ErpProjectService`
- `ErpProjectServiceImpl`
- `ErpProjectController`

改造：

- `ErpSaleOrderDO`
- 销售订单 VO
- 销售订单 Service
- 销售订单分页查询

### 4.3 后端需优先提供的接口

#### 项目接口

- `GET /erp/project/page`
- `GET /erp/project/get`
- `POST /erp/project/create`
- `PUT /erp/project/update`
- `DELETE /erp/project/delete`
- `GET /erp/project/simple-list`

#### 销售订单改造接口

- `GET /erp/sale-order/page`
- `GET /erp/sale-order/get`
- `POST /erp/sale-order/create`
- `PUT /erp/sale-order/update`

新增/改造字段：

- `projectId`
- `projectName`
- `deliveryDate`

### 4.4 前端跟进开发

API：

- `src/api/erp/project/index.ts`
- `src/api/erp/sale/order/index.ts`

页面：

- `src/views/erp/project/project/index.vue`
- `src/views/erp/project/project/ProjectForm.vue`
- 改造 `src/views/erp/sale/order/index.vue`
- 改造 `src/views/erp/sale/order/SaleOrderForm.vue`

### 4.5 本阶段最小联调闭环

1. 创建项目
2. 创建销售订单并绑定项目、交期
3. 销售订单列表按项目筛选
4. 销售订单详情显示项目与交期

### 4.6 联调完成标志

- 前端可独立完成项目管理
- 销售订单已具备项目维度
- 后续 MRP 可直接消费 `projectId + deliveryDate`

---

## S2 制造主数据补强

### 5.1 本阶段目标

打通：

`产品制造属性 -> 计划参数 -> BOM`

### 5.2 后端先做

数据库：

- `erp_product` 增加制造属性字段
- 复用或补强 `erp_material_plan_rule`

后端类改造：

- `ErpProductDO`
- 产品相关 VO / Service / Mapper
- `ErpMaterialPlanRuleController`
- `ErpMaterialPlanRuleService`

BOM 补强：

- `ErpBomServiceImpl`

### 5.3 后端需优先提供的接口

#### 产品接口

- `GET /erp/product/page`
- `GET /erp/product/get`
- `POST /erp/product/create`
- `PUT /erp/product/update`

新增/改造字段：

- `supplyType`
- `mrpEnable`
- `defaultRouteId`
- `defaultSupplierId`

#### 计划参数接口

- `GET /erp/material-plan-rule/page`
- `GET /erp/material-plan-rule/get`
- `POST /erp/material-plan-rule/create`
- `PUT /erp/material-plan-rule/update`
- `DELETE /erp/material-plan-rule/delete`

#### BOM 接口

- `GET /erp/bom/page`
- `GET /erp/bom/get`
- `POST /erp/bom/create`
- `PUT /erp/bom/update`
- `DELETE /erp/bom/delete`

### 5.4 前端跟进开发

API：

- `src/api/erp/product/product/index.ts`
- `src/api/erp/mrp/plan-rule/index.ts`
- `src/api/erp/mrp/bom/index.ts`

页面：

- 改造 `src/views/erp/product/product/ProductForm.vue`
- `src/views/erp/mrp/plan-rule/index.vue`
- `src/views/erp/mrp/plan-rule/PlanRuleForm.vue`
- `src/views/erp/mrp/bom/index.vue`
- `src/views/erp/mrp/bom/BomForm.vue`

### 5.5 本阶段最小联调闭环

1. 创建产品并维护制造属性
2. 为产品维护计划参数
3. 创建产品 BOM
4. 能查询到产品、计划参数、BOM 全部信息

### 5.6 联调完成标志

- MRP 所需主数据齐备
- 产品制造属性与计划参数稳定
- BOM 能用于后续运算

---

## S3 BOM / MRP 交期驱动与项目追溯

### 6.1 本阶段目标

打通：

`销售订单(项目+交期) -> MRP -> 结果/缺料/建议`

### 6.2 后端先做

数据库：

- `erp_mrp_demand` 增加 `project_id`
- `erp_mrp_result` 增加 `project_id`
- `erp_mrp_shortage` 增加 `project_id`
- `erp_purchase_suggest` 增加 `project_id`
- `erp_production_suggest` 增加 `project_id`

核心改造：

- `ErpMrpCalcServiceImpl`

改造重点：

- 需求日期从 `orderTime` 切换为 `deliveryDate`
- 结果、缺料、建议透传 `projectId`
- 查询支持按项目筛选

### 6.3 后端需优先提供的接口

#### MRP 计划接口

- `POST /erp/mrp-plan/create`
- `POST /erp/mrp-plan/run`
- `GET /erp/mrp-plan/get`
- `GET /erp/mrp-plan/page`

#### MRP 结果接口

- `GET /erp/mrp-plan/result`
- `GET /erp/mrp-plan/shortage`

#### 建议接口

- `GET /erp/mrp-suggest/purchase/page`
- `GET /erp/mrp-suggest/production/page`

建议返回字段至少包含：

- `projectId`
- `projectName`
- `sourceOrderId`
- `sourceItemId`

### 6.4 前端跟进开发

API：

- `src/api/erp/mrp/plan/index.ts`
- `src/api/erp/mrp/suggest/index.ts`

页面：

- `src/views/erp/mrp/plan/index.vue`
- `src/views/erp/mrp/plan/PlanForm.vue`
- `src/views/erp/mrp/result/index.vue`
- `src/views/erp/mrp/shortage/index.vue`
- `src/views/erp/mrp/suggest/purchase.vue`
- `src/views/erp/mrp/suggest/production.vue`

### 6.5 本阶段最小联调闭环

1. 创建 MRP 计划
2. 运行 MRP
3. 查看 MRP 结果
4. 查看缺料
5. 查看采购建议和生产建议

### 6.6 联调完成标志

- MRP 按交期运算
- MRP 结果能追溯项目
- 建议页能按项目筛选

---

## S4 建议单转采购单 / 工单

### 7.1 本阶段目标

打通：

`采购建议 -> 采购单`

`生产建议 -> 工单`

### 7.2 后端先做

采购方向：

- 建议单转采购单服务
- 采购单增加来源字段

生产方向：

- 建议单转工单服务
- 工单增加项目与来源字段

### 7.3 后端需优先提供的接口

#### 采购建议转单接口

- `POST /erp/mrp-suggest/purchase/convert`

#### 生产建议转工单接口

- `POST /erp/mrp-suggest/production/convert`

#### 采购订单查询接口增强

- `GET /erp/purchase-order/page`
- `GET /erp/purchase-order/get`

#### 工单查询接口增强

- `GET /erp/production-order/page`
- `GET /erp/production-order/get`

新增返回字段建议：

- `projectId`
- `projectName`
- `sourceType`
- `sourceId`
- `sourceItemId`

### 7.4 前端跟进开发

页面改造：

- `src/views/erp/mrp/suggest/purchase.vue`
- `src/views/erp/mrp/suggest/production.vue`
- `src/views/erp/purchase/order/index.vue`
- `src/views/erp/production/order/index.vue`

API 改造：

- `src/api/erp/mrp/suggest/index.ts`
- `src/api/erp/purchase/order/index.ts`
- `src/api/erp/production/order/index.ts`

### 7.5 本阶段最小联调闭环

1. 采购建议转采购单
2. 生产建议转工单
3. 采购单可查看来源
4. 工单可查看来源

### 7.6 联调完成标志

- 建议单转单可用
- 来源追溯不断链
- 前端可跳转或展示来源信息

---

## S5 工单与完工闭环

### 8.1 本阶段目标

打通：

`工单 -> 下达 -> 完工`

### 8.2 后端先做

数据库：

- `erp_production_order` 增加项目与来源字段
- 新增 `erp_production_complete_record`

服务改造：

- 工单下达逻辑
- 完工登记逻辑
- 工单数量回写逻辑
- 工单状态流转逻辑

### 8.3 后端需优先提供的接口

#### 工单接口

- `POST /erp/production-order/create`
- `PUT /erp/production-order/update`
- `PUT /erp/production-order/release`
- `PUT /erp/production-order/finish`
- `GET /erp/production-order/get`
- `GET /erp/production-order/page`

#### 完工记录接口

- `POST /erp/production-complete-record/create`
- `GET /erp/production-complete-record/page`

### 8.4 前端跟进开发

API：

- `src/api/erp/production/order/index.ts`
- `src/api/erp/production/complete/index.ts`

页面：

- `src/views/erp/production/order/index.vue`
- `src/views/erp/production/order/ProductionOrderForm.vue`
- `src/views/erp/production/complete/index.vue`
- `src/views/erp/production/complete/CompleteForm.vue`

### 8.5 本阶段最小联调闭环

1. 查看工单
2. 下达工单
3. 完工登记
4. 工单已完工数量变化
5. 工单状态变化

### 8.6 联调完成标志

- 工单具备最小执行闭环
- 完工后数据能回写
- 后续报工和领料可继续在此基础扩展

---

## S6 采购追溯与库存增强

### 9.1 本阶段目标

打通：

`采购单 / 入库 -> 来源追溯`

`库存 -> MRP 统一口径`

### 9.2 后端先做

采购追溯：

- 采购单增加来源查询字段
- 入库增加来源说明字段
- 增加按项目聚合采购进度接口

库存增强：

- 新增库存统一口径服务
- 统一可用库存 / 在途 / 在制 / 占用口径

### 9.3 后端需优先提供的接口

#### 采购接口增强

- `GET /erp/purchase-order/page`
- `GET /erp/purchase-order/get`
- `GET /erp/purchase-in/page`

#### 库存接口增强

- `GET /erp/stock/page`
- `GET /erp/stock-record/page`

新增返回字段建议：

- `projectName`
- `sourceType`
- `sourceRemark`
- `reservedCount`
- `incomingCount`
- `wipCount`
- `availableCount`

### 9.4 前端跟进开发

页面改造：

- `src/views/erp/purchase/order/index.vue`
- `src/views/erp/purchase/in/index.vue`
- `src/views/erp/stock/stock/index.vue`
- `src/views/erp/stock/record/index.vue`

API 改造：

- `src/api/erp/purchase/order/index.ts`
- `src/api/erp/purchase/in/index.ts`
- `src/api/erp/stock/stock/index.ts`
- `src/api/erp/stock/record/index.ts`

### 9.5 本阶段最小联调闭环

1. 采购订单展示来源
2. 采购入库展示来源
3. 库存台账展示占用 / 在途 / 在制
4. 与 MRP 结果口径一致

### 9.6 联调完成标志

- 采购链路来源可追溯
- 库存口径统一
- 为报工/领料等二期功能做好底座

---

## S7 工艺管理

### 10.1 本阶段目标

打通：

`工序 -> 工艺路线 -> 产品默认路线`

### 10.2 后端先做

数据库：

- `mes_process`
- `mes_route`
- `mes_route_step`

服务与接口：

- 工序 CRUD
- 路线 CRUD
- 路线步骤保存
- 产品默认路线绑定

### 10.3 后端需优先提供的接口

#### 工序接口

- `GET /erp/process/page`
- `GET /erp/process/get`
- `POST /erp/process/create`
- `PUT /erp/process/update`
- `DELETE /erp/process/delete`
- `GET /erp/process/simple-list`

#### 工艺路线接口

- `GET /erp/route/page`
- `GET /erp/route/get`
- `POST /erp/route/create`
- `PUT /erp/route/update`
- `DELETE /erp/route/delete`
- `GET /erp/route/simple-list`

### 10.4 前端跟进开发

API：

- `src/api/erp/process/index.ts`
- `src/api/erp/route/index.ts`

页面：

- `src/views/erp/process/process/index.vue`
- `src/views/erp/process/process/ProcessForm.vue`
- `src/views/erp/route/route/index.vue`
- `src/views/erp/route/route/RouteForm.vue`

产品页面改造：

- `src/views/erp/product/product/ProductForm.vue`

### 10.5 本阶段最小联调闭环

1. 创建工序
2. 创建工艺路线
3. 配置工艺路线步骤
4. 产品绑定默认路线

### 10.6 联调完成标志

- 工艺基础数据稳定
- 可为工单和报工扩展提供支撑

---

## S8 文件管理

### 11.1 本阶段目标

打通：

`文件上传 -> 文件元数据 -> 业务关联`

### 11.2 后端先做

数据库：

- `file_document`
- `file_document_rel`

服务：

- 上传后元数据入库
- 文件与业务绑定
- 按业务查询附件

### 11.3 后端需优先提供的接口

- `GET /erp/file-document/page`
- `GET /erp/file-document/get`
- `POST /erp/file-document/create`
- `DELETE /erp/file-document/delete`
- `GET /erp/file-document/list-by-biz`

### 11.4 前端跟进开发

API：

- `src/api/erp/file/index.ts`

页面：

- `src/views/erp/file/document/index.vue`
- `src/views/erp/file/document/DocumentForm.vue`

局部增强：

- 项目详情资料区
- 产品详情资料区
- BOM 详情资料区
- 工艺路线详情资料区

### 11.5 本阶段最小联调闭环

1. 上传文件
2. 绑定到业务对象
3. 在业务详情页查看文件

### 11.6 联调完成标志

- 文件能按业务对象正确关联
- 附件查询逻辑稳定

---

## S9 项目看板

### 12.1 本阶段目标

打通：

`项目 -> 销售 / 采购 / 生产 / 缺料 聚合`

### 12.2 后端先做

聚合服务：

- 项目概览
- 项目采购进度
- 项目生产进度
- 项目缺料汇总

### 12.3 后端需优先提供的接口

- `GET /erp/project-dashboard/overview`
- `GET /erp/project-dashboard/purchase-progress`
- `GET /erp/project-dashboard/production-progress`
- `GET /erp/project-dashboard/shortage-summary`

### 12.4 前端跟进开发

API：

- `src/api/erp/project-dashboard/index.ts`

页面：

- `src/views/erp/project-dashboard/index.vue`

### 12.5 本阶段最小联调闭环

1. 选择项目
2. 查看项目整体概况
3. 查看采购进度
4. 查看生产进度
5. 查看缺料情况

### 12.6 联调完成标志

- 项目维度的核心状态可被汇总查看

---

## 5. 接口与前端页面对照总表

| 阶段 | 后端接口 | 前端 API 文件 | 前端页面 |
| --- | --- | --- | --- |
| S1 | `/erp/project/*` | `src/api/erp/project/index.ts` | `src/views/erp/project/project/*` |
| S1 | `/erp/sale-order/*` | `src/api/erp/sale/order/index.ts` | `src/views/erp/sale/order/*` |
| S2 | `/erp/material-plan-rule/*` | `src/api/erp/mrp/plan-rule/index.ts` | `src/views/erp/mrp/plan-rule/*` |
| S2 | `/erp/bom/*` | `src/api/erp/mrp/bom/index.ts` | `src/views/erp/mrp/bom/*` |
| S2 | `/erp/product/*` | `src/api/erp/product/product/index.ts` | `src/views/erp/product/product/*` |
| S3 | `/erp/mrp-plan/*` | `src/api/erp/mrp/plan/index.ts` | `src/views/erp/mrp/plan/*` |
| S3 | `/erp/mrp-plan/result` | `src/api/erp/mrp/plan/index.ts` | `src/views/erp/mrp/result/index.vue` |
| S3 | `/erp/mrp-plan/shortage` | `src/api/erp/mrp/plan/index.ts` | `src/views/erp/mrp/shortage/index.vue` |
| S3 | `/erp/mrp-suggest/*` | `src/api/erp/mrp/suggest/index.ts` | `src/views/erp/mrp/suggest/*` |
| S4 | `/erp/mrp-suggest/purchase/convert` | `src/api/erp/mrp/suggest/index.ts` | `src/views/erp/mrp/suggest/purchase.vue` |
| S4 | `/erp/mrp-suggest/production/convert` | `src/api/erp/mrp/suggest/index.ts` | `src/views/erp/mrp/suggest/production.vue` |
| S4 | `/erp/purchase-order/*` | `src/api/erp/purchase/order/index.ts` | `src/views/erp/purchase/order/*` |
| S4-S5 | `/erp/production-order/*` | `src/api/erp/production/order/index.ts` | `src/views/erp/production/order/*` |
| S5 | `/erp/production-complete-record/*` | `src/api/erp/production/complete/index.ts` | `src/views/erp/production/complete/*` |
| S6 | `/erp/purchase-in/*` | `src/api/erp/purchase/in/index.ts` | `src/views/erp/purchase/in/*` |
| S6 | `/erp/stock/*` | `src/api/erp/stock/stock/index.ts` | `src/views/erp/stock/stock/*` |
| S6 | `/erp/stock-record/*` | `src/api/erp/stock/record/index.ts` | `src/views/erp/stock/record/*` |
| S7 | `/erp/process/*` | `src/api/erp/process/index.ts` | `src/views/erp/process/process/*` |
| S7 | `/erp/route/*` | `src/api/erp/route/index.ts` | `src/views/erp/route/route/*` |
| S8 | `/erp/file-document/*` | `src/api/erp/file/index.ts` | `src/views/erp/file/document/*` |
| S9 | `/erp/project-dashboard/*` | `src/api/erp/project-dashboard/index.ts` | `src/views/erp/project-dashboard/index.vue` |

---

## 6. 每阶段接口交付优先级

### 6.1 第一优先

必须先给前端的接口：

- `simple-list`
- `page`
- `get`
- `create`
- `update`

原因：

- 这类接口足够支撑列表、表单、详情三大基础页面

### 6.2 第二优先

联动类接口：

- `run`
- `convert`
- `release`
- `finish`

原因：

- 这类接口依赖前置模型与数据已经稳定

### 6.3 第三优先

聚合和增强类接口：

- `dashboard`
- `summary`
- `progress`

原因：

- 这类接口适合在主链路可跑通后再补

---

## 7. 推荐排期视角

## 第 1 周

后端：

- 项目表与销售订单项目化
- 产品制造属性
- 计划参数接口

前端：

- 项目页面
- 销售订单项目化改造
- 产品制造属性改造
- 计划参数页面

## 第 2 周

后端：

- BOM 增强
- MRP 交期驱动
- MRP 项目追溯

前端：

- BOM 页面
- MRP 计划页
- MRP 结果页
- 缺料页
- 建议页

## 第 3 周

后端：

- 建议转采购单 / 工单
- 工单增强
- 完工逻辑

前端：

- 建议转单
- 工单页面
- 完工页面

## 第 4 周

后端：

- 采购追溯
- 库存口径
- 工艺管理或文件管理

前端：

- 采购来源增强
- 库存增强
- 工艺页面或文件页面

---

## 8. 一期最小联动闭环

如果资源有限，建议至少完成以下闭环：

1. 项目 CRUD
2. 销售订单绑定项目与交期
3. 产品制造属性 + 计划参数 + BOM
4. 创建并运行 MRP
5. 查看结果 / 缺料 / 建议
6. 生产建议转工单
7. 工单下达
8. 完工登记

对应链路：

`项目 -> 销售订单 -> MRP -> 生产建议 -> 工单 -> 完工`

如果再补采购链路，则是：

`项目 -> 销售订单 -> MRP -> 采购建议 -> 采购单 -> 入库`

---

## 9. 风险与避免方式

### 9.1 风险：前端先做，后端字段反复改

避免方式：

- 先定 VO 字段
- 先给 simple list 和详情返回结构

### 9.2 风险：主链路未闭环就开始做看板

避免方式：

- 看板永远放在主链路之后

### 9.3 风险：转单逻辑和来源字段不统一

避免方式：

- 统一使用：
  - `projectId`
  - `sourceType`
  - `sourceId`
  - `sourceItemId`

### 9.4 风险：库存口径前后不一致

避免方式：

- 后端统一库存聚合服务
- 前端不自行计算库存衍生值

---

## 10. 使用建议

你可以把本文档直接当作排期依据：

- 后端按阶段交接口
- 前端按阶段接页面
- 每阶段结束必须完成一个最小闭环联调

建议评审时三份文档配合使用：

- `hm-MES前端详细开发任务清单.md`
- `hm-MES后端详细开发任务清单.md`
- `hm-MES前后端联动开发顺序与接口对照表.md`

---
