/*
 Target: ERP project lifecycle fields compatibility
 Schema: ruoyi-vue-pro
 Date: 2026-06-09
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @lifecycle_stage_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'lifecycle_stage'
);

SET @lifecycle_stage_sql := IF(
  @lifecycle_stage_exists = 0,
  'ALTER TABLE `erp_project` ADD COLUMN `lifecycle_stage` varchar(32) NULL DEFAULT NULL COMMENT ''生命周期阶段'' AFTER `remark`',
  'SELECT 1'
);

PREPARE lifecycle_stage_stmt FROM @lifecycle_stage_sql;
EXECUTE lifecycle_stage_stmt;
DEALLOCATE PREPARE lifecycle_stage_stmt;

SET @current_blocker_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'current_blocker'
);

SET @current_blocker_sql := IF(
  @current_blocker_exists = 0,
  'ALTER TABLE `erp_project` ADD COLUMN `current_blocker` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT ''当前阻塞项'' AFTER `lifecycle_stage`',
  'SELECT 1'
);

PREPARE current_blocker_stmt FROM @current_blocker_sql;
EXECUTE current_blocker_stmt;
DEALLOCATE PREPARE current_blocker_stmt;

SET @current_pending_role_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'current_pending_role'
);

SET @current_pending_role_sql := IF(
  @current_pending_role_exists = 0,
  'ALTER TABLE `erp_project` ADD COLUMN `current_pending_role` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT ''当前待处理角色'' AFTER `current_blocker`',
  'SELECT 1'
);

PREPARE current_pending_role_stmt FROM @current_pending_role_sql;
EXECUTE current_pending_role_stmt;
DEALLOCATE PREPARE current_pending_role_stmt;

SET @contract_exec_status_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'contract_exec_status'
);

SET @contract_exec_status_sql := IF(
  @contract_exec_status_exists = 0,
  'ALTER TABLE `erp_project` ADD COLUMN `contract_exec_status` varchar(32) NULL DEFAULT NULL COMMENT ''合同执行状态'' AFTER `current_pending_role`',
  'SELECT 1'
);

PREPARE contract_exec_status_stmt FROM @contract_exec_status_sql;
EXECUTE contract_exec_status_stmt;
DEALLOCATE PREPARE contract_exec_status_stmt;

SET @receipt_exec_status_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'receipt_exec_status'
);

SET @receipt_exec_status_sql := IF(
  @receipt_exec_status_exists = 0,
  'ALTER TABLE `erp_project` ADD COLUMN `receipt_exec_status` varchar(32) NULL DEFAULT NULL COMMENT ''回款执行状态'' AFTER `contract_exec_status`',
  'SELECT 1'
);

PREPARE receipt_exec_status_stmt FROM @receipt_exec_status_sql;
EXECUTE receipt_exec_status_stmt;
DEALLOCATE PREPARE receipt_exec_status_stmt;

SET @shipment_exec_status_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'shipment_exec_status'
);

SET @shipment_exec_status_sql := IF(
  @shipment_exec_status_exists = 0,
  'ALTER TABLE `erp_project` ADD COLUMN `shipment_exec_status` varchar(32) NULL DEFAULT NULL COMMENT ''发货执行状态'' AFTER `receipt_exec_status`',
  'SELECT 1'
);

PREPARE shipment_exec_status_stmt FROM @shipment_exec_status_sql;
EXECUTE shipment_exec_status_stmt;
DEALLOCATE PREPARE shipment_exec_status_stmt;

SET @invoice_exec_status_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'invoice_exec_status'
);

SET @invoice_exec_status_sql := IF(
  @invoice_exec_status_exists = 0,
  'ALTER TABLE `erp_project` ADD COLUMN `invoice_exec_status` varchar(32) NULL DEFAULT NULL COMMENT ''开票执行状态'' AFTER `shipment_exec_status`',
  'SELECT 1'
);

PREPARE invoice_exec_status_stmt FROM @invoice_exec_status_sql;
EXECUTE invoice_exec_status_stmt;
DEALLOCATE PREPARE invoice_exec_status_stmt;

SET FOREIGN_KEY_CHECKS = 1;
