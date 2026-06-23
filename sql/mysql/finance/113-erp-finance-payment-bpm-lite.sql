/*
 Target: ERP finance payment lightweight BPM integration
 Schema: ruoyi-vue-pro
 Date: 2026-05-22
 Note: Compatible with MySQL versions that do not support ALTER TABLE ... ADD COLUMN IF NOT EXISTS
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @add_process_instance_id = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_finance_payment'
        AND COLUMN_NAME = 'process_instance_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_finance_payment` ADD COLUMN `process_instance_id` varchar(64) DEFAULT NULL COMMENT ''BPM流程实例编号'' AFTER `status`'
  )
);
PREPARE stmt FROM @add_process_instance_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_idx_process_instance_id = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_finance_payment'
        AND INDEX_NAME = 'idx_erp_finance_payment_process_instance_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_finance_payment` ADD INDEX `idx_erp_finance_payment_process_instance_id` (`process_instance_id`)'
  )
);
PREPARE stmt FROM @add_idx_process_instance_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;
