-- 文件操作日志表
CREATE TABLE IF NOT EXISTS `infra_file_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `file_id` bigint NOT NULL COMMENT '文件ID',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件名称',
  `operation` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型：UPLOAD-上传, DOWNLOAD-下载, DELETE-删除, VIEW-查看, RESTORE-恢复, ROLLBACK-回滚, PERMANENT_DELETE-永久删除',
  `user_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人名称',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作IP',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '浏览器信息',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作说明',
  `result` tinyint NOT NULL DEFAULT 0 COMMENT '操作结果：0-成功, 1-失败',
  `fail_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '失败原因',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_file_id` (`file_id`) USING BTREE,
  KEY `idx_operation` (`operation`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件操作日志表';

-- 文件访问统计表
CREATE TABLE IF NOT EXISTS `infra_file_access_stats` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `file_id` bigint NOT NULL COMMENT '文件ID',
  `stats_date` date NOT NULL COMMENT '统计日期',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '查看次数',
  `download_count` int NOT NULL DEFAULT 0 COMMENT '下载次数',
  `unique_visitor_count` int NOT NULL DEFAULT 0 COMMENT '独立访客数',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_file_date` (`file_id`, `stats_date`) USING BTREE,
  KEY `idx_stats_date` (`stats_date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件访问统计表';

-- 文件权限表
CREATE TABLE IF NOT EXISTS `infra_file_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `file_id` bigint NOT NULL COMMENT '文件ID',
  `grant_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '授权类型：USER-用户, ROLE-角色, DEPT-部门',
  `grant_target_id` bigint NOT NULL COMMENT '授权目标ID（用户ID/角色ID/部门ID）',
  `grant_target_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '授权目标名称',
  `permissions` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限类型：VIEW-查看, DOWNLOAD-下载, EDIT-编辑, DELETE-删除, SHARE-分享，多个权限用逗号分隔',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间（null表示永不过期）',
  `grant_user_id` bigint DEFAULT NULL COMMENT '授权人ID',
  `grant_user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '授权人名称',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_file_id` (`file_id`) USING BTREE,
  KEY `idx_grant_type_target` (`grant_type`, `grant_target_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件权限表';

-- =====================================================
-- 以下 ALTER TABLE 语句使用存储过程避免字段已存在的错误
-- =====================================================

DELIMITER //

CREATE PROCEDURE IF NOT EXISTS add_columns_if_not_exists()
BEGIN
    -- 检查并添加 folder_id 字段
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'infra_file' AND COLUMN_NAME = 'folder_id') THEN
        ALTER TABLE `infra_file` ADD COLUMN `folder_id` bigint DEFAULT NULL COMMENT '文件夹ID' AFTER `size`;
    END IF;

    -- 检查并添加 tag_ids 字段
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'infra_file' AND COLUMN_NAME = 'tag_ids') THEN
        ALTER TABLE `infra_file` ADD COLUMN `tag_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签ID（多个标签用逗号分隔）' AFTER `folder_id`;
    END IF;

    -- 检查并添加 view_count 字段
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'infra_file' AND COLUMN_NAME = 'view_count') THEN
        ALTER TABLE `infra_file` ADD COLUMN `view_count` int DEFAULT 0 COMMENT '总查看次数' AFTER `tag_ids`;
    END IF;

    -- 检查并添加 download_count 字段
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'infra_file' AND COLUMN_NAME = 'download_count') THEN
        ALTER TABLE `infra_file` ADD COLUMN `download_count` int DEFAULT 0 COMMENT '总下载次数' AFTER `view_count`;
    END IF;

    -- 检查并添加 last_access_time 字段
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'infra_file' AND COLUMN_NAME = 'last_access_time') THEN
        ALTER TABLE `infra_file` ADD COLUMN `last_access_time` datetime DEFAULT NULL COMMENT '最后访问时间' AFTER `download_count`;
    END IF;

    -- 检查并添加 delete_user_id 字段
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'infra_file' AND COLUMN_NAME = 'delete_user_id') THEN
        ALTER TABLE `infra_file` ADD COLUMN `delete_user_id` bigint DEFAULT NULL COMMENT '删除人ID' AFTER `last_access_time`;
    END IF;

    -- 检查并添加 delete_user_name 字段
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'infra_file' AND COLUMN_NAME = 'delete_user_name') THEN
        ALTER TABLE `infra_file` ADD COLUMN `delete_user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '删除人名称' AFTER `delete_user_id`;
    END IF;

    -- 检查并添加 delete_time 字段
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'infra_file' AND COLUMN_NAME = 'delete_time') THEN
        ALTER TABLE `infra_file` ADD COLUMN `delete_time` datetime DEFAULT NULL COMMENT '删除时间' AFTER `delete_user_name`;
    END IF;

    -- 检查并添加 delete_reason 字段
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'infra_file' AND COLUMN_NAME = 'delete_reason') THEN
        ALTER TABLE `infra_file` ADD COLUMN `delete_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '删除原因' AFTER `delete_time`;
    END IF;

    -- 检查并添加索引
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'infra_file' AND INDEX_NAME = 'idx_folder_id') THEN
        ALTER TABLE `infra_file` ADD INDEX `idx_folder_id` (`folder_id`) USING BTREE;
    END IF;

    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'infra_file' AND INDEX_NAME = 'idx_delete_time') THEN
        ALTER TABLE `infra_file` ADD INDEX `idx_delete_time` (`delete_time`) USING BTREE;
    END IF;
END //

DELIMITER ;

-- 执行存储过程
CALL add_columns_if_not_exists();

-- 删除存储过程
DROP PROCEDURE IF EXISTS add_columns_if_not_exists;
