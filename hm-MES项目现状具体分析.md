# hm-MES 项目现状具体分析

## 1. 分析方式

本文档不是只根据需求文档推演，而是直接对照当前仓库代码进行分析。

分析范围包括：

- 后端 `yudao-module-erp`
- 前端 `yudao-ui-admin-vue3`
- 已有 ERP / MRP 实现

目标是回答三个问题：

1. 当前项目到底已经做到什么程度
2. 哪些能力只是“有原型”，还不能直接支撑一期目标
3. 具体还差哪些前后端开发任务

## 2. 项目真实现状结论

当前项目的实际状态不是“从零开始做 hm-MES”，而是：

- 已有较完整 ERP 基础
- 已有一部分轻量 MRP 后端原型
- 前端 ERP 页面已有较多基础页面
- 但项目、文件、工艺、MRP 前端、生产执行闭环、项目看板仍明显不足

可以更直接地说：

> 当前项目适合在现有 ERP 基础上扩展成“轻量 hm-MES 一期”，但当前还不能直接满足文档里的一期交付目标。

## 3. 实际代码层面的判断

## 3.1 销售模块：已有基础，但没有项目化

### 已有内容

销售订单后端控制器已存在：

- `ErpSaleOrderController`

销售订单前端 API 和表单页面也已存在：

- `src/api/erp/sale/order/index.ts`
- `src/views/erp/sale/order/SaleOrderForm.vue`

说明：

- 销售订单的基础 CRUD 已具备
- 前后端是打通的

### 实际缺口

当前销售订单数据模型中没有看到：

- `projectId`
- `deliveryDate`

在 [ErpSaleOrderDO.java#L48](/D:/ruoyi-vue-pro/yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/dal/dataobject/sale/ErpSaleOrderDO.java#L48) 到 [ErpSaleOrderDO.java#L107](/D:/ruoyi-vue-pro/yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/dal/dataobject/sale/ErpSaleOrderDO.java#L107) 中，当前只有客户、账户、销售员、下单时间、金额、附件、备注等字段。

在 [SaleOrderForm.vue#L17](/D:/ruoyi-vue-pro/yudao-ui/yudao-ui-admin-vue3/src/views/erp/sale/order/SaleOrderForm.vue#L17) 到 [SaleOrderForm.vue#L78](/D:/ruoyi-vue-pro/yudao-ui/yudao-ui-admin-vue3/src/views/erp/sale/order/SaleOrderForm.vue#L17) 中，当前表单也只有：

- 订单时间
- 客户
- 销售员
- 备注
- 附件

在 [index.ts#L4](/D:/ruoyi-vue-pro/yudao-ui/yudao-ui-admin-vue3/src/api/erp/sale/order/index.ts#L4) 到 [index.ts#L15](/D:/ruoyi-vue-pro/yudao-ui/yudao-ui-admin-vue3/src/api/erp/sale/order/index.ts#L15) 中，销售订单前端类型也没有项目和交期字段。

### 判断

销售模块当前属于：

- `基础能力已完成`
- `项目化能力未开始`

这意味着文档中的 `M1` 不是“补几个字段就完”，而是要在现有销售之上正式引入项目维度。

## 3.2 主数据模块：产品/采购/库存基础在，但制造主数据不完整

### 已有内容

后端已有这些控制器：

- `ErpProductController`
- `ErpPurchaseOrderController`
- `ErpStockController`
- `ErpWarehouseController`

前端已有这些目录：

- `src/views/erp/product`
- `src/views/erp/purchase`
- `src/views/erp/stock`

说明产品、采购、库存这几块不是空白，而是已经具备 ERP 基础。

### 实际缺口

虽然主数据基础存在，但对制造场景还不够：

- 未看到明确“项目制造属性”的产品字段
- 未看到前端已有“计划参数维护页”
- 虽然存在 `ErpMaterialPlanRuleDO` 和 `ErpMaterialPlanRuleMapper`，但目前没有发现对应 Controller，也没有发现前端 API / 页面

这说明：

- 计划参数底层表已经开始做了
- 但“可维护、可配置、可被业务使用”的完整交付面还没形成

### 判断

主数据模块当前属于：

- `ERP 主数据基础已有`
- `制造主数据仍需补强`

## 3.3 文件管理：当前几乎没有业务层实现

### 已有内容

前端有通用上传组件：

- `src/components/UploadFile/src/UploadFile.vue`

销售订单表单里也已经使用了上传能力：

- [SaleOrderForm.vue#L75](/D:/ruoyi-vue-pro/yudao-ui/yudao-ui-admin-vue3/src/views/erp/sale/order/SaleOrderForm.vue#L75)

说明当前项目有“基础文件上传能力”。

### 实际缺口

但当前没有发现明确的业务文件模块代码：

- 没有项目资料实体
- 没有文件业务关联实体
- 没有按项目 / 产品 / BOM / 工艺挂接文件的后端逻辑
- 没有文件台账页面

### 判断

文件管理当前不是“没做完”，而是：

- `底层上传能力有`
- `业务文件管理层基本没做`

也就是说 `M3` 需要前后端都新建。

## 3.4 工艺模块：当前基本空白

### 实际缺口

当前没有发现明确的以下后端领域类：

- `Process`
- `Route`

也没有发现对应前端业务页面：

- `process`
- `route`

注意：前端里虽然有 BPM 的 `ProcessDesigner` 组件，但那是工作流设计器，不是制造工艺模块，不能算做工艺已实现。

### 判断

工艺模块当前属于：

- `前后端基本空白`

这部分必须按业务重新建模，不能误判成“现成可用”。

## 3.5 BOM / MRP：后端有原型，前端基本没接

### 已有后端内容

后端已经存在：

- `ErpBomController`
- `ErpMrpPlanController`
- `ErpMrpSuggestController`
- `ErpProductionOrderController`

以及对应 Service：

- `ErpBomService`
- `ErpMaterialPlanRuleService`
- `ErpMrpCalcService`
- `ErpMrpPlanService`
- `ErpMrpSuggestService`
- `ErpProductionOrderService`

说明：

- BOM、MRP 计划、建议、工单这些后端基础已经搭起来了

### 已有后端能力的具体程度

在 [ErpMrpPlanController.java#L40](/D:/ruoyi-vue-pro/yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/controller/admin/mrp/ErpMrpPlanController.java#L40) 到 [ErpMrpPlanController.java#L108](/D:/ruoyi-vue-pro/yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/controller/admin/mrp/ErpMrpPlanController.java#L108) 中，已经具备：

- 创建计划
- 运行计划
- 查询计划
- 查询运算结果
- 查询缺料清单

在 [ErpMrpCalcServiceImpl.java#L67](/D:/ruoyi-vue-pro/yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/service/mrp/ErpMrpCalcServiceImpl.java#L67) 到 [ErpMrpCalcServiceImpl.java#L205](/D:/ruoyi-vue-pro/yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/service/mrp/ErpMrpCalcServiceImpl.java#L205) 中，已经实现了：

- 销售单取数
- BOM 展开
- 库存、在途、在制参与计算
- 安全库存参与计算
- 最小起订量与倍数规则
- 采购建议和生产建议输出

### 实际缺口

#### 缺口 1：前端没有对应模块

当前前端 `src/views/erp` 里没有：

- `mrp`
- `bom`
- `production`

当前前端 `src/api/erp` 里也没有：

- `mrp`

这说明当前 MRP 虽然后端有了，但业务方还不能通过现有前端完整使用。

#### 缺口 2：需求日期逻辑仍偏 ERP，不够 MES / 项目制造化

在 [ErpMrpCalcServiceImpl.java#L72](/D:/ruoyi-vue-pro/yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/service/mrp/ErpMrpCalcServiceImpl.java#L72) 到 [ErpMrpCalcServiceImpl.java#L89](/D:/ruoyi-vue-pro/yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/service/mrp/ErpMrpCalcServiceImpl.java#L72) 中，当前按销售订单 `orderTime` 来取计划范围和需求日期。

这会带来两个问题：

- 没有按交期驱动
- 不能很好支持项目维度制造计划

#### 缺口 3：项目维度追溯不够

当前 MRP 结果里主要保留的是：

- `sourceOrderId`
- `sourceItemId`

没有看到项目维度字段，这意味着后续项目总览和项目进度看板还接不上。

### 判断

`M6` 当前属于：

- `后端已有较强原型`
- `前端基本未交付`
- `业务口径还需从 ERP 逻辑升级到项目制造逻辑`

## 3.6 采购执行：基础完整，但和 MRP / 项目的关系还不够强

### 已有内容

采购前后端都已有：

- 采购订单
- 采购入库
- 供应商

说明采购本身不是问题。

### 实际缺口

当前仍缺：

- 采购建议转采购单时的项目来源沉淀
- 采购单对 MRP 建议来源的追溯
- 项目维度采购执行视图

### 判断

`M7` 当前属于：

- `功能基础已在`
- `制造链路追溯能力不足`

## 3.7 生产执行：有工单原型，但没有真正的执行闭环

### 已有内容

后端已有工单创建、释放、完工逻辑。

在 [ErpProductionOrderServiceImpl.java#L43](/D:/ruoyi-vue-pro/yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/service/mrp/ErpProductionOrderServiceImpl.java#L43) 到 [ErpProductionOrderServiceImpl.java#L109](/D:/ruoyi-vue-pro/yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/service/mrp/ErpProductionOrderServiceImpl.java#L109) 中，当前已经支持：

- 手工创建工单
- 从建议生成工单
- 工单释放
- 工单完工

### 实际缺口

#### 缺口 1：没有领料记录

当前没有发现：

- `IssueRecord`
- 领料相关 Controller / Service / 页面

#### 缺口 2：没有报工记录

当前没有发现：

- `ReportRecord`
- 报工相关 Controller / Service / 页面

#### 缺口 3：完工逻辑过轻

在 [ErpProductionOrderServiceImpl.java#L99](/D:/ruoyi-vue-pro/yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/service/mrp/ErpProductionOrderServiceImpl.java#L99) 到 [ErpProductionOrderServiceImpl.java#L109](/D:/ruoyi-vue-pro/yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/service/mrp/ErpProductionOrderServiceImpl.java#L109) 中，当前完工只做：

- 成品库存增加
- 工单状态改为完成

但没有做：

- 原料扣减
- 实际领料核销
- 完工记录留痕
- 项目维度回写

### 判断

`M8` 当前属于：

- `有工单原型`
- `没有真正的制造执行闭环`

这是当前项目最容易被高估的模块之一。

## 3.8 项目进度看板：当前基本没有

### 实际缺口

当前没有发现明确的：

- 项目进度聚合后端接口
- 项目总览页面
- 项目缺料页面
- 项目工单进度页面

### 判断

`M9` 当前属于：

- `前后端基本空白`

## 4. 基于实际代码得出的待开发结论

## 4.1 后端最需要补的任务

### 第一优先级

- 项目主表与项目 CRUD
- 销售订单增加 `projectId` 和 `deliveryDate`
- 文件业务关联层
- 工序 / 工艺路线 / 产品绑定路线
- 计划参数交付面
- MRP 按交期驱动
- MRP 结果增加项目维度追溯
- 工单领料 / 报工 / 完工记录
- 完工时原料扣减
- 项目进度聚合接口

### 第二优先级

- 采购单来源追溯
- 工单来源追溯
- MRP 日志增强
- 项目采购进度聚合

## 4.2 前端最需要补的任务

### 第一优先级

- 项目管理页
- 销售订单表单增加项目与交期字段
- 文件台账页
- 项目资料页
- 工序管理页
- 工艺路线页
- BOM 页面
- 计划参数页面
- MRP 页面
- 缺料结果页
- 建议转采购页
- 建议转工单页
- 生产工单页
- 领料页
- 报工页
- 完工页
- 项目总览页

### 第二优先级

- 项目采购视图
- 项目工单进度页
- MRP 日志页

## 5. 最重要的真实判断

### 判断 1

当前项目不是“没有基础”，而是“ERP 基础够用，制造链路没闭环”。

### 判断 2

当前项目最值得复用的是：

- 销售
- 采购
- 库存
- 产品
- 前端基础框架
- MRP 后端原型

### 判断 3

当前项目最缺的不是普通 CRUD，而是：

- 项目维度建模
- 文件与工艺关联
- 生产执行留痕
- 项目级聚合视图

### 判断 4

如果不先补“项目 + 交期 + 领料 / 报工 / 完工闭环”，即使 MRP 现在能跑，也只能算演示级，不足以支撑文档的一期目标。

## 6. 建议的落地顺序

按当前代码现实情况，最合理的顺序是：

1. 项目化销售
2. 文件管理
3. 工艺管理
4. 产品制造属性与计划参数
5. BOM / MRP 前端落地
6. MRP 按交期与项目追溯补强
7. 采购来源追溯
8. 工单领料 / 报工 / 完工闭环
9. 项目进度看板

