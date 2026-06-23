SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
