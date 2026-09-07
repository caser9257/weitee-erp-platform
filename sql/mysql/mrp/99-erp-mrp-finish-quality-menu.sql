/*
 委外与完工质检导航补齐
 目标：为成品质检补齐正式菜单入口，并把菜单授权同步到已有 QMS 角色
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


SET @finish_quality_fixed_id := 932309;

SET @qms_root_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `parent_id` = 0
    AND `path` = '/qms'
    AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

SET @finish_quality_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/mrp/finish-quality/index'
    AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT COALESCE(@finish_quality_menu_id, @finish_quality_fixed_id),
       '成品质检', '', 2, 40,
       @qms_root_id, 'finish-quality', 'ep:circle-check', 'erp/mrp/finish-quality/index', 'ErpProductionFinishQualityPage',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @qms_root_id IS NOT NULL
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

SET @finish_quality_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/mrp/finish-quality/index'
    AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

DROP TEMPORARY TABLE IF EXISTS `tmp_finish_quality_role_ids`;
CREATE TEMPORARY TABLE `tmp_finish_quality_role_ids` (
  `role_id` BIGINT NOT NULL PRIMARY KEY
) ENGINE=MEMORY;

INSERT INTO `tmp_finish_quality_role_ids` (`role_id`)
SELECT DISTINCT rm.`role_id`
FROM `system_role_menu` rm
JOIN `system_menu` sm ON sm.`id` = rm.`menu_id`
WHERE rm.`deleted` = b'0'
  AND sm.`deleted` = b'0'
  AND (
    sm.`id` = @qms_root_id
    OR sm.`parent_id` = @qms_root_id
  );

INSERT IGNORE INTO `tmp_finish_quality_role_ids` (`role_id`) VALUES (1);

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT role_ids.`role_id`, @finish_quality_menu_id, '1', NOW(), '1', NOW(), b'0'
FROM `tmp_finish_quality_role_ids` role_ids
WHERE @finish_quality_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` rm
    WHERE rm.`role_id` = role_ids.`role_id`
      AND rm.`menu_id` = @finish_quality_menu_id
      AND rm.`deleted` = b'0'
  );

DROP TEMPORARY TABLE IF EXISTS `tmp_finish_quality_role_ids`;

SET FOREIGN_KEY_CHECKS = 1;
