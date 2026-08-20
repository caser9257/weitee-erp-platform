-- BOM 明细位号字段迁移
-- 目的：为制造 BOM 明细（erp_bom_item）与研发 BOM 明细（erp_rd_bom_item）补充位号（reference_designator）字段，
--       对齐前端 BOM 明细模型（电子 BOM 场景，如 R1、C2、U3），支撑位号录入与后续准确性校验
-- 幂等：使用 information_schema 检查列是否存在，可重复执行

SET @bom_item_has_col := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_bom_item' AND COLUMN_NAME = 'reference_designator'
);
SET @sql_bom_item := IF(@bom_item_has_col = 0,
    'ALTER TABLE `erp_bom_item` ADD COLUMN `reference_designator` varchar(512) DEFAULT NULL COMMENT ''位号（如 R1,C2,U3）'' AFTER `loss_rate`',
    'SELECT 1');
PREPARE stmt_bom_item FROM @sql_bom_item;
EXECUTE stmt_bom_item;
DEALLOCATE PREPARE stmt_bom_item;

SET @rd_bom_item_has_col := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_rd_bom_item' AND COLUMN_NAME = 'reference_designator'
);
SET @sql_rd_bom_item := IF(@rd_bom_item_has_col = 0,
    'ALTER TABLE `erp_rd_bom_item` ADD COLUMN `reference_designator` varchar(512) DEFAULT NULL COMMENT ''位号（如 R1,C2,U3）'' AFTER `loss_rate`',
    'SELECT 1');
PREPARE stmt_rd_bom_item FROM @sql_rd_bom_item;
EXECUTE stmt_rd_bom_item;
DEALLOCATE PREPARE stmt_rd_bom_item;
