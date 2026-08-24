/*
 Target: ERP MRP supply-chain role compatibility
 Schema: ruoyi-vue-pro
 Date: 2026-04-13
 Note:
   1. Keep BOM permissions independent from plan-rule permissions
   2. Create or repair the supply_chain_manager role
   3. Bind BOM management + MRP capability set to supply_chain_manager
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;


SET @mrp_parent_id := COALESCE(
  (SELECT `parent_id` FROM `system_menu`
   WHERE `component` = 'erp/mrp/plan-rule/index' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  (SELECT `parent_id` FROM `system_menu`
   WHERE `component` = 'erp/mrp/bom/index' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  (SELECT `parent_id` FROM `system_menu`
   WHERE `component` = 'erp/mrp/suggest/index' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  (SELECT `parent_id` FROM `system_menu`
   WHERE `component` = 'erp/mrp/plan/index' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  (SELECT `id` FROM `system_menu`
   WHERE `parent_id` = 2563 AND `path` = 'mrp' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  (SELECT `id` FROM `system_menu`
   WHERE `path` = '/mrp' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1)
);

SET @plan_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/plan/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

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

SET @suggest_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/suggest/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @purchase_order_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/purchase/order/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @purchase_module_menu_id := (
  SELECT `parent_id` FROM `system_menu`
  WHERE `id` = @purchase_order_menu_id
  ORDER BY `id` LIMIT 1
);

UPDATE `system_menu`
SET `name` = '计划参数查询',
    `parent_id` = COALESCE(@plan_rule_menu_id, `parent_id`),
    `sort` = 1,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:mrp-plan-rule:query'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '计划参数创建',
    `parent_id` = COALESCE(@plan_rule_menu_id, `parent_id`),
    `sort` = 2,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:mrp-plan-rule:create'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '计划参数更新',
    `parent_id` = COALESCE(@plan_rule_menu_id, `parent_id`),
    `sort` = 3,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:mrp-plan-rule:update'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '计划参数删除',
    `parent_id` = COALESCE(@plan_rule_menu_id, `parent_id`),
    `sort` = 4,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:mrp-plan-rule:delete'
  AND `deleted` = b'0';

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '计划参数查询', 'erp:mrp-plan-rule:query',
       3, 1, @plan_rule_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @plan_rule_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:mrp-plan-rule:query' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '计划参数创建', 'erp:mrp-plan-rule:create',
       3, 2, @plan_rule_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @plan_rule_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:mrp-plan-rule:create' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '计划参数更新', 'erp:mrp-plan-rule:update',
       3, 3, @plan_rule_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @plan_rule_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:mrp-plan-rule:update' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '计划参数删除', 'erp:mrp-plan-rule:delete',
       3, 4, @plan_rule_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @plan_rule_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:mrp-plan-rule:delete' AND `deleted` = b'0'
  );

UPDATE `system_menu`
SET `name` = 'BOM 管理',
    `parent_id` = COALESCE(@mrp_parent_id, `parent_id`),
    `path` = 'bom',
    `icon` = 'ep:operation',
    `component` = 'erp/mrp/bom/index',
    `component_name` = 'ErpManufactureBom',
    `type` = 2,
    `sort` = 15,
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
    `parent_id` = COALESCE(@bom_menu_id, `parent_id`),
    `sort` = 1,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:bom:query'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = 'BOM 创建',
    `parent_id` = COALESCE(@bom_menu_id, `parent_id`),
    `sort` = 2,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:bom:create'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = 'BOM 更新',
    `parent_id` = COALESCE(@bom_menu_id, `parent_id`),
    `sort` = 3,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:bom:update'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = 'BOM 删除',
    `parent_id` = COALESCE(@bom_menu_id, `parent_id`),
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
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:bom:query' AND `deleted` = b'0'
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
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:bom:create' AND `deleted` = b'0'
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
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:bom:update' AND `deleted` = b'0'
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
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:bom:delete' AND `deleted` = b'0'
  );

UPDATE `system_menu`
SET `permission` = 'erp:mrp-suggest:approve',
    `name` = '建议审核通过',
    `parent_id` = COALESCE(@suggest_menu_id, `parent_id`),
    `sort` = 2,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:mrp-suggest:update'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '建议总览查询',
    `parent_id` = COALESCE(@suggest_menu_id, `parent_id`),
    `sort` = 1,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:mrp-suggest:query'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '建议审核通过',
    `parent_id` = COALESCE(@suggest_menu_id, `parent_id`),
    `sort` = 2,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:mrp-suggest:approve'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '建议驳回',
    `parent_id` = COALESCE(@suggest_menu_id, `parent_id`),
    `sort` = 3,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:mrp-suggest:reject'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '建议转采购订单',
    `parent_id` = COALESCE(@suggest_menu_id, `parent_id`),
    `sort` = 4,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:mrp-suggest:convert-purchase'
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = '建议转生产工单',
    `parent_id` = COALESCE(@suggest_menu_id, `parent_id`),
    `sort` = 5,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:mrp-suggest:convert-production'
  AND `deleted` = b'0';

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '建议审核通过', 'erp:mrp-suggest:approve',
       3, 2, @suggest_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @suggest_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:mrp-suggest:approve' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '建议驳回', 'erp:mrp-suggest:reject',
       3, 3, @suggest_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @suggest_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:mrp-suggest:reject' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '建议转采购订单', 'erp:mrp-suggest:convert-purchase',
       3, 4, @suggest_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @suggest_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:mrp-suggest:convert-purchase' AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '建议转生产工单', 'erp:mrp-suggest:convert-production',
       3, 5, @suggest_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @suggest_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:mrp-suggest:convert-production' AND `deleted` = b'0'
  );

SET @supply_chain_role_id := (
  SELECT `id` FROM `system_role`
  WHERE `code` = 'supply_chain_manager'

  ORDER BY `deleted` ASC, `id` ASC
  LIMIT 1
);

INSERT INTO `system_role`
(`id`, `name`, `code`, `sort`, `data_scope`, `data_scope_dept_ids`, `status`, `type`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT COALESCE(@supply_chain_role_id, (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_role` t)),
       '供应链经理', 'supply_chain_manager', 60, 1, '', 0, 2,
       '负责 BOM 维护、计划参数维护、MRP 计划执行、建议审核与转单', '1', NOW(), '1', NOW(), b'0'
WHERE @supply_chain_role_id IS NULL;

SET @supply_chain_role_id := (
  SELECT `id` FROM `system_role`
  WHERE `code` = 'supply_chain_manager'

  ORDER BY `deleted` ASC, `id` ASC
  LIMIT 1
);

UPDATE `system_role`
SET `name` = '供应链经理',
    `sort` = 60,
    `data_scope` = 1,
    `data_scope_dept_ids` = '',
    `status` = 0,
    `type` = 2,
    `remark` = '负责 BOM 维护、计划参数维护、MRP 计划执行、建议审核与转单',
    `deleted` = b'0',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @supply_chain_role_id;

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @supply_chain_role_id, target.`menu_id`, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT 2563 AS `menu_id`
  UNION
  SELECT @mrp_parent_id
  UNION
  SELECT @plan_menu_id
  UNION
  SELECT @plan_rule_menu_id
  UNION
  SELECT @bom_menu_id
  UNION
  SELECT @suggest_menu_id
  UNION
  SELECT @purchase_module_menu_id
  UNION
  SELECT @purchase_order_menu_id
  UNION
  SELECT `id` FROM `system_menu`
  WHERE `permission` IN (
    'erp:bom:query',
    'erp:bom:create',
    'erp:bom:update',
    'erp:bom:delete',
    'erp:mrp-plan:query',
    'erp:mrp-plan:create',
    'erp:mrp-plan:run',
    'erp:mrp-plan-rule:query',
    'erp:mrp-plan-rule:create',
    'erp:mrp-plan-rule:update',
    'erp:mrp-plan-rule:delete',
    'erp:mrp-suggest:query',
    'erp:mrp-suggest:approve',
    'erp:mrp-suggest:reject',
    'erp:mrp-suggest:convert-purchase',
    'erp:mrp-suggest:convert-production',
    'erp:purchase-order:query',
    'erp:production-order:query'
  )
    AND `deleted` = b'0'
) target
WHERE @supply_chain_role_id IS NOT NULL
  AND target.`menu_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = @supply_chain_role_id
      AND rm.`menu_id` = target.`menu_id`

      AND rm.`deleted` = b'0'
  );

SET FOREIGN_KEY_CHECKS = 1;
