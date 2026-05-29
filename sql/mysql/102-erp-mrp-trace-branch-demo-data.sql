/*
 ERP MRP 双分支追溯测试数据
 目标：
 - 根产品下挂两个不同父件
 - 两个父件下复用同一个共享物料
 - 两个 BOM 项分别配置不同替代料
 - 提供一张已审核销售订单和一个待运行 MRP 计划
 - 可重复执行，保持幂等
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

START TRANSACTION;

SET @tenant_id := COALESCE((SELECT id FROM system_tenant WHERE deleted = b'0' ORDER BY id LIMIT 1), 1);
SET @creator := 'tester';

SET @unit_id := 993021;
SET @category_root_id := 993011;
SET @category_leaf_id := 993012;
SET @customer_id := 993801;
SET @project_id := 993701;
SET @plan_id := 993601;
SET @sale_order_id := 993901;
SET @sale_order_item_id := 993911;

SET @root_product_id := 993001;
SET @branch_a_id := 993002;
SET @branch_b_id := 993003;
SET @shared_material_id := 993004;
SET @substitute_a_id := 993005;
SET @substitute_b_id := 993006;

SET @root_bom_id := 993101;
SET @branch_a_bom_id := 993111;
SET @branch_b_bom_id := 993121;

SET @root_bom_item_a_id := 993102;
SET @root_bom_item_b_id := 993103;
SET @branch_a_shared_bom_item_id := 993112;
SET @branch_b_shared_bom_item_id := 993122;

SET @branch_a_substitute_id := 993201;
SET @branch_b_substitute_id := 993202;

DELETE FROM `erp_mrp_stock_reservation` WHERE `plan_id` = @plan_id OR `source_order_id` = @sale_order_id;
DELETE FROM `erp_mrp_trace_node` WHERE `plan_id` = @plan_id;
DELETE FROM `erp_mrp_result` WHERE `plan_id` = @plan_id;
DELETE FROM `erp_mrp_shortage` WHERE `plan_id` = @plan_id;
DELETE FROM `erp_purchase_suggest` WHERE `plan_id` = @plan_id;
DELETE FROM `erp_production_suggest` WHERE `plan_id` = @plan_id;
DELETE FROM `erp_mrp_demand` WHERE `plan_id` = @plan_id;
DELETE FROM `erp_mrp_plan` WHERE `id` = @plan_id;

DELETE FROM `erp_bom_item_substitute` WHERE `id` IN (@branch_a_substitute_id, @branch_b_substitute_id);
DELETE FROM `erp_bom_item` WHERE `id` IN (@root_bom_item_a_id, @root_bom_item_b_id, @branch_a_shared_bom_item_id, @branch_b_shared_bom_item_id);
DELETE FROM `erp_bom` WHERE `id` IN (@root_bom_id, @branch_a_bom_id, @branch_b_bom_id);

DELETE FROM `erp_sale_order_items` WHERE `id` = @sale_order_item_id;
DELETE FROM `erp_sale_order` WHERE `id` = @sale_order_id;
DELETE FROM `erp_project` WHERE `id` = @project_id;
DELETE FROM `erp_customer` WHERE `id` = @customer_id;
DELETE FROM `erp_product` WHERE `id` IN (@root_product_id, @branch_a_id, @branch_b_id, @shared_material_id, @substitute_a_id, @substitute_b_id);
DELETE FROM `erp_product_category` WHERE `id` IN (@category_root_id, @category_leaf_id);
DELETE FROM `erp_product_unit` WHERE `id` = @unit_id;

INSERT INTO `erp_product_unit`
(`id`, `name`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(@unit_id, '个', 0, @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `status` = VALUES(`status`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`),
  `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `erp_product_category`
(`id`, `parent_id`, `name`, `code`, `sort`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(@category_root_id, 0, 'MRP 双分支演示', 'MRP-TRACE-DEMO', 1, 0, @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(@category_leaf_id, @category_root_id, '共享物料', 'MRP-TRACE-DEMO-LEAF', 2, 0, @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
  `parent_id` = VALUES(`parent_id`),
  `name` = VALUES(`name`),
  `code` = VALUES(`code`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`),
  `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `erp_product`
(`id`, `name`, `material_code`, `bar_code`, `category_id`, `unit_id`, `status`, `standard`, `remark`, `expiry_day`, `batch_control_flag`,
 `inspection_required_flag`, `weight`, `purchase_price`, `sale_price`, `min_price`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(@root_product_id, 'MRP 双分支根产品', 'MRP-TRACE-ROOT', 'MRP-TRACE-ROOT-001', @category_root_id, @unit_id, 0, '根产品', 'MRP 追溯测试根产品',
 3650, b'0', b'0', 1.200000, 120.000000, 168.000000, 150.000000, @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(@branch_a_id, 'A 分支父件', 'MRP-TRACE-A', 'MRP-TRACE-A-001', @category_leaf_id, @unit_id, 0, 'A 分支半成品', 'A 分支中间件',
 3650, b'0', b'0', 0.800000, 20.000000, 32.000000, 25.000000, @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(@branch_b_id, 'B 分支父件', 'MRP-TRACE-B', 'MRP-TRACE-B-001', @category_leaf_id, @unit_id, 0, 'B 分支半成品', 'B 分支中间件',
 3650, b'0', b'0', 0.900000, 22.000000, 35.000000, 28.000000, @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(@shared_material_id, '共享缺料物料', 'MRP-TRACE-C', 'MRP-TRACE-C-001', @category_leaf_id, @unit_id, 0, '共享组件', '同一物料在两个父件下重复出现',
 3650, b'0', b'0', 0.200000, 4.500000, 7.800000, 6.200000, @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(@substitute_a_id, 'A 分支替代料', 'MRP-TRACE-CA', 'MRP-TRACE-CA-001', @category_leaf_id, @unit_id, 0, '替代料 A', '仅用于 A 分支 BOM 项',
 3650, b'0', b'0', 0.180000, 3.200000, 5.600000, 4.800000, @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(@substitute_b_id, 'B 分支替代料', 'MRP-TRACE-CB', 'MRP-TRACE-CB-001', @category_leaf_id, @unit_id, 0, '替代料 B', '仅用于 B 分支 BOM 项',
 3650, b'0', b'0', 0.190000, 3.500000, 5.900000, 5.100000, @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `material_code` = VALUES(`material_code`),
  `bar_code` = VALUES(`bar_code`),
  `category_id` = VALUES(`category_id`),
  `unit_id` = VALUES(`unit_id`),
  `status` = VALUES(`status`),
  `standard` = VALUES(`standard`),
  `remark` = VALUES(`remark`),
  `expiry_day` = VALUES(`expiry_day`),
  `batch_control_flag` = VALUES(`batch_control_flag`),
  `inspection_required_flag` = VALUES(`inspection_required_flag`),
  `weight` = VALUES(`weight`),
  `purchase_price` = VALUES(`purchase_price`),
  `sale_price` = VALUES(`sale_price`),
  `min_price` = VALUES(`min_price`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`),
  `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `erp_customer`
(`id`, `name`, `contact`, `mobile`, `telephone`, `email`, `fax`, `remark`, `status`, `sort`,
 `tax_no`, `tax_percent`, `bank_name`, `bank_account`, `bank_address`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(@customer_id, 'MRP 追溯测试客户', NULL, NULL, NULL, NULL, NULL, '双分支 MRP 测试客户', 0, 1,
 NULL, NULL, NULL, NULL, NULL, @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `contact` = VALUES(`contact`),
  `mobile` = VALUES(`mobile`),
  `telephone` = VALUES(`telephone`),
  `email` = VALUES(`email`),
  `fax` = VALUES(`fax`),
  `remark` = VALUES(`remark`),
  `status` = VALUES(`status`),
  `sort` = VALUES(`sort`),
  `tax_no` = VALUES(`tax_no`),
  `tax_percent` = VALUES(`tax_percent`),
  `bank_name` = VALUES(`bank_name`),
  `bank_account` = VALUES(`bank_account`),
  `bank_address` = VALUES(`bank_address`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`),
  `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `erp_project`
(`id`, `no`, `name`, `project_type`, `business_type`, `source_type`, `source_project_id`, `sale_order_id`, `project_manager_id`,
 `plan_coordinator_id`, `material_controller_id`, `owner_dept_id`, `current_stage_code`, `risk_level`, `customer_id`, `status`,
 `pc_status`, `mc_status`, `pc_confirm_time`, `mc_confirm_time`, `pc_remark`, `mc_remark`, `delivery_date`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(@project_id, 'MRP-TRACE-PROJ-001', 'MRP 双分支追溯项目', NULL, NULL, NULL, NULL, NULL, NULL,
NULL, NULL, NULL, NULL, NULL, @customer_id, 1,
'PENDING', 'PENDING', NULL, NULL, NULL, NULL, DATE_ADD(CURDATE(), INTERVAL 7 DAY), '双分支 MRP 测试项目',
 @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
  `no` = VALUES(`no`),
  `name` = VALUES(`name`),
  `customer_id` = VALUES(`customer_id`),
  `status` = VALUES(`status`),
  `delivery_date` = VALUES(`delivery_date`),
  `remark` = VALUES(`remark`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`),
  `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `erp_bom`
(`id`, `bom_code`, `product_id`, `version`, `status`, `source_rd_bom_id`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(@root_bom_id, 'MRP-TRACE-BOM-ROOT', @root_product_id, 'V1.0', 1, NULL, '根产品 BOM', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(@branch_a_bom_id, 'MRP-TRACE-BOM-A', @branch_a_id, 'V1.0', 1, NULL, 'A 分支 BOM', @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(@branch_b_bom_id, 'MRP-TRACE-BOM-B', @branch_b_id, 'V1.0', 1, NULL, 'B 分支 BOM', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
  `bom_code` = VALUES(`bom_code`),
  `product_id` = VALUES(`product_id`),
  `version` = VALUES(`version`),
  `status` = VALUES(`status`),
  `source_rd_bom_id` = VALUES(`source_rd_bom_id`),
  `remark` = VALUES(`remark`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`),
  `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `erp_bom_item`
(`id`, `bom_id`, `material_id`, `material_type`, `unit_id`, `usage_qty`, `loss_rate`, `lead_time_day`,
 `mrp_enable_flag`, `supply_owner`, `sort`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(@root_bom_item_a_id, @root_bom_id, @branch_a_id, 1, @unit_id, 1.000000, 0.0000, 0, b'1', 'COMPANY', 1, '根产品下的 A 分支父件',
 @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(@root_bom_item_b_id, @root_bom_id, @branch_b_id, 1, @unit_id, 1.000000, 0.0000, 0, b'1', 'COMPANY', 2, '根产品下的 B 分支父件',
 @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(@branch_a_shared_bom_item_id, @branch_a_bom_id, @shared_material_id, 1, @unit_id, 2.000000, 0.0000, 0, b'1', 'COMPANY', 1, 'A 分支命中的共享物料',
 @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(@branch_b_shared_bom_item_id, @branch_b_bom_id, @shared_material_id, 1, @unit_id, 3.000000, 0.0000, 0, b'1', 'COMPANY', 1, 'B 分支命中的共享物料',
 @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
  `bom_id` = VALUES(`bom_id`),
  `material_id` = VALUES(`material_id`),
  `material_type` = VALUES(`material_type`),
  `unit_id` = VALUES(`unit_id`),
  `usage_qty` = VALUES(`usage_qty`),
  `loss_rate` = VALUES(`loss_rate`),
  `lead_time_day` = VALUES(`lead_time_day`),
  `mrp_enable_flag` = VALUES(`mrp_enable_flag`),
  `supply_owner` = VALUES(`supply_owner`),
  `sort` = VALUES(`sort`),
  `remark` = VALUES(`remark`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`),
  `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `erp_bom_item_substitute`
(`id`, `bom_item_id`, `substitute_material_id`, `priority`, `replace_ratio`, `enable_auto_recommend`, `sort`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(@branch_a_substitute_id, @branch_a_shared_bom_item_id, @substitute_a_id, 1, 1.000000, b'1', 1, 'A 分支替代料',
 @creator, NOW(), @creator, NOW(), b'0', @tenant_id),
(@branch_b_substitute_id, @branch_b_shared_bom_item_id, @substitute_b_id, 1, 1.000000, b'1', 1, 'B 分支替代料',
 @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
  `bom_item_id` = VALUES(`bom_item_id`),
  `substitute_material_id` = VALUES(`substitute_material_id`),
  `priority` = VALUES(`priority`),
  `replace_ratio` = VALUES(`replace_ratio`),
  `enable_auto_recommend` = VALUES(`enable_auto_recommend`),
  `sort` = VALUES(`sort`),
  `remark` = VALUES(`remark`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`),
  `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `erp_sale_order`
(`id`, `no`, `status`, `customer_id`, `account_id`, `sale_user_id`, `order_time`, `project_id`, `delivery_date`,
 `total_count`, `total_price`, `total_product_price`, `total_tax_price`, `discount_percent`, `discount_price`,
 `deposit_price`, `file_url`, `remark`, `out_count`, `return_count`, `delivery_ready_status`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(@sale_order_id, 'SO-TRACE-DEMO-20260520-001', 20, @customer_id, NULL, NULL, NOW(), @project_id, CURDATE(),
 10.000000, 1680.000000, 1680.000000, 0.000000, 0.000000, 0.000000,
 0.000000, NULL, 'MRP 双分支追溯测试销售订单', 0.000000, 0.000000, 'NOT_READY',
 @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
ON DUPLICATE KEY UPDATE
  `no` = VALUES(`no`),
  `status` = VALUES(`status`),
  `customer_id` = VALUES(`customer_id`),
  `account_id` = VALUES(`account_id`),
  `sale_user_id` = VALUES(`sale_user_id`),
  `order_time` = VALUES(`order_time`),
  `project_id` = VALUES(`project_id`),
  `delivery_date` = VALUES(`delivery_date`),
  `total_count` = VALUES(`total_count`),
  `total_price` = VALUES(`total_price`),
  `total_product_price` = VALUES(`total_product_price`),
  `total_tax_price` = VALUES(`total_tax_price`),
  `discount_percent` = VALUES(`discount_percent`),
  `discount_price` = VALUES(`discount_price`),
  `deposit_price` = VALUES(`deposit_price`),
  `file_url` = VALUES(`file_url`),
  `remark` = VALUES(`remark`),
  `out_count` = VALUES(`out_count`),
  `return_count` = VALUES(`return_count`),
  `delivery_ready_status` = VALUES(`delivery_ready_status`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`),
  `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `erp_sale_order_items`
(`id`, `order_id`, `product_id`, `product_unit_id`, `product_price`, `count`, `total_price`, `tax_percent`, `tax_price`,
 `remark`, `out_count`, `return_count`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(@sale_order_item_id, @sale_order_id, @root_product_id, @unit_id, 168.000000, 10.000000, 1680.000000, 0.000000, 0.000000,
 '触发 MRP 的根产品订单项', 0.000000, 0.000000, @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
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
  `tenant_id` = VALUES(`tenant_id`);

INSERT INTO `erp_mrp_plan`
(`id`, `plan_no`, `plan_name`, `plan_start_date`, `plan_end_date`, `status`, `run_time`, `operator_id`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(@plan_id, 'MRP-TRACE-PLAN-20260520-001', 'MRP 双分支追溯测试计划', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 DAY), 0, NULL, 1,
 '双分支 trace 归因验证计划', @creator, NOW(), @creator, NOW(), b'0', @tenant_id)
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
  `tenant_id` = VALUES(`tenant_id`);

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
