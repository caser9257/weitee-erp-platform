-- =====================================================
-- ERP finance dual ledger config phase1
-- 1. add dual-ledger config table
-- 2. define external/internal ledger mapping by biz type
-- =====================================================

CREATE TABLE IF NOT EXISTS `erp_finance_dual_ledger_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `biz_type` INT NOT NULL COMMENT '业务类型',
    `external_ledger_id` BIGINT NOT NULL COMMENT '对外账账簿编号',
    `internal_ledger_id` BIGINT NOT NULL COMMENT '内部账账簿编号',
    `status` INT NOT NULL DEFAULT 0 COMMENT '启用状态',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dual_ledger_config_biz_type` (`tenant_id`, `biz_type`, `deleted`),
    KEY `idx_dual_ledger_config_status` (`tenant_id`, `status`, `deleted`),
    KEY `idx_dual_ledger_config_external` (`tenant_id`, `external_ledger_id`, `deleted`),
    KEY `idx_dual_ledger_config_internal` (`tenant_id`, `internal_ledger_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 双账套账簿映射配置';
