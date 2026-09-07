-- 173-erp-purchase-return-iqc-trace.sql
-- 采购退货单补充"来源质检单编号"字段，实现退货单到 IQC 质检单的结构化追溯
-- 幂等写法：先检查列/索引是否存在，再执行

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;

-- 采购退货：仅当列不存在时添加
SET @col_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_purchase_return'
    AND COLUMN_NAME = 'quality_id'
);
SET @sql = IF(@col_exists = 0,
  'ALTER TABLE erp_purchase_return ADD COLUMN quality_id BIGINT DEFAULT NULL COMMENT ''来源质检单编号，关联 erp_purchase_in_quality.id，手工退货单为 NULL''',
  'SELECT 1 AS skip'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 采购退货：仅当索引不存在时添加，支撑 createReturnFromQuality 的 selectByQualityId 幂等判重
SET @idx_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_purchase_return'
    AND INDEX_NAME = 'idx_quality_id'
);
SET @sql = IF(@idx_exists = 0,
  'ALTER TABLE erp_purchase_return ADD INDEX idx_quality_id (quality_id)',
  'SELECT 1 AS skip'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
