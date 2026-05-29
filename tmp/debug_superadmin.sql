SELECT u.id AS user_id, u.username, ur.role_id, r.code, r.name, r.status, r.deleted
FROM system_users u
LEFT JOIN system_user_role ur ON ur.user_id = u.id AND ur.deleted = b'0'
LEFT JOIN system_role r ON r.id = ur.role_id
WHERE u.username = 'superadmin'
ORDER BY ur.role_id;

SELECT id, code, name, status, deleted, LENGTH(code) AS code_len, HEX(code) AS code_hex
FROM system_role
WHERE code LIKE '%super%';

SELECT COUNT(*) AS super_admin_role_menu_count
FROM system_role_menu rm
JOIN system_role r ON r.id = rm.role_id
WHERE r.code = 'super_admin' AND rm.deleted = b'0' AND r.deleted = b'0';

SELECT COUNT(*) AS active_menu_count FROM system_menu WHERE deleted = b'0' AND status = 0;
SELECT COUNT(*) AS all_menu_count FROM system_menu WHERE deleted = b'0';
