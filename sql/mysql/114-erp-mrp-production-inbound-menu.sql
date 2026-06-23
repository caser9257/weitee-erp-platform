/*
 自制入库菜单补齐
 目标：为自制入库提供正式 SCM 菜单入口、查询/更新权限点，并同步授权到已有 SCM 角色
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @tenant_id := 1;
SET @scm_root_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `parent_id` = 0
    AND `path` = '/scm'
    AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

SET @production_inbound_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/mrp/production-inbound/index'
    AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT COALESCE(@production_inbound_menu_id, (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t)),
       '自制入库', '', 2, 85,
       @scm_root_id, 'production-inbound', 'ep:box', 'erp/mrp/production-inbound/index', 'ErpProductionInboundPage',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @scm_root_id IS NOT NULL
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`permission` = VALUES(`permission`),
`type` = VALUES(`type`),
`sort` = VALUES(`sort`),
`parent_id` = VALUES(`parent_id`),
`path` = VALUES(`path`),
`icon` = VALUES(`icon`),
`component` = VALUES(`component`),
`component_name` = VALUES(`component_name`),
`status` = VALUES(`status`),
`visible` = VALUES(`visible`),
`keep_alive` = VALUES(`keep_alive`),
`always_show` = VALUES(`always_show`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`);

SET @production_inbound_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/mrp/production-inbound/index'
    AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '自制入库查询', 'erp:production-inbound:query', 3, 1,
       @production_inbound_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @production_inbound_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:production-inbound:query' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '自制入库更新', 'erp:production-inbound:update', 3, 2,
       @production_inbound_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @production_inbound_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:production-inbound:update' AND `deleted` = b'0'
  );

DROP TEMPORARY TABLE IF EXISTS `tmp_production_inbound_role_ids`;
CREATE TEMPORARY TABLE `tmp_production_inbound_role_ids` (
  `role_id` BIGINT NOT NULL PRIMARY KEY
) ENGINE=MEMORY;

INSERT INTO `tmp_production_inbound_role_ids` (`role_id`)
SELECT DISTINCT rm.`role_id`
FROM `system_role_menu` rm
JOIN `system_menu` sm ON sm.`id` = rm.`menu_id`
WHERE rm.`deleted` = b'0'
  AND rm.`tenant_id` = @tenant_id
  AND sm.`deleted` = b'0'
  AND (
    sm.`id` = @scm_root_id
    OR sm.`parent_id` = @scm_root_id
  );

INSERT IGNORE INTO `tmp_production_inbound_role_ids` (`role_id`) VALUES (1);

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT role_ids.`role_id`, @production_inbound_menu_id, '1', NOW(), '1', NOW(), b'0', @tenant_id
FROM `tmp_production_inbound_role_ids` role_ids
WHERE @production_inbound_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` rm
    WHERE rm.`role_id` = role_ids.`role_id`
      AND rm.`menu_id` = @production_inbound_menu_id
      AND rm.`tenant_id` = @tenant_id
      AND rm.`deleted` = b'0'
  );

DROP TEMPORARY TABLE IF EXISTS `tmp_production_inbound_role_ids`;

SET FOREIGN_KEY_CHECKS = 1;
