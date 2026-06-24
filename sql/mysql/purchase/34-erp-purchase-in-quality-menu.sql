/*
 Target: ERP 采购入库 IQC 菜单
 Schema: ruoyi-vue-pro
 Date: 2026-04-13

 说明：
 1. 在“采购管理”下新增“采购入库 IQC”菜单
 2. 默认授予超级管理员
 3. 兼容供应链经理角色，补齐 IQC 页面所需的查询/处理权限
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;


SET @iqc_menu_id = 920561;

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
(@iqc_menu_id, '采购入库 IQC', '', 2, 7, 2602, 'in-quality', 'ep:finished', 'erp/purchase/in-quality/index', 'ErpPurchaseInQuality',
 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0')
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

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, @iqc_menu_id, '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu`
  WHERE `role_id` = 1 AND `menu_id` = @iqc_menu_id AND `deleted` = b'0'
);

SET @supply_chain_role_id := (
  SELECT `id` FROM `system_role`
  WHERE `code` = 'supply_chain_manager'
  ORDER BY `deleted` ASC, `id` ASC
  LIMIT 1
);

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @supply_chain_role_id, target.`menu_id`, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT 2563 AS `menu_id`
  UNION
  SELECT 2602
  UNION
  SELECT @iqc_menu_id
  UNION
  SELECT 2674
  UNION
  SELECT 2676
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
