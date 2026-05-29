/*
 Target: BPM 审批场景菜单和权限点
 Schema: ruoyi-vue-pro
 Date: 2026-05-28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

-- 获取工作流程父菜单 ID
SET @bpm_parent_id := (
    SELECT `id` FROM `system_menu`
    WHERE `name` = '工作流程' AND `deleted` = b'0'
    ORDER BY `id` LIMIT 1
);

-- 如果找不到"工作流程"，尝试查找"流程管理"
IF @bpm_parent_id IS NULL THEN
    SET @bpm_parent_id := (
        SELECT `id` FROM `system_menu`
        WHERE `name` = '流程管理' AND `deleted` = b'0'
        ORDER BY `id` LIMIT 1
    );
END IF;

-- 如果还是找不到，使用 0（顶级菜单）
IF @bpm_parent_id IS NULL THEN
    SET @bpm_parent_id := 0;
END IF;

-- 创建审批场景目录菜单
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 113100, '审批管理', '', 1, 50, @bpm_parent_id, '/approval', 'ep:stamp', '', 'ApprovalRoot', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `parent_id` = @bpm_parent_id AND `path` = '/approval' AND `deleted` = b'0'
);

-- 获取审批管理目录 ID
SET @approval_root_id := (
    SELECT `id` FROM `system_menu`
    WHERE `path` = '/approval' AND `parent_id` = @bpm_parent_id AND `deleted` = b'0'
    ORDER BY `id` LIMIT 1
);

-- 创建审批场景菜单
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 113101, '审批场景', 'bpm:approval-scene:query', 2, 10, @approval_root_id, 'scene', 'ep:coordinate', 'bpm/approval/scene/index', 'BpmApprovalScene', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `component` = 'bpm/approval/scene/index' AND `deleted` = b'0'
);

-- 获取审批场景菜单 ID
SET @scene_menu_id := (
    SELECT `id` FROM `system_menu`
    WHERE `component` = 'bpm/approval/scene/index' AND `deleted` = b'0'
    ORDER BY `id` LIMIT 1
);

-- 创建审批场景按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 113102, '审批场景查询', 'bpm:approval-scene:query', 3, 1, @scene_menu_id, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM DUAL
WHERE @scene_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'bpm:approval-scene:query' AND `parent_id` = @scene_menu_id AND `deleted` = b'0'
);

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 113103, '审批场景创建', 'bpm:approval-scene:create', 3, 2, @scene_menu_id, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM DUAL
WHERE @scene_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'bpm:approval-scene:create' AND `parent_id` = @scene_menu_id AND `deleted` = b'0'
);

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 113104, '审批场景更新', 'bpm:approval-scene:update', 3, 3, @scene_menu_id, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM DUAL
WHERE @scene_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'bpm:approval-scene:update' AND `parent_id` = @scene_menu_id AND `deleted` = b'0'
);

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 113105, '审批场景删除', 'bpm:approval-scene:delete', 3, 4, @scene_menu_id, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM DUAL
WHERE @scene_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'bpm:approval-scene:delete' AND `parent_id` = @scene_menu_id AND `deleted` = b'0'
);

SET FOREIGN_KEY_CHECKS = 1;
