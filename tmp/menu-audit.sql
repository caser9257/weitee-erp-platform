SELECT id, name, type, parent_id, path, component, status, visible
FROM system_menu
WHERE deleted = b'0'
  AND (
    path IN ('/project','/master-data','/sales','/pmo','/rd','/scm','/process','/mes','/qms','/finance','/hr','/system','/erp','/iot','/mrp','/manufacturing')
    OR parent_id IN (
      SELECT id FROM system_menu WHERE deleted = b'0' AND path IN ('/project','/master-data','/sales','/pmo','/rd','/scm','/process','/mes','/qms','/finance','/hr','/system','/erp','/iot','/mrp','/manufacturing')
    )
  )
ORDER BY parent_id, sort, id;
