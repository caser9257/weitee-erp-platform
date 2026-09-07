/*
 Target: ERP dual-layer BOM role compatibility
 Schema: ruoyi-vue-pro
 Date: 2026-04-13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;



SET @manufacture_bom_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/mrp/bom/index'
    AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

UPDATE `system_menu`
SET `name` = '制造BOM生效停用',
    `parent_id` = COALESCE(@manufacture_bom_menu_id, `parent_id`),
    `sort` = 5,
    `updater` = '1',
    `update_time` = NOW()
WHERE `permission` = 'erp:bom:update-status'
  AND `deleted` = b'0';

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '制造BOM生效停用', 'erp:bom:update-status',
       3, 5, @manufacture_bom_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @manufacture_bom_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:bom:update-status' AND `deleted` = b'0'
  );

SET @supply_chain_role_id := (
  SELECT `id`
  FROM `system_role`
  WHERE `code` = 'supply_chain_manager'

  ORDER BY `deleted` ASC, `id` ASC
  LIMIT 1
);

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @supply_chain_role_id, m.`id`, '1', NOW(), '1', NOW(), b'0'
FROM `system_menu` m
WHERE @supply_chain_role_id IS NOT NULL
  AND m.`permission` IN (
    'erp:bom:query', 'erp:bom:create', 'erp:bom:update', 'erp:bom:delete', 'erp:bom:update-status'
  )
  AND m.`deleted` = b'0'
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` rm
    WHERE rm.`role_id` = @supply_chain_role_id
      AND rm.`menu_id` = m.`id`

      AND rm.`deleted` = b'0'
  );

SET FOREIGN_KEY_CHECKS = 1;
