# Spec: 发货放行校验能力

---

## 1. 能力概述

发货放行校验是销售执行闭环的核心能力，用于根据合同条款和实际收款情况，自动判断销售订单是否允许发货。

---

## 2. 业务规则

### 2.1 放行规则枚举

| 规则编码 | 规则名称 | 校验逻辑 |
|----------|----------|----------|
| SIGN_AND_SHIP | 签约即发 | 合同生效后即可发货，但仍需财务审核 |
| AFTER_PAYMENT | 到账后发 | 收到全额货款后方可发货 |
| AFTER_PREPAYMENT | 达到预付款比例后发 | 收到约定比例的预付款后方可发货 |
| FINANCE_APPROVAL | 财务审核后发 | 需财务人员审核通过后方可发货 |

### 2.2 通用校验（所有规则都需满足）

1. 订单状态 = 已审批通过
2. 合同状态 = 已生效
3. finance_approval_required = 1 时，财务审核状态 = 已通过

### 2.3 校验结果

| 结果状态 | 说明 |
|----------|------|
| RELEASED | 已放行，允许发货 |
| BLOCKED | 阻塞，存在不满足条件 |
| FINANCE_REVIEW | 待财务审核 |
| PENDING | 待校验 |

---

## 3. 接口规范

### 3.1 发货放行校验

**POST** `/api/erp/shipment-release/check`

**请求参数**：
```json
{
  "orderId": 123456
}
```

**响应参数**：
```json
{
  "releasable": true,
  "releaseStatus": "RELEASED",
  "blockerReasons": [],
  "pendingRole": null,
  "details": [
    {
      "checkItem": "合同状态校验",
      "passed": true,
      "message": "合同已生效"
    }
  ]
}
```

### 3.2 财务审核通过

**POST** `/api/erp/shipment-release/approve`

**请求参数**：
```json
{
  "orderId": 123456,
  "approverId": 100,
  "remark": "审核通过"
}
```

### 3.3 财务审核驳回

**POST** `/api/erp/shipment-release/reject`

**请求参数**：
```json
{
  "orderId": 123456,
  "approverId": 100,
  "reason": "收款不足"
}
```

---

## 4. 数据模型

### 4.1 erp_sale_order 扩展字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| contract_id | BIGINT | 关联合同编号 |
| shipment_release_status | VARCHAR(32) | 发货放行状态 |
| shipment_release_reason | VARCHAR(255) | 放行阻塞原因 |

### 4.2 erp_sale_out 扩展字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| shipment_release_status | VARCHAR(32) | 出库时的放行状态（快照） |
| finance_approver_id | BIGINT | 财务审核人 |
| finance_approve_time | DATETIME | 财务审核时间 |

---

## 5. 测试用例

### 5.1 正常流程

**场景**：签约即发 + 已审批 + 已生效

**输入**：
- 订单状态：已审批通过
- 合同状态：已生效
- 放行规则：SIGN_AND_SHIP
- finance_approval_required：1
- 财务审核状态：已通过

**预期输出**：
- releasable：true
- releaseStatus：RELEASED

### 5.2 阻塞场景

**场景**：到账后发 + 收款不足

**输入**：
- 订单状态：已审批通过
- 合同状态：已生效
- 放行规则：AFTER_PAYMENT
- 订单金额：128000
- 已收款金额：96000

**预期输出**：
- releasable：false
- releaseStatus：BLOCKED
- blockerReasons：["已收款金额 ¥96,000 < 订单金额 ¥128,000"]

### 5.3 待财务审核场景

**场景**：需要财务审核 + 未审核

**输入**：
- 订单状态：已审批通过
- 合同状态：已生效
- finance_approval_required：1
- 财务审核状态：未审核

**预期输出**：
- releasable：false
- releaseStatus：FINANCE_REVIEW
- pendingRole：财务人员
