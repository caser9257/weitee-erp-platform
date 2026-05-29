-- =====================================================
-- ERP finance prepayment phase1
-- =====================================================

CREATE TABLE `erp_finance_prepayment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `no` VARCHAR(64) NOT NULL COMMENT 'prepayment no',
    `status` INT NOT NULL DEFAULT 10 COMMENT 'prepayment status',
    `prepayment_time` DATETIME NOT NULL COMMENT 'prepayment time',
    `finance_user_id` BIGINT NULL COMMENT 'finance user id',
    `supplier_id` BIGINT NOT NULL COMMENT 'supplier id',
    `account_id` BIGINT NOT NULL COMMENT 'account id',
    `prepayment_price` DECIMAL(24, 6) NOT NULL COMMENT 'prepayment price',
    `allocated_price` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'allocated price',
    `remain_price` DECIMAL(24, 6) NOT NULL COMMENT 'remain price',
    `remark` VARCHAR(255) NULL COMMENT 'remark',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'creator',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'updater',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'tenant id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_finance_prepayment_no` (`tenant_id`, `no`, `deleted`),
    KEY `idx_finance_prepayment_supplier_status` (`tenant_id`, `supplier_id`, `status`, `deleted`),
    KEY `idx_finance_prepayment_time` (`tenant_id`, `prepayment_time`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP finance prepayment';

CREATE TABLE `erp_finance_prepayment_allocate` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `prepayment_id` BIGINT NOT NULL COMMENT 'prepayment id',
    `ap_statement_id` BIGINT NOT NULL COMMENT 'ap statement id',
    `allocate_amount` DECIMAL(24, 6) NOT NULL COMMENT 'allocate amount',
    `supplier_id` BIGINT NOT NULL COMMENT 'supplier id',
    `biz_type` INT NOT NULL COMMENT 'biz type',
    `biz_id` BIGINT NOT NULL COMMENT 'biz id',
    `biz_no` VARCHAR(64) NOT NULL COMMENT 'biz no',
    `status` INT NOT NULL DEFAULT 10 COMMENT 'allocate status',
    `remark` VARCHAR(255) NULL COMMENT 'remark',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'creator',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'updater',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'tenant id',
    PRIMARY KEY (`id`),
    KEY `idx_finance_prepayment_allocate_prepayment_status` (`tenant_id`, `prepayment_id`, `status`, `deleted`),
    KEY `idx_finance_prepayment_allocate_statement_status` (`tenant_id`, `ap_statement_id`, `status`, `deleted`),
    KEY `idx_finance_prepayment_allocate_biz` (`tenant_id`, `biz_type`, `biz_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP finance prepayment allocate';

