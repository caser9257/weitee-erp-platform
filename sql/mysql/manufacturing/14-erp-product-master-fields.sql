/* 产品主档补充字段：与产品主档列表和编辑表单保持一致 */

SET NAMES utf8mb4;

SET @add_product_packaging = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'packaging'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `packaging` varchar(255) DEFAULT NULL COMMENT ''产品封装'' AFTER `standard`'
);
PREPARE stmt FROM @add_product_packaging;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_product_quality_grade = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'quality_grade'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `quality_grade` varchar(255) DEFAULT NULL COMMENT ''质量等级'' AFTER `packaging`'
);
PREPARE stmt FROM @add_product_quality_grade;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_product_brand_manufacturer = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'brand_manufacturer'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `brand_manufacturer` varchar(255) DEFAULT NULL COMMENT ''品牌/制造商'' AFTER `quality_grade`'
);
PREPARE stmt FROM @add_product_brand_manufacturer;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_product_alternative_model = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'alternative_model'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `alternative_model` varchar(255) DEFAULT NULL COMMENT ''替代型号'' AFTER `brand_manufacturer`'
);
PREPARE stmt FROM @add_product_alternative_model;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
