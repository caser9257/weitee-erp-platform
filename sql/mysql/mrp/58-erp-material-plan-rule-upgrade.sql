/*
 * Target: ERP 计划参数升级
 * Schema: ruoyi-vue-pro
 * Date: 2026-04-20
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE `ruoyi-vue-pro`;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_material_plan_rule` ADD COLUMN `replenish_mode` varchar(32) NOT NULL DEFAULT ''LOT_FOR_LOT'' COMMENT ''补货策略'' AFTER `supply_type`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_material_plan_rule'
    AND COLUMN_NAME = 'replenish_mode'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_material_plan_rule` ADD COLUMN `fixed_order_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT ''固定批量'' AFTER `order_multiple`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_material_plan_rule'
    AND COLUMN_NAME = 'fixed_order_qty'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_material_plan_rule` ADD COLUMN `shortage_warn_flag` bit(1) NOT NULL DEFAULT b''1'' COMMENT ''缺料预警开关'' AFTER `enable_flag`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_material_plan_rule'
    AND COLUMN_NAME = 'shortage_warn_flag'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_material_plan_rule` ADD COLUMN `remark` varchar(255) DEFAULT NULL COMMENT ''备注'' AFTER `default_supplier_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_material_plan_rule'
    AND COLUMN_NAME = 'remark'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `erp_material_plan_rule`
SET `replenish_mode` = 'LOT_FOR_LOT'
WHERE `replenish_mode` IS NULL
   OR `replenish_mode` = '';

UPDATE `erp_material_plan_rule`
SET `fixed_order_qty` = 0.000000
WHERE `fixed_order_qty` IS NULL;

SET FOREIGN_KEY_CHECKS = 1;
