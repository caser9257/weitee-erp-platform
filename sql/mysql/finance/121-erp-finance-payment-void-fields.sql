/*
 Target: ERP finance payment void fields
 Schema: ruoyi-vue-pro
 Date: 2026-05-25
 Note: Compatible with MySQL versions that do not support ALTER TABLE ... ADD COLUMN IF NOT EXISTS
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


SET @add_void_reason = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_finance_payment'
        AND COLUMN_NAME = 'void_reason'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_finance_payment` ADD COLUMN `void_reason` varchar(255) DEFAULT NULL COMMENT ''作废原因'' AFTER `remark`'
  )
);
PREPARE stmt FROM @add_void_reason;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_void_time = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_finance_payment'
        AND COLUMN_NAME = 'void_time'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_finance_payment` ADD COLUMN `void_time` datetime DEFAULT NULL COMMENT ''作废时间'' AFTER `void_reason`'
  )
);
PREPARE stmt FROM @add_void_time;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_void_by = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_finance_payment'
        AND COLUMN_NAME = 'void_by'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_finance_payment` ADD COLUMN `void_by` bigint DEFAULT NULL COMMENT ''作废操作人'' AFTER `void_time`'
  )
);
PREPARE stmt FROM @add_void_by;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;
