/*
 ERP MRP 全链路测试数据 - 基础主数据
 作用：
 - 准备可复用的单位、分类、物料、客户、项目、BOM 和替代料
 - 只放主数据，不放计划结果
 - 支持重复执行
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

START TRANSACTION;

SET @creator := 'tester';

SET @unit_id := 993021;
SET @category_root_id := 993011;
SET @category_leaf_id := 993012;
SET @customer_id := 993801;
SET @project_id := 993701;

SET @root_product_id := 993001;
SET @branch_a_id := 993002;
SET @branch_b_id := 993003;
SET @branch_c_id := 993007;
SET @shared_material_id := 993004;
SET @substitute_a_id := 993005;
SET @substitute_b_id := 993006;

SET @root_bom_id := 993101;
SET @branch_a_bom_id := 993111;
SET @branch_b_bom_id := 993121;

SET @root_bom_item_a_id := 993102;
SET @root_bom_item_b_id := 993103;
SET @root_bom_item_c_id := 993104;
SET @branch_a_shared_bom_item_id := 993112;
SET @branch_b_shared_bom_item_id := 993122;

SET @branch_a_substitute_id := 993201;
SET @branch_b_substitute_id := 993202;

DELETE FROM `erp_mrp_stock_reservation` WHERE `plan_id` = 993601 OR `source_order_id` = 993901;
DELETE FROM `erp_mrp_result_component` WHERE `plan_id` = 993601;
DELETE FROM `erp_mrp_trace_node` WHERE `plan_id` = 993601;
DELETE FROM `erp_mrp_result` WHERE `plan_id` = 993601;
DELETE FROM `erp_mrp_shortage` WHERE `plan_id` = 993601;
DELETE FROM `erp_purchase_suggest` WHERE `plan_id` = 993601;
DELETE FROM `erp_production_suggest` WHERE `plan_id` = 993601;
DELETE FROM `erp_mrp_demand` WHERE `plan_id` = 993601;
DELETE FROM `erp_mrp_plan` WHERE `id` = 993601;

DELETE FROM `erp_project_role_task` WHERE `project_id` = @project_id;
DELETE FROM `erp_sale_order_audit_log` WHERE `order_id` = 993901;
DELETE FROM `erp_sale_order_reject_log` WHERE `order_id` = 993901;
DELETE FROM `erp_sale_order_items` WHERE `id` = 993911 OR `order_id` = 993901;
DELETE FROM `erp_sale_order` WHERE `id` = 993901;
DELETE FROM `erp_project` WHERE `id` = @project_id;
DELETE FROM `erp_bom_item_substitute` WHERE `id` IN (@branch_a_substitute_id, @branch_b_substitute_id);
DELETE FROM `erp_bom_item` WHERE `id` IN (
    @root_bom_item_a_id,
    @root_bom_item_b_id,
    @root_bom_item_c_id,
    @branch_a_shared_bom_item_id,
    @branch_b_shared_bom_item_id
);
DELETE FROM `erp_bom` WHERE `id` IN (@root_bom_id, @branch_a_bom_id, @branch_b_bom_id);
DELETE FROM `erp_product` WHERE `id` IN (
    @root_product_id,
    @branch_a_id,
    @branch_b_id,
    @branch_c_id,
    @shared_material_id,
    @substitute_a_id,
    @substitute_b_id
);
DELETE FROM `erp_customer` WHERE `id` = @customer_id;
DELETE FROM `erp_product_category` WHERE `id` IN (@category_root_id, @category_leaf_id);
DELETE FROM `erp_product_unit` WHERE `id` = @unit_id;

INSERT INTO `erp_product_unit`
(`id`, `name`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@unit_id, '个', 0, @creator, NOW(), @creator, NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `status` = VALUES(`status`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`);

INSERT INTO `erp_product_category`
(`id`, `parent_id`, `name`, `code`, `sort`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@category_root_id, 0, 'MRP 全链路测试', 'MRP-FULL-CHAIN', 1, 0, @creator, NOW(), @creator, NOW(), b'0'),
(@category_leaf_id, @category_root_id, 'MRP 追溯物料', 'MRP-FULL-CHAIN-LEAF', 2, 0, @creator, NOW(), @creator, NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `parent_id` = VALUES(`parent_id`),
  `name` = VALUES(`name`),
  `code` = VALUES(`code`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`);

INSERT INTO `erp_product`
(`id`, `name`, `material_code`, `bar_code`, `category_id`, `unit_id`, `status`, `standard`, `remark`, `expiry_day`, `batch_control_flag`,
 `inspection_required_flag`, `weight`, `purchase_price`, `sale_price`, `min_price`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@root_product_id, 'MRP 全链路根产品', 'MRP-FULL-ROOT', 'MRP-FULL-ROOT-001', @category_root_id, @unit_id, 0, '根产品', '销售订单触发的 MRP 根产品',
 3650, b'0', b'0', 1.200000, 120.000000, 188.000000, 168.000000, @creator, NOW(), @creator, NOW(), b'0'),
(@branch_a_id, '制造分支 A', 'MRP-FULL-A', 'MRP-FULL-A-001', @category_leaf_id, @unit_id, 0, '半成品 A', 'A 分支制造件',
 3650, b'0', b'0', 0.800000, 28.000000, 42.000000, 36.000000, @creator, NOW(), @creator, NOW(), b'0'),
(@branch_b_id, '制造分支 B', 'MRP-FULL-B', 'MRP-FULL-B-001', @category_leaf_id, @unit_id, 0, '半成品 B', 'B 分支制造件',
 3650, b'0', b'0', 0.900000, 32.000000, 48.000000, 40.000000, @creator, NOW(), @creator, NOW(), b'0'),
(@branch_c_id, '缺料分支 C', 'MRP-FULL-C', 'MRP-FULL-C-001', @category_leaf_id, @unit_id, 0, '半成品 C', '无 BOM 的制造件，用于短缺分支',
 3650, b'0', b'0', 0.700000, 24.000000, 38.000000, 30.000000, @creator, NOW(), @creator, NOW(), b'0'),
(@shared_material_id, '共享采购件', 'MRP-FULL-SHARED', 'MRP-FULL-SHARED-001', @category_leaf_id, @unit_id, 0, '通用组件', '同一物料在两个父件下重复出现',
 3650, b'0', b'0', 0.200000, 4.500000, 7.800000, 6.200000, @creator, NOW(), @creator, NOW(), b'0'),
(@substitute_a_id, 'A 分支替代料', 'MRP-FULL-SUB-A', 'MRP-FULL-SUB-A-001', @category_leaf_id, @unit_id, 0, '替代料 A', '仅用于 A 分支 BOM 项',
 3650, b'0', b'0', 0.180000, 3.200000, 5.600000, 4.800000, @creator, NOW(), @creator, NOW(), b'0'),
(@substitute_b_id, 'B 分支替代料', 'MRP-FULL-SUB-B', 'MRP-FULL-SUB-B-001', @category_leaf_id, @unit_id, 0, '替代料 B', '仅用于 B 分支 BOM 项',
 3650, b'0', b'0', 0.190000, 3.500000, 5.900000, 5.100000, @creator, NOW(), @creator, NOW(), b'0')
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
  `deleted` = VALUES(`deleted`);

INSERT INTO `erp_customer`
(`id`, `name`, `contact`, `mobile`, `telephone`, `email`, `fax`, `remark`, `status`, `sort`,
 `tax_no`, `tax_percent`, `bank_name`, `bank_account`, `bank_address`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@customer_id, 'MRP 全链路测试客户', NULL, NULL, NULL, NULL, NULL, '全链路 MRP 测试客户', 0, 1,
 NULL, NULL, NULL, NULL, NULL, @creator, NOW(), @creator, NOW(), b'0')
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
  `deleted` = VALUES(`deleted`);

INSERT INTO `erp_project`
(`id`, `no`, `name`, `project_type`, `business_type`, `source_type`, `source_project_id`, `sale_order_id`, `project_manager_id`,
 `plan_coordinator_id`, `material_controller_id`, `owner_dept_id`, `current_stage_code`, `risk_level`, `customer_id`, `status`,
 `pc_status`, `mc_status`, `pc_confirm_time`, `mc_confirm_time`, `pc_remark`, `mc_remark`, `delivery_date`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@project_id, 'MRP-FULL-PROJ-001', 'MRP 全链路测试项目', NULL, NULL, NULL, NULL, NULL, NULL,
 1, 1, NULL, NULL, NULL, @customer_id, 1,
 'PENDING', 'PENDING', NULL, NULL, NULL, NULL, '2026-05-28', 'MRP 全链路验证项目',
 @creator, NOW(), @creator, NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `no` = VALUES(`no`),
  `name` = VALUES(`name`),
  `customer_id` = VALUES(`customer_id`),
  `plan_coordinator_id` = VALUES(`plan_coordinator_id`),
  `material_controller_id` = VALUES(`material_controller_id`),
  `status` = VALUES(`status`),
  `pc_status` = VALUES(`pc_status`),
  `mc_status` = VALUES(`mc_status`),
  `delivery_date` = VALUES(`delivery_date`),
  `remark` = VALUES(`remark`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`);

INSERT INTO `erp_bom`
(`id`, `bom_code`, `product_id`, `version`, `status`, `source_rd_bom_id`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@root_bom_id, 'MRP-FULL-BOM-ROOT', @root_product_id, 'V1.0', 1, NULL, '根产品 BOM', @creator, NOW(), @creator, NOW(), b'0'),
(@branch_a_bom_id, 'MRP-FULL-BOM-A', @branch_a_id, 'V1.0', 1, NULL, 'A 分支 BOM', @creator, NOW(), @creator, NOW(), b'0'),
(@branch_b_bom_id, 'MRP-FULL-BOM-B', @branch_b_id, 'V1.0', 1, NULL, 'B 分支 BOM', @creator, NOW(), @creator, NOW(), b'0')
ON DUPLICATE KEY UPDATE
  `bom_code` = VALUES(`bom_code`),
  `product_id` = VALUES(`product_id`),
  `version` = VALUES(`version`),
  `status` = VALUES(`status`),
  `source_rd_bom_id` = VALUES(`source_rd_bom_id`),
  `remark` = VALUES(`remark`),
  `updater` = VALUES(`updater`),
  `update_time` = NOW(),
  `deleted` = VALUES(`deleted`);

INSERT INTO `erp_bom_item`
(`id`, `bom_id`, `material_id`, `material_type`, `unit_id`, `usage_qty`, `loss_rate`, `lead_time_day`,
 `mrp_enable_flag`, `supply_owner`, `sort`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@root_bom_item_a_id, @root_bom_id, @branch_a_id, 1, @unit_id, 1.000000, 0.0000, 0, b'1', 'COMPANY', 1, '根产品下的 A 制造分支',
 @creator, NOW(), @creator, NOW(), b'0'),
(@root_bom_item_b_id, @root_bom_id, @branch_b_id, 1, @unit_id, 1.000000, 0.0000, 0, b'1', 'COMPANY', 2, '根产品下的 B 制造分支',
 @creator, NOW(), @creator, NOW(), b'0'),
(@root_bom_item_c_id, @root_bom_id, @branch_c_id, 1, @unit_id, 1.000000, 0.0000, 0, b'1', 'COMPANY', 3, '根产品下的 C 缺料分支',
 @creator, NOW(), @creator, NOW(), b'0'),
(@branch_a_shared_bom_item_id, @branch_a_bom_id, @shared_material_id, 2, @unit_id, 2.000000, 0.0000, 0, b'1', 'COMPANY', 1, 'A 分支共享采购件',
 @creator, NOW(), @creator, NOW(), b'0'),
(@branch_b_shared_bom_item_id, @branch_b_bom_id, @shared_material_id, 2, @unit_id, 3.000000, 0.0000, 0, b'1', 'COMPANY', 1, 'B 分支共享采购件',
 @creator, NOW(), @creator, NOW(), b'0')
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
  `deleted` = VALUES(`deleted`);

INSERT INTO `erp_bom_item_substitute`
(`id`, `bom_item_id`, `substitute_material_id`, `priority`, `replace_ratio`, `enable_auto_recommend`, `sort`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@branch_a_substitute_id, @branch_a_shared_bom_item_id, @substitute_a_id, 1, 1.000000, b'1', 1, 'A 分支替代料',
 @creator, NOW(), @creator, NOW(), b'0'),
(@branch_b_substitute_id, @branch_b_shared_bom_item_id, @substitute_b_id, 1, 1.000000, b'1', 1, 'B 分支替代料',
 @creator, NOW(), @creator, NOW(), b'0')
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
  `deleted` = VALUES(`deleted`);

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
