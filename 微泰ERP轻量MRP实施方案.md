# 微泰ERP轻量MRP实施方案

## 1. 文档目标

本文档用于明确在 `ruoyi-vue-pro` 项目中落地轻量 MRP 的可行方案，目标是优先打通销售、采购、生产三个环节，实现以下能力：

- 销售订单驱动需求
- 自动需求计算
- 缺料分析
- 采购建议生成
- 生产建议生成
- 轻量生产计划闭环

本文档明确限定范围为“轻量 MRP”，不在第一阶段内实现完整 APS、MES、MRP II、精细成本和复杂工序管理。

## 2. 建设目标

### 2.1 一期目标

围绕销售、采购、生产三个环节，建立最小闭环：

1. 销售订单作为需求来源进入系统
2. 系统基于 BOM 自动展开物料需求
3. 系统扣减库存、在途采购、在制供给后计算净需求
4. 系统输出缺料清单
5. 系统自动生成采购建议和生产建议
6. 建议单可转正式采购单或生产工单

### 2.2 一期不做的内容

- 有限产能排程
- 复杂委外加工全流程
- 工序级 MES 管理
- 高级替代料优选策略
- 精细人工和制造费用核算
- 高级预测、滚动计划、产能平衡

## 3. 项目现状与可行性判断

## 3.1 当前项目基础

当前仓库已经存在 ERP 模块代码基础，具备以下可复用能力：

- 销售订单
- 采购订单
- 采购入库
- 库存管理
- 收付款基础能力
- 产品、供应商、客户基础资料

当前目录中已有 `yudao-module-erp`，并包含采购、销售、库存相关控制器、服务层、数据层代码。

### 3.2 当前限制

当前主工程默认并未启用 ERP 模块，需要先进行模块接入：

- 根工程 `pom.xml` 中 `yudao-module-erp` 仍为注释状态
- `yudao-server/pom.xml` 中 `yudao-module-erp` 依赖也仍为注释状态

因此，可行性判断为：

- 技术上可行
- 代码基础可复用
- 第一阶段应避免扩散到完整制造系统
- 适合先做“销售驱动的轻量 MRP”

## 4. 总体业务方案

## 4.1 业务闭环

建议采用如下主链路：

1. 销售录入销售订单
2. 已审核销售订单进入 MRP 运算池
3. 系统根据销售订单展开多级 BOM
4. 系统统计现有可用库存、在途采购、在制生产
5. 系统计算各层物料净需求
6. 外购件生成采购建议
7. 自制件生成生产建议
8. 采购建议确认后转采购订单
9. 生产建议确认后转生产工单
10. 采购到货、生产完工后回写库存，供下次 MRP 运算继续使用

## 4.2 一期业务主线

一期只围绕三个对象展开：

- 销售订单
- 物料/BOM
- 建议单/工单

这可以保证范围收敛，尽快上线试运行。

## 5. 功能清单

## 5.1 主数据管理

### 5.1.1 物料主数据

字段建议：

- 物料编码
- 物料名称
- 规格型号
- 基本单位
- 物料分类
- 供应方式：采购件、自制件
- 安全库存
- 提前期
- 最小采购量
- 批量倍数
- 是否参与 MRP
- 启用状态

### 5.1.2 BOM 管理

功能包括：

- 成品/半成品 BOM 建立
- 子件维护
- 用量维护
- 版本维护
- 启停用
- BOM 查询
- 按产品查看 BOM 树

一期建议先支持：

- 多级 BOM
- 单位用量
- 简单损耗率字段预留

### 5.1.3 仓库供给数据

MRP 运算依赖以下供给数据：

- 可用库存
- 在途采购数量
- 已下达未完工生产单数量

## 5.2 销售环节

### 5.2.1 销售订单作为需求源

功能要求：

- 已审核销售订单可进入 MRP
- 支持按交期筛选参与运算
- 支持“是否纳入 MRP”标识
- 支持按客户、订单、产品筛选

### 5.2.2 销售需求池

建议新增一个轻量需求池视图，用于统一展示：

- 来源订单
- 产品
- 数量
- 交期
- 已分解状态
- 已建议状态

## 5.3 MRP 运算

### 5.3.1 运算入口

功能包括：

- 一键运算
- 按日期范围运算
- 按销售订单运算
- 按产品运算
- 重算
- 查看运算日志

### 5.3.2 运算内容

系统需要完成：

1. 提取需求
2. 展开 BOM
3. 统计供给
4. 计算净需求
5. 识别缺料
6. 输出采购建议和生产建议

### 5.3.3 缺料分析

支持以下维度：

- 按销售订单查看缺料
- 按产品查看缺料
- 按物料查看缺料
- 按需求日期查看缺料

## 5.4 建议单管理

### 5.4.1 采购建议单

字段建议：

- 建议单号
- 物料
- 建议数量
- 建议到货日期
- 来源销售订单
- 来源产品
- 状态：待确认、已确认、已转单、已关闭

功能包括：

- 查看建议
- 调整建议数量
- 确认建议
- 转采购订单
- 关闭建议

### 5.4.2 生产建议单

字段建议：

- 建议单号
- 产品/半成品
- 建议数量
- 建议开工日期
- 建议完工日期
- 来源销售订单
- 状态：待确认、已确认、已转工单、已关闭

功能包括：

- 查看建议
- 调整建议数量
- 确认建议
- 转生产工单
- 关闭建议

## 5.5 生产环节

### 5.5.1 轻量生产工单

一期只实现最小工单能力：

- 工单号
- 产品
- 数量
- 计划开始时间
- 计划结束时间
- 状态：待下达、生产中、已完工、已关闭
- 来源建议单

### 5.5.2 完工反馈

提供以下功能：

- 完工数量录入
- 完工入库
- 在制数量回写
- 关闭工单

一期不做：

- 复杂工序流转
- 派工报工
- 工艺路线
- 条码追溯

## 5.6 查询与预警

### 5.6.1 查询

- 缺料清单
- 建议单清单
- MRP 运算结果清单
- 生产工单执行清单

### 5.6.2 预警

- 库存不足预警
- 交期紧张预警
- 建议未处理预警
- 工单延期预警

## 6. 数据库表设计草案

## 6.1 BOM 主表 `erp_bom`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| bom_code | varchar(64) | BOM 编号 |
| product_id | bigint | 成品/半成品 ID |
| version | varchar(32) | 版本号 |
| status | tinyint | 状态 |
| remark | varchar(255) | 备注 |
| creator | varchar(64) | 创建人 |
| create_time | datetime | 创建时间 |
| updater | varchar(64) | 更新人 |
| update_time | datetime | 更新时间 |
| deleted | bit | 逻辑删除 |

## 6.2 BOM 子表 `erp_bom_item`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| bom_id | bigint | BOM 主表 ID |
| material_id | bigint | 子件物料 ID |
| material_type | tinyint | 采购件/自制件 |
| unit_id | bigint | 单位 ID |
| usage_qty | decimal(24,6) | 单位用量 |
| loss_rate | decimal(10,4) | 损耗率 |
| lead_time_day | int | 提前期 |
| sort | int | 排序 |
| remark | varchar(255) | 备注 |
| creator | varchar(64) | 创建人 |
| create_time | datetime | 创建时间 |
| updater | varchar(64) | 更新人 |
| update_time | datetime | 更新时间 |
| deleted | bit | 逻辑删除 |

## 6.3 计划参数表 `erp_material_plan_rule`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| product_id | bigint | 物料/产品 ID |
| supply_type | varchar(16) | PURCHASE/MAKE |
| safety_stock | decimal(24,6) | 安全库存 |
| min_order_qty | decimal(24,6) | 最小下单量 |
| order_multiple | decimal(24,6) | 批量倍数 |
| purchase_lead_day | int | 采购提前期 |
| make_lead_day | int | 生产提前期 |
| enable_flag | bit | 是否启用 |

## 6.4 MRP 计划表 `erp_mrp_plan`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| plan_no | varchar(64) | 运算单号 |
| plan_name | varchar(128) | 运算名称 |
| plan_start_date | date | 计划开始日期 |
| plan_end_date | date | 计划结束日期 |
| status | tinyint | 草稿/已运算/已关闭 |
| run_time | datetime | 运算时间 |
| operator_id | bigint | 操作人 |
| remark | varchar(255) | 备注 |

## 6.5 MRP 需求表 `erp_mrp_demand`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| plan_id | bigint | 运算计划 ID |
| source_type | varchar(32) | SALE_ORDER |
| source_id | bigint | 来源单据 ID |
| product_id | bigint | 产品 ID |
| demand_qty | decimal(24,6) | 需求数量 |
| demand_date | date | 需求日期 |

## 6.6 MRP 结果表 `erp_mrp_result`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| plan_id | bigint | 运算计划 ID |
| root_product_id | bigint | 顶层产品 ID |
| material_id | bigint | 物料 ID |
| gross_demand_qty | decimal(24,6) | 毛需求 |
| available_stock_qty | decimal(24,6) | 可用库存 |
| incoming_qty | decimal(24,6) | 在途采购 |
| wip_qty | decimal(24,6) | 在制数量 |
| net_demand_qty | decimal(24,6) | 净需求 |
| suggest_type | varchar(16) | PURCHASE/MAKE |
| suggest_date | date | 建议日期 |

## 6.7 缺料表 `erp_mrp_shortage`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| plan_id | bigint | 运算计划 ID |
| material_id | bigint | 物料 ID |
| shortage_qty | decimal(24,6) | 缺料数量 |
| required_date | date | 需求日期 |
| source_order_id | bigint | 来源销售订单 |

## 6.8 采购建议表 `erp_purchase_suggest`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| plan_id | bigint | 运算计划 ID |
| material_id | bigint | 物料 ID |
| suggest_qty | decimal(24,6) | 建议数量 |
| suggest_arrival_date | date | 建议到货日期 |
| status | tinyint | 状态 |
| convert_purchase_order_id | bigint | 转采购单 ID |

## 6.9 生产建议表 `erp_production_suggest`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| plan_id | bigint | 运算计划 ID |
| product_id | bigint | 产品 ID |
| suggest_qty | decimal(24,6) | 建议数量 |
| suggest_start_date | date | 建议开工日期 |
| suggest_end_date | date | 建议完工日期 |
| status | tinyint | 状态 |
| convert_production_order_id | bigint | 转工单 ID |

## 6.10 生产工单表 `erp_production_order`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| order_no | varchar(64) | 工单编号 |
| product_id | bigint | 产品 ID |
| plan_qty | decimal(24,6) | 计划数量 |
| finished_qty | decimal(24,6) | 完工数量 |
| plan_start_time | datetime | 计划开始时间 |
| plan_end_time | datetime | 计划结束时间 |
| status | tinyint | 状态 |
| source_type | varchar(32) | 来源类型 |
| source_id | bigint | 来源单 ID |

## 7. 后端模块拆分方案

## 7.1 建议实现方式

建议直接在 `yudao-module-erp` 中扩展，不新建独立 module。

原因如下：

- 当前目标聚焦，直接放入 ERP 模块成本最低
- 现有 ERP 已经有标准 controller/service/dal 分层
- 采购、销售、库存都在该模块中，跨域调用更方便

## 7.2 目录结构建议

```text
yudao-module-erp
  └─ src/main/java/.../module/erp
     ├─ controller/admin/mrp
     │  ├─ ErpBomController
     │  ├─ ErpMrpPlanController
     │  ├─ ErpMrpSuggestController
     │  └─ ErpProductionOrderController
     ├─ controller/admin/mrp/vo
     │  ├─ bom
     │  ├─ plan
     │  ├─ suggest
     │  └─ production
     ├─ service/mrp
     │  ├─ ErpBomService
     │  ├─ ErpMrpPlanService
     │  ├─ ErpMrpCalcService
     │  ├─ ErpMrpSuggestService
     │  └─ ErpProductionOrderService
     ├─ dal/dataobject/mrp
     └─ dal/mysql/mrp
```

## 7.3 核心服务职责

### 7.3.1 `ErpBomService`

负责：

- BOM 新增、修改、删除
- BOM 版本启停
- BOM 树查询

### 7.3.2 `ErpMrpPlanService`

负责：

- 运算计划创建
- 运算触发
- 运算结果查询
- 运算状态管理

### 7.3.3 `ErpMrpCalcService`

负责：

- 销售需求提取
- 多级 BOM 展开
- 库存、在途、在制汇总
- 净需求计算
- 缺料分析
- 建议单生成

### 7.3.4 `ErpMrpSuggestService`

负责：

- 采购建议确认
- 生产建议确认
- 建议转采购单
- 建议转生产工单

### 7.3.5 `ErpProductionOrderService`

负责：

- 工单创建
- 工单下达
- 完工回报
- 完工入库

## 7.4 接口建议

### 7.4.1 BOM 接口

- `POST /erp/bom/create`
- `PUT /erp/bom/update`
- `GET /erp/bom/get`
- `GET /erp/bom/page`
- `GET /erp/bom/tree`

### 7.4.2 MRP 运算接口

- `POST /erp/mrp-plan/create`
- `POST /erp/mrp-plan/run`
- `GET /erp/mrp-plan/get`
- `GET /erp/mrp-plan/page`
- `GET /erp/mrp-plan/result`
- `GET /erp/mrp-plan/shortage`

### 7.4.3 建议单接口

- `GET /erp/mrp-suggest/purchase-page`
- `GET /erp/mrp-suggest/production-page`
- `POST /erp/mrp-suggest/purchase-confirm`
- `POST /erp/mrp-suggest/production-confirm`
- `POST /erp/mrp-suggest/purchase-convert`
- `POST /erp/mrp-suggest/production-convert`

### 7.4.4 工单接口

- `POST /erp/production-order/create`
- `PUT /erp/production-order/update`
- `PUT /erp/production-order/release`
- `PUT /erp/production-order/finish`
- `GET /erp/production-order/page`

## 8. 前端页面建议

最小页面集合：

1. BOM 管理页
2. MRP 运算页
3. 缺料分析页
4. 采购建议页
5. 生产建议页
6. 生产工单页

页面上建议重点突出：

- 运算参数
- 缺料结果
- 建议转单
- 执行状态

一期不要做过重的可视化大屏，以列表、树、明细抽屉为主。

## 9. 关键流程图

## 9.1 销售到 MRP 到采购/生产流程

```text
销售订单审核通过
    ↓
进入 MRP 需求池
    ↓
按销售订单展开 BOM
    ↓
汇总库存 / 在途采购 / 在制工单
    ↓
计算净需求
    ↓
判断物料属性
    ├─ 外购件 → 生成采购建议 → 确认后转采购订单
    └─ 自制件 → 生成生产建议 → 确认后转生产工单
```

## 9.2 生产闭环流程

```text
生产建议生成
    ↓
确认生产建议
    ↓
转生产工单
    ↓
工单下达
    ↓
完工反馈
    ↓
完工入库
    ↓
更新库存与在制
```

## 10. MRP 计算逻辑

## 10.1 运算输入

一期运算输入包括：

- 已审核销售订单
- 有效 BOM
- 可用库存
- 在途采购数量
- 已下达未完工生产数量
- 物料计划参数

## 10.2 运算规则

### 10.2.1 毛需求

毛需求来源于销售订单数量，并通过多级 BOM 逐层展开。

公式：

```text
子件毛需求 = 父件需求数量 × BOM 用量 × (1 + 损耗率)
```

### 10.2.2 可供给量

```text
可供给量 = 可用库存 + 在途采购 + 在制数量
```

### 10.2.3 净需求

```text
净需求 = 毛需求 - 可供给量
如果净需求 < 0，则按 0 处理
如果计算后低于安全库存，则补足安全库存差额
```

### 10.2.4 建议数量修正

```text
建议数量 = max(净需求, 最小下单量)
若存在批量倍数，则向上取整到批量倍数
```

### 10.2.5 建议日期

```text
采购建议日期 = 需求日期 - 采购提前期
生产建议开工日期 = 需求日期 - 生产提前期
生产建议完工日期 = 需求日期
```

## 10.3 伪代码

```java
for each saleOrder in confirmedSaleOrders:
    for each saleItem in saleOrder.items:
        explodeBom(saleItem.productId, saleItem.qty, saleItem.deliveryDate)

void explodeBom(Long productId, BigDecimal qty, LocalDate demandDate) {
    Bom bom = loadEffectiveBom(productId);
    if (bom == null) {
        recordShortage(productId, qty, demandDate);
        return;
    }

    for each bomItem in bom.items:
        BigDecimal gross = qty * bomItem.usageQty * (1 + bomItem.lossRate);
        SupplyInfo supply = loadSupplyInfo(bomItem.materialId, demandDate);
        BigDecimal net = gross - supply.available - supply.incoming - supply.wip;
        net = max(net, 0);
        net = applySafetyStockAndLotRule(bomItem.materialId, net);

        saveMrpResult(...);

        if (net > 0) {
            if (bomItem.materialType == PURCHASE) {
                createPurchaseSuggest(...);
            } else {
                createProductionSuggest(...);
                explodeBom(bomItem.materialId, net, demandDate.minusDays(makeLeadDay));
            }
        }
    }
}
```

## 11. 开发排期与人天预估

## 11.1 角色配置建议

建议最小配置：

- 后端开发 1 人
- 前端开发 1 人
- 测试/实施 1 人兼职
- 产品/业务顾问 1 人兼职

## 11.2 分阶段排期

### 第一阶段：基础数据与建模

周期：2 周  
人天：8-10 人天

工作内容：

- 启用 ERP 模块
- 设计并落地 BOM 表
- 设计并落地计划参数表
- 补充物料 MRP 属性
- 增加轻量生产工单表和接口骨架

### 第二阶段：MRP 核心运算

周期：2-3 周  
人天：10-14 人天

工作内容：

- 销售订单取数
- BOM 多级展开
- 供给量汇总
- 净需求计算
- 缺料分析
- 运算结果持久化

### 第三阶段：建议单与转单

周期：2 周  
人天：8-10 人天

工作内容：

- 采购建议单
- 生产建议单
- 建议确认
- 转采购单
- 转生产工单

### 第四阶段：前端页面与联调

周期：2 周  
人天：8-12 人天

工作内容：

- BOM 页面
- MRP 运算页面
- 缺料分析页面
- 建议单页面
- 工单页面
- 前后端联调

### 第五阶段：测试与上线准备

周期：1 周  
人天：5-7 人天

工作内容：

- 测试用例执行
- 参数初始化
- 初始数据导入
- 用户培训
- 试运行支持

## 11.3 总体估算

最小可上线版本：

- 39-53 人天

稳妥版本：

- 45-60 人天

## 12. 风险与控制建议

## 12.1 BOM 数据质量风险

风险说明：

- BOM 不准确，MRP 结果必然失真

控制建议：

- 上线前先完成核心产品 BOM 梳理
- 首批只纳入重点产品

## 12.2 物料主数据不统一风险

风险说明：

- 物料编码、单位、采购/自制属性不统一，导致建议错误

控制建议：

- 先建立统一物料主数据规则
- 严格维护采购件/自制件标识

## 12.3 生产边界失控风险

风险说明：

- 若同时要求做复杂工序、报工、派工，周期会迅速失控

控制建议：

- 一期只做轻量工单
- 工序管理放到二期以后

## 12.4 业务期望过高风险

风险说明：

- 业务方容易把轻量 MRP 理解成完整制造系统

控制建议：

- 在项目启动时明确范围
- 先承诺“算得出、看得见、转得动”

## 13. 一期验收标准

建议按以下标准验收：

1. 已审核销售订单可进入 MRP 运算
2. 系统可基于多级 BOM 自动展开需求
3. 系统可识别库存、在途、在制并计算净需求
4. 系统可输出缺料分析结果
5. 外购件可生成采购建议并转采购订单
6. 自制件可生成生产建议并转生产工单
7. 工单完工后可更新供给数据

## 14. 结论

在当前 `ruoyi-vue-pro` 项目中落地轻量 MRP 是可行的，且适合采用“小步快跑”的方式建设。

最优落地路径不是先做完整制造系统，而是先围绕销售订单驱动需求，完成以下最小能力闭环：

- BOM 管理
- 自动需求计算
- 缺料分析
- 采购建议
- 生产建议
- 轻量工单闭环

建议项目以一期轻量 MRP 为目标，优先跑通销售、采购、生产三个环节，待业务稳定后，再逐步扩展到工序、报工、委外、质量和成本管理。
