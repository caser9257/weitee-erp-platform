-- =====================================================
-- ERP finance receipt file url phase1
-- =====================================================

ALTER TABLE `erp_finance_receipt`
    ADD COLUMN `file_url` VARCHAR(512) NULL COMMENT '附件地址' AFTER `remark`;
