/*
 Target: ERP MRP traceable shortage notification
 Schema: ruoyi-vue-pro
 Date: 2026-05-20
 Purpose:
   - Add trace node storage for MRP BOM explosion
   - Add trace reference fields to result / shortage / purchase suggest / production suggest
   - Safe to run repeatedly
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


SET @create_mrp_trace_node = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.TABLES
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_trace_node'
    ),
    'SELECT 1',
    'CREATE TABLE `erp_mrp_trace_node` (
      `id` bigint NOT NULL AUTO_INCREMENT,
      `plan_id` bigint NOT NULL,
      `root_product_id` bigint NOT NULL,
      `parent_trace_node_id` bigint DEFAULT NULL,
      `parent_material_id` bigint DEFAULT NULL,
      `trace_level` int NOT NULL DEFAULT 0,
      `material_id` bigint NOT NULL,
      `bom_id` bigint DEFAULT NULL,
      `bom_item_id` bigint DEFAULT NULL,
      `trace_path_key` varchar(512) NOT NULL,
      `project_id` bigint DEFAULT NULL,
      `source_order_id` bigint DEFAULT NULL,
      `source_item_id` bigint DEFAULT NULL,
      `gross_demand_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
      `available_stock_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
      `incoming_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
      `wip_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
      `reserved_stock_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
      `safety_stock_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
      `theoretical_net_demand_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
      `execution_net_demand_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
      `policy_code` varchar(32) DEFAULT NULL,
      `policy_version` int DEFAULT NULL,
      `business_type` varchar(32) DEFAULT NULL,
      `mrp_enable_flag` tinyint(1) DEFAULT NULL,
      `supply_owner` varchar(32) DEFAULT NULL,
      `suggest_type` varchar(16) DEFAULT NULL,
      `skip_reason` varchar(64) DEFAULT NULL,
      `suggest_date` date DEFAULT NULL,
      `demand_date` date DEFAULT NULL,
      `creator` varchar(64) DEFAULT '''',
      `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
      `updater` varchar(64) DEFAULT '''',
      `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      `deleted` bit(1) NOT NULL DEFAULT b''0'',
      PRIMARY KEY (`id`),
      UNIQUE KEY `uk_erp_mrp_trace_node_tenant_plan_path` (`plan_id`, `trace_path_key`),
      KEY `idx_erp_mrp_trace_node_tenant_plan` (`plan_id`, `id`),
      KEY `idx_erp_mrp_trace_node_tenant_parent` (`parent_trace_node_id`, `id`),
      KEY `idx_erp_mrp_trace_node_tenant_material` (`material_id`, `id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4'
  )
);
PREPARE stmt FROM @create_mrp_trace_node;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_result_trace_node_id = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'trace_node_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result`
      ADD COLUMN `trace_node_id` bigint DEFAULT NULL COMMENT ''trace node id'' AFTER `id`'
  )
);
PREPARE stmt FROM @add_mrp_result_trace_node_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_result_trace_path_key = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'trace_path_key'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result`
      ADD COLUMN `trace_path_key` varchar(1024) DEFAULT NULL COMMENT ''trace path key'' AFTER `trace_node_id`'
  )
);
PREPARE stmt FROM @add_mrp_result_trace_path_key;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_result_trace_level = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'trace_level'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result`
      ADD COLUMN `trace_level` int DEFAULT NULL COMMENT ''trace level'' AFTER `trace_path_key`'
  )
);
PREPARE stmt FROM @add_mrp_result_trace_level;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_result_parent_material_id = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'parent_material_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result`
      ADD COLUMN `parent_material_id` bigint DEFAULT NULL COMMENT ''parent material id'' AFTER `trace_level`'
  )
);
PREPARE stmt FROM @add_mrp_result_parent_material_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_result_bom_item_id = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'bom_item_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result`
      ADD COLUMN `bom_item_id` bigint DEFAULT NULL COMMENT ''bom item id'' AFTER `parent_material_id`'
  )
);
PREPARE stmt FROM @add_mrp_result_bom_item_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_shortage_trace_node_id = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_shortage'
        AND COLUMN_NAME = 'trace_node_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_shortage`
      ADD COLUMN `trace_node_id` bigint DEFAULT NULL COMMENT ''trace node id'' AFTER `id`'
  )
);
PREPARE stmt FROM @add_mrp_shortage_trace_node_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_shortage_trace_path_key = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_shortage'
        AND COLUMN_NAME = 'trace_path_key'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_shortage`
      ADD COLUMN `trace_path_key` varchar(1024) DEFAULT NULL COMMENT ''trace path key'' AFTER `trace_node_id`'
  )
);
PREPARE stmt FROM @add_mrp_shortage_trace_path_key;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_shortage_trace_level = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_shortage'
        AND COLUMN_NAME = 'trace_level'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_shortage`
      ADD COLUMN `trace_level` int DEFAULT NULL COMMENT ''trace level'' AFTER `trace_path_key`'
  )
);
PREPARE stmt FROM @add_mrp_shortage_trace_level;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_shortage_parent_material_id = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_shortage'
        AND COLUMN_NAME = 'parent_material_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_shortage`
      ADD COLUMN `parent_material_id` bigint DEFAULT NULL COMMENT ''parent material id'' AFTER `trace_level`'
  )
);
PREPARE stmt FROM @add_mrp_shortage_parent_material_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_shortage_bom_item_id = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_shortage'
        AND COLUMN_NAME = 'bom_item_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_shortage`
      ADD COLUMN `bom_item_id` bigint DEFAULT NULL COMMENT ''bom item id'' AFTER `parent_material_id`'
  )
);
PREPARE stmt FROM @add_mrp_shortage_bom_item_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_purchase_suggest_trace_node_id = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_purchase_suggest'
        AND COLUMN_NAME = 'trace_node_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_suggest`
      ADD COLUMN `trace_node_id` bigint DEFAULT NULL COMMENT ''trace node id'' AFTER `id`'
  )
);
PREPARE stmt FROM @add_purchase_suggest_trace_node_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_purchase_suggest_trace_path_key = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_purchase_suggest'
        AND COLUMN_NAME = 'trace_path_key'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_suggest`
      ADD COLUMN `trace_path_key` varchar(1024) DEFAULT NULL COMMENT ''trace path key'' AFTER `trace_node_id`'
  )
);
PREPARE stmt FROM @add_purchase_suggest_trace_path_key;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_purchase_suggest_trace_level = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_purchase_suggest'
        AND COLUMN_NAME = 'trace_level'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_suggest`
      ADD COLUMN `trace_level` int DEFAULT NULL COMMENT ''trace level'' AFTER `trace_path_key`'
  )
);
PREPARE stmt FROM @add_purchase_suggest_trace_level;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_purchase_suggest_parent_material_id = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_purchase_suggest'
        AND COLUMN_NAME = 'parent_material_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_suggest`
      ADD COLUMN `parent_material_id` bigint DEFAULT NULL COMMENT ''parent material id'' AFTER `trace_level`'
  )
);
PREPARE stmt FROM @add_purchase_suggest_parent_material_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_purchase_suggest_bom_item_id = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_purchase_suggest'
        AND COLUMN_NAME = 'bom_item_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_suggest`
      ADD COLUMN `bom_item_id` bigint DEFAULT NULL COMMENT ''bom item id'' AFTER `parent_material_id`'
  )
);
PREPARE stmt FROM @add_purchase_suggest_bom_item_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_production_suggest_trace_node_id = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_suggest'
        AND COLUMN_NAME = 'trace_node_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_suggest`
      ADD COLUMN `trace_node_id` bigint DEFAULT NULL COMMENT ''trace node id'' AFTER `id`'
  )
);
PREPARE stmt FROM @add_production_suggest_trace_node_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_production_suggest_trace_path_key = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_suggest'
        AND COLUMN_NAME = 'trace_path_key'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_suggest`
      ADD COLUMN `trace_path_key` varchar(1024) DEFAULT NULL COMMENT ''trace path key'' AFTER `trace_node_id`'
  )
);
PREPARE stmt FROM @add_production_suggest_trace_path_key;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_production_suggest_trace_level = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_suggest'
        AND COLUMN_NAME = 'trace_level'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_suggest`
      ADD COLUMN `trace_level` int DEFAULT NULL COMMENT ''trace level'' AFTER `trace_path_key`'
  )
);
PREPARE stmt FROM @add_production_suggest_trace_level;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_production_suggest_parent_material_id = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_suggest'
        AND COLUMN_NAME = 'parent_material_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_suggest`
      ADD COLUMN `parent_material_id` bigint DEFAULT NULL COMMENT ''parent material id'' AFTER `trace_level`'
  )
);
PREPARE stmt FROM @add_production_suggest_parent_material_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_production_suggest_bom_item_id = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_production_suggest'
        AND COLUMN_NAME = 'bom_item_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_production_suggest`
      ADD COLUMN `bom_item_id` bigint DEFAULT NULL COMMENT ''bom item id'' AFTER `parent_material_id`'
  )
);
PREPARE stmt FROM @add_production_suggest_bom_item_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;
