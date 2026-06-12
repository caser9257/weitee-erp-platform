/*
 Target: BPM 审批方案菜单和权限点
 Schema: ruoyi-vue-pro
 Date: 2026-06-12
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

-- 获取审批管理目录 ID（复用审批场景创建的目录）
SET @approval_root_id := (
    SELECT `id` FROM `system_menu`
    WHERE `path` = '/approval' AND `deleted` = b'0'
    ORDER BY `id` LIMIT 1
);

-- 如果审批管理目录不存在，则创建
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 113200, '审批管理', '', 1, 50, 
    COALESCE(
        (SELECT `id` FROM `system_menu` WHERE `name` = '工作流程' AND `deleted` = b'0' ORDER BY `id` LIMIT 1),
        (SELECT `id` FROM `system_menu` WHERE `name` = '流程管理' AND `deleted` = b'0' ORDER BY `id` LIMIT 1),
        0
    ), 
    '/approval', 'ep:stamp', '', 'ApprovalRoot', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `path` = '/approval' AND `deleted` = b'0'
);

-- 重新获取审批管理目录 ID
SET @approval_root_id := (
    SELECT `id` FROM `system_menu`
    WHERE `path` = '/approval' AND `deleted` = b'0'
    ORDER BY `id` LIMIT 1
);

-- 创建审批方案菜单
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 113201, '审批方案', 'bpm:approval-scheme:query', 2, 20, @approval_root_id, 'scheme', 'ep:connection', 'bpm/approval/scheme/index', 'BpmApprovalScheme', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM DUAL
WHERE @approval_root_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `component` = 'bpm/approval/scheme/index' AND `deleted` = b'0'
);

-- 获取审批方案菜单 ID
SET @scheme_menu_id := (
    SELECT `id` FROM `system_menu`
    WHERE `component` = 'bpm/approval/scheme/index' AND `deleted` = b'0'
    ORDER BY `id` LIMIT 1
);

-- 创建审批方案按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 113202, '审批方案查询', 'bpm:approval-scheme:query', 3, 1, @scheme_menu_id, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM DUAL
WHERE @scheme_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'bpm:approval-scheme:query' AND `parent_id` = @scheme_menu_id AND `deleted` = b'0'
);

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 113203, '审批方案创建', 'bpm:approval-scheme:create', 3, 2, @scheme_menu_id, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM DUAL
WHERE @scheme_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'bpm:approval-scheme:create' AND `parent_id` = @scheme_menu_id AND `deleted` = b'0'
);

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 113204, '审批方案更新', 'bpm:approval-scheme:update', 3, 3, @scheme_menu_id, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM DUAL
WHERE @scheme_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'bpm:approval-scheme:update' AND `parent_id` = @scheme_menu_id AND `deleted` = b'0'
);

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 113205, '审批方案发布', 'bpm:approval-scheme:publish', 3, 4, @scheme_menu_id, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM DUAL
WHERE @scheme_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'bpm:approval-scheme:publish' AND `parent_id` = @scheme_menu_id AND `deleted` = b'0'
);

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 113206, '审批方案删除', 'bpm:approval-scheme:delete', 3, 5, @scheme_menu_id, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM DUAL
WHERE @scheme_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'bpm:approval-scheme:delete' AND `parent_id` = @scheme_menu_id AND `deleted` = b'0'
);

SET FOREIGN_KEY_CHECKS = 1;
