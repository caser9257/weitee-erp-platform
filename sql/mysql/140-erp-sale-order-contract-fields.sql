/*
 Target: ERP sale order contract fields compatibility
 Schema: ruoyi-vue-pro
 Date: 2026-06-09
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @delivery_ready_status_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_sale_order'
    AND COLUMN_NAME = 'delivery_ready_status'
);

SET @delivery_ready_status_sql := IF(
  @delivery_ready_status_exists = 0,
  'ALTER TABLE `erp_sale_order` ADD COLUMN `delivery_ready_status` varchar(32) NOT NULL DEFAULT ''NOT_READY'' COMMENT ''发货准备状态'' AFTER `return_count`',
  'SELECT 1'
);

PREPARE delivery_ready_status_stmt FROM @delivery_ready_status_sql;
EXECUTE delivery_ready_status_stmt;
DEALLOCATE PREPARE delivery_ready_status_stmt;

SET @contract_id_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_sale_order'
    AND COLUMN_NAME = 'contract_id'
);

SET @contract_id_sql := IF(
  @contract_id_exists = 0,
  'ALTER TABLE `erp_sale_order` ADD COLUMN `contract_id` bigint NULL DEFAULT NULL COMMENT ''关联合同编号'' AFTER `delivery_ready_status`',
  'SELECT 1'
);

PREPARE contract_id_stmt FROM @contract_id_sql;
EXECUTE contract_id_stmt;
DEALLOCATE PREPARE contract_id_stmt;

SET @contract_no_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_sale_order'
    AND COLUMN_NAME = 'contract_no'
);

SET @contract_no_sql := IF(
  @contract_no_exists = 0,
  'ALTER TABLE `erp_sale_order` ADD COLUMN `contract_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT ''合同编号（冗余存储）'' AFTER `contract_id`',
  'SELECT 1'
);

PREPARE contract_no_stmt FROM @contract_no_sql;
EXECUTE contract_no_stmt;
DEALLOCATE PREPARE contract_no_stmt;

SET @shipment_release_status_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_sale_order'
    AND COLUMN_NAME = 'shipment_release_status'
);

SET @shipment_release_status_sql := IF(
  @shipment_release_status_exists = 0,
  'ALTER TABLE `erp_sale_order` ADD COLUMN `shipment_release_status` varchar(32) NULL DEFAULT NULL COMMENT ''发货放行状态'' AFTER `contract_no`',
  'SELECT 1'
);

PREPARE shipment_release_status_stmt FROM @shipment_release_status_sql;
EXECUTE shipment_release_status_stmt;
DEALLOCATE PREPARE shipment_release_status_stmt;

SET @shipment_release_reason_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_sale_order'
    AND COLUMN_NAME = 'shipment_release_reason'
);

SET @shipment_release_reason_sql := IF(
  @shipment_release_reason_exists = 0,
  'ALTER TABLE `erp_sale_order` ADD COLUMN `shipment_release_reason` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT ''放行阻塞原因'' AFTER `shipment_release_status`',
  'SELECT 1'
);

PREPARE shipment_release_reason_stmt FROM @shipment_release_reason_sql;
EXECUTE shipment_release_reason_stmt;
DEALLOCATE PREPARE shipment_release_reason_stmt;

SET @invoice_status_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_sale_order'
    AND COLUMN_NAME = 'invoice_status'
);

SET @invoice_status_sql := IF(
  @invoice_status_exists = 0,
  'ALTER TABLE `erp_sale_order` ADD COLUMN `invoice_status` varchar(32) NULL DEFAULT NULL COMMENT ''开票状态'' AFTER `shipment_release_reason`',
  'SELECT 1'
);

PREPARE invoice_status_stmt FROM @invoice_status_sql;
EXECUTE invoice_status_stmt;
DEALLOCATE PREPARE invoice_status_stmt;

SET @acceptance_status_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_sale_order'
    AND COLUMN_NAME = 'acceptance_status'
);

SET @acceptance_status_sql := IF(
  @acceptance_status_exists = 0,
  'ALTER TABLE `erp_sale_order` ADD COLUMN `acceptance_status` varchar(32) NULL DEFAULT NULL COMMENT ''验收状态'' AFTER `invoice_status`',
  'SELECT 1'
);

PREPARE acceptance_status_stmt FROM @acceptance_status_sql;
EXECUTE acceptance_status_stmt;
DEALLOCATE PREPARE acceptance_status_stmt;

SET FOREIGN_KEY_CHECKS = 1;
