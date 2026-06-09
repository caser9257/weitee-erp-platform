# Design Document: 市场部销售执行闭环系统

---

## 1. 架构设计

### 1.1 系统架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              前端层 (Vue 3)                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │ 合同表单扩展  │  │ 订单合同关联  │  │ 发货放行审核  │  │ 市场执行台账  │   │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘   │
└─────────┼─────────────────┼─────────────────┼─────────────────┼────────────┘
          ▼                 ▼                 ▼                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            后端服务层 (Spring Boot)                          │
├─────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ yudao-module-crm: CrmContractService (扩展商业条款)                  │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ yudao-module-erp:                                                   │   │
│  │   · ErpShipmentReleaseService (新增：放行校验)                       │   │
│  │   · ErpMarketExecutionLedgerService (新增：市场台账)                 │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ yudao-module-pmo: PmoProjectLifecycleService (新增：生命周期)        │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
          ▼                 ▼                 ▼                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            数据持久层 (MySQL)                                │
├─────────────────────────────────────────────────────────────────────────────┤
│  crm_contract(扩展)  erp_sale_order(扩展)  erp_sale_out(扩展)  pmo_project(扩展) │
│                              pmo_project_lifecycle_timeline(新增)            │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1.2 模块依赖关系

```
yudao-module-crm  ◀─── yudao-module-erp  ───▶  yudao-module-pmo
      │                      │                       │
      └──────────────────────┼───────────────────────┘
                             ▼
                      yudao-framework
```

---

## 2. 数据模型设计

### 2.1 合同商业条款扩展（crm_contract）

**新增字段**：

| 字段名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| shipment_release_rule | VARCHAR(32) | 'SIGN_AND_SHIP' | 发货放行规则 |
| invoice_trigger | VARCHAR(32) | 'AFTER_SHIPMENT' | 开票触发条件 |
| collection_rule | VARCHAR(32) | 'BEFORE_SHIPMENT' | 收款规则 |
| prepayment_amount | DECIMAL(12,2) | NULL | 预付款金额 |
| prepayment_ratio | DECIMAL(5,2) | NULL | 预付款比例 |
| finance_approval_required | TINYINT(1) | 1 | 是否需要财务审核（默认是） |
| acceptance_required | TINYINT(1) | 0 | 是否需要验收 |
| payment_terms | VARCHAR(500) | NULL | 付款条件说明 |
| shipment_conditions | VARCHAR(500) | NULL | 发货条件说明 |
| invoice_conditions | VARCHAR(500) | NULL | 开票条件说明 |

### 2.2 销售订单扩展（erp_sale_order）

**新增字段**：

| 字段名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| contract_id | BIGINT | NULL | 关联合同编号 |
| contract_no | VARCHAR(64) | NULL | 合同编号（冗余） |
| shipment_release_status | VARCHAR(32) | 'PENDING' | 发货放行状态 |
| shipment_release_reason | VARCHAR(255) | NULL | 放行阻塞原因 |
| invoice_status | VARCHAR(32) | 'NOT_INVOICED' | 开票状态 |
| acceptance_status | VARCHAR(32) | 'NOT_REQUIRED' | 验收状态 |

### 2.3 项目生命周期扩展（pmo_project）

**新增字段**：

| 字段名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| lifecycle_stage | VARCHAR(32) | 'CONTRACT' | 当前生命周期阶段 |
| current_blocker | VARCHAR(255) | NULL | 当前阻塞项 |
| current_pending_role | VARCHAR(64) | NULL | 当前待办角色 |
| contract_exec_status | VARCHAR(32) | 'PENDING' | 合同执行状态 |
| receipt_exec_status | VARCHAR(32) | 'PENDING' | 收款执行状态 |
| shipment_exec_status | VARCHAR(32) | 'PENDING' | 发货执行状态 |
| invoice_exec_status | VARCHAR(32) | 'PENDING' | 开票执行状态 |

---

## 3. 枚举设计

### 3.1 发货放行规则（ShipmentReleaseRule）

```java
public enum ShipmentReleaseRule {
    SIGN_AND_SHIP("签约即发", 1),
    AFTER_PAYMENT("到账后发", 2),
    AFTER_PREPAYMENT("达到预付款比例后发", 3),
    FINANCE_APPROVAL("财务审核后发", 4);
}
```

### 3.2 开票触发条件（InvoiceTrigger）

```java
public enum InvoiceTrigger {
    PREPAYMENT_FULL("预付款全额开票", 1),
    PREPAYMENT_PARTIAL("预付款部分开票", 2),
    PREPAYMENT_ONLY("仅预付款开票", 3),
    AFTER_SHIPMENT("发货后开票", 4),
    AFTER_DELIVERY_RECEIPT("交付收款后开票", 5),
    MANUAL("手工决定", 99);
}
```

### 3.3 收款规则（CollectionRule）

```java
public enum CollectionRule {
    BEFORE_SHIPMENT("发货前付款", 1),
    ON_SHIPMENT("发货时付款", 2),
    AFTER_SHIPMENT("发货后约定期限付款", 3);
}
```

### 3.4 项目生命周期阶段（ProjectLifecycleStage）

```java
public enum ProjectLifecycleStage {
    // 主干流程
    CONTRACT("合同", 1),
    ORDER("订单", 2),
    PAYMENT("收款", 3),
    SHIPMENT("发货", 4),
    OUTBOUND("出库", 5),
    INVOICE("开票", 6),
    CLOSED("关闭", 7),
    
    // 异常状态
    CONTRACT_REJECTED("合同驳回", -1),
    BLOCKED("阻塞", -2),
    RETURNED("退货", -3),
    DISPUTED("争议", -4);
}
```

---

## 4. 接口设计

### 4.1 发货放行校验接口

**POST** `/api/erp/shipment-release/check`

**请求**：
```json
{
  "orderId": 123456
}
```

**响应**：
```json
{
  "code": 0,
  "data": {
    "releasable": true,
    "releaseStatus": "RELEASED",
    "blockerReasons": [],
    "pendingRole": null,
    "details": [
      {
        "checkItem": "合同状态校验",
        "passed": true,
        "message": "合同已生效"
      },
      {
        "checkItem": "收款金额校验",
        "passed": true,
        "message": "已收款金额 ¥128,000 >= 订单金额 ¥128,000"
      }
    ]
  }
}
```

### 4.2 市场执行台账分页查询接口

**GET** `/api/erp/market-ledger/page`

**请求参数**：
- pageNo: 页码
- pageSize: 每页条数
- projectNo: 项目编号（模糊查询）
- contractNo: 合同编号（模糊查询）
- customerName: 客户名称（模糊查询）
- saleUserId: 销售员编号
- lifecycleStage: 生命周期阶段
- releaseStatus: 放行状态

**响应**：
```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "projectId": 1001,
        "projectNo": "PRJ-2026-001",
        "projectName": "某某智能设备项目",
        "lifecycleStage": "OUTBOUND",
        "contractNo": "CRM-CT-2026-012",
        "orderNo": "SO-2026-042",
        "orderTotalPrice": 128000.00,
        "receivedAmount": 96000.00,
        "receiptProgress": 75.00,
        "shippedCount": 8,
        "orderCount": 8,
        "shipmentProgress": 100.00,
        "invoiceStatus": "NOT_INVOICED"
      }
    ],
    "total": 128
  }
}
```

### 4.3 项目生命周期时间线接口

**GET** `/api/pmo/project/{id}/lifecycle-timeline`

**响应**：
```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "projectId": 1001,
      "stageCode": "CONTRACT",
      "stageName": "合同",
      "happenTime": "2026-06-01 10:30:00",
      "operatorId": 100,
      "operatorName": "张三",
      "remark": "合同签订"
    },
    {
      "id": 2,
      "projectId": 1001,
      "stageCode": "ORDER",
      "stageName": "订单",
      "happenTime": "2026-06-02 14:00:00",
      "operatorId": 101,
      "operatorName": "李四",
      "remark": "销售订单创建"
    }
  ]
}
```

---

## 5. 业务规则设计

### 5.1 发货放行校验规则

```
┌─────────────────────────────────────────────────────────────────┐
│                      发货放行校验流程                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. 获取订单信息                                                 │
│     ↓                                                           │
│  2. 校验订单状态 = 已审批通过                                    │
│     ↓ (不通过 → 返回阻塞)                                       │
│  3. 获取关联合同信息                                             │
│     ↓                                                           │
│  4. 校验合同状态 = 已生效                                        │
│     ↓ (不通过 → 返回阻塞)                                       │
│  5. 读取合同放行规则                                             │
│     ↓                                                           │
│  6. 根据放行规则执行校验：                                       │
│     · SIGN_AND_SHIP → 直接通过                                  │
│     · AFTER_PAYMENT → 校验已收款 >= 订单金额                     │
│     · AFTER_PREPAYMENT → 校验已收款 >= 预付款金额                │
│     · FINANCE_APPROVAL → 校验财务审核状态 = 已通过               │
│     ↓                                                           │
│  7. 通用校验：finance_approval_required = 1 ?                    │
│     ↓ (是 → 校验财务审核状态)                                    │
│  8. 返回校验结果                                                 │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 生命周期阶段流转规则

**正向流转**：
```
CONTRACT → ORDER → PAYMENT → SHIPMENT → OUTBOUND → INVOICE → CLOSED
```

**异常状态**：
- CONTRACT_REJECTED: 合同审批驳回
- BLOCKED: 放行校验不通过
- RETURNED: 客户退货
- DISPUTED: 发票争议

---

## 6. 前端设计

### 6.1 页面清单

| 页面 | 路由 | 类型 |
|------|------|------|
| 合同表单-商业条款Tab | - | 改造 |
| 销售订单-合同关联 | - | 改造 |
| 发货放行审核 | /erp/sale/shipment-release | 新增 |
| 市场执行台账 | /erp/sale/market-ledger | 新增 |
| 项目生命周期 | /pmo/project/lifecycle | 新增 |

### 6.2 设计规范

遵循 AGENTS.md 中的前端设计规范：
- 浅色极简商务风
- 卡片化分层布局
- 现代 DataTable 风格
- 状态 Tag + 微进度条
- 右侧 Drawer 详情面板

---

## 7. 测试策略

### 7.1 单元测试

- 发货放行校验服务：覆盖所有放行规则组合
- 生命周期阶段流转：覆盖正向流转和异常状态
- 台账聚合查询：验证数据聚合正确性

### 7.2 集成测试

- 合同创建 → 订单创建 → 收款登记 → 放行校验 → 出库 全链路测试
- 市场台账数据一致性验证

### 7.3 前端测试

- 表单校验：必填字段、格式校验
- 联动逻辑：合同选择后条款回显
- 页面交互：筛选、分页、详情查看
