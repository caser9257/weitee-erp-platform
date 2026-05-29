/*
 Target: ERP sale delivery-ready compatibility
 Schema: ruoyi-vue-pro
 Date: 2026-04-15
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @delivery_ready_status_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_sale_order'
    AND COLUMN_NAME = 'delivery_ready_status'
);

SET @delivery_ready_status_sql := IF(
  @delivery_ready_status_exists = 0,
  'ALTER TABLE `erp_sale_order` ADD COLUMN `delivery_ready_status` varchar(32) NOT NULL DEFAULT ''NOT_READY'' COMMENT ''delivery ready status'' AFTER `return_count`',
  'SELECT 1'
);

PREPARE delivery_ready_status_stmt FROM @delivery_ready_status_sql;
EXECUTE delivery_ready_status_stmt;
DEALLOCATE PREPARE delivery_ready_status_stmt;

SET FOREIGN_KEY_CHECKS = 1;
