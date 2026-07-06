-- =====================================================
-- ERP outsource phase1
-- =====================================================

CREATE TABLE `erp_outsource_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `no` VARCHAR(64) NOT NULL COMMENT 'outsource order no',
    `order_type` INT NOT NULL COMMENT 'order type',
    `supplier_id` BIGINT NOT NULL COMMENT 'supplier id',
    `product_id` BIGINT NOT NULL COMMENT 'product id',
    `bom_id` BIGINT NULL COMMENT 'bom id',
    `project_id` BIGINT NULL COMMENT 'project id',
    `process_name` VARCHAR(128) NULL COMMENT 'process name',
    `planned_qty` DECIMAL(24, 6) NOT NULL COMMENT 'planned qty',
    `issued_qty` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'issued qty',
    `returned_qty` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'returned qty',
    `finished_qty` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'finished qty',
    `status` INT NOT NULL DEFAULT 10 COMMENT 'status',
    `remark` VARCHAR(255) NULL COMMENT 'remark',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'creator',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'updater',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_outsource_order_no` (`no`, `deleted`),
    KEY `idx_outsource_order_supplier_status` (`supplier_id`, `status`, `deleted`),
    KEY `idx_outsource_order_product_status` (`product_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP outsource order';

CREATE TABLE `erp_outsource_issue` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `issue_no` VARCHAR(64) NOT NULL COMMENT 'issue no',
    `order_id` BIGINT NOT NULL COMMENT 'outsource order id',
    `issue_time` DATETIME NOT NULL COMMENT 'issue time',
    `status` INT NOT NULL DEFAULT 20 COMMENT 'status',
    `issue_qty` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'issue qty',
    `issue_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'issue amount',
    `remark` VARCHAR(255) NULL COMMENT 'remark',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'creator',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'updater',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_outsource_issue_no` (`issue_no`, `deleted`),
    KEY `idx_outsource_issue_order` (`order_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP outsource issue';

CREATE TABLE `erp_outsource_issue_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `issue_id` BIGINT NOT NULL COMMENT 'issue id',
    `material_id` BIGINT NOT NULL COMMENT 'material id',
    `warehouse_id` BIGINT NOT NULL COMMENT 'warehouse id',
    `issue_qty` DECIMAL(24, 6) NOT NULL COMMENT 'issue qty',
    `issue_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'issue amount',
    `remark` VARCHAR(255) NULL COMMENT 'remark',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'creator',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'updater',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
    PRIMARY KEY (`id`),
    KEY `idx_outsource_issue_item_issue` (`issue_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP outsource issue item';

CREATE TABLE `erp_outsource_issue_batch` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `issue_item_id` BIGINT NOT NULL COMMENT 'issue item id',
    `stock_batch_id` BIGINT NOT NULL COMMENT 'stock batch id',
    `batch_no` VARCHAR(64) NOT NULL COMMENT 'batch no',
    `issue_qty` DECIMAL(24, 6) NOT NULL COMMENT 'issue qty',
    `issue_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'issue amount',
    `inbound_time` DATETIME NULL COMMENT 'inbound time',
    `produce_date` DATE NULL COMMENT 'produce date',
    `expire_date` DATE NULL COMMENT 'expire date',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'creator',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'updater',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
    PRIMARY KEY (`id`),
    KEY `idx_outsource_issue_batch_issue_item` (`issue_item_id`, `deleted`),
    KEY `idx_outsource_issue_batch_stock_batch` (`stock_batch_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP outsource issue batch';

CREATE TABLE `erp_outsource_return` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `return_no` VARCHAR(64) NOT NULL COMMENT 'return no',
    `order_id` BIGINT NOT NULL COMMENT 'outsource order id',
    `return_time` DATETIME NOT NULL COMMENT 'return time',
    `status` INT NOT NULL DEFAULT 20 COMMENT 'status',
    `return_qty` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'return qty',
    `return_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'return amount',
    `remark` VARCHAR(255) NULL COMMENT 'remark',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'creator',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'updater',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_outsource_return_no` (`return_no`, `deleted`),
    KEY `idx_outsource_return_order` (`order_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP outsource return';

CREATE TABLE `erp_outsource_return_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `return_id` BIGINT NOT NULL COMMENT 'return id',
    `material_id` BIGINT NOT NULL COMMENT 'material id',
    `warehouse_id` BIGINT NOT NULL COMMENT 'warehouse id',
    `return_qty` DECIMAL(24, 6) NOT NULL COMMENT 'return qty',
    `return_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'return amount',
    `remark` VARCHAR(255) NULL COMMENT 'remark',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'creator',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'updater',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
    PRIMARY KEY (`id`),
    KEY `idx_outsource_return_item_return` (`return_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP outsource return item';

CREATE TABLE `erp_outsource_return_batch` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `return_item_id` BIGINT NOT NULL COMMENT 'return item id',
    `issue_batch_id` BIGINT NOT NULL COMMENT 'issue batch id',
    `stock_batch_id` BIGINT NOT NULL COMMENT 'stock batch id',
    `batch_no` VARCHAR(64) NOT NULL COMMENT 'batch no',
    `return_qty` DECIMAL(24, 6) NOT NULL COMMENT 'return qty',
    `return_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'return amount',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'creator',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'updater',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
    PRIMARY KEY (`id`),
    KEY `idx_outsource_return_batch_return_item` (`return_item_id`, `deleted`),
    KEY `idx_outsource_return_batch_issue_batch` (`issue_batch_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP outsource return batch';

CREATE TABLE `erp_outsource_inbound` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `inbound_no` VARCHAR(64) NOT NULL COMMENT 'inbound no',
    `order_id` BIGINT NOT NULL COMMENT 'outsource order id',
    `warehouse_id` BIGINT NOT NULL COMMENT 'warehouse id',
    `batch_no` VARCHAR(64) NOT NULL COMMENT 'batch no',
    `inbound_time` DATETIME NOT NULL COMMENT 'inbound time',
    `produce_date` DATE NULL COMMENT 'produce date',
    `expire_date` DATE NULL COMMENT 'expire date',
    `status` INT NOT NULL DEFAULT 20 COMMENT 'status',
    `inbound_qty` DECIMAL(24, 6) NOT NULL COMMENT 'inbound qty',
    `material_cost` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'material cost',
    `process_fee` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'process fee',
    `total_cost` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'total cost',
    `unit_cost` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'unit cost',
    `remark` VARCHAR(255) NULL COMMENT 'remark',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'creator',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'updater',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_outsource_inbound_no` (`inbound_no`, `deleted`),
    KEY `idx_outsource_inbound_order` (`order_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP outsource inbound';

CREATE TABLE `erp_outsource_fee` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `fee_no` VARCHAR(64) NOT NULL COMMENT 'fee no',
    `order_id` BIGINT NOT NULL COMMENT 'outsource order id',
    `fee_time` DATETIME NOT NULL COMMENT 'fee time',
    `status` INT NOT NULL DEFAULT 20 COMMENT 'status',
    `fee_amount` DECIMAL(24, 6) NOT NULL COMMENT 'fee amount',
    `remark` VARCHAR(255) NULL COMMENT 'remark',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'creator',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'updater',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_outsource_fee_no` (`fee_no`, `deleted`),
    KEY `idx_outsource_fee_order` (`order_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP outsource fee';
