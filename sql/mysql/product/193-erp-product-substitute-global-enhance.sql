-- 物料替代料关联增强：新增替代类型与启用状态字段（P7）
ALTER TABLE `erp_product_substitute`
    ADD COLUMN `substitute_type` tinyint   DEFAULT 1 COMMENT '替代类型：1-全局通用 2-临时替代' AFTER `replace_ratio`,
    ADD COLUMN `status`          tinyint   DEFAULT 0 COMMENT '启用状态：0-启用 1-停用' AFTER `substitute_type`,
    ADD INDEX `idx_product_id_status` (`product_id`, `status`);