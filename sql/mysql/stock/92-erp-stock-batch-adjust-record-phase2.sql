-- =====================================================
-- ERP stock batch adjust / record phase2
-- 1. add indexes for stock batch record page query
-- 2. add dictionary values for manual batch adjustment
-- =====================================================

SET @stock_batch_record_product_time_index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_stock_batch_record'
      AND INDEX_NAME = 'idx_stock_batch_record_product_time'
);
SET @stock_batch_record_product_time_index_sql := IF(
    @stock_batch_record_product_time_index_exists > 0,
    'SELECT ''idx_stock_batch_record_product_time already exists''',
    'ALTER TABLE `erp_stock_batch_record` ADD KEY `idx_stock_batch_record_product_time` (`product_id`, `warehouse_id`, `deleted`, `create_time`)'
);
PREPARE stock_batch_record_product_time_index_stmt FROM @stock_batch_record_product_time_index_sql;
EXECUTE stock_batch_record_product_time_index_stmt;
DEALLOCATE PREPARE stock_batch_record_product_time_index_stmt;

SET @stock_batch_record_biz_no_time_index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_stock_batch_record'
      AND INDEX_NAME = 'idx_stock_batch_record_biz_no_time'
);
SET @stock_batch_record_biz_no_time_index_sql := IF(
    @stock_batch_record_biz_no_time_index_exists > 0,
    'SELECT ''idx_stock_batch_record_biz_no_time already exists''',
    'ALTER TABLE `erp_stock_batch_record` ADD KEY `idx_stock_batch_record_biz_no_time` (`biz_type`, `biz_no`, `deleted`, `create_time`)'
);
PREPARE stock_batch_record_biz_no_time_index_stmt FROM @stock_batch_record_biz_no_time_index_sql;
EXECUTE stock_batch_record_biz_no_time_index_stmt;
DEALLOCATE PREPARE stock_batch_record_biz_no_time_index_stmt;

INSERT INTO `system_dict_data`
(`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT
    (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_dict_data` t),
    100, '批次调增', '100', 'erp_stock_record_biz_type', 0, 'success', '', '批次库存手工调增', '1', NOW(), '1', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `system_dict_data`
    WHERE `dict_type` = 'erp_stock_record_biz_type'
      AND `value` = '100'
      AND `deleted` = b'0'
);

INSERT INTO `system_dict_data`
(`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT
    (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_dict_data` t),
    101, '批次调减', '101', 'erp_stock_record_biz_type', 0, 'warning', '', '批次库存手工调减', '1', NOW(), '1', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `system_dict_data`
    WHERE `dict_type` = 'erp_stock_record_biz_type'
      AND `value` = '101'
      AND `deleted` = b'0'
);
