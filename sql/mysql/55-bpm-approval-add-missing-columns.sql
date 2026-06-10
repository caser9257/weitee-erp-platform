-- ERP 通用审批接入平台 - 字段补充脚本
-- 创建日期：2026-06-10
-- 说明：为现有表添加缺失的字段

-- 1. 为 bpm_approval_scheme 表添加 owner_user_id 字段
ALTER TABLE `bpm_approval_scheme` ADD COLUMN `owner_user_id` BIGINT NOT NULL DEFAULT 0 COMMENT '归属用户ID（配置管理员）' AFTER `latest_version_id`;
ALTER TABLE `bpm_approval_scheme` ADD INDEX `idx_bpm_approval_scheme_owner` (`owner_user_id`);

-- 2. 为 bpm_approval_scheme 表添加 scene_id 字段
ALTER TABLE `bpm_approval_scheme` ADD COLUMN `scene_id` BIGINT COMMENT '关联场景ID' AFTER `biz_type`;
ALTER TABLE `bpm_approval_scheme` ADD INDEX `idx_bpm_approval_scheme_scene` (`scene_id`);

-- 3. 为 bpm_approval_instance_snapshot 表添加 approval_id 字段（如果还没有）
ALTER TABLE `bpm_approval_instance_snapshot` ADD COLUMN `approval_id` VARCHAR(64) COMMENT '审批ID（业务唯一标识）' AFTER `id`;
ALTER TABLE `bpm_approval_instance_snapshot` ADD UNIQUE INDEX `uk_bpm_approval_snapshot_approval_id` (`approval_id`);

-- 4. 为 bpm_approval_instance_snapshot 表添加 start_user_id 字段（如果还没有）
ALTER TABLE `bpm_approval_instance_snapshot` ADD COLUMN `start_user_id` BIGINT COMMENT '发起人ID' AFTER `result_reason`;
ALTER TABLE `bpm_approval_instance_snapshot` ADD INDEX `idx_bpm_approval_snapshot_user` (`start_user_id`);

-- 5. 为 bpm_approval_instance_snapshot 表添加 start_time 和 end_time 字段（如果还没有）
ALTER TABLE `bpm_approval_instance_snapshot` ADD COLUMN `start_time` DATETIME COMMENT '发起时间' AFTER `start_user_id`;
ALTER TABLE `bpm_approval_instance_snapshot` ADD COLUMN `end_time` DATETIME COMMENT '结束时间' AFTER `start_time`;
