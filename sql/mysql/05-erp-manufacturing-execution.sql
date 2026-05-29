/*
 Navicat / MySQL Init Script
 Target: Manufacturing Execution
 Schema: ruoyi-vue-pro
 Date: 2026-04-01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_production_order_step
-- ----------------------------
DROP TABLE IF EXISTS `erp_production_order_step`;
CREATE TABLE `erp_production_order_step` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工单工序编号',
  `production_order_id` bigint NOT NULL COMMENT '生产工单编号',
  `route_step_id` bigint DEFAULT NULL COMMENT '工艺路线工序编号',
  `step_no` int NOT NULL COMMENT '工序顺序',
  `step_code` varchar(64) NOT NULL COMMENT '工序编码',
  `step_name` varchar(128) NOT NULL COMMENT '工序名称',
  `work_center_id` bigint DEFAULT NULL COMMENT '工作中心编号',
  `device_id` bigint DEFAULT NULL COMMENT '设备编号',
  `plan_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '计划数量',
  `reported_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '累计报工数量',
  `qualified_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '累计合格数量',
  `scrap_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '累计报废数量',
  `step_status` tinyint NOT NULL DEFAULT 0 COMMENT '工序状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_production_order_step_no` (`tenant_id`, `production_order_id`, `step_no`),
  KEY `idx_erp_production_order_step_order_id` (`production_order_id`),
  KEY `idx_erp_production_order_step_route_step_id` (`route_step_id`),
  KEY `idx_erp_production_order_step_center_id` (`work_center_id`),
  KEY `idx_erp_production_order_step_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 生产工单工序表';

-- ----------------------------
-- Table structure for erp_production_report
-- ----------------------------
DROP TABLE IF EXISTS `erp_production_report`;
CREATE TABLE `erp_production_report` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '生产报工编号',
  `report_no` varchar(64) NOT NULL COMMENT '报工单号',
  `production_order_id` bigint NOT NULL COMMENT '生产工单编号',
  `report_date` datetime NOT NULL COMMENT '报工时间',
  `report_user_id` bigint DEFAULT NULL COMMENT '报工人',
  `report_type` tinyint NOT NULL DEFAULT 1 COMMENT '报工类型',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '批次号',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_production_report_no` (`tenant_id`, `report_no`),
  KEY `idx_erp_production_report_order_id` (`production_order_id`),
  KEY `idx_erp_production_report_batch_no` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 生产报工主表';

-- ----------------------------
-- Table structure for erp_production_report_item
-- ----------------------------
DROP TABLE IF EXISTS `erp_production_report_item`;
CREATE TABLE `erp_production_report_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '生产报工明细编号',
  `report_id` bigint NOT NULL COMMENT '生产报工编号',
  `production_order_step_id` bigint NOT NULL COMMENT '工单工序编号',
  `device_id` bigint DEFAULT NULL COMMENT '设备编号',
  `worker_user_id` bigint DEFAULT NULL COMMENT '作业人',
  `reported_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '报工数量',
  `qualified_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '合格数量',
  `scrap_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '报废数量',
  `work_hour` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '工时',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '批次号',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_erp_production_report_item_report_id` (`report_id`),
  KEY `idx_erp_production_report_item_step_id` (`production_order_step_id`),
  KEY `idx_erp_production_report_item_device_id` (`device_id`),
  KEY `idx_erp_production_report_item_batch_no` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 生产报工明细表';

-- ----------------------------
-- Table structure for erp_production_completion
-- ----------------------------
DROP TABLE IF EXISTS `erp_production_completion`;
CREATE TABLE `erp_production_completion` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '完工入库编号',
  `completion_no` varchar(64) NOT NULL COMMENT '完工单号',
  `production_order_id` bigint NOT NULL COMMENT '生产工单编号',
  `warehouse_id` bigint NOT NULL COMMENT '入库仓库',
  `product_id` bigint NOT NULL COMMENT '产品编号',
  `batch_no` varchar(64) NOT NULL COMMENT '成品批次号',
  `completion_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '完工数量',
  `stock_in_id` bigint DEFAULT NULL COMMENT '关联其它入库单编号',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_production_completion_no` (`tenant_id`, `completion_no`),
  KEY `idx_erp_production_completion_order_id` (`production_order_id`),
  KEY `idx_erp_production_completion_stock_in_id` (`stock_in_id`),
  KEY `idx_erp_production_completion_batch_no` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 完工入库表';

-- ----------------------------
-- Table structure for erp_production_material
-- ----------------------------
DROP TABLE IF EXISTS `erp_production_material`;
CREATE TABLE `erp_production_material` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工单用料编号',
  `production_order_id` bigint NOT NULL COMMENT '生产工单编号',
  `production_order_step_id` bigint DEFAULT NULL COMMENT '工单工序编号',
  `bom_item_id` bigint DEFAULT NULL COMMENT 'BOM 明细编号',
  `material_id` bigint NOT NULL COMMENT '物料编号',
  `required_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '需求数量',
  `issued_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '已领数量',
  `returned_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '已退数量',
  `scrap_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '损耗数量',
  `supply_warehouse_id` bigint DEFAULT NULL COMMENT '供应仓库编号',
  `issue_mode` tinyint NOT NULL DEFAULT 1 COMMENT '发料方式',
  `backflush_flag` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否倒冲',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_erp_production_material_order_id` (`production_order_id`),
  KEY `idx_erp_production_material_step_id` (`production_order_step_id`),
  KEY `idx_erp_production_material_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 工单用料快照表';

-- ----------------------------
-- Table structure for erp_material_issue
-- ----------------------------
DROP TABLE IF EXISTS `erp_material_issue`;
CREATE TABLE `erp_material_issue` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '生产领料编号',
  `issue_no` varchar(64) NOT NULL COMMENT '领料单号',
  `production_order_id` bigint NOT NULL COMMENT '生产工单编号',
  `issue_date` datetime NOT NULL COMMENT '领料时间',
  `issue_type` tinyint NOT NULL DEFAULT 1 COMMENT '领料类型',
  `warehouse_id` bigint NOT NULL COMMENT '仓库编号',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_material_issue_no` (`tenant_id`, `issue_no`),
  KEY `idx_erp_material_issue_order_id` (`production_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 生产领料主表';

-- ----------------------------
-- Table structure for erp_material_issue_item
-- ----------------------------
DROP TABLE IF EXISTS `erp_material_issue_item`;
CREATE TABLE `erp_material_issue_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '生产领料明细编号',
  `issue_id` bigint NOT NULL COMMENT '生产领料编号',
  `production_material_id` bigint DEFAULT NULL COMMENT '工单用料编号',
  `material_id` bigint NOT NULL COMMENT '物料编号',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '批次号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库编号',
  `required_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '应领数量',
  `issue_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '实领数量',
  `stock_out_id` bigint DEFAULT NULL COMMENT '关联其它出库单编号',
  `stock_out_item_id` bigint DEFAULT NULL COMMENT '关联其它出库明细编号',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_erp_material_issue_item_issue_id` (`issue_id`),
  KEY `idx_erp_material_issue_item_material_id` (`material_id`),
  KEY `idx_erp_material_issue_item_batch_no` (`batch_no`),
  KEY `idx_erp_material_issue_item_stock_out_id` (`stock_out_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 生产领料明细表';

-- ----------------------------
-- Table structure for erp_material_return
-- ----------------------------
DROP TABLE IF EXISTS `erp_material_return`;
CREATE TABLE `erp_material_return` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '生产退料编号',
  `return_no` varchar(64) NOT NULL COMMENT '退料单号',
  `production_order_id` bigint NOT NULL COMMENT '生产工单编号',
  `return_date` datetime NOT NULL COMMENT '退料时间',
  `warehouse_id` bigint NOT NULL COMMENT '仓库编号',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_material_return_no` (`tenant_id`, `return_no`),
  KEY `idx_erp_material_return_order_id` (`production_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 生产退料主表';

-- ----------------------------
-- Table structure for erp_material_return_item
-- ----------------------------
DROP TABLE IF EXISTS `erp_material_return_item`;
CREATE TABLE `erp_material_return_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '生产退料明细编号',
  `return_id` bigint NOT NULL COMMENT '生产退料编号',
  `production_material_id` bigint DEFAULT NULL COMMENT '工单用料编号',
  `material_id` bigint NOT NULL COMMENT '物料编号',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '批次号',
  `return_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '退料数量',
  `stock_in_id` bigint DEFAULT NULL COMMENT '关联其它入库单编号',
  `stock_in_item_id` bigint DEFAULT NULL COMMENT '关联其它入库明细编号',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_erp_material_return_item_return_id` (`return_id`),
  KEY `idx_erp_material_return_item_material_id` (`material_id`),
  KEY `idx_erp_material_return_item_batch_no` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 生产退料明细表';

SET FOREIGN_KEY_CHECKS = 1;
