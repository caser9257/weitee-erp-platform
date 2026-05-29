-- =====================================================
-- ERP finance expense phase2
-- 1. add supplier_id to expense header
-- 2. add expense item table
-- 3. add supplier/status index for trace and payment queries
-- =====================================================

SET @expense_supplier_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_finance_expense'
      AND COLUMN_NAME = 'supplier_id'
);
SET @expense_supplier_column_sql := IF(
    @expense_supplier_column_exists > 0,
    'SELECT ''erp_finance_expense.supplier_id already exists''',
    'ALTER TABLE `erp_finance_expense` ADD COLUMN `supplier_id` BIGINT NULL COMMENT ''付款对象编号'' AFTER `project_id`'
);
PREPARE expense_supplier_column_stmt FROM @expense_supplier_column_sql;
EXECUTE expense_supplier_column_stmt;
DEALLOCATE PREPARE expense_supplier_column_stmt;

SET @expense_supplier_index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_finance_expense'
      AND INDEX_NAME = 'idx_finance_expense_supplier_status'
);
SET @expense_supplier_index_sql := IF(
    @expense_supplier_index_exists > 0,
    'SELECT ''idx_finance_expense_supplier_status already exists''',
    'ALTER TABLE `erp_finance_expense` ADD KEY `idx_finance_expense_supplier_status` (`tenant_id`, `supplier_id`, `status`, `deleted`)'
);
PREPARE expense_supplier_index_stmt FROM @expense_supplier_index_sql;
EXECUTE expense_supplier_index_stmt;
DEALLOCATE PREPARE expense_supplier_index_stmt;

CREATE TABLE IF NOT EXISTS `erp_finance_expense_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `expense_id` BIGINT NOT NULL COMMENT '费用单编号',
    `item_name` VARCHAR(128) NOT NULL COMMENT '费用内容',
    `amount` DECIMAL(24, 6) NOT NULL COMMENT '金额',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_finance_expense_item_expense` (`tenant_id`, `expense_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 费用报销明细';
