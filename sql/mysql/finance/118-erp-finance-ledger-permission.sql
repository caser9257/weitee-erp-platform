-- =====================================================
-- ERP finance ledger role permission
-- 1. add ledger-role association table
-- 2. add audit operation log table
-- =====================================================

-- 账簿-角色关联表
CREATE TABLE IF NOT EXISTS `erp_finance_ledger_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `ledger_id` BIGINT NOT NULL COMMENT '账簿编号',
    `role_id` BIGINT NOT NULL COMMENT '角色编号',
    `status` INT NOT NULL DEFAULT 0 COMMENT '启用状态',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ledger_role` (`ledger_id`, `role_id`, `deleted`),
    KEY `idx_ledger_role_ledger` (`ledger_id`, `deleted`),
    KEY `idx_ledger_role_role` (`role_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 账簿角色关联';

-- 审计操作日志表
CREATE TABLE IF NOT EXISTS `erp_finance_audit_operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `user_id` BIGINT NOT NULL COMMENT '操作人ID',
    `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型（query/export/trace）',
    `operation_desc` VARCHAR(255) NOT NULL COMMENT '操作描述',
    `target_type` VARCHAR(50) NULL COMMENT '操作对象类型（voucher/ledger/balance）',
    `target_id` BIGINT NULL COMMENT '操作对象ID',
    `ledger_id` BIGINT NULL COMMENT '账簿ID',
    `query_params` TEXT NULL COMMENT '查询参数（JSON）',
    `result_count` INT NULL COMMENT '结果数量',
    `ip` VARCHAR(50) NULL COMMENT '操作IP',
    `user_agent` VARCHAR(255) NULL COMMENT '用户代理',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_audit_log_user` (`user_id`, `deleted`),
    KEY `idx_audit_log_type` (`operation_type`, `deleted`),
    KEY `idx_audit_log_time` (`create_time`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 审计操作日志';

-- 双账套金额差异计算日志表
CREATE TABLE IF NOT EXISTS `erp_finance_dual_ledger_amount_diff_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `source_voucher_id` BIGINT NOT NULL COMMENT '源凭证编号',
    `target_voucher_id` BIGINT NOT NULL COMMENT '目标凭证编号',
    `diff_item_type` INT NOT NULL COMMENT '差异项类型',
    `calculation_type` INT NOT NULL COMMENT '计算类型',
    `internal_amount` DECIMAL(18,2) NOT NULL COMMENT '内部账金额',
    `external_amount` DECIMAL(18,2) NOT NULL COMMENT '外部账金额',
    `diff_amount` DECIMAL(18,2) NOT NULL COMMENT '差异金额',
    `ratio` DECIMAL(10,4) NULL COMMENT '比例系数',
    `fixed_amount` DECIMAL(18,2) NULL COMMENT '固定差额',
    `biz_type` INT NOT NULL COMMENT '业务类型',
    `biz_id` BIGINT NOT NULL COMMENT '业务单据编号',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_amount_diff_log_source` (`source_voucher_id`, `deleted`),
    KEY `idx_amount_diff_log_biz` (`biz_type`, `biz_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 双账套金额差异计算日志';
