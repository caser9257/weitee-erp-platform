/*
 Target: ERP 采购入库 IQC 权限修复（多租户兼容）
 Schema: ruoyi-vue-pro
 Date: 2026-04-13

 说明：
 1. 修复 IQC 菜单存在，但不同 tenant_id 角色未获得权限的问题
 2. 兼容供应链经理、IQC 质检员、超级管理员等不同租户角色
 3. 脚本可重复执行
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @purchase_root_menu_id = 2563;
SET @purchase_menu_id = 2602;

SET @iqc_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE (`component_name` = 'ErpPurchaseInQuality' OR (`path` = 'in-quality' AND `parent_id` = @purchase_menu_id))
    AND `deleted` = b'0'
  ORDER BY `id` ASC
  LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT COALESCE(@iqc_menu_id, 920561), '采购入库 IQC', '', 2, 7, @purchase_menu_id, 'in-quality', 'ep:finished',
       'erp/purchase/in-quality/index', 'ErpPurchaseInQuality',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
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

SET @iqc_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE (`component_name` = 'ErpPurchaseInQuality' OR (`path` = 'in-quality' AND `parent_id` = @purchase_menu_id))
    AND `deleted` = b'0'
  ORDER BY `id` ASC
  LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 920562, '质检单查询', 'erp:purchase-in-quality:query', 3, 1, @iqc_menu_id,
       '', '', '', '', 0, b'1', b'0', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @iqc_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:purchase-in-quality:query'
      AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 920563, '质检单创建', 'erp:purchase-in-quality:create', 3, 2, @iqc_menu_id,
       '', '', '', '', 0, b'1', b'0', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @iqc_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:purchase-in-quality:create'
      AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 920564, '质检单初检', 'erp:purchase-in-quality:first-check', 3, 3, @iqc_menu_id,
       '', '', '', '', 0, b'1', b'0', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @iqc_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:purchase-in-quality:first-check'
      AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 920565, '发起复检', 'erp:purchase-in-quality:start-recheck', 3, 4, @iqc_menu_id,
       '', '', '', '', 0, b'1', b'0', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @iqc_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:purchase-in-quality:start-recheck'
      AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 920566, '质检单复检', 'erp:purchase-in-quality:recheck', 3, 5, @iqc_menu_id,
       '', '', '', '', 0, b'1', b'0', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @iqc_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:purchase-in-quality:recheck'
      AND `deleted` = b'0'
  );

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 920567, '指派质检人', 'erp:purchase-in-quality:assign-checker', 3, 6, @iqc_menu_id,
       '', '', '', '', 0, b'1', b'0', b'0', '1', NOW(), '1', NOW(), b'0'
WHERE @iqc_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:purchase-in-quality:assign-checker'
      AND `deleted` = b'0'
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

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT r.`id`, target.`menu_id`, '1', NOW(), '1', NOW(), b'0', r.`tenant_id`
FROM `system_role` r
JOIN (
  SELECT @purchase_root_menu_id AS `menu_id`
  UNION ALL SELECT @purchase_menu_id
  UNION ALL SELECT @iqc_menu_id
  UNION ALL SELECT @iqc_query_menu_id
  UNION ALL SELECT @iqc_create_menu_id
  UNION ALL SELECT @iqc_start_recheck_menu_id
  UNION ALL SELECT @iqc_assign_checker_menu_id
) target ON 1 = 1
WHERE r.`deleted` = b'0'
  AND r.`code` = 'supply_chain_manager'
  AND target.`menu_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` rm
    WHERE rm.`role_id` = r.`id`
      AND rm.`menu_id` = target.`menu_id`
      AND rm.`tenant_id` = r.`tenant_id`
      AND rm.`deleted` = b'0'
  );

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT r.`id`, target.`menu_id`, '1', NOW(), '1', NOW(), b'0', r.`tenant_id`
FROM `system_role` r
JOIN (
  SELECT @purchase_root_menu_id AS `menu_id`
  UNION ALL SELECT @purchase_menu_id
  UNION ALL SELECT @iqc_menu_id
  UNION ALL SELECT @iqc_query_menu_id
  UNION ALL SELECT @iqc_first_check_menu_id
  UNION ALL SELECT @iqc_recheck_menu_id
) target ON 1 = 1
WHERE r.`deleted` = b'0'
  AND r.`code` = 'erp_iqc_inspector'
  AND target.`menu_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` rm
    WHERE rm.`role_id` = r.`id`
      AND rm.`menu_id` = target.`menu_id`
      AND rm.`tenant_id` = r.`tenant_id`
      AND rm.`deleted` = b'0'
  );

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT r.`id`, target.`menu_id`, '1', NOW(), '1', NOW(), b'0', r.`tenant_id`
FROM `system_role` r
JOIN (
  SELECT @purchase_root_menu_id AS `menu_id`
  UNION ALL SELECT @purchase_menu_id
  UNION ALL SELECT @iqc_menu_id
  UNION ALL SELECT @iqc_query_menu_id
  UNION ALL SELECT @iqc_create_menu_id
  UNION ALL SELECT @iqc_first_check_menu_id
  UNION ALL SELECT @iqc_start_recheck_menu_id
  UNION ALL SELECT @iqc_recheck_menu_id
  UNION ALL SELECT @iqc_assign_checker_menu_id
) target ON 1 = 1
WHERE r.`deleted` = b'0'
  AND r.`code` = 'super_admin'
  AND target.`menu_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` rm
    WHERE rm.`role_id` = r.`id`
      AND rm.`menu_id` = target.`menu_id`
      AND rm.`tenant_id` = r.`tenant_id`
      AND rm.`deleted` = b'0'
  );

DELETE rm
FROM `system_role_menu` rm
JOIN `system_role` r ON r.`id` = rm.`role_id`
WHERE rm.`deleted` = b'0'
  AND r.`deleted` = b'0'
  AND r.`code` = 'erp_iqc_inspector'
  AND rm.`menu_id` IN (
    COALESCE(@iqc_create_menu_id, -1),
    COALESCE(@iqc_start_recheck_menu_id, -1),
    COALESCE(@iqc_assign_checker_menu_id, -1)
  );

SET FOREIGN_KEY_CHECKS = 1;
