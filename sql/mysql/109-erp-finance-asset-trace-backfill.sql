SELECT r.id, r.code, r.tenant_id, m.permission, rm.tenant_id
FROM system_role r
         LEFT JOIN system_role_menu rm ON rm.role_id = r.id AND rm.deleted = b'0'
         LEFT JOIN system_menu m ON m.id = rm.menu_id AND m.deleted = b'0'
WHERE r.code = 'erp_finance_manager'
  AND m.permission IN ('erp:finance-report:query', 'erp:finance-voucher:query')
ORDER BY r.id, m.permission;
