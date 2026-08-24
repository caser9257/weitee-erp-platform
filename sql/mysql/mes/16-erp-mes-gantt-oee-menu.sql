-- MES 排程模块第三阶段：甘特图 + OEE 看板菜单
-- 归属：制造执行管理（/mes）下新增"排程甘特图""OEE 分析"

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

SET @mes_root_id := (
  SELECT `id` FROM `system_menu`
  WHERE `parent_id` = 0 AND `path` = '/mes' AND `deleted` = b'0'
  ORDER BY `id` LIMIT 1
);
SET @mes_root_id := IFNULL(@mes_root_id, 930170);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 931902, '排程甘特图', '', 2, 55, @mes_root_id, 'work-task-gantt', 'ep:histogram',
       'mes/work-task-gantt/index', 'MesWorkTaskGantt',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = @mes_root_id AND `path` = 'work-task-gantt' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 931903, 'OEE 分析', '', 2, 60, @mes_root_id, 'oee', 'ep:data-analysis',
       'mes/oee/index', 'MesOee',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = @mes_root_id AND `path` = 'oee' AND `deleted` = b'0'
);

-- 按钮权限点
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 931930, '排程甘特查询', 'mes:work-task:query', 3, 1, 931902, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = 931902 AND `permission` = 'mes:work-task:query' AND `deleted` = b'0'
);

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 931931, 'OEE 查询', 'mes:oee:query', 3, 1, 931903, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_menu`
  WHERE `parent_id` = 931903 AND `permission` = 'mes:oee:query' AND `deleted` = b'0'
);

-- 管理员授权
INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, t.menu_id, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT 931902 AS menu_id
  UNION ALL SELECT 931903
  UNION ALL SELECT 931930
  UNION ALL SELECT 931931
) t
WHERE NOT EXISTS (
  SELECT 1 FROM `system_role_menu` rm
  WHERE rm.`role_id` = 1 AND rm.`menu_id` = t.menu_id AND rm.`deleted` = b'0'
);

SET FOREIGN_KEY_CHECKS = 1;

SELECT id, name, permission, type, sort, parent_id, path, component FROM system_menu
WHERE id IN (931902, 931903, 931930, 931931) AND deleted = 0;
