SELECT r.id AS role_id, r.code, r.name, rm.menu_id, m.name AS menu_name, m.permission, m.type, m.parent_id, m.status, rm.tenant_id
FROM system_role r
JOIN system_role_menu rm ON rm.role_id = r.id AND rm.deleted = b'0'
JOIN system_menu m ON m.id = rm.menu_id AND m.deleted = b'0'
WHERE r.code = 'erp_finance_manager'
  AND (
    m.permission LIKE 'erp:finance-%'
    OR m.path IN ('finance', 'ledger', 'period', 'subject', 'report-item', 'reports', 'voucher')
  )
ORDER BY m.type, m.sort, m.id;
