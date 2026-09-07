/*
 Target: ERP MRP business routing compatible fields
 Schema: ruoyi-vue-pro
 Date: 2026-04-17
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


SET @add_bom_item_mrp_enable_flag = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_bom_item'
        AND COLUMN_NAME = 'mrp_enable_flag'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_bom_item` ADD COLUMN `mrp_enable_flag` tinyint(1) DEFAULT 1 COMMENT ''whether participate in mrp'' AFTER `lead_time_day`'
  )
);
PREPARE stmt FROM @add_bom_item_mrp_enable_flag;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_bom_item_supply_owner = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_bom_item'
        AND COLUMN_NAME = 'supply_owner'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_bom_item` ADD COLUMN `supply_owner` varchar(32) DEFAULT NULL COMMENT ''supply owner'' AFTER `mrp_enable_flag`'
  )
);
PREPARE stmt FROM @add_bom_item_supply_owner;
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
    'ALTER TABLE `erp_mrp_result` ADD COLUMN `business_type` varchar(32) DEFAULT NULL COMMENT ''business type'' AFTER `net_demand_qty`'
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
    'ALTER TABLE `erp_mrp_result` ADD COLUMN `mrp_enable_flag` tinyint(1) DEFAULT NULL COMMENT ''mrp enable flag'' AFTER `business_type`'
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
    'ALTER TABLE `erp_mrp_result` ADD COLUMN `supply_owner` varchar(32) DEFAULT NULL COMMENT ''supply owner'' AFTER `mrp_enable_flag`'
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
    'ALTER TABLE `erp_mrp_result` ADD COLUMN `skip_reason` varchar(64) DEFAULT NULL COMMENT ''skip reason'' AFTER `supply_owner`'
  )
);
PREPARE stmt FROM @add_mrp_result_skip_reason;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;
