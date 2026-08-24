-- 物料位置独立列：与位号分离，避免安装说明挤占备注
ALTER TABLE `erp_rd_bom_item`
    ADD COLUMN `position` varchar(512) NULL DEFAULT NULL COMMENT '物料位置（安装位置，如 安装于电源外壳上）' AFTER `reference_designator`,
    ADD INDEX `idx_position` (`position`);
ALTER TABLE `erp_bom_item`
    ADD COLUMN `position` varchar(512) NULL DEFAULT NULL COMMENT '物料位置（安装位置）' AFTER `reference_designator`,
    ADD INDEX `idx_position` (`position`);
