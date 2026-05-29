SELECT rm.menu_id, m.name, m.permission, m.type, m.parent_id, m.path, m.component_name
FROM system_role_menu rm
JOIN system_menu m ON m.id = rm.menu_id AND m.deleted = b'0'
WHERE rm.deleted = b'0'
  AND rm.role_id = 940002
  AND (
    m.path = 'account'
    OR m.component = 'erp/finance/account/index'
    OR m.component_name IN ('FormalFinanceAccount', 'ErpAccount')
    OR m.permission LIKE 'erp:account:%'
  )
ORDER BY m.type, m.sort, m.id;
