-- =====================================================
-- ERP purchase engineering fee
-- =====================================================

ALTER TABLE `erp_purchase_order_items`
    ADD COLUMN `engineering_fee` decimal(24, 6) NULL COMMENT 'engineering fee' AFTER `product_price`;

ALTER TABLE `erp_purchase_order_items`
    ADD COLUMN `pricing_bom_id` bigint NULL COMMENT 'pricing bom id' AFTER `engineering_fee`,
    ADD COLUMN `pricing_bom_version` varchar(64) NULL COMMENT 'pricing bom version' AFTER `pricing_bom_id`;

ALTER TABLE `erp_purchase_in_items`
    ADD COLUMN `engineering_fee` decimal(24, 6) NULL COMMENT 'engineering fee' AFTER `product_price`;

ALTER TABLE `erp_purchase_in_items`
    ADD COLUMN `pricing_bom_id` bigint NULL COMMENT 'pricing bom id' AFTER `engineering_fee`,
    ADD COLUMN `pricing_bom_version` varchar(64) NULL COMMENT 'pricing bom version' AFTER `pricing_bom_id`;
