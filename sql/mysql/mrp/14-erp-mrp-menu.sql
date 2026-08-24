/*
 Target: ERP MRP menu migration
 Schema: ruoyi-vue-pro
 Date: 2026-04-13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), 'MRP 管理', '', 1, 60, 2563, 'mrp', 'ep:data-analysis', '', '',
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

SET @plan_rule_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/plan-rule/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

UPDATE `system_menu`
SET `name` = '计划参数',
    `type` = 2,
    `sort` = 10,
    `parent_id` = @mrp_parent_id,
    `path` = 'plan-rule',
    `icon` = 'ep:setting',
    `component_name` = 'ErpPlanRule',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @plan_rule_menu_id;

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '计划参数', '', 2, 10, @mrp_parent_id, 'plan-rule', 'ep:setting',
       'erp/mrp/plan-rule/index', 'ErpPlanRule',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @plan_rule_menu_id IS NULL;

SET @plan_rule_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/plan-rule/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @bom_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/bom/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

UPDATE `system_menu`
SET `name` = 'BOM 管理',
    `type` = 2,
    `sort` = 15,
    `parent_id` = @mrp_parent_id,
    `path` = 'bom',
    `icon` = 'ep:operation',
    `component` = 'erp/mrp/bom/index',
    `component_name` = 'ErpManufactureBom',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @bom_menu_id;

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), 'BOM 管理', '', 2, 15,
       @mrp_parent_id, 'bom', 'ep:operation', 'erp/mrp/bom/index', 'ErpManufactureBom',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @mrp_parent_id IS NOT NULL
  AND @bom_menu_id IS NULL;

SET @bom_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/bom/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

UPDATE `system_menu`
SET `name` = 'BOM 查询',
    `parent_id` = @bom_menu_id,
    `sort` = 1,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:bom:query'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = 'BOM 创建',
    `parent_id` = @bom_menu_id,
    `sort` = 2,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:bom:create'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = 'BOM 更新',
    `parent_id` = @bom_menu_id,
    `sort` = 3,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:bom:update'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = 'BOM 删除',
    `parent_id` = @bom_menu_id,
    `sort` = 4,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:bom:delete'
  AND `deleted` = b'0';

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), 'BOM 查询', 'erp:bom:query',
       3, 1, @bom_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @bom_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:bom:query' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), 'BOM 创建', 'erp:bom:create',
       3, 2, @bom_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @bom_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:bom:create' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), 'BOM 更新', 'erp:bom:update',
       3, 3, @bom_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @bom_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:bom:update' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), 'BOM 删除', 'erp:bom:delete',
       3, 4, @bom_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @bom_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:bom:delete' AND `deleted` = b'0'
  );

SET @suggest_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/suggest/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

UPDATE `system_menu`
SET `name` = '建议总览',
    `type` = 2,
    `sort` = 20,
    `parent_id` = @mrp_parent_id,
    `path` = 'suggest',
    `icon` = 'ep:histogram',
    `component_name` = 'ErpMrpSuggest',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @suggest_menu_id;

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '建议总览', '', 2, 20, @mrp_parent_id, 'suggest', 'ep:histogram',
       'erp/mrp/suggest/index', 'ErpMrpSuggest',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @suggest_menu_id IS NULL;

SET @suggest_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/suggest/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

UPDATE `system_menu`
SET `parent_id` = @suggest_menu_id, `updater` = '1', `update_time` = NOW()
WHERE `permission` IN ('erp:mrp-suggest:query', 'erp:mrp-suggest:update')
  AND `deleted` = b'0';

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '建议总览查询', 'erp:mrp-suggest:query', 3, 1, @suggest_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:mrp-suggest:query' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '建议总览操作', 'erp:mrp-suggest:update', 3, 2, @suggest_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu` WHERE `permission` = 'erp:mrp-suggest:update' AND `deleted` = b'0'
);

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, m.`id`, '1', NOW(), '1', NOW(), b'0'
FROM `system_menu` m
WHERE m.`id` IN (@mrp_parent_id, @plan_rule_menu_id, @bom_menu_id, @suggest_menu_id)
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = 1 AND rm.`menu_id` = m.`id` AND rm.`deleted` = b'0'
  );

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, m.`id`, '1', NOW(), '1', NOW(), b'0'
FROM `system_menu` m
WHERE m.`permission` IN (
  'erp:bom:query', 'erp:bom:create', 'erp:bom:update', 'erp:bom:delete',
  'erp:mrp-suggest:query', 'erp:mrp-suggest:update'
)
  AND m.`deleted` = b'0'
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = 1 AND rm.`menu_id` = m.`id` AND rm.`deleted` = b'0'
  );

SET FOREIGN_KEY_CHECKS = 1;
