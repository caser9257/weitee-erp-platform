/*
 委外入库菜单补齐
 目标：为委外入库提供可进入的 MRP 菜单入口，并补齐权限点
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @scm_root_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/scm' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

SET @outsource_inbound_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/outsource-inbound/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '委外入库', '', 2, 80,
       @scm_root_id, 'outsource-inbound', 'ep:box', 'erp/mrp/outsource-inbound/index', 'ErpMrpOutsourceInboundPage',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @scm_root_id IS NOT NULL
  AND @outsource_inbound_menu_id IS NULL;

SET @outsource_inbound_menu_id := (
  SELECT `id` FROM `system_menu`
  WHERE `component` = 'erp/mrp/outsource-inbound/index' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '委外入库查询', 'erp:outsource-inbound:query', 3, 1,
       @outsource_inbound_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @outsource_inbound_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:outsource-inbound:query' AND `deleted` = b'0'
  );
