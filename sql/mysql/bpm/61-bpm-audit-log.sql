-- 审计日志表
-- 创建日期：2026-06-11

-- 创建审批审计日志表
CREATE TABLE IF NOT EXISTS `bpm_approval_audit_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `approval_id` VARCHAR(64) NOT NULL COMMENT '审批ID',
    `task_id` VARCHAR(64) COMMENT '任务ID',
    `action` VARCHAR(50) NOT NULL COMMENT '操作类型',
    `operator_user_id` BIGINT NOT NULL COMMENT '操作人ID',
    `operator_user_name` VARCHAR(100) COMMENT '操作人姓名',
    `target_user_id` BIGINT COMMENT '目标用户ID',
    `target_user_name` VARCHAR(100) COMMENT '目标用户姓名',
    `comment` VARCHAR(500) COMMENT '操作意见',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `user_agent` VARCHAR(500) COMMENT '用户代理',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_approval_id` (`approval_id`),
    INDEX `idx_task_id` (`task_id`),
    INDEX `idx_operator_user_id` (`operator_user_id`),
    INDEX `idx_action` (`action`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批审计日志表';

-- 创建操作类型枚举注释
-- SUBMIT: 提交审批
-- APPROVE: 审批通过
-- REJECT: 审批拒绝
-- RETURN: 审批驳回
-- CANCEL: 撤回审批
-- TRANSFER: 转办任务
-- DELEGATE: 委托任务
-- URGE: 催办任务
-- TIMEOUT: 超时处理
-- CANCEL_BY_ADMIN: 管理员终止
