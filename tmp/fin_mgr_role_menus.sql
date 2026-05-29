SELECT rm.menu_id, m.name, m.permission, m.type, m.parent_id, m.path, m.component_name, m.status
FROM system_role_menu rm
JOIN system_menu m ON m.id = rm.menu_id AND m.deleted = b'0'
WHERE rm.deleted = b'0'
  AND rm.role_id = 940002
ORDER BY m.parent_id, m.type, m.sort, m.id;
