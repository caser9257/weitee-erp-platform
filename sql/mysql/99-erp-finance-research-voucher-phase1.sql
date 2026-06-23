-- 99-erp-finance-research-voucher-phase1.sql
ALTER TABLE `erp_finance_expense` 
ADD COLUMN `research_category` INT NULL COMMENT '研发支出分类（费用化/资本化）' AFTER `expense_type`;

-- 财务账簿映射表
CREATE TABLE IF NOT EXISTS `erp_finance_ledger_mapping` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `external_ledger_id` BIGINT NOT NULL COMMENT '对外账簿编号',
    `internal_ledger_id` BIGINT NOT NULL COMMENT '内部账簿编号',
    `mapping_type` VARCHAR(32) NOT NULL COMMENT '映射类型',
    `mapping_rule` TEXT NULL COMMENT '映射规则',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_finance_ledger_mapping_external` (`tenant_id`, `external_ledger_id`, `deleted`),
    UNIQUE KEY `uk_finance_ledger_mapping_internal` (`tenant_id`, `internal_ledger_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 财务账簿映射';

ALTER TABLE `erp_finance_voucher_template` 
ADD COLUMN `research_category` INT NULL COMMENT '研发支出分类' AFTER `biz_type`,
ADD COLUMN `research_template` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否研发专项模板' AFTER `research_category`;

-- 凭证日志表
CREATE TABLE IF NOT EXISTS `erp_finance_voucher_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `voucher_id` BIGINT NOT NULL COMMENT '凭证编号',
    `operation_type` VARCHAR(32) NOT NULL COMMENT '操作类型',
    `operation_result` VARCHAR(32) NULL COMMENT '操作结果',
    `operation_detail` TEXT NULL COMMENT '操作详情',
    `operator` VARCHAR(64) NULL COMMENT '操作人',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_finance_voucher_log_voucher` (`tenant_id`, `voucher_id`, `deleted`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 财务凭证日志';
