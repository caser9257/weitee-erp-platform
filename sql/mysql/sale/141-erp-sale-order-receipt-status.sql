/*
 Target: ERP sale order receipt status fields (missing migration)
 Schema: ruoyi-vue-pro
 Date: 2026-06-30
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


-- receipt_status: 收款状态（0=未收款 1=部分收款 2=全额收款）
SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_sale_order'
    AND COLUMN_NAME = 'receipt_status'
);

SET @col_sql := IF(
  @col_exists = 0,
  'ALTER TABLE `erp_sale_order` ADD COLUMN `receipt_status` tinyint NULL DEFAULT 0 COMMENT ''收款状态（0=未收款 1=部分收款 2=全额收款）'' AFTER `acceptance_status`',
  'SELECT 1'
);

PREPARE stmt FROM @col_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- receipt_price: 已收款金额，单位：元
SET @col2_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_sale_order'
    AND COLUMN_NAME = 'receipt_price'
);

SET @col2_sql := IF(
  @col2_exists = 0,
  'ALTER TABLE `erp_sale_order` ADD COLUMN `receipt_price` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT ''已收款金额，单位：元'' AFTER `receipt_status`',
  'SELECT 1'
);

PREPARE stmt2 FROM @col2_sql;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

SET FOREIGN_KEY_CHECKS = 1;
