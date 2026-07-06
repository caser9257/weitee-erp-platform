/*
 Navicat / MySQL Init Script
 Target: Manufacturing Master Data
 Schema: ruoyi-vue-pro
 Date: 2026-04-01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_process_route
-- ----------------------------
DROP TABLE IF EXISTS `erp_process_route`;
CREATE TABLE `erp_process_route` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工艺路线编号',
  `route_code` varchar(64) NOT NULL COMMENT '工艺路线编码',
  `route_name` varchar(128) NOT NULL COMMENT '工艺路线名称',
  `product_id` bigint NOT NULL COMMENT '产品编号',
  `version` varchar(32) NOT NULL COMMENT '版本号',
  `default_flag` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否默认',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `effective_date` date DEFAULT NULL COMMENT '生效日期',
  `expire_date` date DEFAULT NULL COMMENT '失效日期',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_process_route_code` (`route_code`),
  KEY `idx_erp_process_route_product_id` (`product_id`),
  KEY `idx_erp_process_route_product_version` (`product_id`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 工艺路线主表';

-- ----------------------------
-- Table structure for erp_process_route_step
-- ----------------------------
DROP TABLE IF EXISTS `erp_process_route_step`;
CREATE TABLE `erp_process_route_step` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工艺路线工序编号',
  `route_id` bigint NOT NULL COMMENT '工艺路线编号',
  `step_no` int NOT NULL COMMENT '工序顺序',
  `step_code` varchar(64) NOT NULL COMMENT '工序编码',
  `step_name` varchar(128) NOT NULL COMMENT '工序名称',
  `work_center_id` bigint DEFAULT NULL COMMENT '工作中心编号',
  `outsource_flag` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否委外工序',
  `qc_flag` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否质检工序',
  `report_required` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否必须报工',
  `inspect_required` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否必须检验',
  `prepare_time` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '准备工时',
  `process_time` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '加工工时',
  `move_time` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '转移工时',
  `wait_time` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '等待工时',
  `batch_size` decimal(24,6) NOT NULL DEFAULT 1.000000 COMMENT '加工批量',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_process_route_step_no` (`route_id`, `step_no`),
  KEY `idx_erp_process_route_step_route_id` (`route_id`),
  KEY `idx_erp_process_route_step_center_id` (`work_center_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 工艺路线工序表';

-- ----------------------------
-- Table structure for erp_work_center
-- ----------------------------
DROP TABLE IF EXISTS `erp_work_center`;
CREATE TABLE `erp_work_center` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工作中心编号',
  `center_code` varchar(64) NOT NULL COMMENT '工作中心编码',
  `center_name` varchar(128) NOT NULL COMMENT '工作中心名称',
  `dept_id` bigint DEFAULT NULL COMMENT '部门编号',
  `manager_user_id` bigint DEFAULT NULL COMMENT '负责人',
  `enable_device_dispatch` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否启用设备派工',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_work_center_code` (`center_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 工作中心表';

-- ----------------------------
-- Table structure for erp_device
-- ----------------------------
DROP TABLE IF EXISTS `erp_device`;
CREATE TABLE `erp_device` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设备编号',
  `device_code` varchar(64) NOT NULL COMMENT '设备编码',
  `device_name` varchar(128) NOT NULL COMMENT '设备名称',
  `work_center_id` bigint DEFAULT NULL COMMENT '工作中心编号',
  `specification` varchar(128) DEFAULT NULL COMMENT '规格型号',
  `device_status` tinyint NOT NULL DEFAULT 1 COMMENT '设备状态',
  `maintenance_cycle_day` int DEFAULT NULL COMMENT '保养周期天数',
  `check_cycle_day` int DEFAULT NULL COMMENT '点检周期天数',
  `purchase_date` date DEFAULT NULL COMMENT '购置日期',
  `start_use_date` date DEFAULT NULL COMMENT '启用日期',
  `manufacturer` varchar(128) DEFAULT NULL COMMENT '设备厂商',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_device_code` (`device_code`),
  KEY `idx_erp_device_center_id` (`work_center_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 设备台账表';

-- ----------------------------
-- Table structure for erp_device_change_log
-- ----------------------------
DROP TABLE IF EXISTS `erp_device_change_log`;
CREATE TABLE `erp_device_change_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设备变更日志编号',
  `device_id` bigint NOT NULL COMMENT '设备编号',
  `change_type` tinyint NOT NULL COMMENT '变更类型',
  `before_status` tinyint DEFAULT NULL COMMENT '变更前状态',
  `after_status` tinyint DEFAULT NULL COMMENT '变更后状态',
  `change_time` datetime NOT NULL COMMENT '变更时间',
  `biz_type` varchar(32) DEFAULT NULL COMMENT '业务类型',
  `biz_id` bigint DEFAULT NULL COMMENT '业务编号',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_erp_device_change_log_device_id` (`device_id`),
  KEY `idx_erp_device_change_log_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 设备状态变更日志表';

SET FOREIGN_KEY_CHECKS = 1;
