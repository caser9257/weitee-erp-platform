/*
  物料编码沿革（Code Lineage）：放开"被 BOM 引用禁改编码"冻结后的追溯机制
  背景：
    系统内所有单据/BOM 明细均按 product_id 引用物料，material_code 只是展示/检索属性；
    此前以"禁改"回避"改码后历史单据旧码对不上"的追溯问题。本次改为可追溯的放行：
    - 放开 materialCode 冻结（改走两段式变更/直改既有守卫）；
    - 每次改码在同一事务内写沿革表 + 主表冗余最近一次旧码（prev_material_code）；
    - 旧码占用不释放（历史唯一索引），防止新物料复用旧码导致仓库/供应商混料；
      注意与 202 号迁移"废除释放当前码可复用"正交：废除释放的是生效编码，
      沿革表中的历史旧码仍持续占用。
    - 搜索/BOM 导入/Cadence 导入按当前码 miss 后以沿革旧码兜底命中。
  实现要点：
    - 沿革表 append-only，业务不删除不更新；unique(old_code) 承载旧码占用；
    - 无 tenant_id（本项目已去租户化）。
  幂等：按 information_schema 判重，可重复执行。
  注意：不指定 USE，跟随执行时连接的数据库。
*/
SET NAMES utf8mb4;

-- ===================== A. 主表冗余：最近一次变更前编码 =====================
SET @has_prev := (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'erp_product' AND column_name = 'prev_material_code');
SET @ddl := IF(@has_prev = 0,
    'ALTER TABLE `erp_product`
        ADD COLUMN `prev_material_code` varchar(64) DEFAULT NULL COMMENT ''最近一次变更前物料编码（无改码历史为 NULL；完整沿革见 erp_product_code_history）'' AFTER `material_code`',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ===================== B. 编码沿革表 =====================
SET @has_table := (SELECT COUNT(*) FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'erp_product_code_history');
SET @ddl := IF(@has_table = 0,
    'CREATE TABLE `erp_product_code_history` (
        `id` bigint NOT NULL AUTO_INCREMENT COMMENT ''自增主键'',
        `product_id` bigint NOT NULL COMMENT ''物料 ID（引用 erp_product.id，改码不改主键）'',
        `old_code` varchar(64) NOT NULL COMMENT ''变更前编码（旧码占用不释放，unique 防止新物料复用）'',
        `new_code` varchar(64) NOT NULL COMMENT ''变更后编码'',
        `process_instance_id` varchar(128) DEFAULT NULL COMMENT ''关联审批流程实例 ID（直改路径为 NULL）'',
        `reason` varchar(500) DEFAULT NULL COMMENT ''变更原因（审批理由）'',
        `creator` varchar(64) DEFAULT '''' COMMENT ''创建者'',
        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
        `updater` varchar(64) DEFAULT '''' COMMENT ''更新者'',
        `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'',
        `deleted` bit(1) NOT NULL DEFAULT b''0'' COMMENT ''是否删除（沿革 append-only，业务不删除）'',
        PRIMARY KEY (`id`),
        UNIQUE KEY `uk_erp_product_code_history_old_code` (`old_code`),
        KEY `idx_erp_product_code_history_product_id` (`product_id`)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = ''物料编码沿革表（改码追溯/旧码占用）''',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ===================== C. 校验 =====================
SELECT COUNT(*) AS history_table_created FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'erp_product_code_history';
