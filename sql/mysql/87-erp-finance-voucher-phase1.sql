-- =====================================================
-- ERP finance voucher phase1
-- 1. add voucher template header table
-- 2. add voucher template item table
-- 3. add finance voucher header / entry table
-- =====================================================

CREATE TABLE IF NOT EXISTS `erp_finance_voucher_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `ledger_id` BIGINT NOT NULL COMMENT '账簿编号',
    `biz_type` INT NOT NULL COMMENT '业务类型',
    `name` VARCHAR(128) NOT NULL COMMENT '模板名称',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态',
    `auto_generate` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否自动生成',
    `default_summary` VARCHAR(255) NULL COMMENT '默认摘要',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_finance_voucher_template_ledger_biz` (`tenant_id`, `ledger_id`, `biz_type`, `deleted`),
    KEY `idx_finance_voucher_template_page` (`tenant_id`, `ledger_id`, `biz_type`, `status`, `deleted`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 财务凭证模板';

CREATE TABLE IF NOT EXISTS `erp_finance_voucher_template_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `template_id` BIGINT NOT NULL COMMENT '模板编号',
    `entry_no` INT NOT NULL COMMENT '分录顺序',
    `entry_direction` INT NOT NULL COMMENT '分录方向',
    `subject_code` VARCHAR(64) NOT NULL COMMENT '科目编码',
    `subject_name` VARCHAR(128) NOT NULL COMMENT '科目名称',
    `amount_source` INT NOT NULL COMMENT '金额来源',
    `summary` VARCHAR(255) NULL COMMENT '分录摘要',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_finance_voucher_template_item_template` (`tenant_id`, `template_id`, `deleted`, `entry_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 财务凭证模板分录';

CREATE TABLE IF NOT EXISTS `erp_finance_voucher` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `voucher_no` VARCHAR(64) NOT NULL COMMENT '凭证号',
    `ledger_id` BIGINT NOT NULL COMMENT '账簿编号',
    `period_id` BIGINT NOT NULL COMMENT '期间编号',
    `template_id` BIGINT NOT NULL COMMENT '模板编号',
    `biz_type` INT NOT NULL COMMENT '业务类型',
    `biz_id` BIGINT NOT NULL COMMENT '业务单据编号',
    `biz_no` VARCHAR(64) NOT NULL COMMENT '业务单号',
    `voucher_time` DATETIME NOT NULL COMMENT '凭证时间',
    `status` INT NOT NULL DEFAULT 10 COMMENT '凭证状态',
    `total_debit_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '借方合计',
    `total_credit_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '贷方合计',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_finance_voucher_no` (`tenant_id`, `voucher_no`, `deleted`),
    UNIQUE KEY `uk_finance_voucher_ledger_biz` (`tenant_id`, `ledger_id`, `biz_type`, `biz_id`, `deleted`),
    KEY `idx_finance_voucher_page` (`tenant_id`, `ledger_id`, `period_id`, `biz_type`, `status`, `deleted`, `voucher_time`),
    KEY `idx_finance_voucher_biz_no` (`tenant_id`, `biz_no`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 财务凭证';

CREATE TABLE IF NOT EXISTS `erp_finance_voucher_entry` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `voucher_id` BIGINT NOT NULL COMMENT '凭证编号',
    `entry_no` INT NOT NULL COMMENT '分录顺序',
    `summary` VARCHAR(255) NULL COMMENT '摘要',
    `subject_code` VARCHAR(64) NOT NULL COMMENT '科目编码',
    `subject_name` VARCHAR(128) NOT NULL COMMENT '科目名称',
    `debit_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '借方金额',
    `credit_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '贷方金额',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_finance_voucher_entry_voucher` (`tenant_id`, `voucher_id`, `deleted`, `entry_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 财务凭证分录';
