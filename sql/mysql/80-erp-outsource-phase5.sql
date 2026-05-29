-- =====================================================
-- ERP outsource phase5
-- =====================================================

ALTER TABLE `erp_outsource_loss_detail`
    DROP INDEX `uk_outsource_loss_order_issue_batch`,
    ADD KEY `idx_outsource_loss_order_issue_batch` (`tenant_id`, `order_id`, `issue_batch_id`, `deleted`);
