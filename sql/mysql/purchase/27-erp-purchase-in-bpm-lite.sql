/*
 Target: ERP purchase-in lightweight BPM integration
 Schema: ruoyi-vue-pro
 Date: 2026-04-09
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
        AND TABLE_NAME = 'erp_purchase_in'
        AND COLUMN_NAME = 'process_instance_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_in` ADD COLUMN `process_instance_id` varchar(64) DEFAULT NULL COMMENT ''BPM流程实例编号'' AFTER `status`'
  )
);
PREPARE stmt FROM @add_process_instance_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_last_reject_reason = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_purchase_in'
        AND COLUMN_NAME = 'last_reject_reason'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_in` ADD COLUMN `last_reject_reason` varchar(255) DEFAULT NULL COMMENT ''最近一次驳回原因'' AFTER `remark`'
  )
);
PREPARE stmt FROM @add_last_reject_reason;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_last_reject_time = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_purchase_in'
        AND COLUMN_NAME = 'last_reject_time'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_in` ADD COLUMN `last_reject_time` datetime DEFAULT NULL COMMENT ''最近一次驳回时间'' AFTER `last_reject_reason`'
  )
);
PREPARE stmt FROM @add_last_reject_time;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_last_reject_user_id = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_purchase_in'
        AND COLUMN_NAME = 'last_reject_user_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_in` ADD COLUMN `last_reject_user_id` bigint DEFAULT NULL COMMENT ''最近一次驳回人'' AFTER `last_reject_time`'
  )
);
PREPARE stmt FROM @add_last_reject_user_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_idx_process_instance_id = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_purchase_in'
        AND INDEX_NAME = 'idx_erp_purchase_in_process_instance_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_in` ADD INDEX `idx_erp_purchase_in_process_instance_id` (`process_instance_id`)'
  )
);
PREPARE stmt FROM @add_idx_process_instance_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;
