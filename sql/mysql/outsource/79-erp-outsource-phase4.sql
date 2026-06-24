-- =====================================================
-- ERP outsource phase4
-- =====================================================

CREATE TABLE `erp_outsource_loss_detail` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `order_id` BIGINT NOT NULL COMMENT 'outsource order id',
    `issue_batch_id` BIGINT NOT NULL COMMENT 'outsource issue batch id',
    `material_id` BIGINT NOT NULL COMMENT 'material id',
    `warehouse_id` BIGINT NOT NULL COMMENT 'warehouse id',
    `stock_batch_id` BIGINT NOT NULL COMMENT 'stock batch id',
    `batch_no` VARCHAR(64) NOT NULL COMMENT 'batch no',
    `loss_qty` DECIMAL(24, 6) NOT NULL COMMENT 'loss qty',
    `loss_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'loss amount',
    `remark` VARCHAR(255) NULL COMMENT 'remark',
    `creator` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'creator',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `updater` VARCHAR(64) NOT NULL DEFAULT '' COMMENT 'updater',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_outsource_loss_order_issue_batch` (`order_id`, `issue_batch_id`, `deleted`),
    KEY `idx_outsource_loss_order` (`order_id`, `deleted`),
    KEY `idx_outsource_loss_issue_batch` (`issue_batch_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP outsource loss detail';
