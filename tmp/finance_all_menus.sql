SELECT m.id, m.name, m.permission, m.type, m.parent_id, m.path, m.component, m.component_name, m.status
FROM system_menu m
WHERE m.deleted = b'0'
  AND (
    m.path = '/finance'
    OR m.component LIKE 'erp/finance/%'
    OR m.component_name LIKE '%Finance%'
    OR m.permission LIKE 'erp:finance-%'
    OR m.permission LIKE 'erp:ap-%'
    OR m.permission LIKE 'erp:account:%'
  )
ORDER BY m.parent_id, m.type, m.sort, m.id;
