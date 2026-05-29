-- =====================================================
-- ERP purchase finance phase1
-- =====================================================

CREATE TABLE `erp_ap_statement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `statement_no` VARCHAR(64) NOT NULL COMMENT 'statement no',
    `biz_type` INT NOT NULL COMMENT 'biz type',
    `biz_id` BIGINT NOT NULL COMMENT 'biz id',
    `biz_no` VARCHAR(64) NOT NULL COMMENT 'biz no',
    `source_order_id` BIGINT NULL COMMENT 'source order id',
    `source_order_no` VARCHAR(64) NULL COMMENT 'source order no',
    `supplier_id` BIGINT NOT NULL COMMENT 'supplier id',
    `account_id` BIGINT NULL COMMENT 'account id',
    `amount` DECIMAL(24, 6) NOT NULL COMMENT 'statement amount',
    `paid_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'paid amount',
    `remain_amount` DECIMAL(24, 6) NOT NULL COMMENT 'remain amount',
    `currency_code` VARCHAR(16) NOT NULL DEFAULT 'CNY' COMMENT 'currency code',
    `biz_date` DATETIME NOT NULL COMMENT 'biz date',
    `due_date` DATETIME NOT NULL COMMENT 'due date',
    `invoice_status` INT NOT NULL DEFAULT 0 COMMENT 'invoice status',
    `invoice_no` VARCHAR(64) NULL COMMENT 'invoice no',
    `invoice_amount` DECIMAL(24, 6) NULL COMMENT 'invoice amount',
    `status` INT NOT NULL DEFAULT 10 COMMENT 'statement status',
    `remark` VARCHAR(255) NULL COMMENT 'remark',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'creator',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'updater',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'tenant id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ap_statement_biz` (`tenant_id`, `biz_type`, `biz_id`, `deleted`),
    UNIQUE KEY `uk_ap_statement_no` (`tenant_id`, `statement_no`, `deleted`),
    KEY `idx_ap_statement_supplier_status` (`tenant_id`, `supplier_id`, `status`, `deleted`),
    KEY `idx_ap_statement_due_date` (`tenant_id`, `due_date`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP AP statement';

CREATE TABLE `erp_ap_statement_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `statement_id` BIGINT NOT NULL COMMENT 'statement id',
    `item_type` INT NOT NULL COMMENT 'item type',
    `ref_type` INT NULL COMMENT 'ref type',
    `ref_id` BIGINT NULL COMMENT 'ref id',
    `ref_no` VARCHAR(64) NULL COMMENT 'ref no',
    `amount` DECIMAL(24, 6) NOT NULL COMMENT 'change amount',
    `after_paid_amount` DECIMAL(24, 6) NOT NULL COMMENT 'after paid amount',
    `after_remain_amount` DECIMAL(24, 6) NOT NULL COMMENT 'after remain amount',
    `remark` VARCHAR(255) NULL COMMENT 'remark',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'creator',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'updater',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
    `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'tenant id',
    PRIMARY KEY (`id`),
    KEY `idx_ap_statement_item_statement_id` (`tenant_id`, `statement_id`, `deleted`),
    KEY `idx_ap_statement_item_ref` (`tenant_id`, `ref_type`, `ref_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP AP statement item';

CREATE TABLE `erp_finance_payment_allocate` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `payment_id` BIGINT NOT NULL COMMENT 'payment id',
    `payment_item_id` BIGINT NOT NULL COMMENT 'payment item id',
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
    UNIQUE KEY `uk_payment_allocate_item_statement` (`tenant_id`, `payment_item_id`, `ap_statement_id`, `deleted`),
    KEY `idx_payment_allocate_statement_status` (`tenant_id`, `ap_statement_id`, `status`, `deleted`),
    KEY `idx_payment_allocate_payment_status` (`tenant_id`, `payment_id`, `status`, `deleted`),
    KEY `idx_payment_allocate_biz` (`tenant_id`, `biz_type`, `biz_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP payment allocate';

ALTER TABLE `erp_finance_payment_item`
    ADD COLUMN `ap_statement_id` BIGINT NULL COMMENT 'ap statement id' AFTER `payment_id`;

ALTER TABLE `erp_finance_payment_item`
    ADD KEY `idx_finance_payment_item_ap_statement_id` (`ap_statement_id`);
