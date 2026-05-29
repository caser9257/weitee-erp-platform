SELECT id, name, path, component, parent_id, type, status, sort
FROM system_menu
WHERE deleted = b'0'
ORDER BY parent_id, sort, id;
