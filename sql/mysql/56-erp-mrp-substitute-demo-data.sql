/*
 ERP MRP 替代料与库存占用演示数据
 作用：
 - 在 MRP 计划详情里看到缺料清单与替代料推荐
 - 在替代料管理页看到“自动推荐”字段
 - 在库存占用追溯页看到项目、销售单、占用明细
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE `ruoyi-vue-pro`;

-- 兼容旧库：补齐销售单页需要的字段
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
    'ALTER TABLE `erp_sale_order` ADD COLUMN `delivery_ready_status` varchar(32) NOT NULL DEFAULT ''NOT_READY'' COMMENT ''delivery ready status'' AFTER `return_count`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_sale_order'
    AND COLUMN_NAME = 'delivery_ready_status'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 兼容旧库：替代料表
CREATE TABLE IF NOT EXISTS `erp_bom_item_substitute` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `bom_item_id` bigint NOT NULL COMMENT 'BOM 明细编号',
  `substitute_material_id` bigint NOT NULL COMMENT '替代物料编号',
  `priority` int DEFAULT NULL COMMENT '优先级',
  `replace_ratio` decimal(24, 6) DEFAULT NULL COMMENT '替换比例',
  `enable_auto_recommend` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否自动推荐',
  `sort` int DEFAULT NULL COMMENT '排序',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_erp_bom_item_substitute_bom_item_id` (`bom_item_id`),
  KEY `idx_erp_bom_item_substitute_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP BOM 替代料表';

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_bom_item_substitute` ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT ''租户编号'' AFTER `deleted`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_bom_item_substitute'
    AND COLUMN_NAME = 'tenant_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_bom_item_substitute` ADD COLUMN `enable_auto_recommend` bit(1) NOT NULL DEFAULT b''0'' COMMENT ''是否自动推荐'' AFTER `replace_ratio`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_bom_item_substitute'
    AND COLUMN_NAME = 'enable_auto_recommend'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 兼容旧库：库存占用追溯表
CREATE TABLE IF NOT EXISTS `erp_mrp_stock_reservation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plan_id` bigint NOT NULL,
  `project_id` bigint DEFAULT NULL,
  `product_id` bigint NOT NULL,
  `source_order_id` bigint NOT NULL,
  `source_item_id` bigint DEFAULT NULL,
  `reserved_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
  `status` tinyint NOT NULL DEFAULT 0,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  `tenant_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_erp_mrp_stock_reservation_product_id` (`product_id`),
  KEY `idx_erp_mrp_stock_reservation_source_order_id` (`source_order_id`),
  KEY `idx_erp_mrp_stock_reservation_status` (`status`),
  KEY `idx_erp_mrp_stock_reservation_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP MRP 库存占用表';

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_mrp_stock_reservation` ADD COLUMN `project_id` bigint DEFAULT NULL COMMENT ''项目编号'' AFTER `plan_id`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_mrp_stock_reservation'
    AND COLUMN_NAME = 'project_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `erp_mrp_stock_reservation` ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT ''租户编号'' AFTER `deleted`',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'erp_mrp_stock_reservation'
    AND COLUMN_NAME = 'tenant_id'
);
PREPARE stmt FROM @ddl_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

START TRANSACTION;

DELETE FROM `erp_mrp_stock_reservation` WHERE `plan_id` = 960100;
DELETE FROM `erp_mrp_shortage` WHERE `plan_id` = 960100;
DELETE FROM `erp_mrp_result` WHERE `plan_id` = 960100;
DELETE FROM `erp_mrp_plan` WHERE `id` = 960100;
DELETE FROM `erp_bom_item_substitute` WHERE `bom_item_id` IN (960301, 960302);
DELETE FROM `erp_bom_item` WHERE `id` IN (960301, 960302);
DELETE FROM `erp_bom` WHERE `id` = 960200;
DELETE FROM `erp_sale_order_items` WHERE `id` IN (970011, 970012);
DELETE FROM `erp_sale_order` WHERE `id` = 970001;
DELETE FROM `erp_project` WHERE `id` = 960700;
DELETE FROM `erp_customer` WHERE `id` = 960800;
DELETE FROM `erp_product` WHERE `id` BETWEEN 960001 AND 960006;
DELETE FROM `erp_product_category` WHERE `id` IN (960011, 960012);
DELETE FROM `erp_product_unit` WHERE `id` = 960021;

INSERT INTO `erp_product_unit`
(`id`, `name`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(960021, '个', 0, '1', NOW(), '1', NOW(), b'0', 1);

INSERT INTO `erp_product_category`
(`id`, `parent_id`, `name`, `code`, `sort`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(960011, 0, '替代料演示', 'DEMO-MRP', 1, 0, '1', NOW(), '1', NOW(), b'0', 1),
(960012, 960011, '电子元件', 'DEMO-MRP-ELEC', 2, 0, '1', NOW(), '1', NOW(), b'0', 1);

INSERT INTO `erp_product`
(`id`, `name`, `bar_code`, `category_id`, `unit_id`, `status`, `standard`, `remark`, `expiry_day`, `weight`,
 `purchase_price`, `sale_price`, `min_price`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(960001, '替代料推荐演示成品', 'DEMO-MRP-0001', 960012, 960021, 0, '演示成品', '用于查看 MRP 替代料推荐的根产品',
 3650, 0.800000, 120.000000, 168.000000, 150.000000, '1', NOW(), '1', NOW(), b'0', 1),
(960002, '高规格芯片 U1', 'DEMO-MRP-0002', 960012, 960021, 0, 'U1 / 高规格', '缺料项，用于展示替代料推荐',
 3650, 0.002000, 6.500000, 9.800000, 8.900000, '1', NOW(), '1', NOW(), b'0', 1),
(960003, '通用芯片 U1A', 'DEMO-MRP-0003', 960012, 960021, 0, 'U1A / 通用型', '替代料 A',
 3650, 0.002000, 4.200000, 6.600000, 5.800000, '1', NOW(), '1', NOW(), b'0', 1),
(960004, '通用芯片 U1B', 'DEMO-MRP-0004', 960012, 960021, 0, 'U1B / 兼容型', '替代料 B',
 3650, 0.002000, 4.000000, 6.200000, 5.500000, '1', NOW(), '1', NOW(), b'0', 1),
(960005, '标准电阻 10K', 'DEMO-MRP-0005', 960012, 960021, 0, 'R1 / 10K', '正常物料，展示 BOM 结构',
 3650, 0.001000, 0.030000, 0.080000, 0.060000, '1', NOW(), '1', NOW(), b'0', 1),
(960006, '标准电阻 12K', 'DEMO-MRP-0006', 960012, 960021, 0, 'R1A / 12K', '标准电阻的备用料',
 3650, 0.001000, 0.028000, 0.078000, 0.058000, '1', NOW(), '1', NOW(), b'0', 1);

INSERT INTO `erp_customer`
(`id`, `name`, `contact`, `mobile`, `telephone`, `email`, `fax`, `remark`, `status`, `sort`,
 `tax_no`, `tax_percent`, `bank_name`, `bank_account`, `bank_address`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(960800, '库存占用演示客户', NULL, NULL, NULL, NULL, NULL, '用于库存占用追溯页演示', 0, 1,
 NULL, NULL, NULL, NULL, NULL, '1', NOW(), '1', NOW(), b'0', 1);

INSERT INTO `erp_project`
(`id`, `no`, `name`, `customer_id`, `status`, `delivery_date`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(960700, 'DEMO-PROJ-20260420-001', '库存占用追溯演示项目', 960800, 1, DATE_ADD(CURDATE(), INTERVAL 30 DAY),
 '用于库存占用追溯页展示项目维度', '1', NOW(), '1', NOW(), b'0', 1);

INSERT INTO `erp_sale_order`
(`id`, `no`, `status`, `customer_id`, `account_id`, `sale_user_id`, `order_time`, `project_id`, `delivery_date`,
 `total_count`, `total_price`, `total_product_price`, `total_tax_price`, `discount_percent`, `discount_price`,
 `deposit_price`, `file_url`, `remark`, `out_count`, `return_count`, `delivery_ready_status`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(970001, 'DEMO-SO-20260420-001', 20, 960800, NULL, NULL, NOW(), 960700, DATE_ADD(CURDATE(), INTERVAL 7 DAY),
 30.000000, 230.000000, 230.000000, 0.000000, 0.000000, 0.000000,
 0.000000, NULL, '库存占用追溯演示销售单', 0.000000, 0.000000, 'NOT_READY',
 '1', NOW(), '1', NOW(), b'0', 1);

INSERT INTO `erp_sale_order_items`
(`id`, `order_id`, `product_id`, `product_unit_id`, `product_price`, `count`, `total_price`, `tax_percent`, `tax_price`,
 `remark`, `out_count`, `return_count`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(970011, 970001, 960002, 960021, 6.500000, 20.000000, 130.000000, 0.000000, 0.000000,
 'MRP 缺料项', 0.000000, 0.000000, '1', NOW(), '1', NOW(), b'0', 1),
(970012, 970001, 960005, 960021, 10.000000, 10.000000, 100.000000, 0.000000, 0.000000,
 '库存占用辅助项', 0.000000, 0.000000, '1', NOW(), '1', NOW(), b'0', 1);

INSERT INTO `erp_bom`
(`id`, `bom_code`, `product_id`, `version`, `status`, `source_rd_bom_id`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(960200, 'DEMO-BOM-20260420-001', 960001, 'V1.0', 1, NULL, '替代料推荐演示 BOM',
 '1', NOW(), '1', NOW(), b'0', 1);

INSERT INTO `erp_bom_item`
(`id`, `bom_id`, `material_id`, `material_type`, `unit_id`, `usage_qty`, `loss_rate`, `lead_time_day`,
 `mrp_enable_flag`, `supply_owner`, `sort`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(960301, 960200, 960002, 0, 960021, 2.000000, 0.0000, 5, 1, 'COMPANY', 1, '缺料项，配置两条替代料',
 '1', NOW(), '1', NOW(), b'0', 1),
(960302, 960200, 960005, 0, 960021, 4.000000, 0.0000, 3, 1, 'COMPANY', 2, '正常物料，用于演示 BOM 结构',
 '1', NOW(), '1', NOW(), b'0', 1);

INSERT INTO `erp_bom_item_substitute`
(`id`, `bom_item_id`, `substitute_material_id`, `priority`, `replace_ratio`, `enable_auto_recommend`, `sort`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(960401, 960301, 960003, 1, 1.000000, b'1', 1, '优先推荐替代料 A',
 '1', NOW(), '1', NOW(), b'0', 1),
(960402, 960301, 960004, 2, 1.000000, b'0', 2, '备选替代料 B',
 '1', NOW(), '1', NOW(), b'0', 1),
(960403, 960302, 960006, 1, 1.000000, b'0', 1, '正常物料的备用料',
 '1', NOW(), '1', NOW(), b'0', 1);

INSERT INTO `erp_mrp_plan`
(`id`, `plan_no`, `plan_name`, `plan_start_date`, `plan_end_date`, `status`, `run_time`, `operator_id`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(960100, 'DEMO-MRP-20260420-001', '替代料推荐演示计划', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 20, NOW(), 1,
 '演示数据：用于查看 MRP 缺料清单和替代料推荐', '1', NOW(), '1', NOW(), b'0', 1);

INSERT INTO `erp_mrp_result`
(`id`, `plan_id`, `root_product_id`, `material_id`, `gross_demand_qty`, `available_stock_qty`, `incoming_qty`, `wip_qty`,
 `reserved_stock_qty`, `net_demand_qty`, `business_type`, `mrp_enable_flag`, `supply_owner`, `skip_reason`,
 `suggest_type`, `suggest_date`, `source_order_id`, `source_item_id`, `demand_date`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(960201, 960100, 960001, 960002, 20.000000, 0.000000, 0.000000, 0.000000,
 0.000000, 20.000000, 'SELF_RESEARCH', 1, 'COMPANY', NULL,
 'PURCHASE', CURDATE(), 970001, 970011, CURDATE(),
 '1', NOW(), '1', NOW(), b'0', 1);

INSERT INTO `erp_mrp_shortage`
(`id`, `plan_id`, `root_product_id`, `material_id`, `shortage_qty`, `required_date`, `source_order_id`, `source_item_id`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(960301, 960100, 960001, 960002, 20.000000, CURDATE(), 970001, 970011,
 '1', NOW(), '1', NOW(), b'0', 1);

INSERT INTO `erp_mrp_stock_reservation`
(`id`, `plan_id`, `project_id`, `product_id`, `source_order_id`, `source_item_id`, `reserved_qty`, `status`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
(980001, 960100, 960700, 960002, 970001, 970011, 12.000000, 0,
 '1', NOW(), '1', NOW(), b'0', 1),
(980002, 960100, 960700, 960005, 970001, 970012, 8.000000, 1,
 '1', NOW(), '1', NOW(), b'0', 1);

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
