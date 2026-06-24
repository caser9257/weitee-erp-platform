/*
 Target: ERP sale-order reject flow
 Schema: ruoyi-vue-pro
 Date: 2026-04-03
 Note: Uses information_schema compatibility check, works on MySQL 5.7+ and 8.0+
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @add_last_reject_reason = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_sale_order'
        AND COLUMN_NAME = 'last_reject_reason'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_sale_order` ADD COLUMN `last_reject_reason` varchar(255) DEFAULT NULL COMMENT ''最近一次驳回原因'''
  )
);
PREPARE stmt FROM @add_last_reject_reason;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_last_reject_time = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_sale_order'
        AND COLUMN_NAME = 'last_reject_time'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_sale_order` ADD COLUMN `last_reject_time` datetime DEFAULT NULL COMMENT ''最近一次驳回时间'''
  )
);
PREPARE stmt FROM @add_last_reject_time;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_last_reject_user_id = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_sale_order'
        AND COLUMN_NAME = 'last_reject_user_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_sale_order` ADD COLUMN `last_reject_user_id` bigint DEFAULT NULL COMMENT ''最近一次驳回人编号'''
  )
);
PREPARE stmt FROM @add_last_reject_user_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `erp_sale_order_reject_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `reason` varchar(255) NOT NULL,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `idx_erp_sale_order_reject_log_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单驳回日志';

INSERT INTO `system_dict_data`
(`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT
  (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_dict_data` t),
  30, '已驳回', '30', 'erp_audit_status', 0, 'danger', '', '',
  '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_dict_data`
  WHERE `dict_type` = 'erp_audit_status'
    AND `value` = '30'
    AND `deleted` = b'0'
);

SET FOREIGN_KEY_CHECKS = 1;
