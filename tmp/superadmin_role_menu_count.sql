SELECT COUNT(*) AS role_menu_count
FROM system_role_menu rm
JOIN system_role r ON r.id = rm.role_id
WHERE r.code = 'super_admin' AND rm.deleted = b'0' AND r.deleted = b'0';
