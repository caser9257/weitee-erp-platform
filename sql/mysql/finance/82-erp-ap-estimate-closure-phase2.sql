-- =====================================================
-- ERP AP estimate closure phase2
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


SET @current_schema := DATABASE();

SET @ddl_sql := IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @current_schema
          AND TABLE_NAME = 'erp_ap_estimate'
          AND COLUMN_NAME = 'reverse_type'
    ),
    'SELECT ''skip add reverse_type''',
    'ALTER TABLE `erp_ap_estimate` ADD COLUMN `reverse_type` INT NULL COMMENT ''reverse type'' AFTER `reverse_time`'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql := IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @current_schema
          AND TABLE_NAME = 'erp_ap_estimate'
          AND COLUMN_NAME = 'reverse_source_id'
    ),
    'SELECT ''skip add reverse_source_id''',
    'ALTER TABLE `erp_ap_estimate` ADD COLUMN `reverse_source_id` BIGINT NULL COMMENT ''reverse source id'' AFTER `reverse_type`'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql := IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = @current_schema
          AND TABLE_NAME = 'erp_ap_estimate'
          AND COLUMN_NAME = 'reverse_source_no'
    ),
    'SELECT ''skip add reverse_source_no''',
    'ALTER TABLE `erp_ap_estimate` ADD COLUMN `reverse_source_no` VARCHAR(64) NULL COMMENT ''reverse source no'' AFTER `reverse_source_id`'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `erp_ap_estimate`
SET `reverse_type` = CASE
        WHEN `reverse_type` IS NOT NULL THEN `reverse_type`
        WHEN `status` = 30
             AND `reverse_time` IS NOT NULL
             AND `reverse_remark` = '收票联动自动冲回暂估' THEN 20
        WHEN `status` = 30
             AND `reverse_time` IS NOT NULL THEN 10
        ELSE `reverse_type`
    END
WHERE `deleted` = b'0'
  AND `reverse_type` IS NULL;

SET FOREIGN_KEY_CHECKS = 1;
