SELECT u.id AS user_id, u.username, r.id AS role_id, r.code, r.name, ur.tenant_id
FROM system_users u
JOIN system_user_role ur ON ur.user_id = u.id AND ur.deleted = b'0'
JOIN system_role r ON r.id = ur.role_id AND r.deleted = b'0'
WHERE u.username = '财务主管' AND u.deleted = b'0';
