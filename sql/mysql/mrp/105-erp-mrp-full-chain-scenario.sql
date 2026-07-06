/*
 ERP MRP 全链路测试数据 - 场景数据
 作用：
 - 以“已审核销售订单”触发自动 MRP 的语义落地
 - 补齐审核日志、自动计划、trace/result、采购建议、生产建议、短缺和项目待办
 - 支持重复执行
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

START TRANSACTION;

SET @creator := 'tester';

SET @project_id := 993701;
SET @sale_order_id := 993901;
SET @sale_order_item_id := 993911;
SET @plan_id := 993601;

SET @root_product_id := 993001;
SET @branch_a_id := 993002;
SET @branch_b_id := 993003;
SET @branch_c_id := 993007;
SET @shared_material_id := 993004;

SET @root_bom_id := 993101;
SET @branch_a_bom_id := 993111;
SET @branch_b_bom_id := 993121;

SET @root_bom_item_a_id := 993102;
SET @root_bom_item_b_id := 993103;
SET @root_bom_item_c_id := 993104;
SET @branch_a_shared_bom_item_id := 993112;
SET @branch_b_shared_bom_item_id := 993122;

SET @audit_log_id := 993571;
SET @demand_id := 993561;
SET @pc_task_id := 993581;
SET @mc_task_id := 993582;

SET @root_trace_node_id := 993751;
SET @branch_a_trace_node_id := 993752;
SET @shared_a_trace_node_id := 993753;
SET @branch_b_trace_node_id := 993754;
SET @shared_b_trace_node_id := 993755;
SET @branch_c_trace_node_id := 993756;

SET @root_result_id := 993651;
SET @branch_a_result_id := 993652;
SET @shared_a_result_id := 993653;
SET @branch_b_result_id := 993654;
SET @shared_b_result_id := 993655;
SET @branch_c_result_id := 993656;

SET @purchase_suggest_a_id := 993671;
SET @purchase_suggest_b_id := 993672;
SET @production_root_id := 993681;
SET @production_a_id := 993682;
SET @production_b_id := 993683;
SET @shortage_id := 993691;

DELETE FROM `erp_mrp_stock_reservation` WHERE `plan_id` = @plan_id OR `source_order_id` = @sale_order_id;
DELETE FROM `erp_mrp_result_component` WHERE `plan_id` = @plan_id;
DELETE FROM `erp_mrp_trace_node` WHERE `plan_id` = @plan_id;
DELETE FROM `erp_mrp_result` WHERE `plan_id` = @plan_id;
DELETE FROM `erp_mrp_shortage` WHERE `plan_id` = @plan_id;
DELETE FROM `erp_purchase_suggest` WHERE `plan_id` = @plan_id;
DELETE FROM `erp_production_suggest` WHERE `plan_id` = @plan_id;
DELETE FROM `erp_mrp_demand` WHERE `plan_id` = @plan_id;
DELETE FROM `erp_mrp_plan` WHERE `id` = @plan_id;

DELETE FROM `erp_project_role_task`
WHERE `project_id` = @project_id
   OR (`source_type` = 'SALE_ORDER' AND `source_id` = @sale_order_id)
   OR (`source_type` = 'MRP_PLAN' AND `source_id` = @plan_id);

DELETE FROM `erp_sale_order_audit_log` WHERE `order_id` = @sale_order_id;
DELETE FROM `erp_sale_order_reject_log` WHERE `order_id` = @sale_order_id;
DELETE FROM `erp_sale_order_items` WHERE `id` = @sale_order_item_id OR `order_id` = @sale_order_id;
DELETE FROM `erp_sale_order` WHERE `id` = @sale_order_id;

INSERT INTO `erp_sale_order`
(`id`, `no`, `status`, `process_instance_id`, `customer_id`, `project_id`, `business_type`, `settlement_type`, `source_project_id`, `source_product_id`, `account_id`, `sale_user_id`, `order_time`, `delivery_date`,
 `total_count`, `total_price`, `total_product_price`, `total_tax_price`, `discount_percent`, `discount_price`, `deposit_price`, `file_url`, `remark`, `out_count`, `return_count`, `delivery_ready_status`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@sale_order_id, 'SO-FULL-CHAIN-20260520-001', 20, NULL, 993801, @project_id, 'SELF_RESEARCH', 'PRODUCT_SALE', NULL, NULL, NULL, NULL, '2026-05-20 09:00:00', '2026-05-28',
 10.000000, 1880.000000, 1880.000000, 0.000000, 0.000000, 0.000000, 0.000000, NULL, 'MRP 全链路测试销售订单', 0.000000, 0.000000, 'NOT_READY',
 @creator, NOW(), @creator, NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `no` = VALUES(`no`),
  `status` = VALUES(`status`),
  `customer_id` = VALUES(`customer_id`),
  `project_id` = VALUES(`project_id`),
  `business_type` = VALUES(`business_type`),
  `settlement_type` = VALUES(`settlement_type`),
  `order_time` = VALUES(`order_time`),
  `delivery_date` = VALUES(`delivery_date`),
  `total_count` = VALUES(`total_count`),
  `total_price` = VALUES(`total_price`),
  `total_product_price` = VALUES(`total_product_price`),
  `total_tax_price` = VALUES(`total_tax_price`),
  `discount_percent` = VALUES(`discount_percent`),
  `discount_price` = VALUES(`discount_price`),
  `deposit_price` = VALUES(`deposit_price`),
  `remark` = VALUES(`remark`),
  `out_count` = VALUES(`out_count`),
  `return_count` = VALUES(`return_count`),
  `delivery_ready_status` = VALUES(`delivery_ready_status`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`),

UPDATE `erp_project`
SET `sale_order_id` = @sale_order_id,
    `pc_status` = 'PENDING',
    `mc_status` = 'PENDING',
    `updater` = @creator,
    `update_time` = NOW()
WHERE `id` = @project_id;

INSERT INTO `erp_sale_order_items`
(`id`, `order_id`, `product_id`, `product_unit_id`, `product_price`, `count`, `total_price`, `tax_percent`, `tax_price`,
 `remark`, `out_count`, `return_count`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@sale_order_item_id, @sale_order_id, @root_product_id, 993021, 188.000000, 10.000000, 1880.000000, 0.000000, 0.000000,
 '触发 MRP 的根产品订单项', 0.000000, 0.000000, @creator, NOW(), @creator, NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `order_id` = VALUES(`order_id`),
  `product_id` = VALUES(`product_id`),
  `product_unit_id` = VALUES(`product_unit_id`),
  `product_price` = VALUES(`product_price`),
  `count` = VALUES(`count`),
  `total_price` = VALUES(`total_price`),
  `tax_percent` = VALUES(`tax_percent`),
  `tax_price` = VALUES(`tax_price`),
  `remark` = VALUES(`remark`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`),

INSERT INTO `erp_sale_order_audit_log`
(`id`, `order_id`, `action_type`, `before_status`, `after_status`, `reason`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@audit_log_id, @sale_order_id, 'APPROVE', 10, 20, '销售单审核通过，触发 MRP 自动计划', @creator, NOW(), @creator, NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `action_type` = VALUES(`action_type`),
  `before_status` = VALUES(`before_status`),
  `after_status` = VALUES(`after_status`),
  `reason` = VALUES(`reason`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`),

INSERT INTO `erp_mrp_plan`
(`id`, `plan_no`, `plan_name`, `plan_start_date`, `plan_end_date`, `status`, `run_time`, `operator_id`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@plan_id, 'MRP-FULL-CHAIN-PLAN-20260520-001', 'MRP 全链路验证计划', '2026-05-28', '2026-05-28', 20, NOW(), 1,
 '销售审核自动生成的 MRP 计划', @creator, NOW(), @creator, NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `plan_no` = VALUES(`plan_no`),
  `plan_name` = VALUES(`plan_name`),
  `plan_start_date` = VALUES(`plan_start_date`),
  `plan_end_date` = VALUES(`plan_end_date`),
  `status` = VALUES(`status`),
  `run_time` = VALUES(`run_time`),
  `operator_id` = VALUES(`operator_id`),
  `remark` = VALUES(`remark`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`),

INSERT INTO `erp_project_role_task`
(`id`, `project_id`, `role_code`, `task_type`, `task_status`, `assignee_user_id`, `source_type`, `source_id`, `summary`, `due_time`, `finish_time`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@pc_task_id, @project_id, 'PC', 'SALE_APPROVED_PLAN_CONFIRM', 'TODO', 1, 'SALE_ORDER', @sale_order_id,
 '销售订单审批已通过，请 PC 确认计划交付节点', '2026-05-28 00:00:00', NULL, '测试脚本预置 PC 待办', @creator, NOW(), @creator, NOW(), b'0'),
(@mc_task_id, @project_id, 'MC', 'MRP_SUPPLY_CONFIRM', 'TODO', 1, 'MRP_PLAN', @plan_id,
 'MRP 已生成 2 条采购建议、3 条生产建议，请 MC 确认物料准备策略', '2026-05-28 00:00:00', NULL, '测试脚本预置 MC 待办', @creator, NOW(), @creator, NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `task_status` = VALUES(`task_status`),
  `assignee_user_id` = VALUES(`assignee_user_id`),
  `source_type` = VALUES(`source_type`),
  `source_id` = VALUES(`source_id`),
  `summary` = VALUES(`summary`),
  `due_time` = VALUES(`due_time`),
  `finish_time` = VALUES(`finish_time`),
  `remark` = VALUES(`remark`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`),

INSERT INTO `erp_mrp_demand`
(`id`, `plan_id`, `project_id`, `source_type`, `source_id`, `source_item_id`, `product_id`, `demand_qty`, `demand_date`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@demand_id, @plan_id, @project_id, 'SALE_ORDER', @sale_order_id, @sale_order_item_id, @root_product_id, 10.000000, '2026-05-28',
 @creator, NOW(), @creator, NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `project_id` = VALUES(`project_id`),
  `source_type` = VALUES(`source_type`),
  `source_id` = VALUES(`source_id`),
  `source_item_id` = VALUES(`source_item_id`),
  `product_id` = VALUES(`product_id`),
  `demand_qty` = VALUES(`demand_qty`),
  `demand_date` = VALUES(`demand_date`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`),

INSERT INTO `erp_mrp_trace_node`
(`id`, `plan_id`, `root_product_id`, `parent_trace_node_id`, `parent_material_id`, `trace_level`, `material_id`, `bom_id`, `bom_item_id`,
 `trace_path_key`, `project_id`, `source_order_id`, `source_item_id`, `gross_demand_qty`, `available_stock_qty`, `incoming_qty`, `wip_qty`,
 `reserved_stock_qty`, `safety_stock_qty`, `theoretical_net_demand_qty`, `execution_net_demand_qty`, `policy_code`, `policy_version`,
 `business_type`, `mrp_enable_flag`, `supply_owner`, `suggest_type`, `skip_reason`, `suggest_date`, `demand_date`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@root_trace_node_id, @plan_id, @root_product_id, NULL, NULL, 0, @root_product_id, @root_bom_id, NULL,
 '993601|993901|993911|993001|ROOT:993001', @project_id, @sale_order_id, @sale_order_item_id, 10.000000, 0.000000, 0.000000, 0.000000,
 0.000000, 0.000000, 10.000000, 10.000000, 'DEFAULT', 1,
 'SELF_RESEARCH', b'1', 'COMPANY', 'MAKE', NULL, '2026-05-28', '2026-05-28',
 @creator, NOW(), @creator, NOW(), b'0'),
(@branch_a_trace_node_id, @plan_id, @root_product_id, @root_trace_node_id, @root_product_id, 1, @branch_a_id, @root_bom_id, @root_bom_item_a_id,
 '993601|993901|993911|993001|ROOT:993001>993102:993002', @project_id, @sale_order_id, @sale_order_item_id, 10.000000, 0.000000, 0.000000, 0.000000,
 0.000000, 0.000000, 10.000000, 10.000000, 'DEFAULT', 1,
 'SELF_RESEARCH', b'1', 'COMPANY', 'MAKE', NULL, '2026-05-28', '2026-05-28',
 @creator, NOW(), @creator, NOW(), b'0'),
(@shared_a_trace_node_id, @plan_id, @root_product_id, @branch_a_trace_node_id, @branch_a_id, 2, @shared_material_id, @branch_a_bom_id, @branch_a_shared_bom_item_id,
 '993601|993901|993911|993001|ROOT:993001>993102:993002>993112:993004', @project_id, @sale_order_id, @sale_order_item_id, 20.000000, 0.000000, 0.000000, 0.000000,
 0.000000, 0.000000, 20.000000, 20.000000, 'DEFAULT', 1,
 'SELF_RESEARCH', b'1', 'COMPANY', 'PURCHASE', NULL, '2026-05-28', '2026-05-28',
 @creator, NOW(), @creator, NOW(), b'0'),
(@branch_b_trace_node_id, @plan_id, @root_product_id, @root_trace_node_id, @root_product_id, 1, @branch_b_id, @root_bom_id, @root_bom_item_b_id,
 '993601|993901|993911|993001|ROOT:993001>993103:993003', @project_id, @sale_order_id, @sale_order_item_id, 10.000000, 0.000000, 0.000000, 0.000000,
 0.000000, 0.000000, 10.000000, 10.000000, 'DEFAULT', 1,
 'SELF_RESEARCH', b'1', 'COMPANY', 'MAKE', NULL, '2026-05-28', '2026-05-28',
 @creator, NOW(), @creator, NOW(), b'0'),
(@shared_b_trace_node_id, @plan_id, @root_product_id, @branch_b_trace_node_id, @branch_b_id, 2, @shared_material_id, @branch_b_bom_id, @branch_b_shared_bom_item_id,
 '993601|993901|993911|993001|ROOT:993001>993103:993003>993122:993004', @project_id, @sale_order_id, @sale_order_item_id, 30.000000, 0.000000, 0.000000, 0.000000,
 0.000000, 0.000000, 30.000000, 30.000000, 'DEFAULT', 1,
 'SELF_RESEARCH', b'1', 'COMPANY', 'PURCHASE', NULL, '2026-05-28', '2026-05-28',
 @creator, NOW(), @creator, NOW(), b'0'),
(@branch_c_trace_node_id, @plan_id, @root_product_id, @root_trace_node_id, @root_product_id, 1, @branch_c_id, @root_bom_id, @root_bom_item_c_id,
 '993601|993901|993911|993001|ROOT:993001>993104:993007', @project_id, @sale_order_id, @sale_order_item_id, 10.000000, 0.000000, 0.000000, 0.000000,
 0.000000, 0.000000, 10.000000, 10.000000, 'DEFAULT', 1,
 'SELF_RESEARCH', b'1', 'COMPANY', 'MAKE', NULL, '2026-05-28', '2026-05-28',
 @creator, NOW(), @creator, NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `parent_trace_node_id` = VALUES(`parent_trace_node_id`),
  `parent_material_id` = VALUES(`parent_material_id`),
  `trace_level` = VALUES(`trace_level`),
  `material_id` = VALUES(`material_id`),
  `bom_id` = VALUES(`bom_id`),
  `bom_item_id` = VALUES(`bom_item_id`),
  `trace_path_key` = VALUES(`trace_path_key`),
  `gross_demand_qty` = VALUES(`gross_demand_qty`),
  `available_stock_qty` = VALUES(`available_stock_qty`),
  `incoming_qty` = VALUES(`incoming_qty`),
  `wip_qty` = VALUES(`wip_qty`),
  `reserved_stock_qty` = VALUES(`reserved_stock_qty`),
  `safety_stock_qty` = VALUES(`safety_stock_qty`),
  `theoretical_net_demand_qty` = VALUES(`theoretical_net_demand_qty`),
  `execution_net_demand_qty` = VALUES(`execution_net_demand_qty`),
  `policy_code` = VALUES(`policy_code`),
  `policy_version` = VALUES(`policy_version`),
  `business_type` = VALUES(`business_type`),
  `mrp_enable_flag` = VALUES(`mrp_enable_flag`),
  `supply_owner` = VALUES(`supply_owner`),
  `suggest_type` = VALUES(`suggest_type`),
  `skip_reason` = VALUES(`skip_reason`),
  `suggest_date` = VALUES(`suggest_date`),
  `demand_date` = VALUES(`demand_date`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`),

INSERT INTO `erp_mrp_result`
(`id`, `plan_id`, `trace_node_id`, `root_product_id`, `material_id`, `trace_path_key`, `trace_level`, `parent_material_id`, `bom_item_id`,
 `gross_demand_qty`, `available_stock_qty`, `incoming_qty`, `wip_qty`, `reserved_stock_qty`, `net_demand_qty`, `policy_code`, `policy_version`,
 `business_type`, `mrp_enable_flag`, `supply_owner`, `skip_reason`, `suggest_type`, `suggest_date`, `source_order_id`, `source_item_id`, `demand_date`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@root_result_id, @plan_id, @root_trace_node_id, @root_product_id, @root_product_id, '993601|993901|993911|993001|ROOT:993001', 0, NULL, NULL,
 10.000000, 0.000000, 0.000000, 0.000000, 0.000000, 10.000000, 'DEFAULT', 1,
 'SELF_RESEARCH', b'1', 'COMPANY', NULL, 'MAKE', '2026-05-28', @sale_order_id, @sale_order_item_id, '2026-05-28',
 @creator, NOW(), @creator, NOW(), b'0'),
(@branch_a_result_id, @plan_id, @branch_a_trace_node_id, @root_product_id, @branch_a_id, '993601|993901|993911|993001|ROOT:993001>993102:993002', 1, @root_product_id, @root_bom_item_a_id,
 10.000000, 0.000000, 0.000000, 0.000000, 0.000000, 10.000000, 'DEFAULT', 1,
 'SELF_RESEARCH', b'1', 'COMPANY', NULL, 'MAKE', '2026-05-28', @sale_order_id, @sale_order_item_id, '2026-05-28',
 @creator, NOW(), @creator, NOW(), b'0'),
(@shared_a_result_id, @plan_id, @shared_a_trace_node_id, @root_product_id, @shared_material_id, '993601|993901|993911|993001|ROOT:993001>993102:993002>993112:993004', 2, @branch_a_id, @branch_a_shared_bom_item_id,
 20.000000, 0.000000, 0.000000, 0.000000, 0.000000, 20.000000, 'DEFAULT', 1,
 'SELF_RESEARCH', b'1', 'COMPANY', NULL, 'PURCHASE', '2026-05-28', @sale_order_id, @sale_order_item_id, '2026-05-28',
 @creator, NOW(), @creator, NOW(), b'0'),
(@branch_b_result_id, @plan_id, @branch_b_trace_node_id, @root_product_id, @branch_b_id, '993601|993901|993911|993001|ROOT:993001>993103:993003', 1, @root_product_id, @root_bom_item_b_id,
 10.000000, 0.000000, 0.000000, 0.000000, 0.000000, 10.000000, 'DEFAULT', 1,
 'SELF_RESEARCH', b'1', 'COMPANY', NULL, 'MAKE', '2026-05-28', @sale_order_id, @sale_order_item_id, '2026-05-28',
 @creator, NOW(), @creator, NOW(), b'0'),
(@shared_b_result_id, @plan_id, @shared_b_trace_node_id, @root_product_id, @shared_material_id, '993601|993901|993911|993001|ROOT:993001>993103:993003>993122:993004', 2, @branch_b_id, @branch_b_shared_bom_item_id,
 30.000000, 0.000000, 0.000000, 0.000000, 0.000000, 30.000000, 'DEFAULT', 1,
 'SELF_RESEARCH', b'1', 'COMPANY', NULL, 'PURCHASE', '2026-05-28', @sale_order_id, @sale_order_item_id, '2026-05-28',
 @creator, NOW(), @creator, NOW(), b'0'),
(@branch_c_result_id, @plan_id, @branch_c_trace_node_id, @root_product_id, @branch_c_id, '993601|993901|993911|993001|ROOT:993001>993104:993007', 1, @root_product_id, @root_bom_item_c_id,
 10.000000, 0.000000, 0.000000, 0.000000, 0.000000, 10.000000, 'DEFAULT', 1,
 'SELF_RESEARCH', b'1', 'COMPANY', NULL, 'MAKE', '2026-05-28', @sale_order_id, @sale_order_item_id, '2026-05-28',
 @creator, NOW(), @creator, NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `trace_node_id` = VALUES(`trace_node_id`),
  `root_product_id` = VALUES(`root_product_id`),
  `material_id` = VALUES(`material_id`),
  `trace_path_key` = VALUES(`trace_path_key`),
  `trace_level` = VALUES(`trace_level`),
  `parent_material_id` = VALUES(`parent_material_id`),
  `bom_item_id` = VALUES(`bom_item_id`),
  `gross_demand_qty` = VALUES(`gross_demand_qty`),
  `available_stock_qty` = VALUES(`available_stock_qty`),
  `incoming_qty` = VALUES(`incoming_qty`),
  `wip_qty` = VALUES(`wip_qty`),
  `reserved_stock_qty` = VALUES(`reserved_stock_qty`),
  `net_demand_qty` = VALUES(`net_demand_qty`),
  `policy_code` = VALUES(`policy_code`),
  `policy_version` = VALUES(`policy_version`),
  `business_type` = VALUES(`business_type`),
  `mrp_enable_flag` = VALUES(`mrp_enable_flag`),
  `supply_owner` = VALUES(`supply_owner`),
  `skip_reason` = VALUES(`skip_reason`),
  `suggest_type` = VALUES(`suggest_type`),
  `suggest_date` = VALUES(`suggest_date`),
  `source_order_id` = VALUES(`source_order_id`),
  `source_item_id` = VALUES(`source_item_id`),
  `demand_date` = VALUES(`demand_date`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`),

INSERT INTO `erp_mrp_result_component`
(`id`, `plan_id`, `result_id`, `material_id`, `component_code`, `component_name`, `component_role`, `sequence_no`, `enable_flag`, `base_qty`, `consumed_qty`, `remaining_qty`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@root_result_id + 210, @plan_id, @root_result_id, @root_product_id, 'GROSS_DEMAND', '毛需求', 'DEMAND_BASE', 0, b'1', 10.000000, 0.000000, 10.000000, @creator, NOW(), @creator, NOW(), b'0'),
(@root_result_id + 211, @plan_id, @root_result_id, @root_product_id, 'SAFETY_STOCK', '安全库存', 'DEMAND_ADJUST', 0, b'1', 0.000000, 0.000000, 10.000000, @creator, NOW(), @creator, NOW(), b'0'),
(@root_result_id + 212, @plan_id, @root_result_id, @root_product_id, 'ON_HAND_AVAILABLE', '可用库存', 'SUPPLY_CONSUME', 30, b'1', 0.000000, 0.000000, 0.000000, @creator, NOW(), @creator, NOW(), b'0'),

(@branch_a_result_id + 210, @plan_id, @branch_a_result_id, @branch_a_id, 'GROSS_DEMAND', '毛需求', 'DEMAND_BASE', 0, b'1', 10.000000, 0.000000, 10.000000, @creator, NOW(), @creator, NOW(), b'0'),
(@branch_a_result_id + 211, @plan_id, @branch_a_result_id, @branch_a_id, 'SAFETY_STOCK', '安全库存', 'DEMAND_ADJUST', 0, b'1', 0.000000, 0.000000, 10.000000, @creator, NOW(), @creator, NOW(), b'0'),
(@branch_a_result_id + 212, @plan_id, @branch_a_result_id, @branch_a_id, 'ON_HAND_AVAILABLE', '可用库存', 'SUPPLY_CONSUME', 30, b'1', 0.000000, 0.000000, 0.000000, @creator, NOW(), @creator, NOW(), b'0'),

(@shared_a_result_id + 210, @plan_id, @shared_a_result_id, @shared_material_id, 'GROSS_DEMAND', '毛需求', 'DEMAND_BASE', 0, b'1', 20.000000, 0.000000, 20.000000, @creator, NOW(), @creator, NOW(), b'0'),
(@shared_a_result_id + 211, @plan_id, @shared_a_result_id, @shared_material_id, 'SAFETY_STOCK', '安全库存', 'DEMAND_ADJUST', 0, b'1', 0.000000, 0.000000, 20.000000, @creator, NOW(), @creator, NOW(), b'0'),
(@shared_a_result_id + 212, @plan_id, @shared_a_result_id, @shared_material_id, 'ON_HAND_AVAILABLE', '可用库存', 'SUPPLY_CONSUME', 30, b'1', 0.000000, 0.000000, 0.000000, @creator, NOW(), @creator, NOW(), b'0'),

(@branch_b_result_id + 210, @plan_id, @branch_b_result_id, @branch_b_id, 'GROSS_DEMAND', '毛需求', 'DEMAND_BASE', 0, b'1', 10.000000, 0.000000, 10.000000, @creator, NOW(), @creator, NOW(), b'0'),
(@branch_b_result_id + 211, @plan_id, @branch_b_result_id, @branch_b_id, 'SAFETY_STOCK', '安全库存', 'DEMAND_ADJUST', 0, b'1', 0.000000, 0.000000, 10.000000, @creator, NOW(), @creator, NOW(), b'0'),
(@branch_b_result_id + 212, @plan_id, @branch_b_result_id, @branch_b_id, 'ON_HAND_AVAILABLE', '可用库存', 'SUPPLY_CONSUME', 30, b'1', 0.000000, 0.000000, 0.000000, @creator, NOW(), @creator, NOW(), b'0'),

(@shared_b_result_id + 210, @plan_id, @shared_b_result_id, @shared_material_id, 'GROSS_DEMAND', '毛需求', 'DEMAND_BASE', 0, b'1', 30.000000, 0.000000, 30.000000, @creator, NOW(), @creator, NOW(), b'0'),
(@shared_b_result_id + 211, @plan_id, @shared_b_result_id, @shared_material_id, 'SAFETY_STOCK', '安全库存', 'DEMAND_ADJUST', 0, b'1', 0.000000, 0.000000, 30.000000, @creator, NOW(), @creator, NOW(), b'0'),
(@shared_b_result_id + 212, @plan_id, @shared_b_result_id, @shared_material_id, 'ON_HAND_AVAILABLE', '可用库存', 'SUPPLY_CONSUME', 30, b'1', 0.000000, 0.000000, 0.000000, @creator, NOW(), @creator, NOW(), b'0'),

(@branch_c_result_id + 210, @plan_id, @branch_c_result_id, @branch_c_id, 'GROSS_DEMAND', '毛需求', 'DEMAND_BASE', 0, b'1', 10.000000, 0.000000, 10.000000, @creator, NOW(), @creator, NOW(), b'0'),
(@branch_c_result_id + 211, @plan_id, @branch_c_result_id, @branch_c_id, 'SAFETY_STOCK', '安全库存', 'DEMAND_ADJUST', 0, b'1', 0.000000, 0.000000, 10.000000, @creator, NOW(), @creator, NOW(), b'0'),
(@branch_c_result_id + 212, @plan_id, @branch_c_result_id, @branch_c_id, 'ON_HAND_AVAILABLE', '可用库存', 'SUPPLY_CONSUME', 30, b'1', 0.000000, 0.000000, 0.000000, @creator, NOW(), @creator, NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `material_id` = VALUES(`material_id`),
  `component_code` = VALUES(`component_code`),
  `component_name` = VALUES(`component_name`),
  `component_role` = VALUES(`component_role`),
  `sequence_no` = VALUES(`sequence_no`),
  `enable_flag` = VALUES(`enable_flag`),
  `base_qty` = VALUES(`base_qty`),
  `consumed_qty` = VALUES(`consumed_qty`),
  `remaining_qty` = VALUES(`remaining_qty`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`),

INSERT INTO `erp_purchase_suggest`
(`id`, `plan_id`, `trace_node_id`, `project_id`, `material_id`, `trace_path_key`, `trace_level`, `parent_material_id`, `bom_item_id`,
 `suggest_qty`, `suggest_arrival_date`, `gross_demand_qty`, `available_stock_qty`, `incoming_qty`, `wip_qty`, `reserved_stock_qty`, `safety_stock_qty`,
 `net_demand_qty`, `source_order_id`, `source_item_id`, `status`, `convert_purchase_order_id`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@purchase_suggest_a_id, @plan_id, @shared_a_trace_node_id, @project_id, @shared_material_id, '993601|993901|993911|993001|ROOT:993001>993102:993002>993112:993004', 2, @branch_a_id, @branch_a_shared_bom_item_id,
 20.000000, '2026-05-28', 20.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 20.000000, @sale_order_id, @sale_order_item_id, 0, NULL, 'A 分支共享采购件',
 @creator, NOW(), @creator, NOW(), b'0'),
(@purchase_suggest_b_id, @plan_id, @shared_b_trace_node_id, @project_id, @shared_material_id, '993601|993901|993911|993001|ROOT:993001>993103:993003>993122:993004', 2, @branch_b_id, @branch_b_shared_bom_item_id,
 30.000000, '2026-05-28', 30.000000, 0.000000, 0.000000, 0.000000, 0.000000, 0.000000, 30.000000, @sale_order_id, @sale_order_item_id, 0, NULL, 'B 分支共享采购件',
 @creator, NOW(), @creator, NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `trace_node_id` = VALUES(`trace_node_id`),
  `project_id` = VALUES(`project_id`),
  `material_id` = VALUES(`material_id`),
  `trace_path_key` = VALUES(`trace_path_key`),
  `trace_level` = VALUES(`trace_level`),
  `parent_material_id` = VALUES(`parent_material_id`),
  `bom_item_id` = VALUES(`bom_item_id`),
  `suggest_qty` = VALUES(`suggest_qty`),
  `suggest_arrival_date` = VALUES(`suggest_arrival_date`),
  `gross_demand_qty` = VALUES(`gross_demand_qty`),
  `available_stock_qty` = VALUES(`available_stock_qty`),
  `incoming_qty` = VALUES(`incoming_qty`),
  `wip_qty` = VALUES(`wip_qty`),
  `reserved_stock_qty` = VALUES(`reserved_stock_qty`),
  `safety_stock_qty` = VALUES(`safety_stock_qty`),
  `net_demand_qty` = VALUES(`net_demand_qty`),
  `source_order_id` = VALUES(`source_order_id`),
  `source_item_id` = VALUES(`source_item_id`),
  `status` = VALUES(`status`),
  `convert_purchase_order_id` = VALUES(`convert_purchase_order_id`),
  `remark` = VALUES(`remark`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`),

INSERT INTO `erp_production_suggest`
(`id`, `plan_id`, `trace_node_id`, `project_id`, `product_id`, `trace_path_key`, `trace_level`, `parent_material_id`, `bom_item_id`,
 `suggest_qty`, `suggest_start_date`, `suggest_end_date`, `gross_demand_qty`, `available_stock_qty`, `incoming_qty`, `wip_qty`, `reserved_stock_qty`,
 `safety_stock_qty`, `net_demand_qty`, `source_order_id`, `source_item_id`, `status`, `convert_production_order_id`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@production_root_id, @plan_id, @root_trace_node_id, @project_id, @root_product_id, '993601|993901|993911|993001|ROOT:993001', 0, NULL, NULL,
 10.000000, '2026-05-28', '2026-05-28', 10.000000, 0.000000, 0.000000, 0.000000, 0.000000,
 0.000000, 10.000000, @sale_order_id, @sale_order_item_id, 0, NULL, '根产品生产建议',
 @creator, NOW(), @creator, NOW(), b'0'),
(@production_a_id, @plan_id, @branch_a_trace_node_id, @project_id, @branch_a_id, '993601|993901|993911|993001|ROOT:993001>993102:993002', 1, @root_product_id, @root_bom_item_a_id,
 10.000000, '2026-05-28', '2026-05-28', 10.000000, 0.000000, 0.000000, 0.000000, 0.000000,
 0.000000, 10.000000, @sale_order_id, @sale_order_item_id, 0, NULL, 'A 分支生产建议',
 @creator, NOW(), @creator, NOW(), b'0'),
(@production_b_id, @plan_id, @branch_b_trace_node_id, @project_id, @branch_b_id, '993601|993901|993911|993001|ROOT:993001>993103:993003', 1, @root_product_id, @root_bom_item_b_id,
 10.000000, '2026-05-28', '2026-05-28', 10.000000, 0.000000, 0.000000, 0.000000, 0.000000,
 0.000000, 10.000000, @sale_order_id, @sale_order_item_id, 0, NULL, 'B 分支生产建议',
 @creator, NOW(), @creator, NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `trace_node_id` = VALUES(`trace_node_id`),
  `project_id` = VALUES(`project_id`),
  `product_id` = VALUES(`product_id`),
  `trace_path_key` = VALUES(`trace_path_key`),
  `trace_level` = VALUES(`trace_level`),
  `parent_material_id` = VALUES(`parent_material_id`),
  `bom_item_id` = VALUES(`bom_item_id`),
  `suggest_qty` = VALUES(`suggest_qty`),
  `suggest_start_date` = VALUES(`suggest_start_date`),
  `suggest_end_date` = VALUES(`suggest_end_date`),
  `gross_demand_qty` = VALUES(`gross_demand_qty`),
  `available_stock_qty` = VALUES(`available_stock_qty`),
  `incoming_qty` = VALUES(`incoming_qty`),
  `wip_qty` = VALUES(`wip_qty`),
  `reserved_stock_qty` = VALUES(`reserved_stock_qty`),
  `safety_stock_qty` = VALUES(`safety_stock_qty`),
  `net_demand_qty` = VALUES(`net_demand_qty`),
  `source_order_id` = VALUES(`source_order_id`),
  `source_item_id` = VALUES(`source_item_id`),
  `status` = VALUES(`status`),
  `convert_production_order_id` = VALUES(`convert_production_order_id`),
  `remark` = VALUES(`remark`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`),

INSERT INTO `erp_mrp_shortage`
(`id`, `plan_id`, `trace_node_id`, `root_product_id`, `material_id`, `trace_path_key`, `trace_level`, `parent_material_id`, `bom_item_id`,
 `shortage_qty`, `required_date`, `source_order_id`, `source_item_id`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@shortage_id, @plan_id, @branch_c_trace_node_id, @root_product_id, @branch_c_id, '993601|993901|993911|993001|ROOT:993001>993104:993007', 1, @root_product_id, @root_bom_item_c_id,
 10.000000, '2026-05-28', @sale_order_id, @sale_order_item_id,
 @creator, NOW(), @creator, NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `trace_node_id` = VALUES(`trace_node_id`),
  `root_product_id` = VALUES(`root_product_id`),
  `material_id` = VALUES(`material_id`),
  `trace_path_key` = VALUES(`trace_path_key`),
  `trace_level` = VALUES(`trace_level`),
  `parent_material_id` = VALUES(`parent_material_id`),
  `bom_item_id` = VALUES(`bom_item_id`),
  `shortage_qty` = VALUES(`shortage_qty`),
  `required_date` = VALUES(`required_date`),
  `source_order_id` = VALUES(`source_order_id`),
  `source_item_id` = VALUES(`source_item_id`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`),

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
