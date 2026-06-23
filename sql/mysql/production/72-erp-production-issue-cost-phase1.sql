-- =====================================================
-- ERP production issue cost phase1
-- =====================================================

ALTER TABLE `erp_production_issue`
    ADD COLUMN `issue_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '领料金额' AFTER `status`;

ALTER TABLE `erp_production_issue_item`
    ADD COLUMN `issue_amount` DECIMAL(24, 6) NOT NULL DEFAULT 0 COMMENT '领料金额' AFTER `issue_qty`;
