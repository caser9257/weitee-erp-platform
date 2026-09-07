/*
 Target: ERP purchase-order lightweight BPM integration
 Schema: ruoyi-vue-pro
 Date: 2026-04-08
 Note: Compatible with MySQL versions that do not support ALTER TABLE ... ADD COLUMN IF NOT EXISTS
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


SET @add_process_instance_id = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_purchase_order'
        AND COLUMN_NAME = 'process_instance_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_order` ADD COLUMN `process_instance_id` varchar(64) DEFAULT NULL COMMENT ''BPM流程实例编号'' AFTER `status`'
  )
);
PREPARE stmt FROM @add_process_instance_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_last_reject_reason = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_purchase_order'
        AND COLUMN_NAME = 'last_reject_reason'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_order` ADD COLUMN `last_reject_reason` varchar(255) DEFAULT NULL COMMENT ''最近一次驳回原因'' AFTER `remark`'
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
        AND TABLE_NAME = 'erp_purchase_order'
        AND COLUMN_NAME = 'last_reject_time'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_order` ADD COLUMN `last_reject_time` datetime DEFAULT NULL COMMENT ''最近一次驳回时间'' AFTER `last_reject_reason`'
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
        AND TABLE_NAME = 'erp_purchase_order'
        AND COLUMN_NAME = 'last_reject_user_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_order` ADD COLUMN `last_reject_user_id` bigint DEFAULT NULL COMMENT ''最近一次驳回人'' AFTER `last_reject_time`'
  )
);
PREPARE stmt FROM @add_last_reject_user_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_idx_process_instance_id = (
  SELECT IF(
    EXISTS (
      SELECT 1
      FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'erp_purchase_order'
        AND INDEX_NAME = 'idx_erp_purchase_order_process_instance_id'
    ),
    'SELECT 1',
    'ALTER TABLE `erp_purchase_order` ADD INDEX `idx_erp_purchase_order_process_instance_id` (`process_instance_id`)'
  )
);
PREPARE stmt FROM @add_idx_process_instance_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `erp_purchase_order_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `order_id` bigint NOT NULL COMMENT '采购订单编号',
  `action_type` varchar(32) NOT NULL COMMENT '动作类型',
  `before_status` int DEFAULT NULL COMMENT '流转前状态',
  `after_status` int DEFAULT NULL COMMENT '流转后状态',
  `reason` varchar(255) DEFAULT NULL COMMENT '原因',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_purchase_order_audit_log_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 采购订单审批流转日志';

CREATE TABLE IF NOT EXISTS `erp_purchase_order_reject_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `order_id` bigint NOT NULL COMMENT '采购订单编号',
  `reason` varchar(255) DEFAULT NULL COMMENT '驳回原因',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_purchase_order_reject_log_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 采购订单驳回日志';

SET FOREIGN_KEY_CHECKS = 1;
