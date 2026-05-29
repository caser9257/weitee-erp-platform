/*
 * ERP / SCM 正式入口修复
 * 作用：
 * - 补齐 `/scm/suggest` 菜单入口
 * - 让前端 `MRP 计划` 页跳转到的正式建议页可访问
 * - 将新入口同步到当前已拥有 `/scm` 子菜单的角色
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

START TRANSACTION;

SET @tenant_id := COALESCE((SELECT id FROM system_tenant WHERE deleted = b'0' ORDER BY id LIMIT 1), 1);
SET @scm_root_id := COALESCE(
  (SELECT id FROM system_menu WHERE deleted = b'0' AND parent_id = 0 AND path = '/scm' ORDER BY id LIMIT 1),
  0
);
SET @suggest_menu_id := (
  SELECT id
  FROM system_menu
  WHERE deleted = b'0'
    AND parent_id = @scm_root_id
    AND path = 'suggest'
  ORDER BY id
  LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 931603, 'MRP 运算', '', 2, 30, @scm_root_id, 'suggest', 'ep:histogram', 'erp/mrp/suggest/index',
       'FormalScmSuggest', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @suggest_menu_id IS NULL
  AND @scm_root_id <> 0;

UPDATE `system_menu`
SET `name` = 'MRP 运算',
    `type` = 2,
    `sort` = 30,
    `parent_id` = @scm_root_id,
    `path` = 'suggest',
    `icon` = 'ep:histogram',
    `component` = 'erp/mrp/suggest/index',
    `component_name` = 'FormalScmSuggest',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = COALESCE(@suggest_menu_id, 931603);

SET @suggest_menu_id := COALESCE(@suggest_menu_id, 931603);

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT DISTINCT rm.`role_id`, @suggest_menu_id, '1', NOW(), '1', NOW(), b'0', rm.`tenant_id`
FROM `system_role_menu` rm
JOIN `system_menu` sm ON sm.`id` = rm.`menu_id`
WHERE rm.`deleted` = b'0'
  AND sm.`deleted` = b'0'
  AND sm.`parent_id` = @scm_root_id
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` existing
    WHERE existing.`deleted` = b'0'
      AND existing.`role_id` = rm.`role_id`
      AND existing.`menu_id` = @suggest_menu_id
      AND existing.`tenant_id` = rm.`tenant_id`
  );

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
