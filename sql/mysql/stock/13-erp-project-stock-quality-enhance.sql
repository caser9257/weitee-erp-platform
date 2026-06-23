/*
 Navicat / MySQL Init Script
 Target: ERP Project Link + Stock + Quality Enhancement
 Schema: ruoyi-vue-pro
 Date: 2026-04-03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Alter table for erp_purchase_order
-- ----------------------------
SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_purchase_order` ADD COLUMN `project_id` bigint NULL COMMENT ''项目编号'' AFTER `supplier_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_purchase_order'
    AND COLUMN_NAME = 'project_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_purchase_order` ADD COLUMN `business_type` varchar(32) NULL COMMENT ''业务类型'' AFTER `project_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_purchase_order'
    AND COLUMN_NAME = 'business_type'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_purchase_order` ADD COLUMN `source_project_id` bigint NULL COMMENT ''来源项目编号'' AFTER `business_type`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_purchase_order'
    AND COLUMN_NAME = 'source_project_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_purchase_order` ADD KEY `idx_erp_purchase_order_project_id` (`project_id`)',
    'SELECT 1'
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_purchase_order'
    AND INDEX_NAME = 'idx_erp_purchase_order_project_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- Alter table for erp_purchase_suggest
-- ----------------------------
SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_purchase_suggest` ADD COLUMN `project_id` bigint NULL COMMENT ''项目编号'' AFTER `material_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_purchase_suggest'
    AND COLUMN_NAME = 'project_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_purchase_suggest` ADD COLUMN `source_type` varchar(32) NULL COMMENT ''来源类型'' AFTER `project_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_purchase_suggest'
    AND COLUMN_NAME = 'source_type'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_purchase_suggest` ADD COLUMN `business_type` varchar(32) NULL COMMENT ''业务类型'' AFTER `source_type`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_purchase_suggest'
    AND COLUMN_NAME = 'business_type'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- Alter table for erp_production_suggest
-- ----------------------------
SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_production_suggest` ADD COLUMN `project_id` bigint NULL COMMENT ''项目编号'' AFTER `plan_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_production_suggest'
    AND COLUMN_NAME = 'project_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- Alter table for erp_mrp_demand
-- ----------------------------
SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_mrp_demand` ADD COLUMN `project_id` bigint NULL COMMENT ''项目编号'' AFTER `product_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_mrp_demand'
    AND COLUMN_NAME = 'project_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_mrp_demand` ADD KEY `idx_erp_mrp_demand_project_id` (`project_id`)',
    'SELECT 1'
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_mrp_demand'
    AND INDEX_NAME = 'idx_erp_mrp_demand_project_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- Alter table for erp_stock
-- ----------------------------
SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_stock` ADD COLUMN `reserved_count` decimal(24,6) NOT NULL DEFAULT 0 COMMENT ''占用数量'' AFTER `count`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_stock'
    AND COLUMN_NAME = 'reserved_count'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_stock` ADD COLUMN `available_count` decimal(24,6) NOT NULL DEFAULT 0 COMMENT ''可用数量'' AFTER `reserved_count`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_stock'
    AND COLUMN_NAME = 'available_count'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_stock` ADD COLUMN `quality_hold_count` decimal(24,6) NOT NULL DEFAULT 0 COMMENT ''待检冻结数量'' AFTER `available_count`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_stock'
    AND COLUMN_NAME = 'quality_hold_count'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- Alter table for erp_stock_record
-- ----------------------------
SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_stock_record` ADD COLUMN `project_id` bigint NULL COMMENT ''项目编号'' AFTER `product_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_stock_record'
    AND COLUMN_NAME = 'project_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_stock_record` ADD COLUMN `quality_status` varchar(32) NULL COMMENT ''质检状态'' AFTER `batch_no`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_stock_record'
    AND COLUMN_NAME = 'quality_status'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_stock_record` ADD COLUMN `owner_type` varchar(32) NULL COMMENT ''库存归属类型'' AFTER `quality_status`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_stock_record'
    AND COLUMN_NAME = 'owner_type'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- Alter table for erp_purchase_in_items
-- ----------------------------
SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_purchase_in_items` ADD COLUMN `supplier_batch_no` varchar(64) NULL COMMENT ''供应商批次号'' AFTER `batch_no`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_purchase_in_items'
    AND COLUMN_NAME = 'supplier_batch_no'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_purchase_in_items` ADD COLUMN `quality_status` varchar(32) NULL COMMENT ''质检状态'' AFTER `supplier_batch_no`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_purchase_in_items'
    AND COLUMN_NAME = 'quality_status'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_purchase_in_items` ADD COLUMN `inspection_required` bit(1) NOT NULL DEFAULT b''0'' COMMENT ''是否需检'' AFTER `quality_status`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_purchase_in_items'
    AND COLUMN_NAME = 'inspection_required'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_purchase_in_items` ADD COLUMN `inspection_order_id` bigint NULL COMMENT ''检验单编号'' AFTER `inspection_required`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_purchase_in_items'
    AND COLUMN_NAME = 'inspection_order_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- Alter table for erp_stock_in_item
-- ----------------------------
SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_stock_in_item` ADD COLUMN `supplier_batch_no` varchar(64) NULL COMMENT ''供应商批次号'' AFTER `batch_no`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_stock_in_item'
    AND COLUMN_NAME = 'supplier_batch_no'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_stock_in_item` ADD COLUMN `quality_status` varchar(32) NULL COMMENT ''质检状态'' AFTER `supplier_batch_no`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_stock_in_item'
    AND COLUMN_NAME = 'quality_status'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- Create table for erp_stock_lot
-- ----------------------------
CREATE TABLE IF NOT EXISTS `erp_stock_lot` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `product_id` bigint NOT NULL COMMENT '产品编号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库编号',
  `batch_no` varchar(64) NOT NULL COMMENT '批次号',
  `supplier_batch_no` varchar(64) DEFAULT NULL COMMENT '供应商批次号',
  `project_id` bigint DEFAULT NULL COMMENT '项目编号',
  `owner_type` varchar(32) DEFAULT NULL COMMENT '库存归属类型',
  `quality_status` varchar(32) DEFAULT NULL COMMENT '质检状态',
  `count` decimal(24,6) NOT NULL DEFAULT 0 COMMENT '总数量',
  `locked_count` decimal(24,6) NOT NULL DEFAULT 0 COMMENT '锁定数量',
  `available_count` decimal(24,6) NOT NULL DEFAULT 0 COMMENT '可用数量',
  `produce_date` date DEFAULT NULL COMMENT '生产日期',
  `expiry_date` date DEFAULT NULL COMMENT '有效期至',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_erp_stock_lot_product_warehouse` (`product_id`, `warehouse_id`),
  KEY `idx_erp_stock_lot_batch_no` (`batch_no`),
  KEY `idx_erp_stock_lot_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 批次库存表';

-- ----------------------------
-- Create table for erp_stock_reservation
-- ----------------------------
CREATE TABLE IF NOT EXISTS `erp_stock_reservation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `product_id` bigint NOT NULL COMMENT '产品编号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库编号',
  `project_id` bigint DEFAULT NULL COMMENT '项目编号',
  `source_type` varchar(32) NOT NULL COMMENT '来源类型',
  `source_id` bigint NOT NULL COMMENT '来源单据编号',
  `source_item_id` bigint DEFAULT NULL COMMENT '来源明细编号',
  `reserved_count` decimal(24,6) NOT NULL DEFAULT 0 COMMENT '占用数量',
  `released_count` decimal(24,6) NOT NULL DEFAULT 0 COMMENT '释放数量',
  `status` varchar(32) NOT NULL COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_erp_stock_reservation_product_warehouse` (`product_id`, `warehouse_id`),
  KEY `idx_erp_stock_reservation_project_id` (`project_id`),
  KEY `idx_erp_stock_reservation_source` (`source_type`, `source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 库存占用表';

-- ----------------------------
-- Create table for erp_quality_inspection
-- ----------------------------
CREATE TABLE IF NOT EXISTS `erp_quality_inspection` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `no` varchar(64) NOT NULL COMMENT '检验单号',
  `inspection_type` varchar(32) NOT NULL COMMENT '检验类型',
  `source_type` varchar(32) NOT NULL COMMENT '来源类型',
  `source_id` bigint NOT NULL COMMENT '来源单据编号',
  `supplier_id` bigint DEFAULT NULL COMMENT '供应商编号',
  `project_id` bigint DEFAULT NULL COMMENT '项目编号',
  `warehouse_id` bigint DEFAULT NULL COMMENT '仓库编号',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '批次号',
  `status` varchar(32) NOT NULL COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_quality_inspection_no` (`tenant_id`, `no`),
  KEY `idx_erp_quality_inspection_source` (`source_type`, `source_id`),
  KEY `idx_erp_quality_inspection_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 质检单';

-- ----------------------------
-- Create table for erp_quality_inspection_item
-- ----------------------------
CREATE TABLE IF NOT EXISTS `erp_quality_inspection_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `inspection_id` bigint NOT NULL COMMENT '检验单编号',
  `product_id` bigint NOT NULL COMMENT '产品编号',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '批次号',
  `check_item_name` varchar(128) NOT NULL COMMENT '检验项',
  `standard_value` varchar(255) DEFAULT NULL COMMENT '标准值',
  `actual_value` varchar(255) DEFAULT NULL COMMENT '实测值',
  `result` varchar(32) NOT NULL COMMENT '结果',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_erp_quality_inspection_item_inspection_id` (`inspection_id`),
  KEY `idx_erp_quality_inspection_item_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 质检单明细';

SET FOREIGN_KEY_CHECKS = 1;
