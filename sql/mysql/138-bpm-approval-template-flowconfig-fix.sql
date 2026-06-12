/*
 Target: 修复审批模板的 flowConfig 格式，使其与 SimpleProcessDesigner 兼容
 Schema: ruoyi-vue-pro
 Date: 2026-06-12
 说明：将模板的 flowConfig 从简化格式转换为 SimpleFlowNode 树形结构格式
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

-- 更新请假审批模板的 flowConfig
UPDATE `bpm_approval_template` 
SET `flow_config` = '{
  "id": "StartUserNode",
  "type": 10,
  "name": "发起人",
  "childNode": {
    "id": "deptManager",
    "type": 11,
    "name": "直属主管审批",
    "showText": "直属主管审批",
    "candidateStrategy": 21,
    "approveMethod": 3,
    "childNode": {
      "id": "hr",
      "type": 11,
      "name": "HR审批",
      "showText": "HR审批",
      "candidateStrategy": 10,
      "candidateParam": "hr",
      "approveMethod": 3,
      "childNode": {
        "id": "EndEvent",
        "type": 1,
        "name": "结束"
      }
    }
  }
}'
WHERE `code` = 'LEAVE_APPROVAL' AND `deleted` = b'0';

-- 更新报销审批模板的 flowConfig
UPDATE `bpm_approval_template` 
SET `flow_config` = '{
  "id": "StartUserNode",
  "type": 10,
  "name": "发起人",
  "childNode": {
    "id": "deptManager",
    "type": 11,
    "name": "部门经理审批",
    "showText": "部门经理审批",
    "candidateStrategy": 21,
    "approveMethod": 3,
    "childNode": {
      "id": "finance",
      "type": 11,
      "name": "财务审批",
      "showText": "财务审批",
      "candidateStrategy": 10,
      "candidateParam": "finance",
      "approveMethod": 3,
      "childNode": {
        "id": "EndEvent",
        "type": 1,
        "name": "结束"
      }
    }
  }
}'
WHERE `code` = 'EXPENSE_APPROVAL' AND `deleted` = b'0';

-- 更新采购审批模板的 flowConfig
UPDATE `bpm_approval_template` 
SET `flow_config` = '{
  "id": "StartUserNode",
  "type": 10,
  "name": "发起人",
  "childNode": {
    "id": "deptManager",
    "type": 11,
    "name": "部门经理审批",
    "showText": "部门经理审批",
    "candidateStrategy": 21,
    "approveMethod": 3,
    "childNode": {
      "id": "purchase",
      "type": 11,
      "name": "采购经理审批",
      "showText": "采购经理审批",
      "candidateStrategy": 10,
      "candidateParam": "purchase_manager",
      "approveMethod": 3,
      "childNode": {
        "id": "finance",
        "type": 11,
        "name": "财务审批",
        "showText": "财务审批",
        "candidateStrategy": 10,
        "candidateParam": "finance",
        "approveMethod": 3,
        "childNode": {
          "id": "EndEvent",
          "type": 1,
          "name": "结束"
        }
      }
    }
  }
}'
WHERE `code` = 'PURCHASE_APPROVAL' AND `deleted` = b'0';

-- 更新销售审批模板的 flowConfig
UPDATE `bpm_approval_template` 
SET `flow_config` = '{
  "id": "StartUserNode",
  "type": 10,
  "name": "发起人",
  "childNode": {
    "id": "salesManager",
    "type": 11,
    "name": "销售经理审批",
    "showText": "销售经理审批",
    "candidateStrategy": 10,
    "candidateParam": "sales_manager",
    "approveMethod": 3,
    "childNode": {
      "id": "finance",
      "type": 11,
      "name": "财务审批",
      "showText": "财务审批",
      "candidateStrategy": 10,
      "candidateParam": "finance",
      "approveMethod": 3,
      "childNode": {
        "id": "EndEvent",
        "type": 1,
        "name": "结束"
      }
    }
  }
}'
WHERE `code` = 'SALE_APPROVAL' AND `deleted` = b'0';

-- 更新付款审批模板的 flowConfig
UPDATE `bpm_approval_template` 
SET `flow_config` = '{
  "id": "StartUserNode",
  "type": 10,
  "name": "发起人",
  "childNode": {
    "id": "deptManager",
    "type": 11,
    "name": "部门经理审批",
    "showText": "部门经理审批",
    "candidateStrategy": 21,
    "approveMethod": 3,
    "childNode": {
      "id": "finance",
      "type": 11,
      "name": "财务审批",
      "showText": "财务审批",
      "candidateStrategy": 10,
      "candidateParam": "finance",
      "approveMethod": 3,
      "childNode": {
        "id": "cfo",
        "type": 11,
        "name": "财务总监审批",
        "showText": "财务总监审批",
        "candidateStrategy": 10,
        "candidateParam": "cfo",
        "approveMethod": 3,
        "childNode": {
          "id": "EndEvent",
          "type": 1,
          "name": "结束"
        }
      }
    }
  }
}'
WHERE `code` = 'PAYMENT_APPROVAL' AND `deleted` = b'0';

SET FOREIGN_KEY_CHECKS = 1;
