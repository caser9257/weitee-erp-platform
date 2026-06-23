-- ============================================================
-- 应收台账菜单与权限
-- 日期：2026-06-23
-- ============================================================

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;
USE `ruoyi-vue-pro`;

-- -----------------------------------------------------------
-- 应收台账菜单（ID: 931314）
-- 父菜单：财务管理 (930180)
-- -----------------------------------------------------------

-- 删除已存在的菜单（幂等）
DELETE FROM `system_menu` WHERE `id` IN (931314, 93131401, 93131402, 93131403);

-- 应收台账页面菜单
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) 
VALUES (931314, '应收台账', 'erp:ar-statement:query', 2, 25, 930180, 'ar-statement', 'ep:money', 'erp/finance/ar-statement/index', 'ErpArStatement', 0, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0);

-- 应收台账 - 查询按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) 
VALUES (93131401, '查询', 'erp:ar-statement:query', 3, 1, 931314, '', '', '', NULL, 0, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0);

-- 应收台账 - 导出按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) 
VALUES (93131402, '导出', 'erp:ar-statement:export', 3, 2, 931314, '', '', '', NULL, 0, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0);

-- 应收台账 - 汇总查询权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) 
VALUES (93131403, '汇总查询', 'erp:ar-statement:query', 3, 3, 931314, '', '', '', NULL, 0, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0);

SET FOREIGN_KEY_CHECKS = 1;

-- 验证
SELECT id, name, permission, type, sort, parent_id, path, component 
FROM system_menu 
WHERE id = 931314 AND deleted = 0;
