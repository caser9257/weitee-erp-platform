/*
 Target: ERP MRP result/shortage view compatible fields
 Schema: ruoyi-vue-pro
 Date: 2026-04-23
 Purpose:
   - Align erp_mrp_result with current DO fields used by /erp/mrp-plan/result
   - Align erp_mrp_shortage with current DO fields used by /erp/mrp-plan/shortage
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


SET @add_mrp_result_reserved_stock_qty = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'reserved_stock_qty'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result`
      ADD COLUMN `reserved_stock_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT ''reserved stock qty'' AFTER `wip_qty`'
  )
);
PREPARE stmt FROM @add_mrp_result_reserved_stock_qty;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_result_business_type = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'business_type'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result`
      ADD COLUMN `business_type` varchar(32) DEFAULT NULL COMMENT ''business type'' AFTER `net_demand_qty`'
  )
);
PREPARE stmt FROM @add_mrp_result_business_type;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_result_mrp_enable_flag = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'mrp_enable_flag'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result`
      ADD COLUMN `mrp_enable_flag` tinyint(1) DEFAULT NULL COMMENT ''mrp enable flag'' AFTER `business_type`'
  )
);
PREPARE stmt FROM @add_mrp_result_mrp_enable_flag;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_result_supply_owner = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'supply_owner'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result`
      ADD COLUMN `supply_owner` varchar(32) DEFAULT NULL COMMENT ''supply owner'' AFTER `mrp_enable_flag`'
  )
);
PREPARE stmt FROM @add_mrp_result_supply_owner;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_result_skip_reason = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'skip_reason'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result`
      ADD COLUMN `skip_reason` varchar(64) DEFAULT NULL COMMENT ''skip reason'' AFTER `supply_owner`'
  )
);
PREPARE stmt FROM @add_mrp_result_skip_reason;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_result_policy_code = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'policy_code'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result`
      ADD COLUMN `policy_code` varchar(32) DEFAULT NULL COMMENT ''netting policy code'' AFTER `skip_reason`'
  )
);
PREPARE stmt FROM @add_mrp_result_policy_code;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_result_policy_version = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'policy_version'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result`
      ADD COLUMN `policy_version` int DEFAULT NULL COMMENT ''netting policy version'' AFTER `policy_code`'
  )
);
PREPARE stmt FROM @add_mrp_result_policy_version;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_result_source_item_id = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'source_item_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result`
      ADD COLUMN `source_item_id` bigint DEFAULT NULL COMMENT ''source item id'' AFTER `source_order_id`'
  )
);
PREPARE stmt FROM @add_mrp_result_source_item_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_result_demand_date = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'demand_date'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result`
      ADD COLUMN `demand_date` date DEFAULT NULL COMMENT ''demand date'' AFTER `source_item_id`'
  )
);
PREPARE stmt FROM @add_mrp_result_demand_date;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_shortage_source_item_id = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_shortage'
        AND COLUMN_NAME = 'source_item_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_shortage`
      ADD COLUMN `source_item_id` bigint DEFAULT NULL COMMENT ''source item id'' AFTER `source_order_id`'
  )
);
PREPARE stmt FROM @add_mrp_shortage_source_item_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;
