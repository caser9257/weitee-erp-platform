/*
 Target: ERP product mrp_enable column compatibility
 Schema: executed via mysql CLI default database
 Date: 2026-08-11
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

SET @product_mrp_enable_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_product'
    AND COLUMN_NAME = 'mrp_enable'
);

SET @product_mrp_enable_sql := IF(
  @product_mrp_enable_exists > 0,
  'SELECT ''erp_product.mrp_enable already exists''',
  'ALTER TABLE `erp_product` ADD COLUMN `mrp_enable` bit(1) NOT NULL DEFAULT b''1'' COMMENT ''是否参与 MRP'' AFTER `min_price`'
);

PREPARE product_mrp_enable_stmt FROM @product_mrp_enable_sql;
EXECUTE product_mrp_enable_stmt;
DEALLOCATE PREPARE product_mrp_enable_stmt;

SET FOREIGN_KEY_CHECKS = 1;