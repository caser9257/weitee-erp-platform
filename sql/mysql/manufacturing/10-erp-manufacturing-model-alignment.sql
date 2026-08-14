/*
  Manufacturing model alignment migration.
  This migration is additive and intentionally does not drop or rewrite existing data.
  It aligns the Java model with the existing erp_* manufacturing table names.

  兼容性说明：MySQL 8.0/8.4 不支持 `ADD COLUMN IF NOT EXISTS`（该语法仅 MariaDB 支持），
  因此所有新增列统一通过 information_schema.COLUMNS 检测 + PREPARE/EXECUTE 动态 SQL 实现幂等；
  列已存在时仅执行 `SELECT 1` 跳过，不会报错也不会重复加列。
*/

SET NAMES utf8mb4;

-- ==================== erp_product 产品制造属性 ====================

SET @add_product_type = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'product_type'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `product_type` tinyint NOT NULL DEFAULT 1 COMMENT ''产品类型'' AFTER `unit_id`'
);
PREPARE stmt FROM @add_product_type;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_produce_type = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'produce_type'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `produce_type` tinyint NOT NULL DEFAULT 1 COMMENT ''生产方式'' AFTER `product_type`'
);
PREPARE stmt FROM @add_produce_type;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_batch_enable = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'batch_enable'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `batch_enable` bit(1) NOT NULL DEFAULT b''1'' COMMENT ''是否启用批次'' AFTER `produce_type`'
);
PREPARE stmt FROM @add_batch_enable;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_sn_enable = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'sn_enable'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `sn_enable` bit(1) NOT NULL DEFAULT b''0'' COMMENT ''是否启用序列号'' AFTER `batch_enable`'
);
PREPARE stmt FROM @add_sn_enable;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_default_route_id = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'default_route_id'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `default_route_id` bigint DEFAULT NULL COMMENT ''默认工艺路线编号'' AFTER `sn_enable`'
);
PREPARE stmt FROM @add_default_route_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_qc_enable = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'qc_enable'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `qc_enable` bit(1) NOT NULL DEFAULT b''0'' COMMENT ''是否启用质检'' AFTER `default_route_id`'
);
PREPARE stmt FROM @add_qc_enable;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_outsource_enable = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'outsource_enable'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `outsource_enable` bit(1) NOT NULL DEFAULT b''0'' COMMENT ''是否支持委外'' AFTER `qc_enable`'
);
PREPARE stmt FROM @add_outsource_enable;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_cost_method = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_product' AND COLUMN_NAME = 'cost_method'),
  'SELECT 1',
  'ALTER TABLE `erp_product` ADD COLUMN `cost_method` tinyint NOT NULL DEFAULT 1 COMMENT ''成本方式'' AFTER `outsource_enable`'
);
PREPARE stmt FROM @add_cost_method;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ==================== erp_bom 制造 BOM 补充字段 ====================

SET @add_bom_route_id = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_bom' AND COLUMN_NAME = 'route_id'),
  'SELECT 1',
  'ALTER TABLE `erp_bom` ADD COLUMN `route_id` bigint DEFAULT NULL COMMENT ''关联工艺路线编号'' AFTER `product_id`'
);
PREPARE stmt FROM @add_bom_route_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_bom_yield_rate = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_bom' AND COLUMN_NAME = 'yield_rate'),
  'SELECT 1',
  'ALTER TABLE `erp_bom` ADD COLUMN `yield_rate` decimal(10,4) NOT NULL DEFAULT 1.0000 COMMENT ''成品率'' AFTER `version`'
);
PREPARE stmt FROM @add_bom_yield_rate;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_bom_effective_date = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_bom' AND COLUMN_NAME = 'effective_date'),
  'SELECT 1',
  'ALTER TABLE `erp_bom` ADD COLUMN `effective_date` date DEFAULT NULL COMMENT ''生效日期'' AFTER `status`'
);
PREPARE stmt FROM @add_bom_effective_date;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_bom_expire_date = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_bom' AND COLUMN_NAME = 'expire_date'),
  'SELECT 1',
  'ALTER TABLE `erp_bom` ADD COLUMN `expire_date` date DEFAULT NULL COMMENT ''失效日期'' AFTER `effective_date`'
);
PREPARE stmt FROM @add_bom_expire_date;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ==================== erp_bom_item BOM 明细补充字段 ====================

SET @add_issue_mode = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_bom_item' AND COLUMN_NAME = 'issue_mode'),
  'SELECT 1',
  'ALTER TABLE `erp_bom_item` ADD COLUMN `issue_mode` tinyint NOT NULL DEFAULT 1 COMMENT ''发料方式'' AFTER `loss_rate`'
);
PREPARE stmt FROM @add_issue_mode;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_backflush_flag = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_bom_item' AND COLUMN_NAME = 'backflush_flag'),
  'SELECT 1',
  'ALTER TABLE `erp_bom_item` ADD COLUMN `backflush_flag` bit(1) NOT NULL DEFAULT b''0'' COMMENT ''是否倒冲'' AFTER `issue_mode`'
);
PREPARE stmt FROM @add_backflush_flag;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_supply_warehouse_id = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_bom_item' AND COLUMN_NAME = 'supply_warehouse_id'),
  'SELECT 1',
  'ALTER TABLE `erp_bom_item` ADD COLUMN `supply_warehouse_id` bigint DEFAULT NULL COMMENT ''默认供应仓库编号'' AFTER `lead_time_day`'
);
PREPARE stmt FROM @add_supply_warehouse_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_required_step_id = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_bom_item' AND COLUMN_NAME = 'required_step_id'),
  'SELECT 1',
  'ALTER TABLE `erp_bom_item` ADD COLUMN `required_step_id` bigint DEFAULT NULL COMMENT ''指定工序编号'' AFTER `supply_warehouse_id`'
);
PREPARE stmt FROM @add_required_step_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ==================== erp_production_order 生产工单补充字段 ====================

SET @add_order_route_id = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_production_order' AND COLUMN_NAME = 'route_id'),
  'SELECT 1',
  'ALTER TABLE `erp_production_order` ADD COLUMN `route_id` bigint DEFAULT NULL COMMENT ''工艺路线编号'' AFTER `product_id`'
);
PREPARE stmt FROM @add_order_route_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_route_version = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_production_order' AND COLUMN_NAME = 'route_version'),
  'SELECT 1',
  'ALTER TABLE `erp_production_order` ADD COLUMN `route_version` varchar(32) DEFAULT NULL COMMENT ''工艺路线版本'' AFTER `route_id`'
);
PREPARE stmt FROM @add_route_version;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_order_work_center_id = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_production_order' AND COLUMN_NAME = 'work_center_id'),
  'SELECT 1',
  'ALTER TABLE `erp_production_order` ADD COLUMN `work_center_id` bigint DEFAULT NULL COMMENT ''工作中心编号'' AFTER `route_version`'
);
PREPARE stmt FROM @add_order_work_center_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_batch_no = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_production_order' AND COLUMN_NAME = 'batch_no'),
  'SELECT 1',
  'ALTER TABLE `erp_production_order` ADD COLUMN `batch_no` varchar(64) DEFAULT NULL COMMENT ''生产批次号'' AFTER `work_center_id`'
);
PREPARE stmt FROM @add_batch_no;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_scrap_qty = IF(
  EXISTS (SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'erp_production_order' AND COLUMN_NAME = 'scrap_qty'),
  'SELECT 1',
  'ALTER TABLE `erp_production_order` ADD COLUMN `scrap_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT ''报废数量'' AFTER `finished_qty`'
);
PREPARE stmt FROM @add_scrap_qty;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ==================== 制造执行表（仅不存在时创建） ====================

CREATE TABLE IF NOT EXISTS `erp_process_route` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `route_code` varchar(64) NOT NULL,
  `route_name` varchar(128) NOT NULL,
  `product_id` bigint NOT NULL,
  `version` varchar(32) NOT NULL,
  `default_flag` bit(1) NOT NULL DEFAULT b'0',
  `status` tinyint NOT NULL DEFAULT 1,
  `effective_date` date DEFAULT NULL,
  `expire_date` date DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_process_route_code` (`route_code`),
  KEY `idx_erp_process_route_product_id` (`product_id`),
  KEY `idx_erp_process_route_product_version` (`product_id`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 工艺路线主表';

CREATE TABLE IF NOT EXISTS `erp_process_route_step` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `route_id` bigint NOT NULL,
  `step_no` int NOT NULL,
  `step_code` varchar(64) NOT NULL,
  `step_name` varchar(128) NOT NULL,
  `work_center_id` bigint DEFAULT NULL,
  `outsource_flag` bit(1) NOT NULL DEFAULT b'0',
  `qc_flag` bit(1) NOT NULL DEFAULT b'0',
  `report_required` bit(1) NOT NULL DEFAULT b'1',
  `inspect_required` bit(1) NOT NULL DEFAULT b'0',
  `prepare_time` decimal(24,6) NOT NULL DEFAULT 0.000000,
  `process_time` decimal(24,6) NOT NULL DEFAULT 0.000000,
  `move_time` decimal(24,6) NOT NULL DEFAULT 0.000000,
  `wait_time` decimal(24,6) NOT NULL DEFAULT 0.000000,
  `batch_size` decimal(24,6) NOT NULL DEFAULT 1.000000,
  `sort` int NOT NULL DEFAULT 0,
  `status` tinyint NOT NULL DEFAULT 1,
  `remark` varchar(255) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_process_route_step_no` (`route_id`, `step_no`),
  KEY `idx_erp_process_route_step_route_id` (`route_id`),
  KEY `idx_erp_process_route_step_center_id` (`work_center_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 工艺路线工序表';

CREATE TABLE IF NOT EXISTS `erp_work_center` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `center_code` varchar(64) NOT NULL,
  `center_name` varchar(128) NOT NULL,
  `dept_id` bigint DEFAULT NULL,
  `manager_user_id` bigint DEFAULT NULL,
  `enable_device_dispatch` bit(1) NOT NULL DEFAULT b'0',
  `status` tinyint NOT NULL DEFAULT 1,
  `remark` varchar(255) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_work_center_code` (`center_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 工作中心表';

CREATE TABLE IF NOT EXISTS `erp_device` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `device_code` varchar(64) NOT NULL,
  `device_name` varchar(128) NOT NULL,
  `work_center_id` bigint DEFAULT NULL,
  `specification` varchar(128) DEFAULT NULL,
  `device_status` tinyint NOT NULL DEFAULT 1,
  `maintenance_cycle_day` int DEFAULT NULL,
  `check_cycle_day` int DEFAULT NULL,
  `purchase_date` date DEFAULT NULL,
  `start_use_date` date DEFAULT NULL,
  `manufacturer` varchar(128) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_device_code` (`device_code`),
  KEY `idx_erp_device_center_id` (`work_center_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 设备台账表';

CREATE TABLE IF NOT EXISTS `erp_production_order_step` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `production_order_id` bigint NOT NULL,
  `route_step_id` bigint DEFAULT NULL,
  `step_no` int NOT NULL,
  `step_code` varchar(64) NOT NULL,
  `step_name` varchar(128) NOT NULL,
  `work_center_id` bigint DEFAULT NULL,
  `device_id` bigint DEFAULT NULL,
  `plan_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
  `reported_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
  `qualified_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
  `scrap_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
  `step_status` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(255) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_production_order_step_no` (`production_order_id`, `step_no`),
  KEY `idx_erp_production_order_step_order_id` (`production_order_id`),
  KEY `idx_erp_production_order_step_route_step_id` (`route_step_id`),
  KEY `idx_erp_production_order_step_center_id` (`work_center_id`),
  KEY `idx_erp_production_order_step_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 生产工单工序表';

CREATE TABLE IF NOT EXISTS `erp_production_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `report_no` varchar(64) NOT NULL,
  `production_order_id` bigint NOT NULL,
  `report_date` datetime NOT NULL,
  `report_user_id` bigint DEFAULT NULL,
  `report_type` tinyint NOT NULL DEFAULT 1,
  `batch_no` varchar(64) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 0,
  `remark` varchar(255) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_production_report_no` (`report_no`),
  KEY `idx_erp_production_report_order_id` (`production_order_id`),
  KEY `idx_erp_production_report_batch_no` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 生产报工主表';

CREATE TABLE IF NOT EXISTS `erp_production_report_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `report_id` bigint NOT NULL,
  `production_order_step_id` bigint NOT NULL,
  `device_id` bigint DEFAULT NULL,
  `worker_user_id` bigint DEFAULT NULL,
  `reported_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
  `qualified_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
  `scrap_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
  `work_hour` decimal(24,6) NOT NULL DEFAULT 0.000000,
  `batch_no` varchar(64) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `idx_erp_production_report_item_report_id` (`report_id`),
  KEY `idx_erp_production_report_item_step_id` (`production_order_step_id`),
  KEY `idx_erp_production_report_item_device_id` (`device_id`),
  KEY `idx_erp_production_report_item_batch_no` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 生产报工明细表';
