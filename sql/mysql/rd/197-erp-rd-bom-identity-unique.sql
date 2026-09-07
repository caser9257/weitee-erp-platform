/*
  研发 BOM 身份唯一约束（Finding 2：BomCode 归一化）
  内容：
    A. 生成列 identity_version_key（版本归一，草稿 NULL → '__DRAFT__'，删除行追加 '_DEL'）
    B. 生成列 identity_bom_code_key（编码归一，TRIM + 空值兜底）
    C. 唯一索引 uk_rd_bom_product_code_version
  前置条件：执行前须清理存量重复草稿（同一 product_id+bom_code 多条 version=NULL 的记录只保留最新 1 条）
  幂等：GENERATED ALWAYS AS ... IF NOT EXISTS 需要手动判断（MySQL 不支持），本脚本通过信息_schema 判重
  注意：不指定 USE，跟随执行时连接的数据库；本项目已去租户化。
*/
SET NAMES utf8mb4;

-- ===================== A. identity_version_key =====================
SET @has_ivk := (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'erp_rd_bom' AND column_name = 'identity_version_key');
SET @ddl := IF(@has_ivk = 0,
    'ALTER TABLE `erp_rd_bom` ADD COLUMN `identity_version_key` varchar(64) GENERATED ALWAYS AS (CONCAT(COALESCE(NULLIF(TRIM(`version`), ''), ''__DRAFT__''), IF(`deleted` = b''1'', ''_DEL'', ''''))) STORED COMMENT ''版本归一值'' AFTER `version`',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ===================== B. identity_bom_code_key =====================
SET @has_ibk := (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'erp_rd_bom' AND column_name = 'identity_bom_code_key');
SET @ddl := IF(@has_ibk = 0,
    'ALTER TABLE `erp_rd_bom` ADD COLUMN `identity_bom_code_key` varchar(64) GENERATED ALWAYS AS (COALESCE(NULLIF(TRIM(`bom_code`), ''), ''__EMPTY__'')) STORED COMMENT ''BOM编码归一值'' AFTER `bom_code`',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ===================== C. 唯一索引 =====================
SET @has_idx := (SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'erp_rd_bom' AND index_name = 'uk_rd_bom_product_code_version');
SET @ddl := IF(@has_idx = 0,
    'ALTER TABLE `erp_rd_bom` ADD UNIQUE KEY `uk_rd_bom_product_code_version` (`product_id`, `identity_bom_code_key`, `identity_version_key`)',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
