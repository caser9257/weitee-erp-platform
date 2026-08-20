-- 产品导入权限点种子
-- 目的：为“产品信息”菜单补充 erp:product:import 权限点（对应 /erp/product/import 与 /erp/product/get-import-template 接口）
-- 幂等：以 parent_id + permission 判断，可重复执行

INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '产品导入', 'erp:product:import', 3, 6,
       (SELECT `id` FROM `system_menu` WHERE `permission` = 'erp:product:export' AND `deleted` = b'0'
        ORDER BY `id` LIMIT 1),
       '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `parent_id` = (SELECT `id` FROM `system_menu` WHERE `permission` = 'erp:product:export' AND `deleted` = b'0'
                         ORDER BY `id` LIMIT 1)
      AND `permission` = 'erp:product:import' AND `deleted` = b'0'
);
