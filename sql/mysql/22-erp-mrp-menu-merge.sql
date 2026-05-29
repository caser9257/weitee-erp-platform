/*
 Target: ERP MRP menu merge fix
 Schema: ruoyi-vue-pro
 Date: 2026-04-08
 Note: Merge split MRP menu trees caused by mixed parent definitions (/mrp vs mrp)
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @legacy_mrp_parent_id := (
  SELECT `id` FROM `system_menu`
  WHERE `path` = '/mrp' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @erp_mrp_parent_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = 2563 AND `path` = 'mrp' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @canonical_mrp_parent_id := COALESCE(
  (SELECT `parent_id` FROM `system_menu`
   WHERE `component` = 'erp/mrp/plan-rule/index' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  (SELECT `parent_id` FROM `system_menu`
   WHERE `component` = 'erp/mrp/suggest/index' AND `deleted` = b'0'
   ORDER BY `id` LIMIT 1),
  @legacy_mrp_parent_id,
  @erp_mrp_parent_id
);

SET @plan_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/plan/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

UPDATE `system_menu`
SET `parent_id` = @canonical_mrp_parent_id,
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @plan_menu_id
  AND @canonical_mrp_parent_id IS NOT NULL;

UPDATE `system_menu`
SET `parent_id` = @canonical_mrp_parent_id,
    `updater` = '1',
    `update_time` = NOW()
WHERE `parent_id` IN (@legacy_mrp_parent_id, @erp_mrp_parent_id)
  AND `id` <> @canonical_mrp_parent_id
  AND `type` = 2
  AND `deleted` = b'0'
  AND @canonical_mrp_parent_id IS NOT NULL;

UPDATE `system_menu`
SET `status` = 1,
    `visible` = b'0',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` IN (@legacy_mrp_parent_id, @erp_mrp_parent_id)
  AND `id` <> @canonical_mrp_parent_id
  AND `deleted` = b'0';

UPDATE `system_menu`
SET `name` = 'MRP 管理',
    `status` = 0,
    `visible` = b'1',
    `updater` = '1',
    `update_time` = NOW()
WHERE `id` = @canonical_mrp_parent_id
  AND @canonical_mrp_parent_id IS NOT NULL;

SET FOREIGN_KEY_CHECKS = 1;
