/*
  研发 BOM 版本唯一约束：防止并发"发起变更"生成重复版本号
  注意：version 在首建（createRdBom）时为 NULL，MySQL 唯一索引允许多个 NULL，不影响草稿多行
  幂等：先查再建
*/
SET NAMES utf8mb4;
-- 不指定 USE，跟随执行时连接的数据库（与 182+ 口径一致）


SET @uk_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_rd_bom'
    AND INDEX_NAME = 'uk_rd_bom_product_version'
);
SET @sql := IF(@uk_exists = 0,
  'ALTER TABLE `erp_rd_bom` ADD UNIQUE KEY `uk_rd_bom_product_version` (`product_id`, `version`)',
  'SELECT ''uk_rd_bom_product_version already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
