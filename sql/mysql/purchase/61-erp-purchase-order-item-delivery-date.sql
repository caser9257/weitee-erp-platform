ALTER TABLE `erp_purchase_order_items`
    ADD COLUMN `delivery_date` datetime NULL DEFAULT NULL COMMENT '交货日期（采购明细承诺交期）' AFTER `tax_price`;
