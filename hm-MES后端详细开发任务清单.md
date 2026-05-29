# hm-MES 后端详细开发任务清单

## 1. 文档目标

本文档用于将 hm-MES 一期后端工作拆解为可直接执行的开发任务，基于当前项目现状进行增量开发。

适用项目：

- `D:\ruoyi-vue-pro\yudao-module-erp`

开发原则：

- 基于现有 ERP 模块扩展，不新建独立后端服务
- 复用现有 `controller + service + dal + vo` 分层结构
- 优先完成“最小业务闭环”
- 先打通主链路，再补强工艺、文件、看板等增强模块
- 保持现有编码风格、命名规范、异常处理方式一致

---

## 2. 当前后端现状

当前已有 ERP 后端业务域：

- `finance`
- `product`
- `purchase`
- `sale`
- `stock`
- `mrp`
- `statistics`

当前已有的 hm-MES 相关基础能力：

- 销售订单
- 采购订单与采购入库
- 产品、仓库、库存
- BOM / MRP 原型
- 生产工单原型

当前明显缺失或不足的领域：

- `project`
- `file`
- `process`
- `route`
- 生产执行记录闭环
- 项目追溯链路
- 库存统一口径服务
- 项目聚合分析服务

---

## 3. 一期建议范围

建议一期后端优先完成以下模块：

1. 项目化销售
2. 制造主数据补强
3. BOM / MRP 项目化与交期驱动
4. 建议单转执行单
5. 生产工单与完工闭环
6. 采购追溯与库存口径增强

建议二期再做：

1. 工艺管理
2. 文件管理
3. 项目看板聚合
4. 报工、领料等复杂执行闭环

---

## 4. 后端目录规划

建议新增或扩展以下包结构：

### 4.1 新增领域包

- `cn.iocoder.yudao.module.erp.controller.admin.project`
- `cn.iocoder.yudao.module.erp.service.project`
- `cn.iocoder.yudao.module.erp.dal.dataobject.project`
- `cn.iocoder.yudao.module.erp.dal.mysql.project`

- `cn.iocoder.yudao.module.erp.controller.admin.process`
- `cn.iocoder.yudao.module.erp.service.process`
- `cn.iocoder.yudao.module.erp.dal.dataobject.process`
- `cn.iocoder.yudao.module.erp.dal.mysql.process`

- `cn.iocoder.yudao.module.erp.controller.admin.route`
- `cn.iocoder.yudao.module.erp.service.route`
- `cn.iocoder.yudao.module.erp.dal.dataobject.route`
- `cn.iocoder.yudao.module.erp.dal.mysql.route`

- `cn.iocoder.yudao.module.erp.controller.admin.file`
- `cn.iocoder.yudao.module.erp.service.file`
- `cn.iocoder.yudao.module.erp.dal.dataobject.file`
- `cn.iocoder.yudao.module.erp.dal.mysql.file`

### 4.2 扩展已有领域包

- `sale`
- `product`
- `purchase`
- `stock`
- `mrp`

### 4.3 建议新增聚合服务包

- `cn.iocoder.yudao.module.erp.service.dashboard`
- `cn.iocoder.yudao.module.erp.service.inventory`

---

## 5. 开发阶段与优先级

| 阶段 | 模块 | 优先级 | 状态建议 |
| --- | --- | --- | --- |
| S1 | 项目化销售 | P0 | 立即开发 |
| S1 | 制造主数据补强 | P0 | 立即开发 |
| S2 | BOM / MRP 交期驱动与项目追溯 | P0 | 紧接开发 |
| S2 | 建议单转执行单 | P0 | 紧接开发 |
| S3 | 工单与完工闭环 | P0 | 跟进开发 |
| S3 | 采购追溯 / 库存口径增强 | P1 | 跟进开发 |
| S4 | 工艺管理 | P1 | 二阶段 |
| S5 | 文件管理 / 项目看板 | P2 | 后置 |

---

# 6. 模块级详细任务

## M1 项目化销售

### 6.1 目标

在现有销售订单能力基础上，增加“项目”维度，使销售订单能够绑定项目与交期，并形成项目主数据。

### 6.2 数据库任务

#### 新增表

- `erp_project`

建议字段：

- `id`
- `project_code`
- `project_name`
- `customer_id`
- `status`
- `priority`
- `delivery_date`
- `remark`
- `creator`
- `create_time`
- `updater`
- `update_time`
- `deleted`

#### 修改表

- `erp_sale_order`

新增字段：

- `project_id`
- `delivery_date`

建议索引：

- `idx_erp_sale_order_project_id`
- `idx_erp_sale_order_delivery_date`

### 6.3 需新增 DO / Mapper / Service / Controller

建议新增类：

- `ErpProjectDO`
- `ErpProjectMapper`
- `ErpProjectService`
- `ErpProjectServiceImpl`
- `ErpProjectController`

VO 建议新增：

- `ErpProjectSaveReqVO`
- `ErpProjectPageReqVO`
- `ErpProjectRespVO`
- `ErpProjectSimpleRespVO`

### 6.4 需改造销售订单领域

需改造对象：

- `ErpSaleOrderDO`
- 销售订单 create/update/get/page VO
- 销售订单 Mapper 查询条件
- 销售订单 Service 保存逻辑
- 销售订单 Controller 返回结构

改造点：

- 保存项目 ID
- 保存交期字段
- 分页查询支持 `projectId`
- 分页查询支持 `deliveryDate`
- 详情返回项目相关信息

### 6.5 校验逻辑

- 项目存在性校验
- 项目状态校验
- 销售订单保存时交期不能为空或按业务规则可选
- 项目删除前校验是否已被订单引用

### 6.6 联调检查项

- 项目 CRUD 正常
- 销售订单可正确保存 `projectId` 与 `deliveryDate`
- 查询条件生效
- 详情回显项目名称与交期

### 6.7 交付物

- 项目表
- 项目 CRUD 接口
- 销售订单项目化改造

---

## M2 制造主数据补强

### 7.1 目标

补齐产品制造属性与计划参数能力，为 BOM / MRP / 工艺提供统一主数据基础。

### 7.2 数据库任务

#### 修改表

- `erp_product`

建议新增字段：

- `supply_type`
- `mrp_enable`
- `default_route_id`
- `default_supplier_id`

说明：

- 如不希望将所有制造字段放入产品表，可保留一部分在 `erp_material_plan_rule`
- 但前端和 MRP 至少需要明确供给方式、是否参与 MRP、默认路线、默认供应商

#### 复用已有表

- `erp_material_plan_rule`

已有字段可继续使用：

- `supply_type`
- `safety_stock`
- `min_order_qty`
- `order_multiple`
- `purchase_lead_day`
- `make_lead_day`
- `enable_flag`
- `default_supplier_id`

### 7.3 需改造产品领域

需改造对象：

- `ErpProductDO`
- 产品 create/update/get/page VO
- 产品 Mapper
- 产品 Service

改造点：

- 增加制造属性字段
- 增加字段保存与查询
- 增加制造属性校验

### 7.4 需新增计划参数接口

建议新增类：

- `ErpMaterialPlanRuleController`
- `ErpMaterialPlanRuleService`
- `ErpMaterialPlanRuleServiceImpl`

VO 建议新增：

- `ErpMaterialPlanRuleSaveReqVO`
- `ErpMaterialPlanRulePageReqVO`
- `ErpMaterialPlanRuleRespVO`
- `ErpMaterialPlanRuleSimpleRespVO`

### 7.5 BOM 规则补强

需改造：

- `ErpBomService`
- `ErpBomServiceImpl`
- BOM 保存与校验逻辑

补强内容：

- BOM 版本校验
- BOM 状态校验
- 损耗率范围校验
- 提前期校验
- 循环 BOM 校验

### 7.6 联调检查项

- 产品制造属性可保存
- 计划参数可维护
- BOM 保存规则有效
- 非法 BOM 被拦截

### 7.7 交付物

- 产品制造属性字段
- 计划参数接口
- BOM 校验增强

---

## M3 BOM / MRP 项目化与交期驱动

### 8.1 目标

将现有 MRP 从“订单时间驱动”升级为“交期驱动 + 项目追溯”模式，并增强结果查询能力。

### 8.2 数据库任务

#### 修改表

- `erp_mrp_demand`
- `erp_mrp_result`
- `erp_mrp_shortage`
- `erp_purchase_suggest`
- `erp_production_suggest`
- `erp_production_order`

建议新增字段：

- `project_id`

建议索引：

- `idx_xxx_project_id`
- `idx_xxx_plan_id_project_id`

### 8.3 需改造 MRP 运算逻辑

核心改造类：

- `ErpMrpCalcServiceImpl`

改造任务：

#### 需求日期改造

- 当前需求日期来源为 `orderTime`
- 需调整为优先使用 `deliveryDate`
- 若 `deliveryDate` 为空，则回退 `orderTime`

#### 项目来源透传

在以下写入逻辑中增加 `projectId`：

- `ErpMrpDemandDO`
- `ErpMrpResultDO`
- `ErpMrpShortageDO`
- `ErpPurchaseSuggestDO`
- `ErpProductionSuggestDO`

#### 结果查询增强

支持按以下维度筛选：

- `projectId`
- `productId`
- `materialId`
- `planId`

### 8.4 需改造 DO / VO / Mapper / Controller

改造对象：

- `ErpMrpDemandDO`
- `ErpMrpResultDO`
- `ErpMrpShortageDO`
- `ErpPurchaseSuggestDO`
- `ErpProductionSuggestDO`
- `ErpProductionOrderDO`

改造 VO：

- MRP 计划查询 VO
- MRP 结果 VO
- 缺料 VO
- 采购建议 VO
- 生产建议 VO
- 工单查询 VO

改造 Controller：

- `ErpMrpPlanController`
- `ErpMrpSuggestController`

改造 Mapper：

- 各类 page/list 查询增加 `projectId`

### 8.5 需新增或补强查询能力

建议新增接口：

- MRP 结果按项目分页查询
- 缺料按项目查询
- 采购建议按项目查询
- 生产建议按项目查询

### 8.6 联调检查项

- 同一项目销售订单生成的需求带有项目来源
- MRP 结果可按项目筛选
- 缺料可按项目查询
- 建议单中能看到项目来源

### 8.7 交付物

- MRP 交期驱动改造
- MRP 项目追溯链路
- MRP 查询增强接口

---

## M4 建议单转执行单

### 9.1 目标

将 MRP 输出的采购建议、生产建议转换为采购单与工单，并保留来源追溯信息。

### 9.2 采购建议转采购单

需改造：

- `ErpMrpSuggestService`
- `ErpMrpSuggestServiceImpl`
- `ErpPurchaseOrderService`

任务：

- 新增“采购建议转采购单”服务逻辑
- 将建议单信息转换为采购单头/行
- 保留来源字段：
  - `project_id`
  - `source_order_id`
  - `source_item_id`
  - `mrp_plan_id` 可选

建议新增状态流转：

- 待确认
- 已转单
- 已关闭

### 9.3 生产建议转工单

需改造：

- `ErpMrpSuggestService`
- `ErpProductionOrderService`
- `ErpProductionOrderServiceImpl`

任务：

- 新增“生产建议转工单”服务逻辑
- 创建工单时带上来源字段
- 写入来源项目与来源订单

### 9.4 需改造对象

采购相关：

- `ErpPurchaseOrderDO`
- `ErpPurchaseOrderItemDO`
- 采购订单 save/get/page VO

生产相关：

- `ErpProductionOrderDO`
- 工单 save/get/page VO

建议新增字段：

- `project_id`
- `source_type`
- `source_id`
- `source_item_id`

### 9.5 联调检查项

- 采购建议成功转采购单
- 生产建议成功转工单
- 转单后来源追溯信息完整
- 重复转单有防重逻辑

### 9.6 交付物

- 采购建议转单能力
- 生产建议转工单能力
- 转单来源追溯能力

---

## M5 生产工单与完工闭环

### 10.1 目标

在现有生产工单原型基础上，补齐最小执行闭环：创建、下达、完工、数量回写。

### 10.2 数据库任务

#### 修改表

- `erp_production_order`

建议新增字段：

- `project_id`
- `source_order_id`
- `source_item_id`
- `route_id`

#### 新增表

- `erp_production_complete_record`

建议字段：

- `id`
- `production_order_id`
- `project_id`
- `complete_qty`
- `complete_time`
- `remark`
- `creator`
- `create_time`
- `updater`
- `update_time`
- `deleted`

### 10.3 需改造工单领域

需改造：

- `ErpProductionOrderDO`
- `ErpProductionOrderController`
- `ErpProductionOrderService`
- `ErpProductionOrderServiceImpl`
- 工单 save/get/page VO

任务：

- 增加工单来源字段
- 增加工单项目字段
- 增加下达逻辑
- 增加完工逻辑
- 增加数量回写逻辑
- 增加工单状态流转校验

### 10.4 需新增完工记录领域

建议新增类：

- `ErpProductionCompleteRecordDO`
- `ErpProductionCompleteRecordMapper`
- `ErpProductionCompleteRecordService`
- `ErpProductionCompleteRecordServiceImpl`
- `ErpProductionCompleteRecordController`

VO 建议新增：

- `ErpProductionCompleteRecordSaveReqVO`
- `ErpProductionCompleteRecordPageReqVO`
- `ErpProductionCompleteRecordRespVO`

### 10.5 状态机建议

工单状态建议：

- 草稿
- 已下达
- 生产中
- 已完工
- 已关闭

完工逻辑建议：

- 完工数量累加到 `finished_qty`
- 若 `finished_qty >= plan_qty`，自动置为已完工
- 禁止超计划完工

### 10.6 联调检查项

- 工单可创建
- 工单可下达
- 完工登记成功
- 完工数量回写正确
- 工单状态正确流转

### 10.7 交付物

- 工单增强接口
- 完工记录接口
- 工单最小闭环能力

---

## M6 采购执行追溯

### 11.1 目标

在现有采购执行链路中，增加项目与 MRP 来源追溯能力。

### 11.2 需改造采购领域

需改造对象：

- `ErpPurchaseOrderDO`
- `ErpPurchaseOrderItemDO`
- `ErpPurchaseInDO`
- 采购订单 VO
- 采购入库 VO

建议新增字段：

- `project_id`
- `source_type`
- `source_id`
- `source_item_id`
- `mrp_plan_id`

### 11.3 需改造服务逻辑

改造点：

- 转单时回写来源信息
- 查询时返回来源信息
- 入库单查询时可追溯采购单来源
- 项目维度采购汇总接口

### 11.4 建议新增聚合接口

- 按项目查询采购进度
- 按项目查询采购到货情况

### 11.5 联调检查项

- 采购单可展示来源项目
- 入库可追溯来源
- 按项目能聚合采购进度

### 11.6 交付物

- 采购来源追溯能力
- 项目采购进度查询接口

---

## M7 制造库存口径增强

### 12.1 目标

统一 MRP、库存、生产所使用的库存口径，为后续领料、报工、看板奠定基础。

### 12.2 需新增统一库存服务

建议新增类：

- `ErpInventorySnapshotService`
- `ErpInventorySnapshotServiceImpl`

建议统一输出维度：

- 当前库存
- 可用库存
- 占用库存
- 在途库存
- 在制库存

### 12.3 需改造库存与 MRP 逻辑

改造点：

- MRP 运算不再直接散落调用库存 Mapper 计算全部逻辑
- 库存页面查询统一走库存聚合口径
- 工单、采购建议、生产建议共享同一口径定义

### 12.4 数据模型建议

如现有库存模型不足，可新增：

- `erp_stock_reserve`

建议字段：

- `biz_type`
- `biz_id`
- `product_id`
- `reserve_qty`
- `status`

### 12.5 联调检查项

- MRP 结果中的库存口径与库存台账一致
- 在途、在制、占用数量计算可解释
- 后续工单/采购对库存影响可扩展

### 12.6 交付物

- 库存统一口径服务
- 预占模型或预留扩展点

---

## M8 工艺管理

### 13.1 目标

增加工序和工艺路线管理，为产品与工单提供默认工艺路径。

### 13.2 数据库任务

#### 新增表

- `mes_process`
- `mes_route`
- `mes_route_step`

建议字段：

`mes_process`

- `id`
- `process_code`
- `process_name`
- `status`
- `remark`

`mes_route`

- `id`
- `route_code`
- `route_name`
- `status`
- `remark`

`mes_route_step`

- `id`
- `route_id`
- `process_id`
- `step_no`
- `prepare_time`
- `work_time`
- `remark`

### 13.3 需新增领域类

工序：

- `MesProcessDO`
- `MesProcessMapper`
- `MesProcessService`
- `MesProcessServiceImpl`
- `MesProcessController`

工艺路线：

- `MesRouteDO`
- `MesRouteStepDO`
- `MesRouteMapper`
- `MesRouteStepMapper`
- `MesRouteService`
- `MesRouteServiceImpl`
- `MesRouteController`

### 13.4 需改造产品 / 工单领域

改造点：

- 产品支持绑定默认 `route_id`
- 工单创建时可带出默认路线
- 后续报工可扩展到工序维度

### 13.5 联调检查项

- 工序可 CRUD
- 路线可 CRUD
- 路线步骤可保存
- 产品可绑定默认路线

### 13.6 交付物

- 工序接口
- 工艺路线接口
- 产品绑定路线能力

---

## M9 文件管理

### 14.1 目标

建设业务文件主表和业务关联表，实现项目、产品、BOM、工艺等资料统一管理。

### 14.2 数据库任务

#### 新增表

- `file_document`
- `file_document_rel`

建议字段：

`file_document`

- `id`
- `name`
- `url`
- `type`
- `size`
- `category`
- `remark`

`file_document_rel`

- `id`
- `document_id`
- `biz_type`
- `biz_id`

### 14.3 需新增领域类

- `FileDocumentDO`
- `FileDocumentRelDO`
- `FileDocumentMapper`
- `FileDocumentRelMapper`
- `FileDocumentService`
- `FileDocumentServiceImpl`
- `FileDocumentController`

### 14.4 服务逻辑任务

- 上传后元数据入库
- 文件与业务关系绑定
- 按业务类型与业务 ID 查询文件
- 文件下载权限控制

业务类型建议支持：

- `PROJECT`
- `PRODUCT`
- `BOM`
- `ROUTE`
- `PRODUCTION_ORDER`

### 14.5 联调检查项

- 上传后元数据正确入库
- 业务对象可查询附件
- 删除关系不误删物理文件

### 14.6 交付物

- 文件主表
- 文件关联表
- 文件查询接口

---

## M10 项目进度聚合

### 15.1 目标

基于已有项目、订单、MRP、采购、工单、完工数据，提供项目维度聚合接口。

### 15.2 建议新增聚合服务

- `ErpProjectDashboardService`
- `ErpProjectDashboardServiceImpl`
- `ErpProjectDashboardController`

### 15.3 建议提供接口

- 项目概览
- 项目采购进度
- 项目工单进度
- 项目缺料情况
- 项目完工情况

### 15.4 实现建议

- 一期优先走实时聚合查询
- 避免过早引入冗余汇总表
- 若性能不足，再考虑缓存或汇总表

### 15.5 联调检查项

- 单项目聚合结果准确
- 能反映采购、生产、缺料状态
- 与底层单据数据一致

### 15.6 交付物

- 项目看板接口

---

# 7. 公共开发任务

## 16.1 SQL 脚本管理

建议新增独立 SQL 脚本，不直接覆盖旧脚本：

- `mes-01-project.sql`
- `mes-02-product-mrp-base.sql`
- `mes-03-mrp-traceability.sql`
- `mes-04-production-complete.sql`
- `mes-05-process-route.sql`
- `mes-06-file.sql`
- `mes-07-project-dashboard.sql`

要求：

- 每次改动独立脚本
- 新增字段尽量兼容旧数据
- 重要字段加索引

---

## 16.2 权限点与菜单接口支持

每新增一个 Controller，需要同步：

- 权限标识
- 菜单绑定
- 按钮级权限

建议权限点覆盖：

- 查询
- 新增
- 修改
- 删除
- 运行 MRP
- 转采购单
- 转工单
- 下达工单
- 完工登记

---

## 16.3 枚举与字典

需统一补充：

- 项目状态枚举
- BOM 状态枚举
- 建议单状态枚举
- 工单状态枚举
- 供给方式枚举
- 文件业务类型枚举

要求：

- 枚举与数据库值保持一致
- 前后端字典值可统一映射

---

## 16.4 异常与校验

所有新增领域需补充：

- 错误码常量
- 参数校验
- 业务状态校验
- 删除前依赖校验

典型校验：

- 项目不存在
- 项目已被引用不可删
- BOM 循环依赖
- 建议单重复转单
- 工单状态不允许完工
- 完工数量超计划

---

## 16.5 查询与分页规范

所有新分页接口建议统一：

- 支持基础关键字查询
- 支持时间范围
- 支持状态筛选
- 支持项目维度筛选

---

## 16.6 测试与验证

建议补充：

- MRP 运算单元测试
- 工单状态流转测试
- 完工数量回写测试
- 转单防重测试
- 项目聚合结果测试

注意：

- 当前模块下测试基础较弱，建议至少补核心服务测试

---

# 8. 推荐开发顺序

## 第一批

1. 项目主表与项目 CRUD
2. 销售订单项目化改造
3. 产品制造属性改造
4. 计划参数接口

## 第二批

1. BOM 规则补强
2. MRP 交期驱动改造
3. MRP 项目追溯改造
4. 结果 / 缺料 / 建议查询增强

## 第三批

1. 采购建议转采购单
2. 生产建议转工单
3. 工单增强
4. 完工记录与完工逻辑

## 第四批

1. 库存口径统一服务
2. 采购追溯增强
3. 工艺管理
4. 文件管理
5. 项目聚合接口

---

# 9. 后端验收标准

## 9.1 基础验收

- 所有新增表结构可执行
- DO / Mapper / Service / Controller 齐全
- CRUD 接口可正常使用
- 分页查询正常

## 9.2 业务验收

- 销售订单可关联项目与交期
- MRP 按交期计算
- MRP 结果可追溯项目
- 建议单可转采购单 / 工单
- 工单可下达、可完工
- 采购和工单可追溯来源

## 9.3 数据一致性验收

- 项目来源字段前后一致
- 完工数量回写准确
- 工单状态流转正确
- 转单防重有效

## 9.4 聚合验收

- 项目采购进度可查
- 项目生产进度可查
- 项目缺料情况可查

---

# 10. 建议的后端开发排期

## 第 1 周

- 项目表与项目 CRUD
- 销售订单项目化改造
- 产品制造属性字段
- 计划参数接口

## 第 2 周

- BOM 规则补强
- MRP 交期驱动改造
- MRP 项目追溯改造
- 结果与建议查询增强

## 第 3 周

- 建议单转采购单
- 建议单转工单
- 工单增强
- 完工登记

## 第 4 周

- 库存口径增强
- 采购追溯
- 工艺管理或文件管理
- 联调和修正

---

# 11. 最小闭环版本后端范围

如果需要快速落地，建议最先只做这些能力：

- 项目 CRUD
- 销售订单项目化
- 产品制造属性
- 计划参数接口
- BOM 能力增强
- MRP 按交期驱动
- MRP 结果项目追溯
- 采购建议 / 生产建议转单
- 工单与完工闭环

这样即可支撑最小业务链路：

`项目 -> 销售订单 -> MRP -> 建议 -> 采购单/工单 -> 完工`

---
