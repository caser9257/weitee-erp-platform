/*
 Navicat / MySQL Alter Script
 Target: Manufacturing Enhancement Alter
 Schema: ruoyi-vue-pro
 Date: 2026-04-01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Alter for erp_product
-- ----------------------------
ALTER TABLE `erp_product`
  ADD COLUMN `product_type` tinyint NOT NULL DEFAULT 1 COMMENT '产品类型' AFTER `unit_id`,
  ADD COLUMN `produce_type` tinyint NOT NULL DEFAULT 1 COMMENT '生产方式' AFTER `product_type`,
  ADD COLUMN `batch_enable` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否启用批次' AFTER `produce_type`,
  ADD COLUMN `sn_enable` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否启用序列号' AFTER `batch_enable`,
  ADD COLUMN `default_route_id` bigint DEFAULT NULL COMMENT '默认工艺路线编号' AFTER `sn_enable`,
  ADD COLUMN `qc_enable` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否启用质检' AFTER `default_route_id`,
  ADD COLUMN `outsource_enable` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否支持委外' AFTER `qc_enable`,
  ADD COLUMN `cost_method` tinyint NOT NULL DEFAULT 1 COMMENT '成本方式' AFTER `outsource_enable`;

ALTER TABLE `erp_product`
  ADD KEY `idx_erp_product_default_route_id` (`default_route_id`);

-- ----------------------------
-- Alter for erp_bom
-- ----------------------------
ALTER TABLE `erp_bom`
  ADD COLUMN `route_id` bigint DEFAULT NULL COMMENT '关联工艺路线编号' AFTER `product_id`,
  ADD COLUMN `yield_rate` decimal(10,4) NOT NULL DEFAULT 1.0000 COMMENT '成品率' AFTER `version`,
  ADD COLUMN `effective_date` date DEFAULT NULL COMMENT '生效日期' AFTER `status`,
  ADD COLUMN `expire_date` date DEFAULT NULL COMMENT '失效日期' AFTER `effective_date`;

ALTER TABLE `erp_bom`
  ADD KEY `idx_erp_bom_route_id` (`route_id`);

-- ----------------------------
-- Alter for erp_bom_item
-- ----------------------------
ALTER TABLE `erp_bom_item`
  ADD COLUMN `issue_mode` tinyint NOT NULL DEFAULT 1 COMMENT '发料方式' AFTER `loss_rate`,
  ADD COLUMN `backflush_flag` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否倒冲' AFTER `issue_mode`,
  ADD COLUMN `supply_warehouse_id` bigint DEFAULT NULL COMMENT '默认供应仓库编号' AFTER `lead_time_day`,
  ADD COLUMN `required_step_id` bigint DEFAULT NULL COMMENT '指定工序编号' AFTER `supply_warehouse_id`;

ALTER TABLE `erp_bom_item`
  ADD KEY `idx_erp_bom_item_supply_warehouse_id` (`supply_warehouse_id`),
  ADD KEY `idx_erp_bom_item_required_step_id` (`required_step_id`);

-- ----------------------------
-- Alter for erp_production_order
-- ----------------------------
ALTER TABLE `erp_production_order`
  ADD COLUMN `route_id` bigint DEFAULT NULL COMMENT '工艺路线编号' AFTER `product_id`,
  ADD COLUMN `route_version` varchar(32) DEFAULT NULL COMMENT '工艺路线版本' AFTER `route_id`,
  ADD COLUMN `work_center_id` bigint DEFAULT NULL COMMENT '工作中心编号' AFTER `route_version`,
  ADD COLUMN `batch_no` varchar(64) DEFAULT NULL COMMENT '生产批次号' AFTER `work_center_id`,
  ADD COLUMN `scrap_qty` decimal(24,6) NOT NULL DEFAULT 0.000000 COMMENT '报废数量' AFTER `finished_qty`,
  ADD COLUMN `warehouse_id` bigint DEFAULT NULL COMMENT '完工仓库编号' AFTER `scrap_qty`;

ALTER TABLE `erp_production_order`
  ADD KEY `idx_erp_production_order_route_id` (`route_id`),
  ADD KEY `idx_erp_production_order_center_id` (`work_center_id`),
  ADD KEY `idx_erp_production_order_batch_no` (`batch_no`),
  ADD KEY `idx_erp_production_order_source` (`source_type`, `source_id`);

-- ----------------------------
-- Alter for erp_stock_record
-- ----------------------------
ALTER TABLE `erp_stock_record`
  ADD COLUMN `batch_no` varchar(64) DEFAULT NULL COMMENT '批次号' AFTER `product_id`,
  ADD COLUMN `source_order_type` varchar(32) DEFAULT NULL COMMENT '来源单据类型' AFTER `batch_no`,
  ADD COLUMN `source_order_id` bigint DEFAULT NULL COMMENT '来源单据编号' AFTER `source_order_type`,
  ADD COLUMN `source_line_id` bigint DEFAULT NULL COMMENT '来源明细编号' AFTER `source_order_id`;

ALTER TABLE `erp_stock_record`
  ADD KEY `idx_erp_stock_record_batch_no` (`batch_no`),
  ADD KEY `idx_erp_stock_record_source` (`source_order_type`, `source_order_id`);

-- ----------------------------
-- Alter for erp_stock_in_item
-- ----------------------------
ALTER TABLE `erp_stock_in_item`
  ADD COLUMN `batch_no` varchar(64) DEFAULT NULL COMMENT '批次号' AFTER `product_price`;

ALTER TABLE `erp_stock_in_item`
  ADD KEY `idx_erp_stock_in_item_batch_no` (`batch_no`);

-- ----------------------------
-- Alter for erp_stock_out_item
-- ----------------------------
ALTER TABLE `erp_stock_out_item`
  ADD COLUMN `batch_no` varchar(64) DEFAULT NULL COMMENT '批次号' AFTER `product_price`;

ALTER TABLE `erp_stock_out_item`
  ADD KEY `idx_erp_stock_out_item_batch_no` (`batch_no`);

-- ----------------------------
-- Alter for erp_purchase_in_items
-- ----------------------------
ALTER TABLE `erp_purchase_in_items`
  ADD COLUMN `batch_no` varchar(64) DEFAULT NULL COMMENT '批次号' AFTER `product_price`,
  ADD COLUMN `source_outsource_id` bigint DEFAULT NULL COMMENT '来源委外收货编号' AFTER `batch_no`;

ALTER TABLE `erp_purchase_in_items`
  ADD KEY `idx_erp_purchase_in_items_batch_no` (`batch_no`),
  ADD KEY `idx_erp_purchase_in_items_source_outsource_id` (`source_outsource_id`);

SET FOREIGN_KEY_CHECKS = 1;
