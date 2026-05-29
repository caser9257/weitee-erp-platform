SELECT m.id, m.name, m.permission, m.type, m.parent_id, m.path, m.component, m.component_name, m.status
FROM system_menu m
WHERE m.deleted = b'0'
  AND (
    m.path = 'account'
    OR m.component = 'erp/finance/account/index'
    OR m.component_name IN ('FormalFinanceAccount', 'ErpAccount')
    OR m.permission LIKE 'erp:account:%'
  )
ORDER BY m.type, m.sort, m.id;
