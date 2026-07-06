/*
 自制入库单一期
 目标：补齐生产完工质检后的正式入库单据层，为“质检通过后入库”提供独立对象。
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

CREATE TABLE IF NOT EXISTS `erp_production_inbound` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `no` varchar(64) NOT NULL COMMENT '单号',
  `finish_quality_id` bigint NOT NULL COMMENT '成品质检单 id',
  `finish_quality_no` varchar(64) DEFAULT NULL COMMENT '成品质检单号',
  `production_order_id` bigint NOT NULL COMMENT '生产工单 id',
  `production_order_no` varchar(64) NOT NULL COMMENT '生产工单号',
  `project_id` bigint DEFAULT NULL COMMENT '项目 id',
  `product_id` bigint NOT NULL COMMENT '产品 id',
  `warehouse_id` bigint NOT NULL COMMENT '入库仓库 id',
  `inbound_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '入库数量',
  `unit_cost` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '单位成本',
  `total_cost` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '总成本',
  `status` tinyint NOT NULL DEFAULT 10 COMMENT '10 pending, 20 executed, 30 canceled',
  `inbound_time` datetime DEFAULT NULL COMMENT '执行入库时间',
  `executed_by` bigint DEFAULT NULL COMMENT '执行人',
  `executed_time` datetime DEFAULT NULL COMMENT '执行时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_production_inbound_no` (`no`),
  UNIQUE KEY `uk_erp_production_inbound_quality` (`finish_quality_id`),
  KEY `idx_erp_production_inbound_order` (`production_order_id`),
  KEY `idx_erp_production_inbound_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自制入库单';

SET FOREIGN_KEY_CHECKS = 1;
