SELECT u.username, r.id AS role_id, r.code, r.name
FROM `weitee-erp`.system_user_role ur
JOIN `weitee-erp`.system_users u ON u.id = ur.user_id
JOIN `weitee-erp`.system_role r ON r.id = ur.role_id
WHERE u.username IN ('so_apply', 'so_leader', 'so_gm')
ORDER BY u.username, r.id;
