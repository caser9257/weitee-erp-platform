SELECT id, role_id, menu_id, deleted, tenant_id, create_time
FROM system_role_menu
WHERE role_id = 940002
  AND menu_id IN (932321, 932322, 932323, 932324, 932325)
ORDER BY menu_id;
