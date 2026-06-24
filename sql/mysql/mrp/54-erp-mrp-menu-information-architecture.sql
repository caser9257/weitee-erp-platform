/*
 Target: ERP MRP menu information architecture alignment
 Schema: ruoyi-vue-pro
 Date: 2026-04-17
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

INSERT INTO `system_menu`
(`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT '供应链管理','',1,350,0,'/scm','ep:shopping-cart-full','','FormalScmRoot',
       0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu` WHERE `parent_id` = 0 AND `path` = '/scm' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT '工艺管理','',1,370,0,'/process','ep:connection','','FormalProcessRoot',
       0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu` WHERE `parent_id` = 0 AND `path` = '/process' AND `deleted` = b'0'
);

SET @scm_root_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/scm' AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

SET @process_root_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/process' AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

SET @plan_rule_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/mrp/plan-rule/index' AND `deleted` = b'0'
  ORDER BY
    CASE
      WHEN `id` = 5049 THEN 0
      WHEN `id` = 930131 THEN 1
      ELSE 2
    END,
    `id`
  LIMIT 1
);

INSERT INTO `system_menu`
(`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT '计划参数','',2,10,@scm_root_id,'plan-rule','ep:setting','erp/mrp/plan-rule/index','FormalScmPlanRule',
       0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @plan_rule_menu_id IS NULL;

SET @plan_rule_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/mrp/plan-rule/index' AND `deleted` = b'0'
  ORDER BY
    CASE
      WHEN `id` = 5049 THEN 0
      WHEN `id` = 930131 THEN 1
      ELSE 2
    END,
    `id`
  LIMIT 1
);

UPDATE `system_menu`
SET `name` = '计划参数',
    `type` = 2,
    `sort` = 10,
    `parent_id` = @scm_root_id,
    `path` = 'plan-rule',
    `icon` = 'ep:setting',
    `component` = 'erp/mrp/plan-rule/index',
    `component_name` = 'FormalScmPlanRule',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @plan_rule_menu_id;

UPDATE `system_menu`
SET `status` = 1,
    `visible` = b'0',
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND `type` = 2
  AND `component` = 'erp/mrp/plan-rule/index'
  AND `id` <> @plan_rule_menu_id;

SET @plan_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/mrp/plan/index' AND `deleted` = b'0'
  ORDER BY
    CASE
      WHEN `id` = 5068 THEN 0
      WHEN `id` = 930132 THEN 1
      ELSE 2
    END,
    `id`
  LIMIT 1
);

INSERT INTO `system_menu`
(`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 'MRP计划','',2,20,@scm_root_id,'plan','ep:calendar','erp/mrp/plan/index','FormalScmPlan',
       0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @plan_menu_id IS NULL;

SET @plan_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/mrp/plan/index' AND `deleted` = b'0'
  ORDER BY
    CASE
      WHEN `id` = 5068 THEN 0
      WHEN `id` = 930132 THEN 1
      ELSE 2
    END,
    `id`
  LIMIT 1
);

UPDATE `system_menu`
SET `name` = 'MRP计划',
    `type` = 2,
    `sort` = 20,
    `parent_id` = @scm_root_id,
    `path` = 'plan',
    `icon` = 'ep:calendar',
    `component` = 'erp/mrp/plan/index',
    `component_name` = 'FormalScmPlan',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @plan_menu_id;

UPDATE `system_menu`
SET `status` = 1,
    `visible` = b'0',
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND `type` = 2
  AND `component` = 'erp/mrp/plan/index'
  AND `id` <> @plan_menu_id;

SET @suggest_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/mrp/suggest/index' AND `deleted` = b'0'
  ORDER BY
    CASE
      WHEN `id` = 3276 THEN 0
      WHEN `id` = 930134 THEN 1
      ELSE 2
    END,
    `id`
  LIMIT 1
);

INSERT INTO `system_menu`
(`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT 'MRP运算','',2,30,@scm_root_id,'suggest','ep:histogram','erp/mrp/suggest/index','FormalScmSuggest',
       0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @suggest_menu_id IS NULL;

SET @suggest_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/mrp/suggest/index' AND `deleted` = b'0'
  ORDER BY
    CASE
      WHEN `id` = 3276 THEN 0
      WHEN `id` = 930134 THEN 1
      ELSE 2
    END,
    `id`
  LIMIT 1
);

UPDATE `system_menu`
SET `name` = 'MRP运算',
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
WHERE `id` = @suggest_menu_id;

UPDATE `system_menu`
SET `status` = 1,
    `visible` = b'0',
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND `type` = 2
  AND `component` = 'erp/mrp/suggest/index'
  AND `id` <> @suggest_menu_id;

SET @manufacture_bom_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/mrp/bom/index' AND `deleted` = b'0'
  ORDER BY
    CASE
      WHEN `id` = 920524 THEN 0
      WHEN `id` = 930133 THEN 1
      ELSE 2
    END,
    `id`
  LIMIT 1
);

INSERT INTO `system_menu`
(`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,
 `status`,`visible`,`keep_alive`,`always_show`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT '制造BOM','',2,10,@process_root_id,'bom','ep:collection','erp/mrp/bom/index','FormalProcessManufactureBom',
       0,b'1',b'1',b'1','1',NOW(),'1',NOW(),b'0'
WHERE @manufacture_bom_menu_id IS NULL;

SET @manufacture_bom_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/mrp/bom/index' AND `deleted` = b'0'
  ORDER BY
    CASE
      WHEN `id` = 920524 THEN 0
      WHEN `id` = 930133 THEN 1
      ELSE 2
    END,
    `id`
  LIMIT 1
);

UPDATE `system_menu`
SET `name` = '制造BOM',
    `type` = 2,
    `sort` = 10,
    `parent_id` = @process_root_id,
    `path` = 'bom',
    `icon` = 'ep:collection',
    `component` = 'erp/mrp/bom/index',
    `component_name` = 'FormalProcessManufactureBom',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @manufacture_bom_menu_id;

UPDATE `system_menu`
SET `status` = 1,
    `visible` = b'0',
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND `type` = 2
  AND `component` = 'erp/mrp/bom/index'
  AND `id` <> @manufacture_bom_menu_id;

UPDATE `system_menu`
SET `name` = '工艺路线',
    `type` = 2,
    `sort` = 20,
    `parent_id` = @process_root_id,
    `path` = 'route',
    `icon` = 'ep:share',
    `component` = 'erp/manufacturing/process-route/index',
    `component_name` = 'FormalProcessRoute',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND `component` = 'erp/manufacturing/process-route/index';

SET @process_route_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/manufacturing/process-route/index' AND `deleted` = b'0'
  ORDER BY
    CASE
      WHEN `id` = 3201 THEN 0
      WHEN `id` = 930161 THEN 1
      ELSE 2
    END,
    `id`
  LIMIT 1
);

UPDATE `system_menu`
SET `status` = 1,
    `visible` = b'0',
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND `type` = 2
  AND `component` = 'erp/manufacturing/process-route/index'
  AND `id` <> @process_route_menu_id;

UPDATE `system_menu`
SET `name` = '工作中心',
    `type` = 2,
    `sort` = 30,
    `parent_id` = @process_root_id,
    `path` = 'work-center',
    `icon` = 'ep:office-building',
    `component` = 'erp/manufacturing/work-center/index',
    `component_name` = 'FormalProcessWorkCenter',
    `status` = 0,
    `visible` = b'1',
    `keep_alive` = b'1',
    `always_show` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND `component` = 'erp/manufacturing/work-center/index';

SET @work_center_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/manufacturing/work-center/index' AND `deleted` = b'0'
  ORDER BY
    CASE
      WHEN `id` = 3202 THEN 0
      WHEN `id` = 930162 THEN 1
      ELSE 2
    END,
    `id`
  LIMIT 1
);

UPDATE `system_menu`
SET `status` = 1,
    `visible` = b'0',
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND `type` = 2
  AND `component` = 'erp/manufacturing/work-center/index'
  AND `id` <> @work_center_menu_id;

UPDATE `system_menu`
SET `name` = '制造BOM查询',
    `parent_id` = @manufacture_bom_menu_id,
    `sort` = 1,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:bom:query' AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '制造BOM创建',
    `parent_id` = @manufacture_bom_menu_id,
    `sort` = 2,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:bom:create' AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '制造BOM编辑',
    `parent_id` = @manufacture_bom_menu_id,
    `sort` = 3,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:bom:update' AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '制造BOM删除',
    `parent_id` = @manufacture_bom_menu_id,
    `sort` = 4,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:bom:delete' AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '制造BOM生效停用',
    `parent_id` = @manufacture_bom_menu_id,
    `sort` = 5,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:bom:update-status' AND `deleted` = b'0';

INSERT INTO `system_role_menu`
(`role_id`,`menu_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT DISTINCT rm.`role_id`, @scm_root_id, '1', NOW(), '1', NOW(), b'0'
FROM `system_role_menu` rm
WHERE rm.`deleted` = b'0'
  AND rm.`menu_id` IN (@plan_rule_menu_id, @plan_menu_id, @suggest_menu_id)
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` exists_rm
    WHERE exists_rm.`role_id` = rm.`role_id`
      AND exists_rm.`menu_id` = @scm_root_id
      AND exists_rm.`deleted` = b'0'
  );

INSERT INTO `system_role_menu`
(`role_id`,`menu_id`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT DISTINCT rm.`role_id`, @process_root_id, '1', NOW(), '1', NOW(), b'0'
FROM `system_role_menu` rm
WHERE rm.`deleted` = b'0'
  AND rm.`menu_id` IN (@manufacture_bom_menu_id, @process_route_menu_id, @work_center_menu_id)
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` exists_rm
    WHERE exists_rm.`role_id` = rm.`role_id`
      AND exists_rm.`menu_id` = @process_root_id
      AND exists_rm.`deleted` = b'0'
  );

UPDATE `system_menu`
SET `status` = 1,
    `visible` = b'0',
    `updater` = '1',
    `update_time` = NOW()
WHERE `deleted` = b'0'
  AND `type` = 1
  AND `path` IN ('/mrp', 'mrp', '/erp');

SET FOREIGN_KEY_CHECKS = 1;
