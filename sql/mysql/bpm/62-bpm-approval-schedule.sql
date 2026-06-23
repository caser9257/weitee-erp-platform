-- 审批排班表（去除租户）
-- 创建日期：2026-06-11

-- 创建审批排班表
CREATE TABLE IF NOT EXISTS `bpm_approval_schedule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '排班ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `schedule_date` DATE NOT NULL COMMENT '排班日期',
    `schedule_type` VARCHAR(20) NOT NULL COMMENT '排班类型（WORK: 工作日, REST: 休息日, LEAVE: 请假）',
    `start_time` DATETIME COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `remark` VARCHAR(500) COMMENT '备注',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_schedule_date` (`schedule_date`),
    UNIQUE KEY `uk_user_date` (`user_id`, `schedule_date`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批排班表';

-- 创建节假日表
CREATE TABLE IF NOT EXISTS `bpm_approval_holiday` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '节假日ID',
    `holiday_date` DATE NOT NULL COMMENT '节假日日期',
    `holiday_name` VARCHAR(100) NOT NULL COMMENT '节假日名称',
    `holiday_type` VARCHAR(20) NOT NULL COMMENT '节假日类型（NATIONAL: 法定节假日, WEEKEND: 周末, CUSTOM: 自定义）',
    `is_workday` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否为工作日（用于调休）',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_holiday_date` (`holiday_date`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节假日表';
