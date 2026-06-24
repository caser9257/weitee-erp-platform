-- =====================================================
-- ERP finance general ledger phase3
-- 1. add subject balance table
-- 2. support period opening/current/ending balance query
-- =====================================================

CREATE TABLE IF NOT EXISTS `erp_finance_subject_balance` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `ledger_id` BIGINT NOT NULL COMMENT '账簿编号',
    `period_id` BIGINT NOT NULL COMMENT '期间编号',
    `period_sort` INT NOT NULL COMMENT '期间排序值',
    `subject_code` VARCHAR(64) NOT NULL COMMENT '科目编码',
    `subject_name` VARCHAR(128) NOT NULL COMMENT '科目名称',
    `opening_debit_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '期初借方余额',
    `opening_credit_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '期初贷方余额',
    `current_debit_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '本期借方发生额',
    `current_credit_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '本期贷方发生额',
    `ending_debit_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '期末借方余额',
    `ending_credit_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '期末贷方余额',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_finance_subject_balance_period_subject` (`ledger_id`, `period_id`, `subject_code`, `deleted`),
    KEY `idx_finance_subject_balance_page` (`ledger_id`, `period_id`, `deleted`, `subject_code`),
    KEY `idx_finance_subject_balance_subject_period` (`ledger_id`, `subject_code`, `deleted`, `period_sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 财务科目期间余额';
