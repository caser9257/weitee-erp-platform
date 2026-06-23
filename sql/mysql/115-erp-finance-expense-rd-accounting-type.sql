-- =====================================================
-- ERP finance expense rd accounting type phase
-- 1. add rd_accounting_type to expense header
-- 2. backfill history research expenses with EXPENSE(10)
-- =====================================================

SET @expense_rd_accounting_type_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_finance_expense'
      AND COLUMN_NAME = 'rd_accounting_type'
);
SET @expense_rd_accounting_type_column_sql := IF(
    @expense_rd_accounting_type_column_exists > 0,
    'SELECT ''erp_finance_expense.rd_accounting_type already exists''',
    'ALTER TABLE `erp_finance_expense` ADD COLUMN `rd_accounting_type` INT NULL COMMENT ''研发支出口径：10-费用化，20-资本化'' AFTER `expense_type`'
);
PREPARE expense_rd_accounting_type_column_stmt FROM @expense_rd_accounting_type_column_sql;
EXECUTE expense_rd_accounting_type_column_stmt;
DEALLOCATE PREPARE expense_rd_accounting_type_column_stmt;

UPDATE `erp_finance_expense`
SET `rd_accounting_type` = 10
WHERE `expense_type` = 10
  AND `rd_accounting_type` IS NULL;

UPDATE `erp_finance_expense`
SET `rd_accounting_type` = NULL
WHERE `expense_type` <> 10
  AND `rd_accounting_type` IS NOT NULL;

