-- 历史数据兼容脚本（修复版）
-- 创建日期：2026-06-11
-- 说明：将现有的审批数据迁移到新的表结构中

-- 1. 为现有的审批场景补充 owner_user_id 字段
-- 假设所有现有的场景都属于系统管理员（ID=1）
UPDATE `bpm_approval_scene` SET `owner_user_id` = 1 WHERE `owner_user_id` = 0 OR `owner_user_id` IS NULL;

-- 2. 为现有的审批方案补充 owner_user_id 字段
UPDATE `bpm_approval_scheme` SET `owner_user_id` = 1 WHERE `owner_user_id` = 0 OR `owner_user_id` IS NULL;

-- 3. 为现有的审批快照补充 approval_id 字段
-- 使用 UUID 生成唯一的 approval_id
UPDATE `bpm_approval_instance_snapshot` SET `approval_id` = UUID() WHERE `approval_id` IS NULL OR `approval_id` = '';

-- 4. 为现有的审批快照补充 start_user_id 字段
-- 从 creator 字段中提取用户 ID
UPDATE `bpm_approval_instance_snapshot` SET `start_user_id` = CAST(`creator` AS UNSIGNED) WHERE `start_user_id` IS NULL AND `creator` REGEXP '^[0-9]+$';

-- 5. 为现有的审批快照补充 start_time 字段
UPDATE `bpm_approval_instance_snapshot` SET `start_time` = `create_time` WHERE `start_time` IS NULL;

-- 6. 创建索引以提高查询性能
-- 注意：MySQL 不支持 CREATE INDEX IF NOT EXISTS，使用存储过程或忽略错误
-- 这里直接创建索引，如果已存在会报错，可以忽略

-- 检查并创建 approval_id 索引
SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_NAME = 'bpm_approval_instance_snapshot' AND INDEX_NAME = 'idx_approval_instance_snapshot_approval_id') = 0,
    'CREATE INDEX idx_approval_instance_snapshot_approval_id ON bpm_approval_instance_snapshot (approval_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并创建 status 索引
SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_NAME = 'bpm_approval_instance_snapshot' AND INDEX_NAME = 'idx_approval_instance_snapshot_status') = 0,
    'CREATE INDEX idx_approval_instance_snapshot_status ON bpm_approval_instance_snapshot (status)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并创建 start_user_id 索引
SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_NAME = 'bpm_approval_instance_snapshot' AND INDEX_NAME = 'idx_approval_instance_snapshot_start_user_id') = 0,
    'CREATE INDEX idx_approval_instance_snapshot_start_user_id ON bpm_approval_instance_snapshot (start_user_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 7. 创建视图以便查询审批统计
CREATE OR REPLACE VIEW `v_approval_statistics` AS
SELECT 
    `scene_code`,
    COUNT(*) AS `total_count`,
    SUM(CASE WHEN `status` = 1 THEN 1 ELSE 0 END) AS `processing_count`,
    SUM(CASE WHEN `status` = 2 THEN 1 ELSE 0 END) AS `approved_count`,
    SUM(CASE WHEN `status` = 3 THEN 1 ELSE 0 END) AS `rejected_count`,
    SUM(CASE WHEN `status` = 4 THEN 1 ELSE 0 END) AS `cancelled_count`
FROM `bpm_approval_instance_snapshot`
GROUP BY `scene_code`;

-- 8. 创建视图以便查询用户审批统计
CREATE OR REPLACE VIEW `v_user_approval_statistics` AS
SELECT 
    `start_user_id` AS `user_id`,
    COUNT(*) AS `total_count`,
    SUM(CASE WHEN `status` = 1 THEN 1 ELSE 0 END) AS `processing_count`,
    SUM(CASE WHEN `status` = 2 THEN 1 ELSE 0 END) AS `approved_count`,
    SUM(CASE WHEN `status` = 3 THEN 1 ELSE 0 END) AS `rejected_count`,
    SUM(CASE WHEN `status` = 4 THEN 1 ELSE 0 END) AS `cancelled_count`
FROM `bpm_approval_instance_snapshot`
GROUP BY `start_user_id`;
