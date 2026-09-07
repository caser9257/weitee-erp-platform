/*
 Target: ERP sale delivery-ready status + lightweight finished-goods quality
 Schema: ruoyi-vue-pro
 Date: 2026-04-14
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


ALTER TABLE `erp_sale_order`
  ADD COLUMN `delivery_ready_status` varchar(32) NOT NULL DEFAULT 'NOT_READY' COMMENT 'delivery ready status' AFTER `return_count`;

ALTER TABLE `erp_production_order`
  ADD COLUMN `source_order_id` bigint DEFAULT NULL COMMENT 'source sale order id' AFTER `source_id`,
  ADD COLUMN `source_item_id` bigint DEFAULT NULL COMMENT 'source sale order item id' AFTER `source_order_id`;

CREATE TABLE IF NOT EXISTS `erp_production_finish_quality` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `no` varchar(64) NOT NULL,
  `production_order_id` bigint NOT NULL,
  `production_order_no` varchar(64) NOT NULL,
  `source_order_id` bigint DEFAULT NULL,
  `source_item_id` bigint DEFAULT NULL,
  `product_id` bigint NOT NULL,
  `report_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
  `qualified_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
  `unqualified_qty` decimal(24,6) NOT NULL DEFAULT 0.000000,
  `status` tinyint NOT NULL DEFAULT 10 COMMENT '10 pending, 20 partial, 30 passed, 40 failed',
  `checker_user_id` bigint DEFAULT NULL,
  `check_time` datetime DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_production_finish_quality_no` (`no`),
  UNIQUE KEY `uk_erp_production_finish_quality_order` (`production_order_id`),
  KEY `idx_erp_production_finish_quality_source_order` (`source_order_id`),
  KEY `idx_erp_production_finish_quality_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='lightweight production finish quality';

SET FOREIGN_KEY_CHECKS = 1;
