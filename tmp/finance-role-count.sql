SELECT COUNT(*) AS cnt
FROM system_role_menu rm
JOIN system_role r ON r.id = rm.role_id AND r.deleted = b'0'
WHERE r.code = 'erp_finance_manager'
  AND rm.deleted = b'0'
  AND rm.menu_id IN (932321, 932322, 932323, 932324, 932325);
