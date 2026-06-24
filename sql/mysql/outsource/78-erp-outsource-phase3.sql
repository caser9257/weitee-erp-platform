-- =====================================================
-- ERP outsource phase3
-- =====================================================

ALTER TABLE `erp_outsource_order`
    ADD COLUMN `loss_qty` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT 'loss qty' AFTER `finished_qty`,
    ADD COLUMN `close_remark` VARCHAR(255) NULL COMMENT 'close remark' AFTER `status`,
    ADD COLUMN `close_time` DATETIME NULL COMMENT 'close time' AFTER `close_remark`;

ALTER TABLE `erp_outsource_order`
    ADD KEY `idx_outsource_order_status_close_time` (`status`, `close_time`, `deleted`);
