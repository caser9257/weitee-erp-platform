-- 产品单位换算模型：基本单位 + 辅助单位 + 换算率（单层换算）。
-- 语义：1 个辅助单位 = conversion_rate 个其基本单位；基本单位 base_unit_id/conversion_rate 为 NULL。
-- 存量数据全部视为基本单位（type = 0），无需刷库。
ALTER TABLE `erp_product_unit`
    ADD COLUMN `unit_type` TINYINT NOT NULL DEFAULT 0 COMMENT '单位类型：0 基本单位，1 辅助单位' AFTER `status`,
    ADD COLUMN `base_unit_id` BIGINT NULL COMMENT '基本单位编号，辅助单位归属；基本单位为 NULL' AFTER `unit_type`,
    ADD COLUMN `conversion_rate` DECIMAL(24, 10) NULL COMMENT '换算率：1 辅助单位 = conversion_rate 基本单位；基本单位为 NULL' AFTER `base_unit_id`,
    ADD KEY `idx_base_unit_id` (`base_unit_id`);
