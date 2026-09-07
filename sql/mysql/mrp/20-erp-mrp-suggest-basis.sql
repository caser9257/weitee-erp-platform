/*
 Target: ERP MRP suggest basis fields
 Schema: ruoyi-vue-pro
 Date: 2026-04-07
 Note: Compatible with MySQL versions that do not support ALTER TABLE ... ADD COLUMN IF NOT EXISTS
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


SET @add_purchase_gross_demand_qty = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_purchase_suggest'
        AND COLUMN_NAME = 'gross_demand_qty'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_suggest` ADD COLUMN `gross_demand_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT ''gross demand qty'' AFTER `suggest_arrival_date`'
  )
);
PREPARE stmt FROM @add_purchase_gross_demand_qty;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_purchase_available_stock_qty = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_purchase_suggest'
        AND COLUMN_NAME = 'available_stock_qty'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_suggest` ADD COLUMN `available_stock_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT ''available stock qty'' AFTER `gross_demand_qty`'
  )
);
PREPARE stmt FROM @add_purchase_available_stock_qty;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_purchase_incoming_qty = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_purchase_suggest'
        AND COLUMN_NAME = 'incoming_qty'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_suggest` ADD COLUMN `incoming_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT ''incoming qty'' AFTER `available_stock_qty`'
  )
);
PREPARE stmt FROM @add_purchase_incoming_qty;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_purchase_wip_qty = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_purchase_suggest'
        AND COLUMN_NAME = 'wip_qty'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_suggest` ADD COLUMN `wip_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT ''wip qty'' AFTER `incoming_qty`'
  )
);
PREPARE stmt FROM @add_purchase_wip_qty;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_purchase_safety_stock_qty = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_purchase_suggest'
        AND COLUMN_NAME = 'safety_stock_qty'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_suggest` ADD COLUMN `safety_stock_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT ''safety stock qty'' AFTER `wip_qty`'
  )
);
PREPARE stmt FROM @add_purchase_safety_stock_qty;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_purchase_net_demand_qty = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_purchase_suggest'
        AND COLUMN_NAME = 'net_demand_qty'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_suggest` ADD COLUMN `net_demand_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT ''net demand qty'' AFTER `safety_stock_qty`'
  )
);
PREPARE stmt FROM @add_purchase_net_demand_qty;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_production_gross_demand_qty = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_suggest'
        AND COLUMN_NAME = 'gross_demand_qty'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_suggest` ADD COLUMN `gross_demand_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT ''gross demand qty'' AFTER `suggest_end_date`'
  )
);
PREPARE stmt FROM @add_production_gross_demand_qty;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_production_available_stock_qty = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_suggest'
        AND COLUMN_NAME = 'available_stock_qty'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_suggest` ADD COLUMN `available_stock_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT ''available stock qty'' AFTER `gross_demand_qty`'
  )
);
PREPARE stmt FROM @add_production_available_stock_qty;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_production_incoming_qty = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_suggest'
        AND COLUMN_NAME = 'incoming_qty'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_suggest` ADD COLUMN `incoming_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT ''incoming qty'' AFTER `available_stock_qty`'
  )
);
PREPARE stmt FROM @add_production_incoming_qty;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_production_wip_qty = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_suggest'
        AND COLUMN_NAME = 'wip_qty'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_suggest` ADD COLUMN `wip_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT ''wip qty'' AFTER `incoming_qty`'
  )
);
PREPARE stmt FROM @add_production_wip_qty;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_production_safety_stock_qty = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_suggest'
        AND COLUMN_NAME = 'safety_stock_qty'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_suggest` ADD COLUMN `safety_stock_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT ''safety stock qty'' AFTER `wip_qty`'
  )
);
PREPARE stmt FROM @add_production_safety_stock_qty;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_production_net_demand_qty = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_suggest'
        AND COLUMN_NAME = 'net_demand_qty'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_suggest` ADD COLUMN `net_demand_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT ''net demand qty'' AFTER `safety_stock_qty`'
  )
);
PREPARE stmt FROM @add_production_net_demand_qty;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;
