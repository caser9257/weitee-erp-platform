/*
  ERP v5 product schema compatibility migration.
  Scope: weitee-erp.erp_product only.
  The statements are idempotent and preserve existing product data.
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'material_code'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `material_code` varchar(64) DEFAULT NULL COMMENT ''material code'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'packaging'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `packaging` varchar(255) DEFAULT NULL COMMENT ''product packaging'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'quality_grade'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `quality_grade` varchar(255) DEFAULT NULL COMMENT ''quality grade'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'brand_manufacturer'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `brand_manufacturer` varchar(255) DEFAULT NULL COMMENT ''brand or manufacturer'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'alternative_model'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `alternative_model` varchar(255) DEFAULT NULL COMMENT ''alternative model'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'expiry_day'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `expiry_day` int DEFAULT NULL COMMENT ''expiry days'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'batch_control_flag'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `batch_control_flag` bit(1) NOT NULL DEFAULT b''0'' COMMENT ''batch control flag'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'inspection_required_flag'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `inspection_required_flag` bit(1) NOT NULL DEFAULT b''0'' COMMENT ''inspection required flag'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'weight'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `weight` decimal(24,6) DEFAULT NULL COMMENT ''weight'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'purchase_price'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `purchase_price` decimal(24,6) DEFAULT NULL COMMENT ''purchase price'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'sale_price'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `sale_price` decimal(24,6) DEFAULT NULL COMMENT ''sale price'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'min_price'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `min_price` decimal(24,6) DEFAULT NULL COMMENT ''minimum price'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'mrp_enable'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `mrp_enable` bit(1) NOT NULL DEFAULT b''1'' COMMENT ''MRP enabled'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'asset_flag'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `asset_flag` bit(1) NOT NULL DEFAULT b''0'' COMMENT ''asset candidate flag'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @audit_status_added := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'audit_status'),
  0, 1
);
SET @sql := IF(
  @audit_status_added = 1,
  'ALTER TABLE `erp_product` ADD COLUMN `audit_status` int NOT NULL DEFAULT 0 COMMENT ''audit status''',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'process_instance_id'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `process_instance_id` varchar(64) DEFAULT NULL COMMENT ''BPM process instance id'''
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND INDEX_NAME = 'idx_audit_status'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD INDEX `idx_audit_status` (`audit_status`)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS (SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND INDEX_NAME = 'idx_process_instance_id'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD INDEX `idx_process_instance_id` (`process_instance_id`)'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  @audit_status_added = 1,
  'UPDATE `erp_product` SET `audit_status` = CASE WHEN `status` = 1 THEN 20 ELSE 0 END',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;

