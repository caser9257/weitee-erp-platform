SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `erp_mrp_netting_policy` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(32) NOT NULL,
  `name` varchar(64) NOT NULL,
  `version` int NOT NULL DEFAULT 1,
  `enable_flag` bit(1) NOT NULL DEFAULT b'1',
  `default_flag` bit(1) NOT NULL DEFAULT b'0',
  `remark` varchar(255) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_mrp_netting_policy_tenant_code` (`code`),
  KEY `idx_erp_mrp_netting_policy_default` (`default_flag`, `enable_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `erp_mrp_netting_policy_line` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `policy_id` bigint NOT NULL,
  `component_code` varchar(32) NOT NULL,
  `component_role` varchar(32) NOT NULL,
  `enable_flag` bit(1) NOT NULL DEFAULT b'1',
  `sequence_no` int NOT NULL DEFAULT 0,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_mrp_netting_policy_line` (`policy_id`, `component_code`),
  KEY `idx_erp_mrp_netting_policy_line_policy` (`policy_id`, `sequence_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `erp_mrp_policy_binding` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `policy_id` bigint NOT NULL,
  `business_type` varchar(32) NOT NULL,
  `enable_flag` bit(1) NOT NULL DEFAULT b'1',
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_mrp_policy_binding_tenant_business_type` (`business_type`),
  KEY `idx_erp_mrp_policy_binding_policy` (`policy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `erp_mrp_result_component` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plan_id` bigint NOT NULL,
  `result_id` bigint NOT NULL,
  `material_id` bigint NOT NULL,
  `component_code` varchar(32) NOT NULL,
  `component_name` varchar(64) NOT NULL,
  `component_role` varchar(32) NOT NULL,
  `sequence_no` int DEFAULT NULL,
  `enable_flag` bit(1) NOT NULL DEFAULT b'1',
  `base_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
  `consumed_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
  `remaining_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `idx_erp_mrp_result_component_plan_result` (`plan_id`, `result_id`),
  KEY `idx_erp_mrp_result_component_result` (`result_id`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
      ADD COLUMN `policy_code` varchar(32) DEFAULT NULL COMMENT ''净需求策略编码'' AFTER `net_demand_qty`'
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
      ADD COLUMN `policy_version` int DEFAULT NULL COMMENT ''净需求策略版本'' AFTER `policy_code`'
  )
);
PREPARE stmt FROM @add_mrp_result_policy_version;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO `erp_mrp_netting_policy` (
  `code`,
  `name`,
  `version`,
  `enable_flag`,
  `default_flag`,
  `remark`,
  `creator`,
  `updater`
)
SELECT
  'STANDARD_V1',
  '标准净需求策略',
  1,
  b'1',
  b'1',
  '兼容现有轻量 MRP 默认口径',
  '',
  ''
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1
  FROM `erp_mrp_netting_policy`
  WHERE `code` = 'STANDARD_V1'
    AND `deleted` = b'0'
);

SET @standard_policy_id = (
  SELECT `id`
  FROM `erp_mrp_netting_policy`
  WHERE `code` = 'STANDARD_V1'
    AND `deleted` = b'0'
  ORDER BY `id` DESC
  LIMIT 1
);

INSERT INTO `erp_mrp_netting_policy_line` (
  `policy_id`,
  `component_code`,
  `component_role`,
  `enable_flag`,
  `sequence_no`,
  `creator`,
  `updater`
)
SELECT @standard_policy_id, 'SAFETY_STOCK', 'DEMAND_ADJUST', b'1', 0, '', ''
FROM DUAL
WHERE @standard_policy_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `erp_mrp_netting_policy_line`
    WHERE `policy_id` = @standard_policy_id
      AND `component_code` = 'SAFETY_STOCK'
      AND `deleted` = b'0'
  );

INSERT INTO `erp_mrp_netting_policy_line` (
  `policy_id`,
  `component_code`,
  `component_role`,
  `enable_flag`,
  `sequence_no`,
  `creator`,
  `updater`
)
SELECT @standard_policy_id, 'INCOMING_PURCHASE', 'SUPPLY_CONSUME', b'1', 10, '', ''
FROM DUAL
WHERE @standard_policy_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `erp_mrp_netting_policy_line`
    WHERE `policy_id` = @standard_policy_id
      AND `component_code` = 'INCOMING_PURCHASE'
      AND `deleted` = b'0'
  );

INSERT INTO `erp_mrp_netting_policy_line` (
  `policy_id`,
  `component_code`,
  `component_role`,
  `enable_flag`,
  `sequence_no`,
  `creator`,
  `updater`
)
SELECT @standard_policy_id, 'WIP_PRODUCTION', 'SUPPLY_CONSUME', b'1', 20, '', ''
FROM DUAL
WHERE @standard_policy_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `erp_mrp_netting_policy_line`
    WHERE `policy_id` = @standard_policy_id
      AND `component_code` = 'WIP_PRODUCTION'
      AND `deleted` = b'0'
  );

INSERT INTO `erp_mrp_netting_policy_line` (
  `policy_id`,
  `component_code`,
  `component_role`,
  `enable_flag`,
  `sequence_no`,
  `creator`,
  `updater`
)
SELECT @standard_policy_id, 'ON_HAND_AVAILABLE', 'SUPPLY_CONSUME', b'1', 30, '', ''
FROM DUAL
WHERE @standard_policy_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `erp_mrp_netting_policy_line`
    WHERE `policy_id` = @standard_policy_id
      AND `component_code` = 'ON_HAND_AVAILABLE'
      AND `deleted` = b'0'
  );

UPDATE `erp_mrp_result`
SET `policy_code` = 'STANDARD_V1',
    `policy_version` = 1
WHERE (`policy_code` IS NULL OR `policy_code` = '')
  AND `deleted` = b'0';

SET FOREIGN_KEY_CHECKS = 1;
