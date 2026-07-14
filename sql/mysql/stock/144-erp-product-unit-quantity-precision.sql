-- 产品单位数量精度。
-- 现有单位使用 3 位小数作为兼容默认值；上线后必须在“产品单位”维护实际精度，
-- 例如 个/件/台/套设为 0，kg/m/L 按实际业务设为 3 或更高。
ALTER TABLE `erp_product_unit`
    ADD COLUMN `quantity_precision` TINYINT NOT NULL DEFAULT 3 COMMENT '数量精度，0 表示只允许整数' AFTER `status`;
