   SET NAMES utf8mb4;

USE `ruoyi-vue-pro`;

SET @stmt_index_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'erp_ap_statement'
      AND index_name = 'idx_ap_statement_supplier_biz_status_deleted'
);
SET @stmt_sql := IF(
    @stmt_index_exists = 0,
    'ALTER TABLE `erp_ap_statement` ADD INDEX `idx_ap_statement_supplier_biz_status_deleted` (`supplier_id`, `biz_type`, `status`, `deleted`, `biz_id`)',
    'SELECT 1'
);
PREPARE stmt_index FROM @stmt_sql;
EXECUTE stmt_index;
DEALLOCATE PREPARE stmt_index;

SET @pii_index_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'erp_purchase_in_items'
      AND index_name = 'idx_purchase_in_items_in_deleted'
);
SET @pii_sql := IF(
    @pii_index_exists = 0,
    'ALTER TABLE `erp_purchase_in_items` ADD INDEX `idx_purchase_in_items_in_deleted` (`in_id`, `deleted`, `id`)',
    'SELECT 1'
);
PREPARE pii_index FROM @pii_sql;
EXECUTE pii_index;
DEALLOCATE PREPARE pii_index;
