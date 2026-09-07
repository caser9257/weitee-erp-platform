/*
 委外发料菜单补齐
 目标：为委外发料提供可进入的 MRP 菜单入口，并补齐权限点
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


SET @scm_root_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/scm' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @outsource_issue_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/outsource-issue/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '委外发料', '', 2, 70,
       @scm_root_id, 'outsource-issue', 'ep:box', 'erp/mrp/outsource-issue/index', 'ErpMrpOutsourceIssuePage',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @scm_root_id IS NOT NULL
  AND @outsource_issue_menu_id IS NULL;

SET @outsource_issue_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/outsource-issue/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '委外发料查询', 'erp:outsource-issue:query', 3, 1,
       @outsource_issue_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @outsource_issue_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:outsource-issue:query' AND `deleted` = b'0'
  );
