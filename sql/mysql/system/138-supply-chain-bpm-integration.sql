-- 138-supply-chain-bpm-integration.sql
-- 供应链模块 BPM 审批接入：给三张表加 process_instance_id 字段
-- 幂等写法：先检查列是否存在，再 ADD COLUMN

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;

-- 采购退货：仅当列不存在时添加
SET @col_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_purchase_return'
    AND COLUMN_NAME = 'process_instance_id'
);
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE erp_purchase_return ADD COLUMN process_instance_id VARCHAR(64) DEFAULT NULL COMMENT ''BPM 流程实例 ID''',
  'SELECT 1 AS skip'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 其它入库：仅当列不存在时添加
SET @col_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_stock_in'
    AND COLUMN_NAME = 'process_instance_id'
);
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE erp_stock_in ADD COLUMN process_instance_id VARCHAR(64) DEFAULT NULL COMMENT ''BPM 流程实例 ID''',
  'SELECT 1 AS skip'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 其它出库：仅当列不存在时添加
SET @col_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_stock_out'
    AND COLUMN_NAME = 'process_instance_id'
);
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE erp_stock_out ADD COLUMN process_instance_id VARCHAR(64) DEFAULT NULL COMMENT ''BPM 流程实例 ID''',
  'SELECT 1 AS skip'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;
