/*
 Target: BPM model publish permission repair for super admin accounts
 Schema: ruoyi-vue-pro
 Date: 2026-04-09

 Background:
 1. In code, accounts with role code `super_admin` will receive full menu access automatically.
 2. Some environments drift at the data layer: the publish account is not actually bound to `super_admin`,
    or BPM model menu bindings are incomplete in the database snapshot.
 3. This script repairs both sides in an idempotent way.

 Notes:
 - This script does not create a new account.
 - It repairs the tenant-1 super admin role, and ensures common admin accounts can use it.
 - If your actual publishing account uses another username, add it to the username list below.
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id = 1;

SET @super_admin_role_id = (
  SELECT `id`
  FROM `system_role`
  WHERE `tenant_id` = @tenant_id
    AND `code` = 'super_admin'
    AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

/*
 Repair explicit BPM menu bindings for the super_admin role.
 Although the backend already treats super_admin as full-access,
 keeping these rows present makes DB state clearer and avoids confusion
 when people inspect role-menu data directly.
*/
INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT
  @super_admin_role_id,
  candidate.`menu_id`,
  '1',
  NOW(),
  '1',
  NOW(),
  b'0',
  @tenant_id
FROM (
  SELECT 1185 AS `menu_id` UNION ALL -- 工作流程
  SELECT 1186 UNION ALL              -- 流程管理
  SELECT 1193 UNION ALL              -- 流程模型
  SELECT 1194 UNION ALL              -- 模型查询
  SELECT 1195 UNION ALL              -- 模型创建
  SELECT 1197 UNION ALL              -- 模型更新
  SELECT 1198 UNION ALL              -- 模型删除
  SELECT 1199 UNION ALL              -- 模型发布
  SELECT 1215                        -- 流程定义查询
) candidate
LEFT JOIN `system_role_menu` existing
  ON existing.`role_id` = @super_admin_role_id
 AND existing.`menu_id` = candidate.`menu_id`
 AND existing.`tenant_id` = @tenant_id
 AND existing.`deleted` = b'0'
WHERE @super_admin_role_id IS NOT NULL
  AND existing.`id` IS NULL;

/*
 Repair the user-role binding for common admin accounts.
 This is the part that really matters for runtime permission acquisition.
 If the current publishing account uses another username, append it below.
*/
INSERT INTO `system_user_role`
(`user_id`, `role_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT
  candidate_user.`id`,
  @super_admin_role_id,
  '1',
  NOW(),
  '1',
  NOW(),
  b'0',
  @tenant_id
FROM (
  SELECT `id`
  FROM `system_users`
  WHERE `tenant_id` = @tenant_id
    AND `deleted` = b'0'
    AND `username` IN ('admin', 'admin123', 'superadmin')
) candidate_user
LEFT JOIN `system_user_role` existing
  ON existing.`user_id` = candidate_user.`id`
 AND existing.`role_id` = @super_admin_role_id
 AND existing.`tenant_id` = @tenant_id
 AND existing.`deleted` = b'0'
WHERE @super_admin_role_id IS NOT NULL
  AND existing.`id` IS NULL;

SET FOREIGN_KEY_CHECKS = 1;
