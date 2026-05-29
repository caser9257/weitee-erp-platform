-- =====================================================
-- ERP finance dual ledger diff config phase1
-- 1. add dual-ledger diff config table
-- 2. define external/internal difference source by biz type and diff item
-- =====================================================

CREATE TABLE IF NOT EXISTS `erp_finance_dual_ledger_diff_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `biz_type` INT NOT NULL COMMENT '业务类型',
    `diff_item_type` INT NOT NULL COMMENT '差异项类型',
    `external_source_type` INT NOT NULL COMMENT '对外账来源类型',
    `external_source_value` INT NULL COMMENT '对外账来源值',
    `internal_source_type` INT NOT NULL COMMENT '内部账来源类型',
    `internal_source_value` INT NULL COMMENT '内部账来源值',
    `calculation_type` INT NOT NULL DEFAULT 3 COMMENT '计算类型',
    `ratio` DECIMAL(10,4) NULL COMMENT '比例系数',
    `fixed_amount` DECIMAL(18,2) NULL COMMENT '固定差额',
    `status` INT NOT NULL DEFAULT 0 COMMENT '启用状态',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dual_ledger_diff_biz_item` (`tenant_id`, `biz_type`, `diff_item_type`, `deleted`),
    KEY `idx_dual_ledger_diff_status` (`tenant_id`, `status`, `deleted`),
    KEY `idx_dual_ledger_diff_external` (`tenant_id`, `external_source_type`, `deleted`),
    KEY `idx_dual_ledger_diff_internal` (`tenant_id`, `internal_source_type`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 双账套差异项口径配置';
