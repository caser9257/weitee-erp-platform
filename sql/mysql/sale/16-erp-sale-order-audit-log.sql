/*
 Target: ERP sale-order audit history
 Schema: ruoyi-vue-pro
 Date: 2026-04-03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

CREATE TABLE IF NOT EXISTS `erp_sale_order_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `action_type` varchar(32) NOT NULL,
  `before_status` int DEFAULT NULL,
  `after_status` int DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `idx_erp_sale_order_audit_log_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单审批流转日志';

SET FOREIGN_KEY_CHECKS = 1;
