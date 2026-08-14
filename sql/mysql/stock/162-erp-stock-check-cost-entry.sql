-- 盘亏结转没有生产工单归属，允许生产成本条目关联非生产来源单据。
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

SET @column_nullable = (
  SELECT IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_production_cost_entry'
    AND COLUMN_NAME = 'production_order_id'
  LIMIT 1
);
SET @sql = IF(@column_nullable = 'NO',
  'ALTER TABLE erp_production_cost_entry MODIFY COLUMN production_order_id BIGINT NULL COMMENT ''生产工单 id（盘亏等非生产来源可为空）''',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
