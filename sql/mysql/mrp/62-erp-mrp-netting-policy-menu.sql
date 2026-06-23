/*
 Target: ERP netting-policy menu and permission compatibility
 Schema: ruoyi-vue-pro
 Date: 2026-04-22
 Note:
   1. Align netting-policy under SCM information architecture
   2. Create page menu + query/create/update/delete permissions
   3. Grant to admin and supply_chain_manager roles
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @scm_root_id := COALESCE(
  (SELECT `parent_id`
   FROM `system_menu`
   WHERE `component` = 'erp/mrp/stock-reservation/index' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  (SELECT `parent_id`
   FROM `system_menu`
   WHERE `component` = 'erp/mrp/substitute/index' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  (SELECT `parent_id`
   FROM `system_menu`
   WHERE `component` = 'erp/mrp/plan/index' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  (SELECT `id`
   FROM `system_menu`
   WHERE `parent_id` = 0 AND `path` = '/scm' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1)
);

INSERT INTO `system_menu`
(`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT '供应链管理','',1,350,0,'/scm','ep:shopping-cart-full','','FormalScmRoot',
       0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @scm_root_id IS NULL;

SET @scm_root_id := COALESCE(
  @scm_root_id,
  (SELECT `id`
   FROM `system_menu`
   WHERE `parent_id` = 0 AND `path` = '/scm' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1)
);

SET @netting_policy_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/mrp/netting-policy/index' AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

INSERT INTO `system_menu`
(`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT '净需求策略','',2,75,@scm_root_id,'netting-policy','ep:operation',
       'erp/mrp/netting-policy/index','FormalScmNettingPolicy',
       0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @scm_root_id IS NOT NULL
  AND @netting_policy_menu_id IS NULL;

SET @netting_policy_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/mrp/netting-policy/index' AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

UPDATE `system_menu`
SET `name` = '净需求策略',
    `type` = 2,
    `sort` = 75,
    `parent_id` = @scm_root_id,
    `path` = 'netting-policy',
    `icon` = 'ep:operation',
    `component` = 'erp/mrp/netting-policy/index',
    `component_name` = 'FormalScmNettingPolicy',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @netting_policy_menu_id;

UPDATE `system_menu`
SET `status` = 1,
    `visible` = b'0',
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND `type` = 2
  AND `component` = 'erp/mrp/netting-policy/index'
  AND `id` <> @netting_policy_menu_id;

UPDATE `system_menu`
SET `name` = '净需求策略查询',
    `parent_id` = @netting_policy_menu_id,
    `sort` = 1,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:mrp-netting-policy:query'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '净需求策略创建',
    `parent_id` = @netting_policy_menu_id,
    `sort` = 2,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:mrp-netting-policy:create'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '净需求策略编辑',
    `parent_id` = @netting_policy_menu_id,
    `sort` = 3,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:mrp-netting-policy:update'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '净需求策略删除',
    `parent_id` = @netting_policy_menu_id,
    `sort` = 4,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:mrp-netting-policy:delete'
  AND `deleted` = b'0';

INSERT INTO `system_menu`
(`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,
 `component_name`,`status`,`visible`,`keep_alive`,`always_show`,
 `creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '净需求策略查询', 'erp:mrp-netting-policy:query',
       3, 1, @netting_policy_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @netting_policy_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:mrp-netting-policy:query' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,
 `component_name`,`status`,`visible`,`keep_alive`,`always_show`,
 `creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '净需求策略创建', 'erp:mrp-netting-policy:create',
       3, 2, @netting_policy_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @netting_policy_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:mrp-netting-policy:create' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,
 `component_name`,`status`,`visible`,`keep_alive`,`always_show`,
 `creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '净需求策略编辑', 'erp:mrp-netting-policy:update',
       3, 3, @netting_policy_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @netting_policy_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:mrp-netting-policy:update' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,
 `component_name`,`status`,`visible`,`keep_alive`,`always_show`,
 `creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '净需求策略删除', 'erp:mrp-netting-policy:delete',
       3, 4, @netting_policy_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @netting_policy_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:mrp-netting-policy:delete' AND `deleted` = b'0'
  );

SET @admin_tenant_id := COALESCE(
  (SELECT `tenant_id` FROM `system_role` WHERE `id` = 1 LIMIT 1),
  1
);

INSERT INTO `system_role_menu`
(`role_id`,`menu_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`,`tenant_id`)
SELECT 1, target.`menu_id`, '1', NOW(), '1', NOW(), b'0', @admin_tenant_id
FROM (
  SELECT @scm_root_id AS `menu_id`
  UNION
  SELECT @netting_policy_menu_id
  UNION
  SELECT `id` FROM `system_menu`
  WHERE `permission` IN (
    'erp:mrp-netting-policy:query',
    'erp:mrp-netting-policy:create',
    'erp:mrp-netting-policy:update',
    'erp:mrp-netting-policy:delete'
  )
    AND `deleted` = b'0'
) target
WHERE target.`menu_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = 1
      AND rm.`menu_id` = target.`menu_id`
      AND rm.`tenant_id` = @admin_tenant_id
      AND rm.`deleted` = b'0'
  );

INSERT INTO `system_role_menu`
(`role_id`,`menu_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`,`tenant_id`)
SELECT role.`id`, target.`menu_id`, '1', NOW(), '1', NOW(), b'0', role.`tenant_id`
FROM `system_role` role
JOIN (
  SELECT @scm_root_id AS `menu_id`
  UNION
  SELECT @netting_policy_menu_id
  UNION
  SELECT `id` FROM `system_menu`
  WHERE `permission` IN (
    'erp:mrp-netting-policy:query',
    'erp:mrp-netting-policy:create',
    'erp:mrp-netting-policy:update',
    'erp:mrp-netting-policy:delete'
  )
    AND `deleted` = b'0'
) target ON target.`menu_id` IS NOT NULL
WHERE role.`code` = 'supply_chain_manager'
  AND role.`deleted` = b'0'
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = role.`id`
      AND rm.`menu_id` = target.`menu_id`
      AND rm.`tenant_id` = role.`tenant_id`
      AND rm.`deleted` = b'0'
  );

SET FOREIGN_KEY_CHECKS = 1;
