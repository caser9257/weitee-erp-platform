/*
 Target: ERP MRP project-scoped supply and stock reservation
 Schema: ruoyi-vue-pro
 Date: 2026-04-08
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @add_purchase_item_project_id = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_purchase_order_items'
        AND COLUMN_NAME = 'project_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_order_items` ADD COLUMN `project_id` bigint DEFAULT NULL COMMENT ''project id'' AFTER `product_id`'
  )
);
PREPARE stmt FROM @add_purchase_item_project_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_purchase_suggest_project_id = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_purchase_suggest'
        AND COLUMN_NAME = 'project_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_suggest` ADD COLUMN `project_id` bigint DEFAULT NULL COMMENT ''project id'' AFTER `plan_id`'
  )
);
PREPARE stmt FROM @add_purchase_suggest_project_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_production_suggest_project_id = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_suggest'
        AND COLUMN_NAME = 'project_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_suggest` ADD COLUMN `project_id` bigint DEFAULT NULL COMMENT ''project id'' AFTER `plan_id`'
  )
);
PREPARE stmt FROM @add_production_suggest_project_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_production_order_project_id = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_order'
        AND COLUMN_NAME = 'project_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_order` ADD COLUMN `project_id` bigint DEFAULT NULL COMMENT ''project id'' AFTER `product_id`'
  )
);
PREPARE stmt FROM @add_production_order_project_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_result_reserved_stock_qty = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'reserved_stock_qty'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result` ADD COLUMN `reserved_stock_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT ''reserved stock qty'' AFTER `wip_qty`'
  )
);
PREPARE stmt FROM @add_result_reserved_stock_qty;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_purchase_suggest_reserved_stock_qty = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_purchase_suggest'
        AND COLUMN_NAME = 'reserved_stock_qty'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_suggest` ADD COLUMN `reserved_stock_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT ''reserved stock qty'' AFTER `wip_qty`'
  )
);
PREPARE stmt FROM @add_purchase_suggest_reserved_stock_qty;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_production_suggest_reserved_stock_qty = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_suggest'
        AND COLUMN_NAME = 'reserved_stock_qty'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_suggest` ADD COLUMN `reserved_stock_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT ''reserved stock qty'' AFTER `wip_qty`'
  )
);
PREPARE stmt FROM @add_production_suggest_reserved_stock_qty;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `erp_mrp_stock_reservation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plan_id` bigint NOT NULL,
  `project_id` bigint DEFAULT NULL,
  `product_id` bigint NOT NULL,
  `source_order_id` bigint NOT NULL,
  `source_item_id` bigint DEFAULT NULL,
  `reserved_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
  `status` tinyint NOT NULL DEFAULT 0,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `idx_erp_mrp_stock_reservation_product_id` (`product_id`),
  KEY `idx_erp_mrp_stock_reservation_source_order_id` (`source_order_id`),
  KEY `idx_erp_mrp_stock_reservation_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

UPDATE `erp_production_order` po
JOIN `erp_production_suggest` ps ON po.source_type = 'MRP_SUGGEST' AND po.source_id = ps.id
SET po.project_id = ps.project_id
WHERE po.project_id IS NULL
  AND ps.project_id IS NOT NULL;

SET FOREIGN_KEY_CHECKS = 1;
