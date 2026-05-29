-- =====================================================
-- ERP finance voucher phase2
-- 1. add approve / post / reverse audit fields
-- 2. allow reverse voucher to not occupy biz unique key
-- 3. add reverse voucher linkage index
-- =====================================================

SET @voucher_biz_id_nullable := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_finance_voucher'
      AND COLUMN_NAME = 'biz_id'
      AND IS_NULLABLE = 'YES'
);
SET @voucher_biz_id_sql := IF(
    @voucher_biz_id_nullable > 0,
    'SELECT ''erp_finance_voucher.biz_id already nullable''',
    'ALTER TABLE `erp_finance_voucher` MODIFY COLUMN `biz_id` BIGINT NULL COMMENT ''业务单据编号'' '
);
PREPARE voucher_biz_id_stmt FROM @voucher_biz_id_sql;
EXECUTE voucher_biz_id_stmt;
DEALLOCATE PREPARE voucher_biz_id_stmt;

SET @voucher_approve_user_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_finance_voucher'
      AND COLUMN_NAME = 'approve_user_id'
);
SET @voucher_approve_user_sql := IF(
    @voucher_approve_user_exists > 0,
    'SELECT ''erp_finance_voucher.approve_user_id already exists''',
    'ALTER TABLE `erp_finance_voucher` ADD COLUMN `approve_user_id` BIGINT NULL COMMENT ''审核人编号'' AFTER `total_credit_amount`'
);
PREPARE voucher_approve_user_stmt FROM @voucher_approve_user_sql;
EXECUTE voucher_approve_user_stmt;
DEALLOCATE PREPARE voucher_approve_user_stmt;

SET @voucher_approve_time_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_finance_voucher'
      AND COLUMN_NAME = 'approve_time'
);
SET @voucher_approve_time_sql := IF(
    @voucher_approve_time_exists > 0,
    'SELECT ''erp_finance_voucher.approve_time already exists''',
    'ALTER TABLE `erp_finance_voucher` ADD COLUMN `approve_time` DATETIME NULL COMMENT ''审核时间'' AFTER `approve_user_id`'
);
PREPARE voucher_approve_time_stmt FROM @voucher_approve_time_sql;
EXECUTE voucher_approve_time_stmt;
DEALLOCATE PREPARE voucher_approve_time_stmt;

SET @voucher_post_user_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_finance_voucher'
      AND COLUMN_NAME = 'post_user_id'
);
SET @voucher_post_user_sql := IF(
    @voucher_post_user_exists > 0,
    'SELECT ''erp_finance_voucher.post_user_id already exists''',
    'ALTER TABLE `erp_finance_voucher` ADD COLUMN `post_user_id` BIGINT NULL COMMENT ''过账人编号'' AFTER `approve_time`'
);
PREPARE voucher_post_user_stmt FROM @voucher_post_user_sql;
EXECUTE voucher_post_user_stmt;
DEALLOCATE PREPARE voucher_post_user_stmt;

SET @voucher_post_time_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_finance_voucher'
      AND COLUMN_NAME = 'post_time'
);
SET @voucher_post_time_sql := IF(
    @voucher_post_time_exists > 0,
    'SELECT ''erp_finance_voucher.post_time already exists''',
    'ALTER TABLE `erp_finance_voucher` ADD COLUMN `post_time` DATETIME NULL COMMENT ''过账时间'' AFTER `post_user_id`'
);
PREPARE voucher_post_time_stmt FROM @voucher_post_time_sql;
EXECUTE voucher_post_time_stmt;
DEALLOCATE PREPARE voucher_post_time_stmt;

SET @voucher_reverse_user_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_finance_voucher'
      AND COLUMN_NAME = 'reverse_user_id'
);
SET @voucher_reverse_user_sql := IF(
    @voucher_reverse_user_exists > 0,
    'SELECT ''erp_finance_voucher.reverse_user_id already exists''',
    'ALTER TABLE `erp_finance_voucher` ADD COLUMN `reverse_user_id` BIGINT NULL COMMENT ''冲销操作人编号'' AFTER `post_time`'
);
PREPARE voucher_reverse_user_stmt FROM @voucher_reverse_user_sql;
EXECUTE voucher_reverse_user_stmt;
DEALLOCATE PREPARE voucher_reverse_user_stmt;

SET @voucher_reverse_time_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_finance_voucher'
      AND COLUMN_NAME = 'reverse_time'
);
SET @voucher_reverse_time_sql := IF(
    @voucher_reverse_time_exists > 0,
    'SELECT ''erp_finance_voucher.reverse_time already exists''',
    'ALTER TABLE `erp_finance_voucher` ADD COLUMN `reverse_time` DATETIME NULL COMMENT ''冲销时间'' AFTER `reverse_user_id`'
);
PREPARE voucher_reverse_time_stmt FROM @voucher_reverse_time_sql;
EXECUTE voucher_reverse_time_stmt;
DEALLOCATE PREPARE voucher_reverse_time_stmt;

SET @voucher_reverse_voucher_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_finance_voucher'
      AND COLUMN_NAME = 'reverse_voucher_id'
);
SET @voucher_reverse_voucher_sql := IF(
    @voucher_reverse_voucher_exists > 0,
    'SELECT ''erp_finance_voucher.reverse_voucher_id already exists''',
    'ALTER TABLE `erp_finance_voucher` ADD COLUMN `reverse_voucher_id` BIGINT NULL COMMENT ''冲销凭证编号'' AFTER `reverse_time`'
);
PREPARE voucher_reverse_voucher_stmt FROM @voucher_reverse_voucher_sql;
EXECUTE voucher_reverse_voucher_stmt;
DEALLOCATE PREPARE voucher_reverse_voucher_stmt;

SET @voucher_reverse_from_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_finance_voucher'
      AND COLUMN_NAME = 'reverse_from_voucher_id'
);
SET @voucher_reverse_from_sql := IF(
    @voucher_reverse_from_exists > 0,
    'SELECT ''erp_finance_voucher.reverse_from_voucher_id already exists''',
    'ALTER TABLE `erp_finance_voucher` ADD COLUMN `reverse_from_voucher_id` BIGINT NULL COMMENT ''来源凭证编号'' AFTER `reverse_voucher_id`'
);
PREPARE voucher_reverse_from_stmt FROM @voucher_reverse_from_sql;
EXECUTE voucher_reverse_from_stmt;
DEALLOCATE PREPARE voucher_reverse_from_stmt;

SET @voucher_reverse_remark_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_finance_voucher'
      AND COLUMN_NAME = 'reverse_remark'
);
SET @voucher_reverse_remark_sql := IF(
    @voucher_reverse_remark_exists > 0,
    'SELECT ''erp_finance_voucher.reverse_remark already exists''',
    'ALTER TABLE `erp_finance_voucher` ADD COLUMN `reverse_remark` VARCHAR(255) NULL COMMENT ''冲销说明'' AFTER `reverse_from_voucher_id`'
);
PREPARE voucher_reverse_remark_stmt FROM @voucher_reverse_remark_sql;
EXECUTE voucher_reverse_remark_stmt;
DEALLOCATE PREPARE voucher_reverse_remark_stmt;

SET @voucher_reverse_from_index_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'erp_finance_voucher'
      AND INDEX_NAME = 'uk_finance_voucher_reverse_from'
);
SET @voucher_reverse_from_index_sql := IF(
    @voucher_reverse_from_index_exists > 0,
    'SELECT ''uk_finance_voucher_reverse_from already exists''',
    'ALTER TABLE `erp_finance_voucher` ADD UNIQUE KEY `uk_finance_voucher_reverse_from` (`tenant_id`, `reverse_from_voucher_id`, `deleted`)'
);
PREPARE voucher_reverse_from_index_stmt FROM @voucher_reverse_from_index_sql;
EXECUTE voucher_reverse_from_index_stmt;
DEALLOCATE PREPARE voucher_reverse_from_index_stmt;
