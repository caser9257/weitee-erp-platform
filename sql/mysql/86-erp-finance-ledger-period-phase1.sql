-- =====================================================
-- ERP finance ledger + accounting period phase1
-- 1. add finance ledger master table
-- 2. add accounting period table
-- 3. keep voucher / general ledger / report engine for next phase
-- =====================================================

CREATE TABLE IF NOT EXISTS `erp_finance_ledger` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `no` VARCHAR(64) NOT NULL COMMENT '账簿编码',
    `name` VARCHAR(128) NOT NULL COMMENT '账簿名称',
    `status` INT NOT NULL DEFAULT 0 COMMENT '启用状态',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `default_status` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否默认账簿',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_finance_ledger_no` (`tenant_id`, `no`, `deleted`),
    KEY `idx_finance_ledger_status_sort` (`tenant_id`, `status`, `default_status`, `deleted`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 财务账簿';

CREATE TABLE IF NOT EXISTS `erp_finance_period` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `ledger_id` BIGINT NOT NULL COMMENT '账簿编号',
    `period_code` VARCHAR(16) NOT NULL COMMENT '期间编码',
    `period_year` INT NOT NULL COMMENT '会计年度',
    `period_month` INT NOT NULL COMMENT '会计月份',
    `period_sort` INT NOT NULL COMMENT '期间排序值',
    `start_date` DATE NOT NULL COMMENT '开始日期',
    `end_date` DATE NOT NULL COMMENT '结束日期',
    `status` INT NOT NULL DEFAULT 10 COMMENT '期间状态',
    `close_time` DATETIME NULL COMMENT '关账时间',
    `close_user_id` BIGINT NULL COMMENT '关账人编号',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_finance_period_ledger_sort` (`tenant_id`, `ledger_id`, `period_sort`, `deleted`),
    KEY `idx_finance_period_ledger_status` (`tenant_id`, `ledger_id`, `status`, `deleted`, `period_sort`),
    KEY `idx_finance_period_date_range` (`tenant_id`, `ledger_id`, `status`, `deleted`, `start_date`, `end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 会计期间';
