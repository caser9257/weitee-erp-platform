-- 100-erp-finance-dual-write.sql

CREATE TABLE IF NOT EXISTS `erp_finance_dual_write_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `source_voucher_id` BIGINT NOT NULL COMMENT '源凭证编号',
    `target_voucher_id` BIGINT NULL COMMENT '目标凭证编号',
    `source_ledger_id` BIGINT NOT NULL COMMENT '源账簿编号',
    `target_ledger_id` BIGINT NOT NULL COMMENT '目标账簿编号',
    `biz_type` INT NOT NULL COMMENT '业务类型',
    `biz_id` BIGINT NOT NULL COMMENT '业务单据编号',
    `status` INT NOT NULL DEFAULT 0 COMMENT '双写状态',
    `error_message` TEXT NULL COMMENT '错误信息',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    KEY `idx_dual_write_log_source` (`tenant_id`, `source_voucher_id`, `deleted`),
    KEY `idx_dual_write_log_biz` (`tenant_id`, `biz_type`, `biz_id`, `deleted`),
    KEY `idx_dual_write_log_status` (`tenant_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 双写日志';

CREATE TABLE IF NOT EXISTS `erp_finance_dual_write_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `biz_type` INT NOT NULL COMMENT '业务类型',
    `enabled` BIT(1) NOT NULL DEFAULT b'1' COMMENT '是否启用双写',
    `auto_retry` BIT(1) NOT NULL DEFAULT b'1' COMMENT '是否自动重试',
    `max_retry_count` INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dual_write_config_biz` (`tenant_id`, `biz_type`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 双写配置';