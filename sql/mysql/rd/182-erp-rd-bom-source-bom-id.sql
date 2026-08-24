/*
  研发 BOM 版本链结构化：
  1. erp_rd_bom 新增 source_bom_id 列（发起升版变更时指向被变更的旧版本）
  2. 为删除闸门查询补充索引
  3. 从历史 change-log 文本回填存量版本的派生关系：
     CHANGE_CREATE 日志格式为 "发起变更，源 BOM id={id} 源版本={ver} 新版本={ver}"
     解析不出或文本被改动的记录保持 NULL（未知来源），不做猜测性关联
  幂等：列/索引用 information_schema 先查再建；回填只处理 source_bom_id IS NULL 的行
*/
SET NAMES utf8mb4;
-- 不指定 USE，跟随执行时连接的数据库（如：mysql ... weitee-erp < 本文件）

-- 1. 加列
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_rd_bom'
    AND COLUMN_NAME = 'source_bom_id'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `erp_rd_bom` ADD COLUMN `source_bom_id` BIGINT NULL COMMENT ''源版本 BOM 编号（升版变更来源）'' AFTER `last_published_time`',
  'SELECT ''source_bom_id already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 加索引（删除闸门按 source_bom_id 反查）
SET @idx_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_rd_bom'
    AND INDEX_NAME = 'idx_rd_bom_source_bom'
);
SET @sql := IF(@idx_exists = 0,
  'ALTER TABLE `erp_rd_bom` ADD INDEX `idx_rd_bom_source_bom` (`source_bom_id`)',
  'SELECT ''idx_rd_bom_source_bom already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 存量回填：取每条 BOM 最早的 CHANGE_CREATE 日志，解析 "源 BOM id=" 后的数字
UPDATE erp_rd_bom t
SET t.source_bom_id = (
  SELECT CAST(
           SUBSTRING_INDEX(
             SUBSTRING_INDEX(cl.change_detail, '源 BOM id=', -1), ' ', 1
           ) AS UNSIGNED)
  FROM erp_rd_bom_change_log cl
  WHERE cl.bom_id = t.id
    AND cl.change_type = 'CHANGE_CREATE'
    AND cl.change_detail LIKE '%源 BOM id=%'
  ORDER BY cl.id ASC
  LIMIT 1
)
WHERE t.source_bom_id IS NULL
  AND EXISTS (
    SELECT 1 FROM erp_rd_bom_change_log cl2
    WHERE cl2.bom_id = t.id
      AND cl2.change_type = 'CHANGE_CREATE'
      AND cl2.change_detail LIKE '%源 BOM id=%'
  );

/*
  4. 回填残留检测（只读，供 DBA 核验，不改数据）
  预期返回 0 行：若仍有"发过变更但 source_bom_id 为空"的记录，
  说明其日志文本被改动过无法解析，需人工核对后手工补链：
    UPDATE erp_rd_bom SET source_bom_id = <真实源id> WHERE id = <待补id>;
*/
SELECT t.id AS bom_id, t.product_id, t.version, t.remark
FROM erp_rd_bom t
WHERE t.source_bom_id IS NULL
  AND EXISTS (
    SELECT 1 FROM erp_rd_bom_change_log cl2
    WHERE cl2.bom_id = t.id
      AND cl2.change_type = 'CHANGE_CREATE'
      AND cl2.change_detail LIKE '%源 BOM id=%'
  );
