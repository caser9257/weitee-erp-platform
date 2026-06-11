-- =============================================================
-- 租户功能彻底移除 - 数据库清理脚本
-- 生成日期：2026-06-11
-- 说明：
--   1. 删除 system_tenant 表
--   2. 删除 system_tenant_package 表
--   3. 删除 BPM 相关表中的 tenant_id 列
--   4. 所有数据的 tenant_id 均为 0，删除列不影响业务数据
-- =============================================================

-- ========== 1. 删除租户套餐表（先删子表） ==========

DROP TABLE IF EXISTS `system_tenant_package`;

-- ========== 2. 删除租户表 ==========

DROP TABLE IF EXISTS `system_tenant`;

-- ========== 3. 删除 BPM 表中的 tenant_id 列 ==========

ALTER TABLE `bpm_approval_delegation` DROP COLUMN `tenant_id`;
ALTER TABLE `bpm_approval_record` DROP COLUMN `tenant_id`;
ALTER TABLE `bpm_approval_task` DROP COLUMN `tenant_id`;
ALTER TABLE `bpm_approval_template` DROP COLUMN `tenant_id`;
ALTER TABLE `bpm_approval_urge_record` DROP COLUMN `tenant_id`;

-- ========== 4. 清理相关权限数据（可选） ==========

-- 删除租户管理相关的菜单和权限
-- 注意：如果菜单表中存在租户管理相关菜单，需要清理
DELETE FROM `system_menu` WHERE `id` IN (
    SELECT `menu_id` FROM `system_role_menu` WHERE `role_id` IN (
        SELECT `id` FROM `system_role` WHERE `name` = '租户管理员'
    )
);
DELETE FROM `system_role` WHERE `name` = '租户管理员';

-- 删除租户相关的权限标识
DELETE FROM `system_menu` WHERE `permission` LIKE 'system:tenant:%';
DELETE FROM `system_menu` WHERE `permission` LIKE 'system:tenant-package:%';
