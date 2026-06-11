-- 为催办记录表添加 BaseDO 字段
-- 创建日期：2026-06-11

-- 1. 添加 update_time 字段
ALTER TABLE `bpm_approval_urge_record` ADD COLUMN `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `urge_time`;

-- 2. 添加 creator 字段
ALTER TABLE `bpm_approval_urge_record` ADD COLUMN `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者' AFTER `update_time`;

-- 3. 添加 updater 字段
ALTER TABLE `bpm_approval_urge_record` ADD COLUMN `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者' AFTER `creator`;

-- 4. 添加 deleted 字段
ALTER TABLE `bpm_approval_urge_record` ADD COLUMN `deleted` BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除' AFTER `updater`;

-- 5. 添加 tenant_id 字段
ALTER TABLE `bpm_approval_urge_record` ADD COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号' AFTER `deleted`;
