/*
 Navicat / MySQL Init Script
 Target: ERP Project Master Extension + Sale Business Types
 Schema: ruoyi-vue-pro
 Date: 2026-04-03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Alter table for erp_project
-- ----------------------------
SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project` ADD COLUMN `project_type` varchar(32) NULL COMMENT ''项目类型'' AFTER `name`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'project_type'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project` ADD COLUMN `business_type` varchar(32) NULL COMMENT ''业务类型'' AFTER `project_type`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'business_type'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project` ADD COLUMN `source_type` varchar(32) NULL COMMENT ''来源类型'' AFTER `business_type`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'source_type'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project` ADD COLUMN `source_project_id` bigint NULL COMMENT ''来源项目编号'' AFTER `source_type`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'source_project_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project` ADD COLUMN `sale_order_id` bigint NULL COMMENT ''销售订单编号'' AFTER `source_project_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'sale_order_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project` ADD COLUMN `project_manager_id` bigint NULL COMMENT ''项目经理'' AFTER `sale_order_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'project_manager_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project` ADD COLUMN `plan_coordinator_id` bigint NULL COMMENT ''计划负责人'' AFTER `project_manager_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'plan_coordinator_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project` ADD COLUMN `owner_dept_id` bigint NULL COMMENT ''归属部门'' AFTER `plan_coordinator_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'owner_dept_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project` ADD COLUMN `current_stage_code` varchar(64) NULL COMMENT ''当前阶段编码'' AFTER `owner_dept_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'current_stage_code'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project` ADD COLUMN `risk_level` varchar(32) NULL COMMENT ''风险等级'' AFTER `current_stage_code`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND COLUMN_NAME = 'risk_level'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project` ADD KEY `idx_erp_project_project_type` (`project_type`)',
    'SELECT 1'
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND INDEX_NAME = 'idx_erp_project_project_type'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project` ADD KEY `idx_erp_project_business_type` (`business_type`)',
    'SELECT 1'
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND INDEX_NAME = 'idx_erp_project_business_type'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project` ADD KEY `idx_erp_project_source_project_id` (`source_project_id`)',
    'SELECT 1'
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND INDEX_NAME = 'idx_erp_project_source_project_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project` ADD KEY `idx_erp_project_sale_order_id` (`sale_order_id`)',
    'SELECT 1'
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND INDEX_NAME = 'idx_erp_project_sale_order_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_project` ADD KEY `idx_erp_project_current_stage_code` (`current_stage_code`)',
    'SELECT 1'
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_project'
    AND INDEX_NAME = 'idx_erp_project_current_stage_code'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- Alter table for erp_sale_order
-- ----------------------------
SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_sale_order` ADD COLUMN `business_type` varchar(32) NULL COMMENT ''业务类型'' AFTER `project_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_sale_order'
    AND COLUMN_NAME = 'business_type'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_sale_order` ADD COLUMN `source_project_id` bigint NULL COMMENT ''来源研发项目编号'' AFTER `business_type`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_sale_order'
    AND COLUMN_NAME = 'source_project_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_sale_order` ADD COLUMN `settlement_type` varchar(32) NULL COMMENT ''结算类型'' AFTER `source_project_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_sale_order'
    AND COLUMN_NAME = 'settlement_type'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_sale_order` ADD COLUMN `source_product_id` bigint NULL COMMENT ''来源产品编号'' AFTER `settlement_type`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_sale_order'
    AND COLUMN_NAME = 'source_product_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_sale_order` ADD KEY `idx_erp_sale_order_business_type` (`business_type`)',
    'SELECT 1'
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_sale_order'
    AND INDEX_NAME = 'idx_erp_sale_order_business_type'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_sale_order` ADD KEY `idx_erp_sale_order_source_project_id` (`source_project_id`)',
    'SELECT 1'
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_sale_order'
    AND INDEX_NAME = 'idx_erp_sale_order_source_project_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;
