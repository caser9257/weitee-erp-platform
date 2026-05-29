SELECT 'current_superadmin_roles' AS label, GROUP_CONCAT(role_id ORDER BY role_id) AS value
FROM `ruoyi-vue-pro`.system_user_role
WHERE user_id = 145 AND deleted = b'0';

SELECT 'restore_superadmin_roles' AS label, GROUP_CONCAT(role_id ORDER BY role_id) AS value
FROM ruoyi_vue_pro_restore.system_user_role
WHERE user_id = 145 AND deleted = b'0';

SELECT 'current_superadmin_role_codes' AS label, GROUP_CONCAT(r.code ORDER BY r.id) AS value
FROM `ruoyi-vue-pro`.system_user_role ur
JOIN `ruoyi-vue-pro`.system_role r ON r.id = ur.role_id AND r.deleted = b'0'
WHERE ur.user_id = 145 AND ur.deleted = b'0';

SELECT 'restore_superadmin_role_codes' AS label, GROUP_CONCAT(r.code ORDER BY r.id) AS value
FROM ruoyi_vue_pro_restore.system_user_role ur
JOIN ruoyi_vue_pro_restore.system_role r ON r.id = ur.role_id AND r.deleted = b'0'
WHERE ur.user_id = 145 AND ur.deleted = b'0';

SELECT 'current_superadmin_role_menu_count' AS label, COUNT(*) AS value
FROM `ruoyi-vue-pro`.system_role_menu
WHERE role_id IN (
  SELECT role_id FROM `ruoyi-vue-pro`.system_user_role WHERE user_id = 145 AND deleted = b'0'
) AND deleted = b'0';

SELECT 'restore_superadmin_role_menu_count' AS label, COUNT(*) AS value
FROM ruoyi_vue_pro_restore.system_role_menu
WHERE role_id IN (
  SELECT role_id FROM ruoyi_vue_pro_restore.system_user_role WHERE user_id = 145 AND deleted = b'0'
) AND deleted = b'0';

SELECT 'current_user_role_assignments' AS label, u.username, GROUP_CONCAT(r.code ORDER BY r.id) AS role_codes
FROM `ruoyi-vue-pro`.system_users u
LEFT JOIN `ruoyi-vue-pro`.system_user_role ur ON ur.user_id = u.id AND ur.deleted = b'0'
LEFT JOIN `ruoyi-vue-pro`.system_role r ON r.id = ur.role_id AND r.deleted = b'0'
WHERE u.username IN ('superadmin','need','poapply','poleader','pogm','admin')
GROUP BY u.username
ORDER BY u.username;

SELECT 'restore_user_role_assignments' AS label, u.username, GROUP_CONCAT(r.code ORDER BY r.id) AS role_codes
FROM ruoyi_vue_pro_restore.system_users u
LEFT JOIN ruoyi_vue_pro_restore.system_user_role ur ON ur.user_id = u.id AND ur.deleted = b'0'
LEFT JOIN ruoyi_vue_pro_restore.system_role r ON r.id = ur.role_id AND r.deleted = b'0'
WHERE u.username IN ('superadmin','need','poapply','poleader','pogm','admin')
GROUP BY u.username
ORDER BY u.username;

SELECT 'current_role_menu_rows' AS label, r.code, COUNT(*) AS value
FROM `ruoyi-vue-pro`.system_role r
LEFT JOIN `ruoyi-vue-pro`.system_role_menu rm ON rm.role_id = r.id AND rm.deleted = b'0'
WHERE r.code IN ('super_admin','supply_chain_manager')
GROUP BY r.code
ORDER BY r.code;

SELECT 'restore_role_menu_rows' AS label, r.code, COUNT(*) AS value
FROM ruoyi_vue_pro_restore.system_role r
LEFT JOIN ruoyi_vue_pro_restore.system_role_menu rm ON rm.role_id = r.id AND rm.deleted = b'0'
WHERE r.code IN ('super_admin','supply_chain_manager')
GROUP BY r.code
ORDER BY r.code;
