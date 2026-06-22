-- [已废弃] 文件管理菜单种子
-- 注意：此脚本将文件管理错误地放在"财务管理"下，请勿执行
-- 如已执行，请先执行 150-erp-file-management-menu-rollback.sql 回滚
-- 然后执行 152-erp-file-management-menu-correct.sql

-- 文件管理菜单种子

-- 查询财务管理菜单ID
SET @finance_menu_id = (SELECT id FROM system_menu WHERE name = '财务管理' AND deleted = 0 LIMIT 1);

-- 插入菜单
INSERT INTO system_menu (id, parent_id, name, permission, path, component, component_name, icon, sort, status, type, creator, create_time, updater, update_time, deleted)
VALUES (150000, @finance_menu_id, '文件管理', 'infra:file:query', 'file', 'infra/file/index', 'InfraFile', 'ep:folder-opened', 99, 0, 1, '1', NOW(), '1', NOW(), b'0')
ON DUPLICATE KEY UPDATE name = VALUES(name), updater = '1', update_time = NOW();

-- 授权给超级管理员
INSERT INTO system_role_menu (role_id, menu_id)
SELECT 1, 150000
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM system_role_menu WHERE role_id = 1 AND menu_id = 150000
);
