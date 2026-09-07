-- 研发物料(Cadence) 菜单与权限点种子
-- 目的：为“研发物料列表”与“Cadence 数据导入”补充菜单入口与权限点
--   erp:product:rd-cadence:query  -> 页面/列表查询
--   erp:product:rd-cadence:import -> 导入（预检查/导入）
--   erp:product:rd-cadence:export -> 导出
-- 幂等：以 permission 判重，可重复执行；父节点取"研发管理"目录(id=930120)
-- 注意：部门级数据/字段可见性(研发部门 deptId=103)由后端接口再次校验，本种子只控制菜单与接口权限
-- 1) 研发物料(Cadence) 菜单（页面）
-- 父节点选择：优先“启用且可见”的研发管理目录；没有则回退到产品页所在目录；再回退根节点。
-- 不可用 status=0/visible=0 的目录（如 dev 上研发管理曾被整组停用隐藏），否则整棵子树不可见。
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), '研发物料(Cadence)', 'erp:product:rd-cadence:query', 2, 50,
       COALESCE(
         (SELECT `id` FROM `system_menu` WHERE `name` = '研发管理' AND `type` = 1
          AND `status` = 0 AND `visible` = b'1' AND `deleted` = b'0' ORDER BY `id` DESC LIMIT 1),
         (SELECT `parent_id` FROM `system_menu` WHERE `component` = 'erp/product/product/index'
          AND `deleted` = b'0' LIMIT 1),
         0
       ),
       'rd-cadence', 'ep:cpu', 'erp/product/rdCadence/index', 'ErpProductRdCadence',
       0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:product:rd-cadence:query' AND `deleted` = b'0'
);

-- 2) Cadence 导入 按钮权限
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), 'Cadence 导入', 'erp:product:rd-cadence:import', 3, 1,
       (SELECT `id` FROM `system_menu` WHERE `permission` = 'erp:product:rd-cadence:query' AND `deleted` = b'0' LIMIT 1),
       '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:product:rd-cadence:import' AND `deleted` = b'0'
);

-- 3) Cadence 导出 按钮权限
INSERT INTO `system_menu`
(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`,
 `component_name`, `status`, `visible`, `keep_alive`, `always_show`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`)
SELECT (SELECT IFNULL(MAX(t.id), 0) + 1 FROM `system_menu` t), 'Cadence 导出', 'erp:product:rd-cadence:export', 3, 2,
       (SELECT `id` FROM `system_menu` WHERE `permission` = 'erp:product:rd-cadence:query' AND `deleted` = b'0' LIMIT 1),
       '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (
    SELECT 1 FROM `system_menu`
    WHERE `permission` = 'erp:product:rd-cadence:export' AND `deleted` = b'0'
);
