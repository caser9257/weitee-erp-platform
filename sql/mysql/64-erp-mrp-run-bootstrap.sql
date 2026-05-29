/*
 Target: ERP MRP run bootstrap
 Schema: ruoyi-vue-pro
 Date: 2026-04-23
 Purpose:
   - Bootstrap the minimum missing schema used by clicking "运行计划"
   - Cover stock reservation summary, result component, and current result view fields
   - Safe to run repeatedly
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @create_stock_reservation_summary = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.TABLES
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_stock_reservation_summary'
    ),
    'SELECT 1',
    'CREATE TABLE `erp_mrp_stock_reservation_summary` (
      `id` bigint NOT NULL AUTO_INCREMENT,
      `product_id` bigint NOT NULL,
      `active_reserved_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
      `active_project_count` int NOT NULL DEFAULT 0,
      `active_reservation_count` int NOT NULL DEFAULT 0,
      `last_reserved_time` datetime DEFAULT NULL,
      `version` bigint NOT NULL DEFAULT 0,
      `creator` varchar(64) DEFAULT '''',
      `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
      `updater` varchar(64) DEFAULT '''',
      `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      `deleted` bit(1) NOT NULL DEFAULT b''0'',
      `tenant_id` bigint NOT NULL DEFAULT 0,
      PRIMARY KEY (`id`),
      UNIQUE KEY `uk_erp_mrp_stock_reservation_summary_tenant_product` (`tenant_id`, `product_id`),
      KEY `idx_erp_mrp_stock_reservation_summary_qty` (`tenant_id`, `active_reserved_qty`),
      KEY `idx_erp_mrp_stock_reservation_summary_last_time` (`tenant_id`, `last_reserved_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4'
  )
);
PREPARE stmt FROM @create_stock_reservation_summary;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_stock_reservation_tenant_product_status = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_stock_reservation'
        AND INDEX_NAME = 'idx_erp_mrp_stock_reservation_tenant_product_status'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_stock_reservation`
      ADD INDEX `idx_erp_mrp_stock_reservation_tenant_product_status` (`tenant_id`, `product_id`, `status`)'
  )
);
PREPARE stmt FROM @add_stock_reservation_tenant_product_status;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_stock_reservation_tenant_source_order_status = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_stock_reservation'
        AND INDEX_NAME = 'idx_erp_mrp_stock_reservation_tenant_source_order_status'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_stock_reservation`
      ADD INDEX `idx_erp_mrp_stock_reservation_tenant_source_order_status` (`tenant_id`, `source_order_id`, `status`)'
  )
);
PREPARE stmt FROM @add_stock_reservation_tenant_source_order_status;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_stock_reservation_tenant_plan_id = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_stock_reservation'
        AND INDEX_NAME = 'idx_erp_mrp_stock_reservation_tenant_plan_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_stock_reservation`
      ADD INDEX `idx_erp_mrp_stock_reservation_tenant_plan_id` (`tenant_id`, `plan_id`, `id`)'
  )
);
PREPARE stmt FROM @add_stock_reservation_tenant_plan_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_stock_reservation_tenant_project_id = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_stock_reservation'
        AND INDEX_NAME = 'idx_erp_mrp_stock_reservation_tenant_project_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_stock_reservation`
      ADD INDEX `idx_erp_mrp_stock_reservation_tenant_project_id` (`tenant_id`, `project_id`, `id`)'
  )
);
PREPARE stmt FROM @add_stock_reservation_tenant_project_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @create_result_component = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.TABLES
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result_component'
    ),
    'SELECT 1',
    'CREATE TABLE `erp_mrp_result_component` (
      `id` bigint NOT NULL AUTO_INCREMENT,
      `plan_id` bigint NOT NULL,
      `result_id` bigint NOT NULL,
      `material_id` bigint NOT NULL,
      `component_code` varchar(32) NOT NULL,
      `component_name` varchar(64) DEFAULT NULL,
      `component_role` varchar(32) DEFAULT NULL,
      `sequence_no` int NOT NULL DEFAULT 0,
      `enable_flag` tinyint(1) NOT NULL DEFAULT 1,
      `base_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
      `consumed_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
      `remaining_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
      `creator` varchar(64) DEFAULT '''',
      `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
      `updater` varchar(64) DEFAULT '''',
      `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      `deleted` bit(1) NOT NULL DEFAULT b''0'',
      `tenant_id` bigint NOT NULL DEFAULT 0,
      PRIMARY KEY (`id`),
      KEY `idx_erp_mrp_result_component_plan_result` (`tenant_id`, `plan_id`, `result_id`),
      KEY `idx_erp_mrp_result_component_result` (`tenant_id`, `result_id`, `id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4'
  )
);
PREPARE stmt FROM @create_result_component;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @create_netting_policy = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.TABLES
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_netting_policy'
    ),
    'SELECT 1',
    'CREATE TABLE `erp_mrp_netting_policy` (
      `id` bigint NOT NULL AUTO_INCREMENT,
      `code` varchar(32) NOT NULL,
      `name` varchar(64) NOT NULL,
      `version` int NOT NULL DEFAULT 1,
      `enable_flag` tinyint(1) NOT NULL DEFAULT 1,
      `default_flag` tinyint(1) NOT NULL DEFAULT 0,
      `description` varchar(255) DEFAULT NULL,
      `creator` varchar(64) DEFAULT '''',
      `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
      `updater` varchar(64) DEFAULT '''',
      `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      `deleted` bit(1) NOT NULL DEFAULT b''0'',
      `tenant_id` bigint NOT NULL DEFAULT 0,
      PRIMARY KEY (`id`),
      UNIQUE KEY `uk_erp_mrp_netting_policy_tenant_code` (`tenant_id`, `code`),
      KEY `idx_erp_mrp_netting_policy_default` (`tenant_id`, `default_flag`, `enable_flag`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4'
  )
);
PREPARE stmt FROM @create_netting_policy;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @create_netting_policy_line = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.TABLES
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_netting_policy_line'
    ),
    'SELECT 1',
    'CREATE TABLE `erp_mrp_netting_policy_line` (
      `id` bigint NOT NULL AUTO_INCREMENT,
      `policy_id` bigint NOT NULL,
      `component_code` varchar(32) NOT NULL,
      `sequence_no` int NOT NULL DEFAULT 0,
      `enable_flag` tinyint(1) NOT NULL DEFAULT 1,
      `creator` varchar(64) DEFAULT '''',
      `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
      `updater` varchar(64) DEFAULT '''',
      `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      `deleted` bit(1) NOT NULL DEFAULT b''0'',
      `tenant_id` bigint NOT NULL DEFAULT 0,
      PRIMARY KEY (`id`),
      UNIQUE KEY `uk_erp_mrp_netting_policy_line` (`tenant_id`, `policy_id`, `component_code`),
      KEY `idx_erp_mrp_netting_policy_line_policy` (`tenant_id`, `policy_id`, `sequence_no`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4'
  )
);
PREPARE stmt FROM @create_netting_policy_line;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @create_policy_binding = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.TABLES
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_policy_binding'
    ),
    'SELECT 1',
    'CREATE TABLE `erp_mrp_policy_binding` (
      `id` bigint NOT NULL AUTO_INCREMENT,
      `policy_id` bigint NOT NULL,
      `business_type` varchar(32) NOT NULL,
      `creator` varchar(64) DEFAULT '''',
      `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
      `updater` varchar(64) DEFAULT '''',
      `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      `deleted` bit(1) NOT NULL DEFAULT b''0'',
      `tenant_id` bigint NOT NULL DEFAULT 0,
      PRIMARY KEY (`id`),
      UNIQUE KEY `uk_erp_mrp_policy_binding_tenant_business_type` (`tenant_id`, `business_type`),
      KEY `idx_erp_mrp_policy_binding_policy` (`tenant_id`, `policy_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4'
  )
);
PREPARE stmt FROM @create_policy_binding;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_result_policy_code = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'policy_code'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result`
      ADD COLUMN `policy_code` varchar(32) DEFAULT NULL COMMENT ''netting policy code'' AFTER `skip_reason`'
  )
);
PREPARE stmt FROM @add_mrp_result_policy_code;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_result_policy_version = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'policy_version'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result`
      ADD COLUMN `policy_version` int DEFAULT NULL COMMENT ''netting policy version'' AFTER `policy_code`'
  )
);
PREPARE stmt FROM @add_mrp_result_policy_version;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_result_reserved_stock_qty = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'reserved_stock_qty'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result`
      ADD COLUMN `reserved_stock_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT ''reserved stock qty'' AFTER `wip_qty`'
  )
);
PREPARE stmt FROM @add_mrp_result_reserved_stock_qty;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_result_business_type = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'business_type'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result`
      ADD COLUMN `business_type` varchar(32) DEFAULT NULL COMMENT ''business type'' AFTER `net_demand_qty`'
  )
);
PREPARE stmt FROM @add_mrp_result_business_type;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_result_mrp_enable_flag = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'mrp_enable_flag'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result`
      ADD COLUMN `mrp_enable_flag` tinyint(1) DEFAULT NULL COMMENT ''mrp enable flag'' AFTER `business_type`'
  )
);
PREPARE stmt FROM @add_mrp_result_mrp_enable_flag;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_result_supply_owner = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'supply_owner'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result`
      ADD COLUMN `supply_owner` varchar(32) DEFAULT NULL COMMENT ''supply owner'' AFTER `mrp_enable_flag`'
  )
);
PREPARE stmt FROM @add_mrp_result_supply_owner;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_result_skip_reason = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'skip_reason'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result`
      ADD COLUMN `skip_reason` varchar(64) DEFAULT NULL COMMENT ''skip reason'' AFTER `supply_owner`'
  )
);
PREPARE stmt FROM @add_mrp_result_skip_reason;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_result_source_item_id = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'source_item_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result`
      ADD COLUMN `source_item_id` bigint DEFAULT NULL COMMENT ''source item id'' AFTER `source_order_id`'
  )
);
PREPARE stmt FROM @add_mrp_result_source_item_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_result_demand_date = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_result'
        AND COLUMN_NAME = 'demand_date'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_result`
      ADD COLUMN `demand_date` date DEFAULT NULL COMMENT ''demand date'' AFTER `source_item_id`'
  )
);
PREPARE stmt FROM @add_mrp_result_demand_date;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_mrp_shortage_source_item_id = (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_mrp_shortage'
        AND COLUMN_NAME = 'source_item_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_mrp_shortage`
      ADD COLUMN `source_item_id` bigint DEFAULT NULL COMMENT ''source item id'' AFTER `source_order_id`'
  )
);
PREPARE stmt FROM @add_mrp_shortage_source_item_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO `erp_mrp_netting_policy` (
  `code`, `name`, `version`, `enable_flag`, `default_flag`, `description`, `creator`, `updater`, `tenant_id`
)
SELECT
  'STANDARD_V1', '标准净需求策略', 1, 1, 1, '默认 MRP 净需求策略', '', '', 1
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `erp_mrp_netting_policy`
  WHERE `tenant_id` = 1 AND `code` = 'STANDARD_V1' AND `deleted` = b'0'
);

SET @default_policy_id = (
  SELECT `id` FROM `erp_mrp_netting_policy`
  WHERE `tenant_id` = 1 AND `code` = 'STANDARD_V1' AND `deleted` = b'0'
  ORDER BY `id` DESC LIMIT 1
);

INSERT INTO `erp_mrp_netting_policy_line` (
  `policy_id`, `component_code`, `sequence_no`, `enable_flag`, `creator`, `updater`, `tenant_id`
)
SELECT @default_policy_id, 'ON_HAND_AVAILABLE', 10, 1, '', '', 1
FROM DUAL
WHERE @default_policy_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `erp_mrp_netting_policy_line`
    WHERE `tenant_id` = 1 AND `policy_id` = @default_policy_id
      AND `component_code` = 'ON_HAND_AVAILABLE' AND `deleted` = b'0'
  );

INSERT INTO `erp_mrp_netting_policy_line` (
  `policy_id`, `component_code`, `sequence_no`, `enable_flag`, `creator`, `updater`, `tenant_id`
)
SELECT @default_policy_id, 'INCOMING_PURCHASE', 20, 1, '', '', 1
FROM DUAL
WHERE @default_policy_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `erp_mrp_netting_policy_line`
    WHERE `tenant_id` = 1 AND `policy_id` = @default_policy_id
      AND `component_code` = 'INCOMING_PURCHASE' AND `deleted` = b'0'
  );

INSERT INTO `erp_mrp_netting_policy_line` (
  `policy_id`, `component_code`, `sequence_no`, `enable_flag`, `creator`, `updater`, `tenant_id`
)
SELECT @default_policy_id, 'WIP_PRODUCTION', 30, 1, '', '', 1
FROM DUAL
WHERE @default_policy_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `erp_mrp_netting_policy_line`
    WHERE `tenant_id` = 1 AND `policy_id` = @default_policy_id
      AND `component_code` = 'WIP_PRODUCTION' AND `deleted` = b'0'
  );

INSERT INTO `erp_mrp_policy_binding` (
  `policy_id`, `business_type`, `creator`, `updater`, `tenant_id`
)
SELECT @default_policy_id, 'SELF_RESEARCH', '', '', 1
FROM DUAL
WHERE @default_policy_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `erp_mrp_policy_binding`
    WHERE `tenant_id` = 1 AND `business_type` = 'SELF_RESEARCH' AND `deleted` = b'0'
  );

INSERT INTO `erp_mrp_policy_binding` (
  `policy_id`, `business_type`, `creator`, `updater`, `tenant_id`
)
SELECT @default_policy_id, 'CUSTOMER_SUPPLIED', '', '', 1
FROM DUAL
WHERE @default_policy_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `erp_mrp_policy_binding`
    WHERE `tenant_id` = 1 AND `business_type` = 'CUSTOMER_SUPPLIED' AND `deleted` = b'0'
  );

INSERT INTO `erp_mrp_policy_binding` (
  `policy_id`, `business_type`, `creator`, `updater`, `tenant_id`
)
SELECT @default_policy_id, 'TOLL_MANUFACTURING', '', '', 1
FROM DUAL
WHERE @default_policy_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `erp_mrp_policy_binding`
    WHERE `tenant_id` = 1 AND `business_type` = 'TOLL_MANUFACTURING' AND `deleted` = b'0'
  );

INSERT INTO `erp_mrp_stock_reservation_summary` (
  `product_id`,
  `active_reserved_qty`,
  `active_project_count`,
  `active_reservation_count`,
  `last_reserved_time`,
  `version`,
  `creator`,
  `updater`,
  `tenant_id`
)
SELECT
  `product_id`,
  SUM(`reserved_qty`) AS `active_reserved_qty`,
  COUNT(DISTINCT `project_id`) AS `active_project_count`,
  COUNT(*) AS `active_reservation_count`,
  MAX(`create_time`) AS `last_reserved_time`,
  1 AS `version`,
  '' AS `creator`,
  '' AS `updater`,
  `tenant_id`
FROM `erp_mrp_stock_reservation`
WHERE `deleted` = b'0'
  AND `status` = 0
GROUP BY `tenant_id`, `product_id`
ON DUPLICATE KEY UPDATE
  `active_reserved_qty` = VALUES(`active_reserved_qty`),
  `active_project_count` = VALUES(`active_project_count`),
  `active_reservation_count` = VALUES(`active_reservation_count`),
  `last_reserved_time` = VALUES(`last_reserved_time`),
  `version` = `version` + 1,
  `update_time` = CURRENT_TIMESTAMP;

SET FOREIGN_KEY_CHECKS = 1;
