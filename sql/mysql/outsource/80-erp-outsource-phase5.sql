-- =====================================================
-- ERP outsource phase5
-- =====================================================

ALTER TABLE `erp_outsource_loss_detail`
    DROP INDEX `uk_outsource_loss_order_issue_batch`,
    ADD KEY `idx_outsource_loss_order_issue_batch` (`order_id`, `issue_batch_id`, `deleted`);
