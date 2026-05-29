-- 99-erp-finance-dual-write-config.sql

-- 双写配置表
CREATE TABLE IF NOT EXISTS `erp_finance_dual_write_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `ledger_id` BIGINT NOT NULL COMMENT '账簿编号',
    `enable_status` INT NOT NULL DEFAULT 0 COMMENT '启用状态（1-启用，0-禁用）',
    `write_mode` INT NOT NULL DEFAULT 1 COMMENT '双写模式（1-同步，2-异步）',
    `exception_strategy` INT NOT NULL DEFAULT 1 COMMENT '异常处理策略（1-记录日志，2-抛出异常，3-自动重试）',
    `max_retry_count` INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_finance_dual_write_config_ledger` (`tenant_id`, `ledger_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 财务双写配置';
