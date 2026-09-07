-- 单据明细录入单位换算：录入数量 + 换算率快照。
-- 语义：count 仍为产品基本单位记账数量；input_count 为录入单位口径数量；
--       conversion_rate 为录入时的换算率快照（1 录入单位 = conversion_rate 基本单位），基本单位录入时为 NULL。
-- 存量数据 input_count/conversion_rate 为 NULL，读取时按 input_count = count、rate = 1 兼容，无需刷库。
ALTER TABLE `erp_sale_order_items`
    ADD COLUMN `input_count` DECIMAL(24, 6) NULL COMMENT '录入数量（录入单位口径）；NULL 表示与 count 相同' AFTER `product_unit_id`,
    ADD COLUMN `conversion_rate` DECIMAL(24, 10) NULL COMMENT '换算率快照：1 录入单位 = conversion_rate 基本单位；基本单位为 NULL' AFTER `input_count`;
ALTER TABLE `erp_sale_out_items`
    ADD COLUMN `input_count` DECIMAL(24, 6) NULL COMMENT '录入数量（录入单位口径）；NULL 表示与 count 相同' AFTER `product_unit_id`,
    ADD COLUMN `conversion_rate` DECIMAL(24, 10) NULL COMMENT '换算率快照：1 录入单位 = conversion_rate 基本单位；基本单位为 NULL' AFTER `input_count`;
ALTER TABLE `erp_sale_return_items`
    ADD COLUMN `input_count` DECIMAL(24, 6) NULL COMMENT '录入数量（录入单位口径）；NULL 表示与 count 相同' AFTER `product_unit_id`,
    ADD COLUMN `conversion_rate` DECIMAL(24, 10) NULL COMMENT '换算率快照：1 录入单位 = conversion_rate 基本单位；基本单位为 NULL' AFTER `input_count`;
ALTER TABLE `erp_purchase_order_items`
    ADD COLUMN `input_count` DECIMAL(24, 6) NULL COMMENT '录入数量（录入单位口径）；NULL 表示与 count 相同' AFTER `product_unit_id`,
    ADD COLUMN `conversion_rate` DECIMAL(24, 10) NULL COMMENT '换算率快照：1 录入单位 = conversion_rate 基本单位；基本单位为 NULL' AFTER `input_count`;
ALTER TABLE `erp_purchase_in_items`
    ADD COLUMN `input_count` DECIMAL(24, 6) NULL COMMENT '录入数量（录入单位口径）；NULL 表示与 count 相同' AFTER `product_unit_id`,
    ADD COLUMN `conversion_rate` DECIMAL(24, 10) NULL COMMENT '换算率快照：1 录入单位 = conversion_rate 基本单位；基本单位为 NULL' AFTER `input_count`;
ALTER TABLE `erp_purchase_return_items`
    ADD COLUMN `input_count` DECIMAL(24, 6) NULL COMMENT '录入数量（录入单位口径）；NULL 表示与 count 相同' AFTER `product_unit_id`,
    ADD COLUMN `conversion_rate` DECIMAL(24, 10) NULL COMMENT '换算率快照：1 录入单位 = conversion_rate 基本单位；基本单位为 NULL' AFTER `input_count`;
ALTER TABLE `erp_stock_in_item`
    ADD COLUMN `input_count` DECIMAL(24, 6) NULL COMMENT '录入数量（录入单位口径）；NULL 表示与 count 相同' AFTER `product_unit_id`,
    ADD COLUMN `conversion_rate` DECIMAL(24, 10) NULL COMMENT '换算率快照：1 录入单位 = conversion_rate 基本单位；基本单位为 NULL' AFTER `input_count`;
ALTER TABLE `erp_stock_out_item`
    ADD COLUMN `input_count` DECIMAL(24, 6) NULL COMMENT '录入数量（录入单位口径）；NULL 表示与 count 相同' AFTER `product_unit_id`,
    ADD COLUMN `conversion_rate` DECIMAL(24, 10) NULL COMMENT '换算率快照：1 录入单位 = conversion_rate 基本单位；基本单位为 NULL' AFTER `input_count`;
ALTER TABLE `erp_stock_move_item`
    ADD COLUMN `input_count` DECIMAL(24, 6) NULL COMMENT '录入数量（录入单位口径）；NULL 表示与 count 相同' AFTER `product_unit_id`,
    ADD COLUMN `conversion_rate` DECIMAL(24, 10) NULL COMMENT '换算率快照：1 录入单位 = conversion_rate 基本单位；基本单位为 NULL' AFTER `input_count`;
ALTER TABLE `erp_stock_check_item`
    ADD COLUMN `input_count` DECIMAL(24, 6) NULL COMMENT '录入数量（录入单位口径）；NULL 表示与 count 相同' AFTER `product_unit_id`,
    ADD COLUMN `conversion_rate` DECIMAL(24, 10) NULL COMMENT '换算率快照：1 录入单位 = conversion_rate 基本单位；基本单位为 NULL' AFTER `input_count`;
