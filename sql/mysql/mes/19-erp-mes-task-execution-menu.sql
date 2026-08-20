-- MES-B 第二阶段：浏览器工位端现场执行菜单与权限
SET NAMES utf8mb4;

SET @mes_root_id := (
    SELECT `id` FROM `system_menu`
    WHERE `parent_id` = 0 AND `path` = '/mes' AND `deleted` = b'0'
    ORDER BY `id` LIMIT 1
);
SET @mes_root_id := IFNULL(@mes_root_id, 930170);

SET @execution_menu_id := (
    SELECT `id` FROM `system_menu`
    WHERE `parent_id` = @mes_root_id AND `path` = 'pda-report' AND `deleted` = b'0'
    ORDER BY `id` LIMIT 1
);
SET @execution_menu_id := IFNULL(@execution_menu_id, (
    SELECT COALESCE(MAX(`id`), 931980) + 1 FROM `system_menu`
));

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @execution_menu_id, '现场执行', '', 2, 49, @mes_root_id, 'pda-report', 'ep:finished',
       'mes/pda-execute/report', 'ProjectMesPdaReport', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `parent_id` = @mes_root_id AND `path` = 'pda-report' AND `deleted` = b'0'
);

UPDATE `system_menu`
SET `name` = '现场执行', `icon` = 'ep:finished', `component` = 'mes/pda-execute/report',
    `component_name` = 'ProjectMesPdaReport', `sort` = 49, `updater` = '1', `update_time` = NOW()
WHERE `id` = @execution_menu_id AND `deleted` = b'0';

SET @execution_query_id := (
    SELECT `id` FROM `system_menu`
    WHERE `parent_id` = @execution_menu_id AND `permission` = 'mes:task-execution:query' AND `deleted` = b'0'
    ORDER BY `id` LIMIT 1
);
SET @execution_query_id := IFNULL(@execution_query_id, (SELECT COALESCE(MAX(`id`), 931981) + 1 FROM `system_menu`));
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @execution_query_id, '现场执行查询', 'mes:task-execution:query', 3, 1, @execution_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `parent_id` = @execution_menu_id AND `permission` = 'mes:task-execution:query' AND `deleted` = b'0'
);

SET @execution_update_id := (
    SELECT `id` FROM `system_menu`
    WHERE `parent_id` = @execution_menu_id AND `permission` = 'mes:task-execution:update' AND `deleted` = b'0'
    ORDER BY `id` LIMIT 1
);
SET @execution_update_id := IFNULL(@execution_update_id, (SELECT COALESCE(MAX(`id`), 931982) + 1 FROM `system_menu`));
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @execution_update_id, '现场执行状态更新', 'mes:task-execution:update', 3, 2, @execution_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `parent_id` = @execution_menu_id AND `permission` = 'mes:task-execution:update' AND `deleted` = b'0'
);

SET @execution_report_id := (
    SELECT `id` FROM `system_menu`
    WHERE `parent_id` = @execution_menu_id AND `permission` = 'mes:task-execution:report' AND `deleted` = b'0'
    ORDER BY `id` LIMIT 1
);
SET @execution_report_id := IFNULL(@execution_report_id, (SELECT COALESCE(MAX(`id`), 931983) + 1 FROM `system_menu`));
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`,
 `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT @execution_report_id, '现场报工', 'mes:task-execution:report', 3, 3, @execution_menu_id, '', '', '', '',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `parent_id` = @execution_menu_id AND `permission` = 'mes:task-execution:report' AND `deleted` = b'0'
);

INSERT INTO `system_role_menu` (`role_id`, `menu_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT 1, t.menu_id, '1', NOW(), '1', NOW(), b'0'
FROM (
    SELECT @execution_menu_id AS menu_id
    UNION ALL SELECT @execution_query_id
    UNION ALL SELECT @execution_update_id
    UNION ALL SELECT @execution_report_id
) t
WHERE NOT EXISTS (
    SELECT 1 FROM `system_role_menu` rm
    WHERE rm.`role_id` = 1 AND rm.`menu_id` = t.menu_id AND rm.`deleted` = b'0'
);

SELECT id, name, permission, type, parent_id, path, component, component_name
FROM system_menu
WHERE id IN (@execution_menu_id, @execution_query_id, @execution_update_id, @execution_report_id)
  AND deleted = 0;
