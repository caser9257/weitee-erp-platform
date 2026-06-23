-- 文件管理菜单（正确版本）
-- 将文件管理放在"系统管理" -> "基础设施"下

-- 查询基础设施菜单ID
SET @infra_menu_id = (SELECT id FROM system_menu WHERE name = '基础设施' AND deleted = 0 LIMIT 1);

-- 如果基础设施菜单不存在，则放在系统管理下
SET @parent_menu_id = IFNULL(@infra_menu_id, (SELECT id FROM system_menu WHERE name = '系统管理' AND deleted = 0 LIMIT 1));

-- 删除旧的错误菜单（如果存在）
DELETE FROM system_role_menu WHERE menu_id = 150000;
DELETE FROM system_menu WHERE id = 150000;

-- 插入正确的菜单
INSERT INTO system_menu (id, parent_id, name, permission, path, component, component_name, icon, sort, status, type, creator, create_time, updater, update_time, deleted)
VALUES (150000, @parent_menu_id, '文件管理', 'infra:file:query', 'file', 'infra/file/index', 'InfraFile', 'ep:folder-opened', 99, 0, 1, '1', NOW(), '1', NOW(), b'0');

-- 授权给超级管理员
INSERT INTO system_role_menu (role_id, menu_id)
SELECT 1, 150000
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM system_role_menu WHERE role_id = 1 AND menu_id = 150000
);
