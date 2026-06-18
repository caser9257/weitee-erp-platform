-- 市场预警记录表
CREATE TABLE IF NOT EXISTS `erp_market_alert_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
    `rule_code` VARCHAR(50) NOT NULL COMMENT '规则编码',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `level` VARCHAR(20) NOT NULL COMMENT '预警级别（WARNING / DANGER / INFO）',
    `project_id` BIGINT NULL COMMENT '项目ID',
    `order_id` BIGINT NULL COMMENT '订单ID',
    `order_no` VARCHAR(64) NULL COMMENT '订单编号',
    `content` VARCHAR(500) NOT NULL COMMENT '预警内容',
    `trigger_time` DATETIME NOT NULL COMMENT '触发时间',
    `handled` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否已处理',
    `handle_time` DATETIME NULL COMMENT '处理时间',
    `handle_user_id` BIGINT NULL COMMENT '处理人ID',
    `handle_remark` VARCHAR(500) NULL COMMENT '处理备注',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_rule_code` (`rule_code`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_handled` (`handled`),
    INDEX `idx_trigger_time` (`trigger_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='市场预警记录表';

-- 预警处理权限
INSERT INTO system_menu (id, parent_id, name, permission, path, component, icon, sort, status, type, creator, create_time, updater, update_time, deleted)
SELECT 142010, 
       (SELECT id FROM system_menu WHERE name = '市场预警与统计' AND deleted = 0 LIMIT 1),
       '处理预警',
       'erp:market-alert:handle',
       NULL,
       NULL,
       'ep:check',
       10,
       0,
       2,
       '1',
       NOW(),
       '1',
       NOW(),
       b'0'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM system_menu WHERE permission = 'erp:market-alert:handle' AND deleted = 0
);

-- 站内信通知模板：市场预警
INSERT INTO system_notify_template (id, name, code, content, type, status, params, creator, create_time, updater, update_time, deleted)
VALUES (100, '市场预警通知', 'market_alert', '【{level}】{ruleName}\n\n订单：{orderNo}\n内容：{content}\n\n请及时处理。', 1, 0, 'level,ruleName,orderNo,content', '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE content = VALUES(content), updater = '1', update_time = NOW();
