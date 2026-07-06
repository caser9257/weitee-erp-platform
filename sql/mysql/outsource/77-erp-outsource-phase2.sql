-- =====================================================
-- ERP outsource phase2
-- =====================================================

ALTER TABLE `erp_outsource_issue`
    ADD COLUMN `issue_type` INT NOT NULL DEFAULT 10 COMMENT 'issue type' AFTER `order_id`;

ALTER TABLE `erp_outsource_issue`
    ADD KEY `idx_outsource_issue_order_type` (`order_id`, `issue_type`, `deleted`);
