-- =====================================================
-- ERP finance dual ledger diff config compatibility patch
-- 1. backfill calculation columns for old environments
-- 2. keep existing data readable by current dual-ledger result code
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @schema_name := DATABASE();

SET @missing_calculation_type := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'erp_finance_dual_ledger_diff_config'
      AND COLUMN_NAME = 'calculation_type'
);

SET @sql := IF(
    @missing_calculation_type = 0,
    'ALTER TABLE `erp_finance_dual_ledger_diff_config` ADD COLUMN `calculation_type` INT NOT NULL DEFAULT 3 COMMENT ''计算类型'' AFTER `internal_source_value`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @missing_ratio := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'erp_finance_dual_ledger_diff_config'
      AND COLUMN_NAME = 'ratio'
);

SET @sql := IF(
    @missing_ratio = 0,
    'ALTER TABLE `erp_finance_dual_ledger_diff_config` ADD COLUMN `ratio` DECIMAL(10,4) NULL COMMENT ''比例系数'' AFTER `calculation_type`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @missing_fixed_amount := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'erp_finance_dual_ledger_diff_config'
      AND COLUMN_NAME = 'fixed_amount'
);

SET @sql := IF(
    @missing_fixed_amount = 0,
    'ALTER TABLE `erp_finance_dual_ledger_diff_config` ADD COLUMN `fixed_amount` DECIMAL(18,2) NULL COMMENT ''固定差额'' AFTER `ratio`',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `erp_finance_dual_ledger_diff_config`
SET `calculation_type` = 3
WHERE `calculation_type` IS NULL;

SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @schema_name
  AND TABLE_NAME = 'erp_finance_dual_ledger_diff_config'
  AND COLUMN_NAME IN ('calculation_type', 'ratio', 'fixed_amount')
ORDER BY ORDINAL_POSITION;

SET FOREIGN_KEY_CHECKS = 1;
