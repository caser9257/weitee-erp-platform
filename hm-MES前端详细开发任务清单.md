# hm-MES 前端详细开发任务清单

## 1. 文档目标

本文档用于将 hm-MES 一期前端工作拆解为可直接执行的开发任务，基于当前项目现状进行增量开发。

适用项目：

- `D:\ruoyi-vue-pro\yudao-ui\yudao-ui-admin-vue3`

开发原则：

- 基于现有 ERP 前端结构扩展，不新建独立前端项目
- 复用现有 `views + api + components + dialog/form` 模式
- 优先完成“最小业务闭环”
- 先打通主流程，再补看板、资料、增强能力

---

## 2. 当前前端现状

当前已有 ERP 页面目录：

- `src/views/erp/finance`
- `src/views/erp/home`
- `src/views/erp/product`
- `src/views/erp/purchase`
- `src/views/erp/sale`
- `src/views/erp/stock`

当前已有 ERP API 目录：

- `src/api/erp/finance`
- `src/api/erp/product`
- `src/api/erp/purchase`
- `src/api/erp/sale`
- `src/api/erp/statistics`
- `src/api/erp/stock`

当前可直接复用的页面模式：

- 列表页：`index.vue`
- 表单弹窗页：`xxxForm.vue`
- 子表单组件：`components/*`
- 详情能力：通常通过表单回显或独立弹窗承载
- API 组织：每个业务目录下一个 `index.ts`

当前明显缺失的 hm-MES 页面域：

- `project`
- `mrp`
- `process`
- `route`
- `production`
- `file`
- `project-dashboard`

---

## 3. 一期建议范围

建议一期前端优先完成以下模块：

1. 项目化销售
2. 制造主数据补强
3. BOM / MRP
4. 生产工单与完工闭环
5. 采购来源追溯
6. 库存口径展示增强

建议二期再做：

1. 工艺管理完整能力
2. 文件管理
3. 项目进度看板
4. 报工、领料等更复杂执行闭环

---

## 4. 前端目录规划

建议新增以下目录：

### 4.1 API 目录

- `src/api/erp/project`
- `src/api/erp/mrp/bom`
- `src/api/erp/mrp/plan`
- `src/api/erp/mrp/plan-rule`
- `src/api/erp/mrp/suggest`
- `src/api/erp/process`
- `src/api/erp/route`
- `src/api/erp/production/order`
- `src/api/erp/production/complete`
- `src/api/erp/file`
- `src/api/erp/project-dashboard`

### 4.2 页面目录

- `src/views/erp/project/project`
- `src/views/erp/mrp/bom`
- `src/views/erp/mrp/plan`
- `src/views/erp/mrp/result`
- `src/views/erp/mrp/shortage`
- `src/views/erp/mrp/suggest`
- `src/views/erp/process/process`
- `src/views/erp/route/route`
- `src/views/erp/production/order`
- `src/views/erp/production/complete`
- `src/views/erp/file/document`
- `src/views/erp/project-dashboard`

---

## 5. 开发阶段与优先级

| 阶段 | 模块 | 优先级 | 状态建议 |
| --- | --- | --- | --- |
| S1 | 项目化销售 | P0 | 立即开发 |
| S1 | 制造主数据补强 | P0 | 立即开发 |
| S2 | BOM / MRP | P0 | 紧接开发 |
| S2 | 生产工单与完工 | P0 | 紧接开发 |
| S3 | 采购追溯 / 库存增强 | P1 | 跟进开发 |
| S4 | 工艺管理 | P1 | 二阶段 |
| S5 | 文件管理 / 项目看板 | P2 | 后置 |

---

# 6. 模块级详细任务

## M1 项目化销售

### 6.1 目标

在现有销售订单能力基础上，增加“项目”维度，使销售订单能够关联项目和交期，并支持项目管理。

### 6.2 需新增 API

路径：

- `src/api/erp/project/index.ts`

接口建议：

- `getProjectPage`
- `getProject`
- `createProject`
- `updateProject`
- `deleteProject`
- `getProjectSimpleList`

### 6.3 需新增页面

路径：

- `src/views/erp/project/project/index.vue`
- `src/views/erp/project/project/ProjectForm.vue`

页面任务：

#### 项目列表页

- 展示项目编号、项目名称、客户、状态、交期、创建时间
- 支持按项目编号、项目名称、客户、状态筛选
- 支持新增、编辑、删除、导出
- 支持点击查看详情

#### 项目表单页

- 新增项目
- 编辑项目
- 查看项目详情
- 录入字段：
  - 项目编号
  - 项目名称
  - 客户
  - 交期
  - 状态
  - 备注

### 6.4 需改造销售订单 API

路径：

- `src/api/erp/sale/order/index.ts`

任务：

- 在 `SaleOrderVO` 中增加：
  - `projectId`
  - `projectName`
  - `deliveryDate`
- 分页参数增加：
  - `projectId`
  - `deliveryDate`
- 详情接口适配新字段

### 6.5 需改造销售订单页面

路径：

- `src/views/erp/sale/order/index.vue`
- `src/views/erp/sale/order/SaleOrderForm.vue`

任务：

#### 销售订单列表页改造

- 查询区域增加项目选择
- 列表列增加项目名称
- 列表列增加交期
- 支持按项目筛选

#### 销售订单表单改造

- 增加项目下拉框
- 增加交期日期选择器
- 打开表单时加载项目简单列表
- 保存时提交 `projectId` 与 `deliveryDate`

#### 销售订单详情展示改造

- 增加项目名称展示
- 增加交期展示

### 6.6 联调检查项

- 项目可正常创建与查询
- 销售订单可关联项目
- 编辑回显正确
- 列表筛选正确
- 交期格式显示正确

### 6.7 交付物

- 项目管理页面
- 项目 API
- 销售订单项目化改造

---

## M2 制造主数据补强

### 7.1 目标

为产品补充制造属性，并新增 BOM 与计划参数维护页面，形成 MRP 输入基础。

### 7.2 需新增 API

路径：

- `src/api/erp/mrp/bom/index.ts`
- `src/api/erp/mrp/plan-rule/index.ts`

接口建议：

#### BOM API

- `getBomPage`
- `getBom`
- `createBom`
- `updateBom`
- `deleteBom`
- `getBomSimpleList`

#### 计划参数 API

- `getPlanRulePage`
- `getPlanRule`
- `createPlanRule`
- `updatePlanRule`
- `deletePlanRule`

### 7.3 需新增页面

路径：

- `src/views/erp/mrp/bom/index.vue`
- `src/views/erp/mrp/bom/BomForm.vue`
- `src/views/erp/mrp/bom/components/BomItemForm.vue`
- `src/views/erp/mrp/plan-rule/index.vue`
- `src/views/erp/mrp/plan-rule/PlanRuleForm.vue`

页面任务：

#### BOM 列表页

- 展示 BOM 编码、产品、版本、状态、备注、创建时间
- 支持按产品、编码、状态筛选
- 支持新增、编辑、删除、查看

#### BOM 表单页

- 录入 BOM 头信息
- 录入子项明细
- 子项字段：
  - 物料
  - 用量
  - 损耗率
  - 提前期
  - 物料类型
  - 排序

#### 计划参数页

- 展示产品计划参数
- 支持维护：
  - 供给方式
  - 安全库存
  - 最小采购量
  - 倍量
  - 采购提前期
  - 生产提前期
  - 默认供应商
  - 启用状态

### 7.4 需改造产品 API

路径：

- `src/api/erp/product/product/index.ts`

任务：

在产品 VO 中增加制造属性字段，例如：

- `supplyType`
- `mrpEnable`
- `defaultRouteId`
- `defaultSupplierId`

### 7.5 需改造产品页面

路径：

- `src/views/erp/product/product/ProductForm.vue`
- `src/views/erp/product/product/index.vue`

任务：

#### 产品表单页改造

- 增加是否参与 MRP
- 增加供给方式
- 增加默认供应商
- 增加默认工艺路线

#### 产品详情或列表增强

- 增加制造属性展示列或详情区

### 7.6 联调检查项

- BOM 可保存并回显
- BOM 明细子项提交格式正确
- 计划参数保存成功
- 产品制造属性显示正常

### 7.7 交付物

- BOM 页面
- 计划参数页面
- 产品制造属性改造

---

## M3 BOM / MRP 模块

### 8.1 目标

建设前端 MRP 完整页面，支持计划创建、运行、结果查看、缺料分析、建议查询。

### 8.2 需新增 API

路径：

- `src/api/erp/mrp/plan/index.ts`
- `src/api/erp/mrp/suggest/index.ts`

接口建议：

#### MRP 计划

- `getMrpPlanPage`
- `getMrpPlan`
- `createMrpPlan`
- `runMrpPlan`

#### MRP 结果

- `getMrpResultList`
- `getMrpShortageList`

#### 建议单

- `getPurchaseSuggestPage`
- `getProductionSuggestPage`
- `convertPurchaseSuggest`
- `convertProductionSuggest`

### 8.3 需新增页面

路径：

- `src/views/erp/mrp/plan/index.vue`
- `src/views/erp/mrp/plan/PlanForm.vue`
- `src/views/erp/mrp/result/index.vue`
- `src/views/erp/mrp/shortage/index.vue`
- `src/views/erp/mrp/suggest/purchase.vue`
- `src/views/erp/mrp/suggest/production.vue`

### 8.4 页面详细任务

#### MRP 计划列表页

- 展示计划编号、计划名称、开始日期、结束日期、状态、运行时间
- 支持新增计划
- 支持运行计划
- 支持查看结果
- 支持查看缺料
- 支持查看建议

#### MRP 计划创建页

- 录入计划名称
- 录入计划起止日期
- 录入备注
- 保存后允许一键运行

#### MRP 结果页

- 展示：
  - 根产品
  - 物料
  - 毛需求
  - 可用库存
  - 在途
  - 在制
  - 净需求
  - 建议类型
  - 建议日期
  - 项目来源
- 支持按项目、产品、物料筛选

#### 缺料页

- 展示物料、缺料数量、需求日期、项目来源
- 支持按项目筛选
- 支持跳转建议页或工单页

#### 采购建议页

- 展示物料、建议数量、建议到货日期、状态、项目来源
- 支持转采购单
- 支持查看来源单据

#### 生产建议页

- 展示产品、建议数量、建议开工/完工日期、状态、项目来源
- 支持转工单
- 支持查看来源单据

### 8.5 页面交互要求

- MRP 计划运行前二次确认
- 运行后刷新当前页
- 查看结果使用页签或跳转新页面
- 建议页支持快速跳转下游单据

### 8.6 联调检查项

- 创建计划成功
- 运行按钮触发后端
- 结果页显示字段完整
- 项目筛选生效
- 转单按钮按状态正确显示

### 8.7 交付物

- MRP 计划页
- MRP 结果页
- 缺料页
- 采购建议页
- 生产建议页

---

## M4 生产执行

### 9.1 目标

建设工单管理和完工登记前端能力，形成最小生产执行闭环。

### 9.2 需新增 API

路径：

- `src/api/erp/production/order/index.ts`
- `src/api/erp/production/complete/index.ts`

接口建议：

#### 工单 API

- `getProductionOrderPage`
- `getProductionOrder`
- `createProductionOrder`
- `updateProductionOrder`
- `releaseProductionOrder`
- `finishProductionOrder`

#### 完工 API

- `createCompleteRecord`
- `getCompleteRecordPage`

### 9.3 需新增页面

路径：

- `src/views/erp/production/order/index.vue`
- `src/views/erp/production/order/ProductionOrderForm.vue`
- `src/views/erp/production/complete/index.vue`
- `src/views/erp/production/complete/CompleteForm.vue`

### 9.4 页面详细任务

#### 生产工单列表页

- 展示工单号、产品、计划数量、已完工数量、计划开始、计划结束、状态、项目来源
- 支持按工单号、产品、状态、项目筛选
- 支持新增工单
- 支持编辑工单
- 支持下达
- 支持完工
- 支持查看详情

#### 工单表单页

- 新增工单
- 编辑工单
- 字段：
  - 产品
  - 计划数量
  - 计划开始时间
  - 计划结束时间
  - 备注

#### 完工登记页

- 展示工单信息
- 录入完工数量
- 录入完工时间
- 录入备注
- 提交后回写工单

### 9.5 页面交互要求

- 下达按钮仅在草稿状态可点
- 完工按钮仅在已下达状态可点
- 完工时校验数量不超过剩余数量
- 工单详情展示来源项目、来源订单

### 9.6 联调检查项

- 建议单转工单后列表可见
- 工单下达成功
- 完工登记后数量刷新
- 状态更新正确

### 9.7 交付物

- 工单管理页
- 完工登记页
- 工单来源展示

---

## M5 采购执行追溯

### 10.1 目标

在现有采购页面中展示来源项目和 MRP 来源信息，支持从建议单追溯到采购执行。

### 10.2 需改造 API

路径：

- `src/api/erp/purchase/order/index.ts`
- `src/api/erp/purchase/in/index.ts`

任务：

在 VO 中补充：

- `projectId`
- `projectName`
- `sourceType`
- `sourceId`
- `mrpPlanId`

### 10.3 需改造页面

路径：

- `src/views/erp/purchase/order/index.vue`
- `src/views/erp/purchase/order/PurchaseOrderForm.vue`
- `src/views/erp/purchase/in/index.vue`

任务：

#### 采购订单列表改造

- 增加项目列
- 增加来源类型列
- 增加来源单号列

#### 采购订单详情增强

- 展示来源项目
- 展示来源 MRP 建议
- 支持从详情跳转来源

#### 采购入库页面增强

- 增加入库来源说明
- 增加项目展示

### 10.4 联调检查项

- 转单生成的采购单来源字段展示正确
- 来源跳转逻辑正确
- 入库后仍可追溯来源

### 10.5 交付物

- 采购追溯增强版页面

---

## M6 制造库存口径展示

### 11.1 目标

在现有库存页面中增加制造相关库存维度展示。

### 11.2 需改造 API

路径：

- `src/api/erp/stock/stock/index.ts`
- `src/api/erp/stock/record/index.ts`

任务：

VO 增加：

- `reservedCount`
- `incomingCount`
- `wipCount`
- `availableCount`
- `sourceType`
- `sourceRemark`

### 11.3 需改造页面

路径：

- `src/views/erp/stock/stock/index.vue`
- `src/views/erp/stock/record/index.vue`

任务：

#### 库存台账页增强

- 增加占用库存列
- 增加在途库存列
- 增加在制库存列
- 增加可用库存列

#### 库存记录页增强

- 增加来源类型
- 增加来源说明
- 增加关联项目展示

### 11.4 联调检查项

- 新增库存列值显示正确
- 与 MRP 页面数据口径一致
- 来源说明显示完整

### 11.5 交付物

- 增强版库存台账页
- 增强版库存记录页

---

## M7 工艺管理

### 12.1 目标

新增工序与工艺路线维护能力，为产品和工单提供默认路线。

### 12.2 需新增 API

路径：

- `src/api/erp/process/index.ts`
- `src/api/erp/route/index.ts`

接口建议：

#### 工序 API

- `getProcessPage`
- `getProcess`
- `createProcess`
- `updateProcess`
- `deleteProcess`
- `getProcessSimpleList`

#### 工艺路线 API

- `getRoutePage`
- `getRoute`
- `createRoute`
- `updateRoute`
- `deleteRoute`
- `getRouteSimpleList`

### 12.3 需新增页面

路径：

- `src/views/erp/process/process/index.vue`
- `src/views/erp/process/process/ProcessForm.vue`
- `src/views/erp/route/route/index.vue`
- `src/views/erp/route/route/RouteForm.vue`
- `src/views/erp/route/route/components/RouteStepForm.vue`

### 12.4 页面任务

#### 工序管理页

- 展示工序编码、名称、说明、状态
- 支持 CRUD

#### 工艺路线页

- 展示路线编码、名称、适用产品、状态
- 支持 CRUD

#### 工艺路线表单

- 支持路线头信息录入
- 支持路线步骤配置
- 步骤字段：
  - 工序
  - 顺序
  - 准备工时
  - 加工工时
  - 备注

### 12.5 需改造产品页面

路径：

- `src/views/erp/product/product/ProductForm.vue`

任务：

- 增加默认工艺路线选择
- 支持选择 route simple list

### 12.6 交付物

- 工序管理页面
- 工艺路线页面
- 产品绑定工艺路线能力

---

## M8 文件管理

### 13.1 目标

在通用上传组件基础上建设业务文件台账和业务资料展示能力。

### 13.2 需新增 API

路径：

- `src/api/erp/file/index.ts`

接口建议：

- `getDocumentPage`
- `getDocument`
- `createDocument`
- `deleteDocument`
- `getBizDocumentList`

### 13.3 需新增页面

路径：

- `src/views/erp/file/document/index.vue`
- `src/views/erp/file/document/DocumentForm.vue`

### 13.4 页面任务

#### 文件台账页

- 展示文件名、分类、业务类型、业务编号、上传人、上传时间
- 支持按业务类型和业务编号筛选
- 支持下载、删除

#### 文件上传页或弹窗

- 上传文件
- 选择业务类型
- 绑定业务 ID
- 提交保存

### 13.5 页面局部增强

以下页面增加资料区：

- 项目详情页
- 产品详情页
- BOM 详情页
- 工艺路线详情页

资料区能力：

- 文件列表
- 上传
- 下载
- 删除

### 13.6 交付物

- 文件台账页
- 业务资料区块

---

## M9 项目进度看板

### 14.1 目标

通过聚合展示项目在销售、采购、生产、缺料等维度的执行状态。

### 14.2 需新增 API

路径：

- `src/api/erp/project-dashboard/index.ts`

接口建议：

- `getProjectOverview`
- `getProjectPurchaseProgress`
- `getProjectProductionProgress`
- `getProjectShortageSummary`

### 14.3 需新增页面

路径：

- `src/views/erp/project-dashboard/index.vue`

### 14.4 页面任务

#### 项目看板首页

展示模块：

- 项目基础信息
- 销售订单概览
- MRP 缺料汇总
- 采购执行进度
- 工单执行进度
- 完工进度

页面建议：

- 顶部项目切换
- 中部进度卡片
- 下部表格或时间线

### 14.5 交付物

- 项目进度看板页

---

# 7. 公共开发任务

## 15.1 菜单与权限接入

每新增一个模块，需要同步：

- 菜单路由
- 权限标识
- 页面按钮权限

需处理内容：

- 列表权限
- 新增权限
- 编辑权限
- 删除权限
- 审批/运行/转单/下达/完工权限

---

## 15.2 字典与下拉项

需统一处理以下字典或 simple list：

- 项目状态
- BOM 状态
- 建议单状态
- 工单状态
- 供给方式
- 工序状态
- 工艺路线状态
- 文件分类
- 文件业务类型

---

## 15.3 通用组件复用

建议优先复用已有组件：

- `Dialog`
- `ContentWrap`
- `UploadFile`
- `el-table`
- `el-form`
- `el-tabs`

必要时新增通用组件：

- `ProjectSelect`
- `ProductSelect`
- `RouteSelect`
- `BizDocumentPanel`

---

## 15.4 页面统一规范

所有新增页面保持以下风格：

- 列表页使用查询表单 + 表格 + 分页
- 表单页优先使用弹窗
- 子明细使用 `components/*Form.vue`
- API 类型定义集中在 `index.ts`
- 字段命名和后端 VO 保持一致

---

# 8. 推荐开发顺序

## 第一批

1. 项目管理页面
2. 销售订单项目化改造
3. 产品制造属性改造
4. 计划参数页面

## 第二批

1. BOM 页面
2. MRP 计划页
3. MRP 结果页
4. 缺料页
5. 建议页

## 第三批

1. 工单页面
2. 完工登记页
3. 采购来源追溯
4. 库存增强

## 第四批

1. 工艺管理
2. 文件管理
3. 项目看板
4. 联调和修正

---

# 9. 前端验收标准

## 9.1 基础验收

- 所有页面能正常打开
- 无明显报错
- 下拉加载正常
- 新增编辑删除功能正常

## 9.2 类型验收

- `ts` 类型定义完整
- 前后端字段一致
- 无未处理字段回显问题

## 9.3 联调验收

- 销售订单到 MRP 链路可见
- 建议单可转单
- 工单可完工
- 采购和工单可追溯项目
- 库存相关页面口径一致

## 9.4 页面体验验收

- 查询条件完整
- 列表列信息充分
- 关键按钮有禁用逻辑
- 日期、状态、数量显示格式统一

---

# 10. 建议的前端开发排期

## 第 1 周

- 项目管理页面
- 销售订单项目化改造
- 产品制造属性改造
- 计划参数页面

## 第 2 周

- BOM 页面
- MRP 计划页
- MRP 结果页
- 缺料页
- 建议页

## 第 3 周

- 工单页面
- 完工登记页
- 采购来源追溯
- 库存增强

## 第 4 周

- 工艺管理
- 文件管理
- 项目看板
- 联调和修正

---

# 11. 最小闭环版本前端范围

如果需要快速落地，建议最先只做这些页面：

- 项目管理页
- 销售订单项目化改造
- 产品制造属性改造
- BOM 页面
- MRP 计划页
- MRP 结果页
- 采购建议页
- 生产建议页
- 工单列表页
- 完工登记页

这样即可支撑最小业务链路：

`项目 -> 销售订单 -> MRP -> 建议 -> 工单 -> 完工`

---
