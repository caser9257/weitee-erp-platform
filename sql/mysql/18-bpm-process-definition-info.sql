/*
 Target: BPM process definition info compatibility
 Schema: ruoyi-vue-pro
 Date: 2026-04-07
 Note: Compatible with MySQL versions that do not support ALTER TABLE ... ADD COLUMN IF NOT EXISTS
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @add_start_dept_ids = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'bpm_process_definition_info'
        AND COLUMN_NAME = 'start_dept_ids'
    ),
    'SELECT 1',
    'ALTER TABLE `bpm_process_definition_info` ADD COLUMN `start_dept_ids` varchar(256) DEFAULT NULL COMMENT ''鍙彂璧烽儴闂ㄧ紪鍙锋暟缁?''
     AFTER `start_user_ids`'
  )
);
PREPARE stmt FROM @add_start_dept_ids;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_allow_withdraw_task = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'bpm_process_definition_info'
        AND COLUMN_NAME = 'allow_withdraw_task'
    ),
    'SELECT 1',
    'ALTER TABLE `bpm_process_definition_info` ADD COLUMN `allow_withdraw_task` bit(1) DEFAULT NULL COMMENT ''鏄惁鍏佽瀹℃壒浜烘挙鍥炰换鍔?''
     AFTER `allow_cancel_running_process`'
  )
);
PREPARE stmt FROM @add_allow_withdraw_task;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_print_template_setting = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'bpm_process_definition_info'
        AND COLUMN_NAME = 'print_template_setting'
    ),
    'SELECT 1',
    'ALTER TABLE `bpm_process_definition_info` ADD COLUMN `print_template_setting` varchar(2048) DEFAULT NULL COMMENT ''鑷畾涔夋墦鍗版ā鏉胯缃?''
     AFTER `task_after_trigger_setting`'
  )
);
PREPARE stmt FROM @add_print_template_setting;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;
