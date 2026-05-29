SELECT r.code, rm.menu_id, m.name, m.parent_id, m.type, m.status
FROM system_role r
JOIN system_role_menu rm ON rm.role_id = r.id AND rm.deleted = b'0'
LEFT JOIN system_menu m ON m.id = rm.menu_id AND m.deleted = b'0'
WHERE r.code = 'erp_finance_manager'
  AND rm.menu_id IN (2563, 932314, 932315, 932316, 932317, 932318, 932319, 932320, 932321, 932322, 932323, 932324, 932325)
ORDER BY rm.menu_id;
