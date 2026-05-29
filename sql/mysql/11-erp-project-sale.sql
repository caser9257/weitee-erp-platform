/*
 Navicat / MySQL Init Script
 Target: ERP Project + Sale Order Projectization
 Schema: ruoyi-vue-pro
 Date: 2026-04-02
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_project
-- ----------------------------
CREATE TABLE IF NOT EXISTS `erp_project` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目编号',
  `no` varchar(64) NOT NULL COMMENT '项目编号',
  `name` varchar(128) NOT NULL COMMENT '项目名称',
  `customer_id` bigint NOT NULL COMMENT '客户编号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '项目状态',
  `delivery_date` date DEFAULT NULL COMMENT '交期',
  `remark` varchar(1024) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_project_no` (`tenant_id`, `no`),
  KEY `idx_erp_project_customer_id` (`customer_id`),
  KEY `idx_erp_project_status` (`status`),
  KEY `idx_erp_project_delivery_date` (`delivery_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 项目表';

-- ----------------------------
-- Alter table for erp_sale_order
-- ----------------------------
SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_sale_order` ADD COLUMN `project_id` bigint NULL COMMENT ''项目编号'' AFTER `customer_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_sale_order'
    AND COLUMN_NAME = 'project_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_sale_order` ADD COLUMN `delivery_date` date NULL COMMENT ''交期'' AFTER `order_time`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_sale_order'
    AND COLUMN_NAME = 'delivery_date'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_sale_order` ADD KEY `idx_erp_sale_order_project_id` (`project_id`)',
    'SELECT 1'
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_sale_order'
    AND INDEX_NAME = 'idx_erp_sale_order_project_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_sale_order` ADD KEY `idx_erp_sale_order_delivery_date` (`delivery_date`)',
    'SELECT 1'
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_sale_order'
    AND INDEX_NAME = 'idx_erp_sale_order_delivery_date'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;
