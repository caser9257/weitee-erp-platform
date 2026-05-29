/*
 Target: BPM notification policy compatibility
 Schema: ruoyi-vue-pro
 Date: 2026-04-20
 Note: Compatible with MySQL versions that do not support ALTER TABLE ... ADD COLUMN IF NOT EXISTS
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @add_notification_policy_setting = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'bpm_process_definition_info'
        AND COLUMN_NAME = 'notification_policy_setting'
    ),
    'SELECT 1',
    'ALTER TABLE `bpm_process_definition_info` ADD COLUMN `notification_policy_setting` mediumtext DEFAULT NULL COMMENT ''通知策略设置''
     AFTER `task_after_trigger_setting`'
  )
);
PREPARE stmt FROM @add_notification_policy_setting;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;
