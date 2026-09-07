-- =====================================================
-- ERP stock unique key for (product_id, warehouse_id)
-- 根因：erp_stock 仅有主键，updateStockCountIncrement 的
--       "查无则插" 在并发首次入库同一产品+仓库时会插入重复库存行，
--       之后 selectOne 抛 TooManyResults，该仓位全部出入库瘫痪；
--       同时库存变更的行锁（SELECT ... FOR UPDATE）需要该索引支撑，
--       否则退化为全表扫描锁。
-- 约束：若存量数据已存在同一产品+仓库的多条未删除库存行，
--       本脚本的 ALTER 会以 Duplicate entry 失败中止，
--       必须先由业务核对合并重复行后再执行，禁止脚本静默删并。
-- 幂等：索引已存在时跳过。
-- 部署阻断（DEPLOY BLOCKER）：必须先执行本脚本，再发布代码。
--       否则 FOR UPDATE 因缺乏索引会退化为全表扫描锁。
-- =====================================================

SET NAMES utf8mb4;

-- 0. 重复行自检（存在重复时先输出明细，便于定位；随后 ALTER 会失败中止）
SELECT `product_id`, `warehouse_id`, COUNT(*) AS `dup_rows`, GROUP_CONCAT(`id`) AS `stock_ids`
FROM `erp_stock`
WHERE `deleted` = b'0'
GROUP BY `product_id`, `warehouse_id`
HAVING COUNT(*) > 1;

SET @stock_uk_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_stock'
      AND INDEX_NAME = 'uk_stock_product_warehouse'
);
SET @stock_uk_sql := IF(
    @stock_uk_exists > 0,
    'SELECT ''uk_stock_product_warehouse already exists''',
    'ALTER TABLE `erp_stock` ADD UNIQUE KEY `uk_stock_product_warehouse` (`product_id`, `warehouse_id`, `deleted`)'
);
PREPARE stock_uk_stmt FROM @stock_uk_sql;
EXECUTE stock_uk_stmt;
DEALLOCATE PREPARE stock_uk_stmt;
