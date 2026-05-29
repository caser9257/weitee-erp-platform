-- =====================================================
-- ERP finance voucher template amount source expansion
-- add amount_source_value to voucher template item
-- =====================================================

ALTER TABLE `erp_finance_voucher_template_item`
    ADD COLUMN `amount_source_value` DECIMAL(24, 6) NULL COMMENT '金额来源值' AFTER `amount_source`;
