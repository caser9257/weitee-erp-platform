-- =====================================================
-- ERP production cost & issue voucher phase1
-- =====================================================

CREATE TABLE `erp_production_issue_voucher` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `voucher_no` VARCHAR(64) NOT NULL COMMENT 'voucher no',
    `issue_id` BIGINT NOT NULL COMMENT 'production issue id',
    `issue_no` VARCHAR(64) NOT NULL COMMENT 'production issue no',
    `production_order_id` BIGINT NOT NULL COMMENT 'production order id',
    `voucher_time` DATETIME NOT NULL COMMENT 'voucher time',
    `status` INT NOT NULL DEFAULT 10 COMMENT 'voucher status',
    `total_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'total amount',
    `remark` VARCHAR(255) NULL COMMENT 'remark',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'creator',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'updater',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_production_issue_voucher_no` (`voucher_no`, `deleted`),
    UNIQUE KEY `uk_production_issue_voucher_issue` (`issue_id`, `deleted`),
    KEY `idx_production_issue_voucher_order` (`production_order_id`, `deleted`),
    KEY `idx_production_issue_voucher_time` (`voucher_time`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP production issue voucher';

CREATE TABLE `erp_production_issue_voucher_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `voucher_id` BIGINT NOT NULL COMMENT 'voucher id',
    `issue_item_id` BIGINT NOT NULL COMMENT 'issue item id',
    `material_id` BIGINT NOT NULL COMMENT 'material id',
    `warehouse_id` BIGINT NULL COMMENT 'warehouse id',
    `issue_qty` DECIMAL(24, 6) NOT NULL COMMENT 'issue qty',
    `issue_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'issue amount',
    `remark` VARCHAR(255) NULL COMMENT 'remark',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'creator',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'updater',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
    PRIMARY KEY (`id`),
    KEY `idx_production_issue_voucher_item_voucher` (`voucher_id`, `deleted`),
    KEY `idx_production_issue_voucher_item_issue_item` (`issue_item_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP production issue voucher item';

CREATE TABLE `erp_production_cost_entry` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `production_order_id` BIGINT NOT NULL COMMENT 'production order id',
    `cost_type` INT NOT NULL COMMENT 'cost type',
    `source_type` INT NOT NULL DEFAULT 10 COMMENT 'source type',
    `accounting_month` VARCHAR(7) NOT NULL COMMENT 'accounting month',
    `amount` DECIMAL(24, 6) NOT NULL COMMENT 'amount',
    `remark` VARCHAR(255) NULL COMMENT 'remark',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'creator',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'updater',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
    PRIMARY KEY (`id`),
    KEY `idx_production_cost_entry_order` (`production_order_id`, `deleted`),
    KEY `idx_production_cost_entry_month_type` (`accounting_month`, `cost_type`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP production cost entry';
