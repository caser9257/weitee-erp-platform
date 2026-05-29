/*
 Target: local ERP closure bootstrap
 Scope:
   - repair local closure-workbench schema gap
   - do not delete existing business data
   - keep current demo data intact
 Date: 2026-04-21
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

-- sale order delivery-ready status compatibility
SET @ddl_sql = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_sale_order'
        AND COLUMN_NAME = 'delivery_ready_status'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_sale_order`
      ADD COLUMN `delivery_ready_status` varchar(32) NOT NULL DEFAULT ''NOT_READY'' COMMENT ''delivery ready status'' AFTER `return_count`'
  )
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- production order source sale-order relation compatibility
SET @ddl_sql = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_order'
        AND COLUMN_NAME = 'source_order_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_order`
      ADD COLUMN `source_order_id` bigint DEFAULT NULL COMMENT ''source sale order id'' AFTER `source_id`'
  )
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_order'
        AND COLUMN_NAME = 'source_item_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_order`
      ADD COLUMN `source_item_id` bigint DEFAULT NULL COMMENT ''source sale order item id'' AFTER `source_order_id`'
  )
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- lightweight production finish quality table for closure/workbench queries
SET @ddl_sql = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.TABLES
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_finish_quality'
    ),
    'SELECT 1',
    'CREATE TABLE `erp_production_finish_quality` (
      `id` bigint NOT NULL AUTO_INCREMENT,
      `no` varchar(64) NOT NULL,
      `production_order_id` bigint NOT NULL,
      `production_order_no` varchar(64) NOT NULL,
      `source_order_id` bigint DEFAULT NULL,
      `source_item_id` bigint DEFAULT NULL,
      `product_id` bigint NOT NULL,
      `report_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
      `qualified_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
      `unqualified_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
      `status` tinyint NOT NULL DEFAULT 10 COMMENT ''10 pending, 20 partial, 30 passed, 40 failed'',
      `checker_user_id` bigint DEFAULT NULL,
      `check_time` datetime DEFAULT NULL,
      `remark` varchar(255) DEFAULT NULL,
      `creator` varchar(64) DEFAULT '''',
      `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
      `updater` varchar(64) DEFAULT '''',
      `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      `deleted` bit(1) NOT NULL DEFAULT b''0'',
      `tenant_id` bigint NOT NULL DEFAULT 0,
      PRIMARY KEY (`id`),
      UNIQUE KEY `uk_erp_production_finish_quality_no` (`tenant_id`, `no`),
      UNIQUE KEY `uk_erp_production_finish_quality_order` (`tenant_id`, `production_order_id`),
      KEY `idx_erp_production_finish_quality_source_order` (`tenant_id`, `source_order_id`),
      KEY `idx_erp_production_finish_quality_status` (`tenant_id`, `status`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT=''lightweight production finish quality'''
  )
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_finish_quality'
        AND COLUMN_NAME = 'source_order_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_finish_quality`
      ADD COLUMN `source_order_id` bigint DEFAULT NULL COMMENT ''source sale order id'' AFTER `production_order_no`'
  )
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_finish_quality'
        AND COLUMN_NAME = 'source_item_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_finish_quality`
      ADD COLUMN `source_item_id` bigint DEFAULT NULL COMMENT ''source sale order item id'' AFTER `source_order_id`'
  )
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_finish_quality'
        AND COLUMN_NAME = 'qualified_qty'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_finish_quality`
      ADD COLUMN `qualified_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT ''qualified qty'' AFTER `report_qty`'
  )
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_finish_quality'
        AND COLUMN_NAME = 'deleted'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_finish_quality`
      ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b''0'' COMMENT ''deleted'' AFTER `update_time`'
  )
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_finish_quality'
        AND COLUMN_NAME = 'tenant_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_finish_quality`
      ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT ''tenant id'' AFTER `deleted`'
  )
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_finish_quality'
        AND INDEX_NAME = 'uk_erp_production_finish_quality_no'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_finish_quality`
      ADD UNIQUE KEY `uk_erp_production_finish_quality_no` (`tenant_id`, `no`)'
  )
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_finish_quality'
        AND INDEX_NAME = 'uk_erp_production_finish_quality_order'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_finish_quality`
      ADD UNIQUE KEY `uk_erp_production_finish_quality_order` (`tenant_id`, `production_order_id`)'
  )
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_finish_quality'
        AND INDEX_NAME = 'idx_erp_production_finish_quality_source_order'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_finish_quality`
      ADD KEY `idx_erp_production_finish_quality_source_order` (`tenant_id`, `source_order_id`)'
  )
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_finish_quality'
        AND INDEX_NAME = 'idx_erp_production_finish_quality_status'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_finish_quality`
      ADD KEY `idx_erp_production_finish_quality_status` (`tenant_id`, `status`)'
  )
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;
