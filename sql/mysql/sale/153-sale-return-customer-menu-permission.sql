-- ============================================================
-- 销售模块菜单与权限同步脚本
-- 功能：添加销售退货、客户管理菜单，补充发货放行权限标识
-- 日期：2026-06-23
-- ============================================================

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;
USE `ruoyi-vue-pro`;

-- ============================================================
-- 1. 销售退货菜单（ID: 931312）
-- 父菜单：销售管理 (930110)
-- 排序：在发货通知与签收之后
-- ============================================================

-- 删除已存在的菜单（幂等）
DELETE FROM `system_menu` WHERE `id` IN (931312, 93131201, 93131202, 93131203, 93131204, 93131205);

-- 销售退货页面菜单
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) 
VALUES (931312, '销售退货', 'erp:sale-return:query', 2, 48, 930110, 'return', 'ep:minus', 'erp/sale/return/index', 'ProjectSalesReturn', 0, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0);

-- 销售退货 - 新增按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) 
VALUES (93131201, '新增', 'erp:sale-return:create', 3, 1, 931312, '', '', '', NULL, 0, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0);

-- 销售退货 - 修改按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) 
VALUES (93131202, '修改', 'erp:sale-return:update', 3, 2, 931312, '', '', '', NULL, 0, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0);

-- 销售退货 - 删除按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) 
VALUES (93131203, '删除', 'erp:sale-return:delete', 3, 3, 931312, '', '', '', NULL, 0, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0);

-- 销售退货 - 导出按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) 
VALUES (93131204, '导出', 'erp:sale-return:export', 3, 4, 931312, '', '', '', NULL, 0, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0);

-- 销售退货 - 更新状态按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) 
VALUES (93131205, '更新状态', 'erp:sale-return:update-status', 3, 5, 931312, '', '', '', NULL, 0, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0);

-- ============================================================
-- 2. 客户管理菜单（ID: 931313）
-- 父菜单：销售管理 (930110)
-- 排序：在销售退货之后
-- ============================================================

-- 删除已存在的菜单（幂等）
DELETE FROM `system_menu` WHERE `id` IN (931313, 93131301, 93131302, 93131303, 93131304, 93131305, 93131306);

-- 客户管理页面菜单
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) 
VALUES (931313, '客户管理', 'erp:customer:query', 2, 49, 930110, 'customer', 'ep:user', 'erp/sale/customer/index', 'ProjectSalesCustomer', 0, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0);

-- 客户管理 - 新增按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) 
VALUES (93131301, '新增', 'erp:customer:create', 3, 1, 931313, '', '', '', NULL, 0, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0);

-- 客户管理 - 修改按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) 
VALUES (93131302, '修改', 'erp:customer:update', 3, 2, 931313, '', '', '', NULL, 0, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0);

-- 客户管理 - 删除按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) 
VALUES (93131303, '删除', 'erp:customer:delete', 3, 3, 931313, '', '', '', NULL, 0, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0);

-- 客户管理 - 导出按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) 
VALUES (93131304, '导出', 'erp:customer:export', 3, 4, 931313, '', '', '', NULL, 0, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0);

-- 客户管理 - 精简列表权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) 
VALUES (93131305, '精简列表', 'erp:customer:query', 3, 5, 931313, '', '', '', NULL, 0, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0);

-- 客户管理 - 批量更新权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) 
VALUES (93131306, '批量更新', 'erp:customer:update', 3, 6, 931313, '', '', '', NULL, 0, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0);

-- ============================================================
-- 3. 发货放行权限标识补充（ID: 93130805, 93130806）
-- 父菜单：发货放行审核 (931308)
-- 说明：补充 page 和 stats 接口的权限标识
-- ============================================================

-- 删除已存在的权限（幂等）
DELETE FROM `system_menu` WHERE `id` IN (93130805, 93130806);

-- 发货放行 - 分页查询权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) 
VALUES (93130805, '分页查询', 'erp:shipment-release:query', 3, 5, 931308, '', '', '', NULL, 0, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0);

-- 发货放行 - 统计查询权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) 
VALUES (93130806, '统计查询', 'erp:shipment-release:query', 3, 6, 931308, '', '', '', NULL, 0, 1, 1, 1, 'admin', NOW(), 'admin', NOW(), 0);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 验证查询
-- ============================================================

-- 验证新添加的菜单
SELECT id, name, permission, type, sort, parent_id, path, component 
FROM system_menu 
WHERE id IN (931312, 931313) AND deleted = 0;

-- 验证新添加的权限按钮
SELECT id, name, permission, type, parent_id 
FROM system_menu 
WHERE parent_id IN (931312, 931313, 931308) AND type = 3 AND deleted = 0
ORDER BY parent_id, sort;
