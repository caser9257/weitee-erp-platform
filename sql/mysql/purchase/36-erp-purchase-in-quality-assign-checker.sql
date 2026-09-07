/*
 Target: ERP 采购入库 IQC 指派质检人
 Schema: ruoyi-vue-pro
 Date: 2026-04-13

 说明：
 1. 给 erp_purchase_in_quality 增加指派质检人字段
 2. 新增 IQC 指派质检人按钮权限
 3. 超级管理员、供应链经理拥有指派权限
 4. IQC 质检员不再拥有创建 / 发起复检 / 指派权限，只保留查询、首检、复检
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


SET @admin_role_id = 1;
SET @purchase_root_menu_id = 2563;
SET @purchase_menu_id = 2602;

SET @add_assigned_checker_user_id := (
  SELECT COUNT(*)
  FROM `information_schema`.`COLUMNS`
  WHERE `TABLE_SCHEMA` = DATABASE()
    AND `TABLE_NAME` = 'erp_purchase_in_quality'
    AND `COLUMN_NAME` = 'assigned_checker_user_id'
);
SET @sql := IF(
  @add_assigned_checker_user_id = 0,
  'ALTER TABLE `erp_purchase_in_quality` ADD COLUMN `assigned_checker_user_id` bigint DEFAULT NULL COMMENT ''指派质检人'' AFTER `max_sample_count`',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_assigned_checker_time := (
  SELECT COUNT(*)
  FROM `information_schema`.`COLUMNS`
  WHERE `TABLE_SCHEMA` = DATABASE()
    AND `TABLE_NAME` = 'erp_purchase_in_quality'
    AND `COLUMN_NAME` = 'assigned_checker_time'
);
SET @sql := IF(
  @add_assigned_checker_time = 0,
  'ALTER TABLE `erp_purchase_in_quality` ADD COLUMN `assigned_checker_time` datetime DEFAULT NULL COMMENT ''指派时间'' AFTER `assigned_checker_user_id`',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @iqc_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE (`component_name` = 'ErpPurchaseInQuality' OR (`path` = 'in-quality' AND `parent_id` = @purchase_menu_id))
    AND `deleted` = b'0'
  ORDER BY `id` ASC
  LIMIT 1
);
SET @iqc_query_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `permission` = 'erp:purchase-in-quality:query' AND `deleted` = b'0'
  ORDER BY `id` ASC
  LIMIT 1
);
SET @iqc_create_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `permission` = 'erp:purchase-in-quality:create' AND `deleted` = b'0'
  ORDER BY `id` ASC
  LIMIT 1
);
SET @iqc_first_check_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `permission` = 'erp:purchase-in-quality:first-check' AND `deleted` = b'0'
  ORDER BY `id` ASC
  LIMIT 1
);
SET @iqc_start_recheck_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `permission` = 'erp:purchase-in-quality:start-recheck' AND `deleted` = b'0'
  ORDER BY `id` ASC
  LIMIT 1
);
SET @iqc_recheck_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `permission` = 'erp:purchase-in-quality:recheck' AND `deleted` = b'0'
  ORDER BY `id` ASC
  LIMIT 1
);
SET @iqc_assign_checker_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `permission` = 'erp:purchase-in-quality:assign-checker' AND `deleted` = b'0'
  ORDER BY `id` ASC
  LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT COALESCE(@iqc_assign_checker_menu_id, 920567), '指派质检人', 'erp:purchase-in-quality:assign-checker', 3, 6, @iqc_menu_id,
       '', '', '', '', 0, b'1', b'0', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @iqc_menu_id IS NOT NULL
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`permission` = VALUES(`permission`),
`type` = VALUES(`type`),
`sort` = VALUES(`sort`),
`parent_id` = VALUES(`parent_id`),
`status` = VALUES(`status`),
`visible` = VALUES(`visible`),
`keep_alive` = VALUES(`keep_alive`),
`always_show` = VALUES(`always_show`),
`updater` = VALUES(`updater`),
`update_time` = VALUES(`update_time`),
`deleted` = VALUES(`deleted`);

SET @iqc_assign_checker_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `permission` = 'erp:purchase-in-quality:assign-checker' AND `deleted` = b'0'
  ORDER BY `id` ASC
  LIMIT 1
);

SET @iqc_role_id := (
  SELECT `id` FROM `system_role`
  WHERE `code` = 'erp_iqc_inspector'

  ORDER BY `deleted` ASC, `id` ASC
  LIMIT 1
);

SET @supply_chain_role_id := (
  SELECT `id` FROM `system_role`
  WHERE `code` = 'supply_chain_manager'

  ORDER BY `deleted` ASC, `id` ASC
  LIMIT 1
);

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @admin_role_id, target.`menu_id`, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT @purchase_root_menu_id AS `menu_id`
  UNION ALL SELECT @purchase_menu_id
  UNION ALL SELECT @iqc_menu_id
  UNION ALL SELECT @iqc_assign_checker_menu_id
) target
WHERE target.`menu_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = @admin_role_id
      AND rm.`menu_id` = target.`menu_id`

      AND rm.`deleted` = b'0'
  );

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @supply_chain_role_id, target.`menu_id`, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT @purchase_root_menu_id AS `menu_id`
  UNION ALL SELECT @purchase_menu_id
  UNION ALL SELECT @iqc_menu_id
  UNION ALL SELECT @iqc_query_menu_id
  UNION ALL SELECT @iqc_create_menu_id
  UNION ALL SELECT @iqc_start_recheck_menu_id
  UNION ALL SELECT @iqc_assign_checker_menu_id
) target
WHERE @supply_chain_role_id IS NOT NULL
  AND target.`menu_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = @supply_chain_role_id
      AND rm.`menu_id` = target.`menu_id`

      AND rm.`deleted` = b'0'
  );

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @iqc_role_id, target.`menu_id`, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT @purchase_root_menu_id AS `menu_id`
  UNION ALL SELECT @purchase_menu_id
  UNION ALL SELECT @iqc_menu_id
  UNION ALL SELECT @iqc_query_menu_id
  UNION ALL SELECT @iqc_first_check_menu_id
  UNION ALL SELECT @iqc_recheck_menu_id
) target
WHERE @iqc_role_id IS NOT NULL
  AND target.`menu_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = @iqc_role_id
      AND rm.`menu_id` = target.`menu_id`

      AND rm.`deleted` = b'0'
  );

DELETE FROM `system_role_menu`
WHERE `role_id` = @iqc_role_id

  AND `deleted` = b'0'
  AND `menu_id` IN (
    COALESCE(@iqc_create_menu_id, -1),
    COALESCE(@iqc_start_recheck_menu_id, -1),
    COALESCE(@iqc_assign_checker_menu_id, -1)
  );

SET FOREIGN_KEY_CHECKS = 1;
