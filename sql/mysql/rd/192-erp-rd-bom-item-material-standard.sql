/* 研发 BOM 明细保存导入时的产品型号快照，避免产品档案变化或缺失导致历史 BOM 无法展示型号 */
SET NAMES utf8mb4;

SET @rd_bom_item_material_standard_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_rd_bom_item'
      AND COLUMN_NAME = 'material_standard'
);

SET @add_rd_bom_item_material_standard_sql := IF(
    @rd_bom_item_material_standard_exists = 0,
    'ALTER TABLE `erp_rd_bom_item` ADD COLUMN `material_standard` varchar(255) DEFAULT NULL COMMENT ''产品型号快照'' AFTER `material_id`',
    'SELECT 1'
);
PREPARE stmt_rd_bom_item_material_standard FROM @add_rd_bom_item_material_standard_sql;
EXECUTE stmt_rd_bom_item_material_standard;
DEALLOCATE PREPARE stmt_rd_bom_item_material_standard;
