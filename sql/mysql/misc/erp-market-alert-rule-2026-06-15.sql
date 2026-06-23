-- 市场预警规则配置表
CREATE TABLE IF NOT EXISTS `erp_market_alert_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
  `rule_code` VARCHAR(50) NOT NULL COMMENT '规则编码（RECEIPT_OVERDUE / DELIVERY_OVERDUE / RELEASE_BLOCKED / INVOICE_OVERDUE）',
  `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '规则描述',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `threshold_days` INT DEFAULT NULL COMMENT '阈值天数',
  `threshold_amount` DECIMAL(20,2) DEFAULT NULL COMMENT '阈值金额',
  `level` VARCHAR(20) NOT NULL DEFAULT 'WARNING' COMMENT '预警级别（WARNING / DANGER / INFO）',
  `notify_channels` VARCHAR(200) DEFAULT NULL COMMENT '通知渠道（INTERNAL_MSG / EMAIL / SMS）',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_code` (`rule_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='市场预警规则配置表';

-- 初始化默认规则
INSERT INTO `erp_market_alert_rule` (`rule_code`, `rule_name`, `description`, `enabled`, `threshold_days`, `level`, `notify_channels`) VALUES
('RECEIPT_OVERDUE', '收款逾期预警', '订单交期后仍未收到货款', 1, 30, 'WARNING', 'INTERNAL_MSG'),
('DELIVERY_OVERDUE', '交期逾期预警', '订单交期已过但未出库', 1, 0, 'DANGER', 'INTERNAL_MSG'),
('RELEASE_BLOCKED', '放行阻塞预警', '订单放行状态为阻塞', 1, 7, 'WARNING', 'INTERNAL_MSG'),
('INVOICE_OVERDUE', '开票逾期预警', '出库后30天仍未开票', 1, 30, 'WARNING', 'INTERNAL_MSG');
