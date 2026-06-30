/*
 Target: ERP purchase suggest warehouse id compatibility
 Schema: ruoyi-vue-pro
 Date: 2026-06-30
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @warehouse_id_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_purchase_suggest'
    AND COLUMN_NAME = 'warehouse_id'
);

SET @warehouse_id_sql := IF(
  @warehouse_id_exists = 0,
  'ALTER TABLE `erp_purchase_suggest` ADD COLUMN `warehouse_id` bigint NULL DEFAULT NULL COMMENT ''仓库编号'' AFTER `material_id`',
  'SELECT 1'
);

PREPARE warehouse_id_stmt FROM @warehouse_id_sql;
EXECUTE warehouse_id_stmt;
DEALLOCATE PREPARE warehouse_id_stmt;

SET FOREIGN_KEY_CHECKS = 1;
