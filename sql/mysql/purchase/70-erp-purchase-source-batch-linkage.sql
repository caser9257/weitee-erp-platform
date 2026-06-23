ALTER TABLE `erp_purchase_in_items`
    ADD COLUMN `purchase_source_batch_id` BIGINT NULL COMMENT '采购来源批次编号' AFTER `product_id`;

ALTER TABLE `erp_purchase_in_stock_execute_item_batch`
    ADD COLUMN `purchase_source_batch_id` BIGINT NULL COMMENT '采购来源批次编号' AFTER `stock_batch_id`,
    ADD COLUMN `purchase_source_batch_no` VARCHAR(64) NULL COMMENT '采购来源批次号' AFTER `batch_no`;

ALTER TABLE `erp_stock_batch`
    ADD COLUMN `purchase_source_batch_id` BIGINT NULL COMMENT '采购来源批次编号' AFTER `source_biz_item_id`,
    ADD COLUMN `purchase_source_batch_no` VARCHAR(64) NULL COMMENT '采购来源批次号' AFTER `source_biz_no`;
