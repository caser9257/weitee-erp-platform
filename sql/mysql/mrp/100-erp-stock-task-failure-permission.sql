/*
  库存任务失败记录：查询 / 重试 权限点
  父菜单锚定"生产工单"（component = erp/manufacturing/production-order/index）
  幂等：WHERE NOT EXISTS
*/
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE `ruoyi-vue-pro`;

SET @production_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/manufacturing/production-order/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

-- 按钮权限：查询失败记录
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id),0)+1 FROM `system_menu` t), '库存失败记录查询', 'erp:stock-task-failure:query',
       3, 90, @production_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @production_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission`='erp:stock-task-failure:query' AND `deleted`=b'0');

-- 按钮权限：重试失败任务
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id),0)+1 FROM `system_menu` t), '库存失败记录重试', 'erp:stock-task-failure:retry',
       3, 91, @production_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @production_menu_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission`='erp:stock-task-failure:retry' AND `deleted`=b'0');

-- 赋给超级管理员（role_id=1）
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, m.`id`, '1', NOW(), '1', NOW(), b'0'
FROM `system_menu` m
WHERE m.`permission` IN ('erp:stock-task-failure:query','erp:stock-task-failure:retry')
  AND m.`deleted`=b'0'
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id`=1 AND rm.`menu_id`=m.`id` AND rm.`deleted`=b'0'
  );

SET FOREIGN_KEY_CHECKS = 1;
