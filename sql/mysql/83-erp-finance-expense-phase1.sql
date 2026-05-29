-- =====================================================
-- ERP finance expense phase1
-- =====================================================

CREATE TABLE `erp_finance_expense` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `no` VARCHAR(64) NOT NULL COMMENT 'expense no',
    `status` INT NOT NULL DEFAULT 10 COMMENT 'expense status',
    `expense_time` DATETIME NOT NULL COMMENT 'expense time',
    `expense_type` INT NOT NULL COMMENT 'expense type',
    `dept_id` BIGINT NOT NULL COMMENT 'dept id',
    `project_id` BIGINT NULL COMMENT 'project id',
    `finance_user_id` BIGINT NULL COMMENT 'finance user id',
    `account_id` BIGINT NOT NULL COMMENT 'account id',
    `expense_price` DECIMAL(24, 6) NOT NULL COMMENT 'expense price',
    `paid_price` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'paid price',
    `remain_price` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'remain price',
    `remark` VARCHAR(255) NULL COMMENT 'remark',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'creator',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'updater',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'tenant id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_finance_expense_no` (`tenant_id`, `no`, `deleted`),
    KEY `idx_finance_expense_time` (`tenant_id`, `expense_time`, `deleted`),
    KEY `idx_finance_expense_dept_status` (`tenant_id`, `dept_id`, `status`, `deleted`),
    KEY `idx_finance_expense_project_status` (`tenant_id`, `project_id`, `status`, `deleted`),
    KEY `idx_finance_expense_type_status` (`tenant_id`, `expense_type`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP finance expense';
