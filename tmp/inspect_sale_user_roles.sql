SELECT u.username, r.id AS role_id, r.code, r.name
FROM `ruoyi-vue-pro`.system_user_role ur
JOIN `ruoyi-vue-pro`.system_users u ON u.id = ur.user_id
JOIN `ruoyi-vue-pro`.system_role r ON r.id = ur.role_id
WHERE u.username IN ('so_apply', 'so_leader', 'so_gm')
ORDER BY u.username, r.id;
