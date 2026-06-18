-- 删除错误的文件管理菜单
DELETE FROM system_role_menu WHERE menu_id = 150000;
DELETE FROM system_menu WHERE id = 150000;
