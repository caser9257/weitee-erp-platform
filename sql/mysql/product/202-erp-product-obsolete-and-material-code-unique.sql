/*
  产品废除留痕字段 + material_code 唯一非空约束
  背景：
    产品主键为自增 id，material_code（物料编码）此前可空、无唯一约束。本次改造：
    - 新增废除留痕字段（abolish_flag / abolish_time / abolish_reason / abolish_by），废除=销号，编码可复用；
    - material_code 施加 NOT NULL + 唯一约束；废除（abolish_flag=1）或逻辑删除（deleted=1）后编码释放可复用。
  实现要点：
    - MySQL 不支持部分唯一索引（PostgreSQL WHERE 特性），采用"生成列 + 唯一索引"：
      生成列 active_material_code = IF(abolish_flag 或 deleted, NULL, material_code)；
      唯一索引允许多个 NULL，故废除/删除后编码自动退出唯一范围，可复用；
    - 存量治理：唯一空编码物料（id=11785，P0E2 验证原材料，被 BOM 引用不可删）
      分配遗留编码 CONCAT('LEGACY-', id)，再收紧 NOT NULL；
    - 编码复用 = 新物料直接插入相同 material_code（主键 id 不同，生成列唯一范围不含已废除记录）。
  幂等：全部按 information_schema 判重，可重复执行。
  注意：不指定 USE，跟随执行时连接的数据库；本项目已去租户化，不涉及 tenant_id。
*/
SET NAMES utf8mb4;

-- ===================== A. 废除留痕字段 =====================
SET @has_abolish_flag := (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'erp_product' AND column_name = 'abolish_flag');
SET @ddl := IF(@has_abolish_flag = 0,
    'ALTER TABLE `erp_product`
        ADD COLUMN `abolish_flag` bit(1) NOT NULL DEFAULT b''0'' COMMENT ''是否已废除（废除=销号，编码释放可复用）'' AFTER `process_instance_id`,
        ADD COLUMN `abolish_time` datetime DEFAULT NULL COMMENT ''废除时间'' AFTER `abolish_flag`,
        ADD COLUMN `abolish_reason` varchar(500) DEFAULT NULL COMMENT ''废除原因'' AFTER `abolish_time`,
        ADD COLUMN `abolish_by` bigint DEFAULT NULL COMMENT ''废除操作人'' AFTER `abolish_reason`',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ===================== B. 存量空编码治理（幂等） =====================
-- 唯一空编码物料 id=11785 分配遗留编码；未加 NOT NULL 前可重跑（已填则跳过）
UPDATE `erp_product`
SET `material_code` = CONCAT('LEGACY-', id)
WHERE `material_code` IS NULL OR `material_code` = '';

-- ===================== C. material_code NOT NULL =====================
SET @has_nullable := (SELECT IS_NULLABLE FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'erp_product' AND column_name = 'material_code');
SET @ddl := IF(@has_nullable = 'YES',
    'ALTER TABLE `erp_product` MODIFY COLUMN `material_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT ''物料编码''',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ===================== D. 唯一非空约束（生成列方案，废除/删除后可复用编码） =====================
SET @has_gen_col := (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'erp_product' AND column_name = 'active_material_code');
SET @ddl := IF(@has_gen_col = 0,
    'ALTER TABLE `erp_product` ADD COLUMN `active_material_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
        GENERATED ALWAYS AS (IF(`abolish_flag` <> 0 OR `deleted` <> 0, NULL, `material_code`)) STORED COMMENT ''生效物料编码（废除/删除后为 NULL 以释放唯一约束）''',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_uk := (SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'erp_product' AND index_name = 'uk_erp_product_active_material_code');
SET @ddl := IF(@has_uk = 0,
    'ALTER TABLE `erp_product` ADD UNIQUE KEY `uk_erp_product_active_material_code` (`active_material_code`)',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ===================== E. 校验约束生效 =====================
SELECT COUNT(*) AS null_code_remaining FROM `erp_product`
WHERE `material_code` IS NULL OR `material_code` = '';
