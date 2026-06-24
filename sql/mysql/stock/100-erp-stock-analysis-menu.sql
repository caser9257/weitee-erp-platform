/*
 目标：为 SCM 补齐“库存分析”正式入口
 说明：
 1. 菜单挂在 /scm 根节点下
 2. 复用现有库存分析页面，不新增后端接口
 3. 默认授予超级管理员和供应链经理
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE `ruoyi-vue-pro`;

SET @scm_root_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `parent_id` = 0
    AND `path` = '/scm'
    AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

SET @stock_analysis_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/stock/analysis/index'
    AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT COALESCE(@stock_analysis_menu_id, (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t)),
       '库存分析', '', 2, 32,
       @scm_root_id, 'stock-analysis', 'ep:data-analysis', 'erp/stock/analysis/index', 'ProjectScmStockAnalysis',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @scm_root_id IS NOT NULL
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

SET @stock_analysis_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `component` = 'erp/stock/analysis/index'
    AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

SET @stock_analysis_query_menu_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `permission` = 'erp:stock-analysis:query'
    AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT COALESCE(@stock_analysis_query_menu_id, (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t)),
       '库存分析查询', 'erp:stock-analysis:query', 3, 1,
       @stock_analysis_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE @stock_analysis_menu_id IS NOT NULL
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

DROP TEMPORARY TABLE IF EXISTS `tmp_stock_analysis_role_ids`;
CREATE TEMPORARY TABLE `tmp_stock_analysis_role_ids` (
  `role_id` BIGINT NOT NULL PRIMARY KEY
) ENGINE=MEMORY;

INSERT INTO `tmp_stock_analysis_role_ids` (`role_id`)
SELECT DISTINCT rm.`role_id`
FROM `system_role_menu` rm
JOIN `system_menu` sm ON sm.`id` = rm.`menu_id`
WHERE rm.`deleted` = b'0'
  AND sm.`deleted` = b'0'
  AND (
    sm.`id` = @scm_root_id
    OR sm.`parent_id` = @scm_root_id
  );

INSERT IGNORE INTO `tmp_stock_analysis_role_ids` (`role_id`) VALUES (1);

SET @supply_chain_role_id := (
  SELECT `id`
  FROM `system_role`
  WHERE `code` = 'supply_chain_manager'
  ORDER BY `deleted` ASC, `id` ASC
  LIMIT 1
);

INSERT IGNORE INTO `tmp_stock_analysis_role_ids` (`role_id`)
SELECT @supply_chain_role_id
WHERE @supply_chain_role_id IS NOT NULL;

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT role_ids.`role_id`, @stock_analysis_menu_id, '1', NOW(), '1', NOW(), b'0'
FROM `tmp_stock_analysis_role_ids` role_ids
WHERE @stock_analysis_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` rm
    WHERE rm.`role_id` = role_ids.`role_id`
      AND rm.`menu_id` = @stock_analysis_menu_id
      AND rm.`deleted` = b'0'
  );

INSERT INTO `system_role_menu`
(`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT role_ids.`role_id`, @stock_analysis_query_menu_id, '1', NOW(), '1', NOW(), b'0'
FROM `tmp_stock_analysis_role_ids` role_ids
WHERE @stock_analysis_query_menu_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `system_role_menu` rm
    WHERE rm.`role_id` = role_ids.`role_id`
      AND rm.`menu_id` = @stock_analysis_query_menu_id
      AND rm.`deleted` = b'0'
  );

DROP TEMPORARY TABLE IF EXISTS `tmp_stock_analysis_role_ids`;

SET FOREIGN_KEY_CHECKS = 1;
