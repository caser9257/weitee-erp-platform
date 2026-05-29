SELECT id, name, permission, type, parent_id, path, component_name, status
FROM system_menu
WHERE permission IN (
  'erp:finance-ledger:query',
  'erp:finance-period:query',
  'erp:finance-subject:query',
  'erp:finance-report-item:query',
  'erp:finance-voucher:query'
)
ORDER BY id;
