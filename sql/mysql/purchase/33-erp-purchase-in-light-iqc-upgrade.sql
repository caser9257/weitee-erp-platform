-- Light IQC incremental upgrade
-- Switch to your target database before running:
-- USE `ruoyi-vue-pro`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP PROCEDURE IF EXISTS erp_add_column_if_missing;
DROP PROCEDURE IF EXISTS erp_add_index_if_missing;

DELIMITER $$

CREATE PROCEDURE erp_add_column_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND COLUMN_NAME = p_column_name
    ) THEN
        SET @ddl = CONCAT(
            'ALTER TABLE `', p_table_name, '` ADD COLUMN `', p_column_name, '` ', p_definition
        );
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

CREATE PROCEDURE erp_add_index_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64),
    IN p_index_ddl TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND INDEX_NAME = p_index_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ', p_index_ddl);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

DELIMITER ;

-- 1. Upgrade main quality table
CALL erp_add_column_if_missing(
    'erp_purchase_in_quality',
    'current_round_no',
    'int NOT NULL DEFAULT 1 COMMENT ''current round no'' AFTER `result`'
);
CALL erp_add_column_if_missing(
    'erp_purchase_in_quality',
    'recheck_required',
    'bit(1) NOT NULL DEFAULT b''0'' COMMENT ''need recheck'' AFTER `current_round_no`'
);
CALL erp_add_column_if_missing(
    'erp_purchase_in_quality',
    'recheck_reason',
    'varchar(255) DEFAULT NULL COMMENT ''recheck reason'' AFTER `recheck_required`'
);
CALL erp_add_column_if_missing(
    'erp_purchase_in_quality',
    'recheck_apply_user_id',
    'bigint DEFAULT NULL COMMENT ''recheck applicant'' AFTER `recheck_reason`'
);
CALL erp_add_column_if_missing(
    'erp_purchase_in_quality',
    'recheck_apply_time',
    'datetime DEFAULT NULL COMMENT ''recheck apply time'' AFTER `recheck_apply_user_id`'
);
CALL erp_add_column_if_missing(
    'erp_purchase_in_quality',
    'sampling_scheme_id',
    'bigint DEFAULT NULL COMMENT ''sampling scheme id'' AFTER `recheck_apply_time`'
);
CALL erp_add_column_if_missing(
    'erp_purchase_in_quality',
    'sampling_scheme_name',
    'varchar(64) DEFAULT NULL COMMENT ''sampling scheme name'' AFTER `sampling_scheme_id`'
);
CALL erp_add_column_if_missing(
    'erp_purchase_in_quality',
    'sampling_scheme_type',
    'int DEFAULT NULL COMMENT ''sampling scheme type'' AFTER `sampling_scheme_name`'
);
CALL erp_add_column_if_missing(
    'erp_purchase_in_quality',
    'sampling_ratio',
    'decimal(8,4) DEFAULT NULL COMMENT ''sampling ratio'' AFTER `sampling_scheme_type`'
);
CALL erp_add_column_if_missing(
    'erp_purchase_in_quality',
    'sampling_fixed_count',
    'decimal(24,6) DEFAULT NULL COMMENT ''fixed sample count'' AFTER `sampling_ratio`'
);
CALL erp_add_column_if_missing(
    'erp_purchase_in_quality',
    'min_sample_count',
    'decimal(24,6) DEFAULT NULL COMMENT ''min sample count'' AFTER `sampling_fixed_count`'
);
CALL erp_add_column_if_missing(
    'erp_purchase_in_quality',
    'max_sample_count',
    'decimal(24,6) DEFAULT NULL COMMENT ''max sample count'' AFTER `min_sample_count`'
);
CALL erp_add_index_if_missing(
    'erp_purchase_in_quality',
    'idx_purchase_in_quality_round_no',
    'ADD KEY `idx_purchase_in_quality_round_no` (`current_round_no`)'
);

-- Old status migration: old 30/40 => new 50/60
UPDATE `erp_purchase_in_quality` SET `status` = 50 WHERE `status` = 30;
UPDATE `erp_purchase_in_quality` SET `status` = 60 WHERE `status` = 40;

-- 2. Upgrade quality item table
CALL erp_add_column_if_missing(
    'erp_purchase_in_quality_item',
    'sample_count',
    'decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT ''sample count'' AFTER `count`'
);
UPDATE `erp_purchase_in_quality_item`
SET `sample_count` = `count`
WHERE (`sample_count` IS NULL OR `sample_count` = 0)
  AND `deleted` = b'0';

-- 3. Round table
CREATE TABLE IF NOT EXISTS `erp_purchase_in_quality_round` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `quality_id` bigint NOT NULL COMMENT 'quality id',
  `quality_item_id` bigint NOT NULL COMMENT 'quality item id',
  `purchase_in_item_id` bigint NOT NULL COMMENT 'purchase in item id',
  `round_no` int NOT NULL COMMENT 'round no',
  `round_type` int NOT NULL COMMENT 'round type',
  `sample_count` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT 'sample count',
  `pass_count` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT 'pass count',
  `reject_count` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT 'reject count',
  `result` int NOT NULL COMMENT 'round result',
  `checker_user_id` bigint DEFAULT NULL COMMENT 'checker user id',
  `check_time` datetime DEFAULT NULL COMMENT 'check time',
  `remark` varchar(500) DEFAULT NULL COMMENT 'remark',
  `creator` varchar(64) DEFAULT '' COMMENT 'creator',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `updater` varchar(64) DEFAULT '' COMMENT 'updater',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_purchase_in_quality_round_item_no` (`quality_item_id`, `round_no`),
  KEY `idx_purchase_in_quality_round_quality_id` (`quality_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='purchase in quality round';

-- 4. Defect table
CREATE TABLE IF NOT EXISTS `erp_purchase_in_quality_defect` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `quality_id` bigint NOT NULL COMMENT 'quality id',
  `round_id` bigint NOT NULL COMMENT 'round id',
  `quality_item_id` bigint NOT NULL COMMENT 'quality item id',
  `purchase_in_item_id` bigint NOT NULL COMMENT 'purchase in item id',
  `defect_reason_id` bigint NOT NULL COMMENT 'defect reason id',
  `defect_reason_name` varchar(64) NOT NULL COMMENT 'defect reason name',
  `defect_count` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT 'defect count',
  `remark` varchar(255) DEFAULT NULL COMMENT 'remark',
  `creator` varchar(64) DEFAULT '' COMMENT 'creator',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `updater` varchar(64) DEFAULT '' COMMENT 'updater',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
  PRIMARY KEY (`id`),
  KEY `idx_purchase_in_quality_defect_quality_id` (`quality_id`),
  KEY `idx_purchase_in_quality_defect_round_id` (`round_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='purchase in quality defect';

-- 5. Sampling scheme master table
CREATE TABLE IF NOT EXISTS `erp_qc_sampling_scheme` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(64) NOT NULL COMMENT 'name',
  `scheme_type` int NOT NULL COMMENT 'scheme type',
  `supplier_id` bigint DEFAULT NULL COMMENT 'supplier id',
  `product_id` bigint DEFAULT NULL COMMENT 'product id',
  `sample_ratio` decimal(8,4) DEFAULT NULL COMMENT 'sample ratio',
  `sample_fixed_count` decimal(24,6) DEFAULT NULL COMMENT 'sample fixed count',
  `min_sample_count` decimal(24,6) DEFAULT NULL COMMENT 'min sample count',
  `max_sample_count` decimal(24,6) DEFAULT NULL COMMENT 'max sample count',
  `priority` int NOT NULL DEFAULT '0' COMMENT 'priority',
  `is_default` bit(1) NOT NULL DEFAULT b'0' COMMENT 'is default',
  `status` int NOT NULL DEFAULT '0' COMMENT 'status',
  `remark` varchar(255) DEFAULT NULL COMMENT 'remark',
  `creator` varchar(64) DEFAULT '' COMMENT 'creator',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `updater` varchar(64) DEFAULT '' COMMENT 'updater',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
  PRIMARY KEY (`id`),
  KEY `idx_qc_sampling_scheme_supplier_product` (`supplier_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='qc sampling scheme';

-- 6. Defect reason master table
CREATE TABLE IF NOT EXISTS `erp_qc_defect_reason` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `code` varchar(32) NOT NULL COMMENT 'code',
  `name` varchar(64) NOT NULL COMMENT 'name',
  `sort` int NOT NULL DEFAULT '0' COMMENT 'sort',
  `status` int NOT NULL DEFAULT '0' COMMENT 'status',
  `remark` varchar(255) DEFAULT NULL COMMENT 'remark',
  `creator` varchar(64) DEFAULT '' COMMENT 'creator',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `updater` varchar(64) DEFAULT '' COMMENT 'updater',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT 'deleted',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_qc_defect_reason_code` (`code`),
  UNIQUE KEY `uk_qc_defect_reason_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='qc defect reason';

-- 7. Seed data
INSERT INTO `erp_qc_sampling_scheme`
(`name`, `scheme_type`, `priority`, `is_default`, `status`, `remark`, `creator`, `updater`)
SELECT 'DEFAULT_FULL_CHECK', 10, 0, b'1', 0, 'seed', 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM `erp_qc_sampling_scheme`
    WHERE `name` = 'DEFAULT_FULL_CHECK' AND `deleted` = b'0'
);

INSERT INTO `erp_qc_defect_reason`
(`code`, `name`, `sort`, `status`, `remark`, `creator`, `updater`)
SELECT 'SIZE', 'SizeIssue', 10, 0, 'seed', 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM `erp_qc_defect_reason`
    WHERE `code` = 'SIZE' AND `deleted` = b'0'
);

INSERT INTO `erp_qc_defect_reason`
(`code`, `name`, `sort`, `status`, `remark`, `creator`, `updater`)
SELECT 'LOOK', 'AppearanceIssue', 20, 0, 'seed', 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM `erp_qc_defect_reason`
    WHERE `code` = 'LOOK' AND `deleted` = b'0'
);

INSERT INTO `erp_qc_defect_reason`
(`code`, `name`, `sort`, `status`, `remark`, `creator`, `updater`)
SELECT 'FUNC', 'FunctionIssue', 30, 0, 'seed', 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM `erp_qc_defect_reason`
    WHERE `code` = 'FUNC' AND `deleted` = b'0'
);

DROP PROCEDURE IF EXISTS erp_add_column_if_missing;
DROP PROCEDURE IF EXISTS erp_add_index_if_missing;

SET FOREIGN_KEY_CHECKS = 1;
