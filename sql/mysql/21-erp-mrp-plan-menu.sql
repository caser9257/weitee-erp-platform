/*
 Target: ERP MRP plan menu migration
 Schema: ruoyi-vue-pro
 Date: 2026-04-07
 Note: Compatible with MySQL versions that do not support ALTER TABLE ... ADD COLUMN IF NOT EXISTS
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), 'MRP 管理', '', 1, 60, 2563, 'mrp',
       'ep:data-analysis', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = 2563 AND `path` = 'mrp' AND `deleted` = b'0'
);

SET @mrp_parent_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = 2563 AND `path` = 'mrp' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @plan_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/plan/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

UPDATE `system_menu`
SET `name` = 'MRP计划',
    `type` = 2,
    `sort` = 15,
    `parent_id` = @mrp_parent_id,
    `path` = 'plan',
    `icon` = 'ep:calendar',
    `component_name` = 'ErpMrpPlan',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @plan_menu_id;

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), 'MRP计划', '', 2, 15, @mrp_parent_id, 'plan',
       'ep:calendar', 'erp/mrp/plan/index', 'ErpMrpPlan',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @plan_menu_id IS NULL;

SET @plan_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/plan/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

UPDATE `system_menu`
SET `parent_id` = @plan_menu_id, `updater` = '1', `update_time` = NOW()
WHERE `permission` IN ('erp:mrp-plan:query', 'erp:mrp-plan:create', 'erp:mrp-plan:run')
  AND `deleted` = b'0';

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), 'MRP计划查询', 'erp:mrp-plan:query', 3, 1, @plan_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:mrp-plan:query' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), 'MRP计划创建', 'erp:mrp-plan:create', 3, 2, @plan_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:mrp-plan:create' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), 'MRP计划运行', 'erp:mrp-plan:run', 3, 3, @plan_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:mrp-plan:run' AND `deleted` = b'0'
);

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, m.`id`, '1', NOW(), '1', NOW(), b'0'
FROM `system_menu` m
WHERE m.`id` IN (@mrp_parent_id, @plan_menu_id)
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = 1 AND rm.`menu_id` = m.`id` AND rm.`deleted` = b'0'
  );

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, m.`id`, '1', NOW(), '1', NOW(), b'0'
FROM `system_menu` m
WHERE m.`permission` IN ('erp:mrp-plan:query', 'erp:mrp-plan:create', 'erp:mrp-plan:run')
  AND m.`deleted` = b'0'
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = 1 AND rm.`menu_id` = m.`id` AND rm.`deleted` = b'0'
  );

SET FOREIGN_KEY_CHECKS = 1;
