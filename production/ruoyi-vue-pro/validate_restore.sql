SELECT 'users_restore' AS label, COUNT(*) AS value FROM ruoyi_vue_pro_restore.system_users;
SELECT 'users_current' AS label, COUNT(*) AS value FROM `weitee-erp`.system_users;
SELECT 'menu_restore' AS label, COUNT(*) AS value FROM ruoyi_vue_pro_restore.system_menu;
SELECT 'menu_current' AS label, COUNT(*) AS value FROM `weitee-erp`.system_menu;
SELECT 'role_restore' AS label, COUNT(*) AS value FROM ruoyi_vue_pro_restore.system_role;
SELECT 'role_current' AS label, COUNT(*) AS value FROM `weitee-erp`.system_role;

SELECT 'restore_user' AS label, username, nickname
FROM ruoyi_vue_pro_restore.system_users
WHERE username IN ('admin', 'so_apply', 'so_leader', 'so_gm', 'poapply', 'poleader', 'pogm')
ORDER BY username;

SELECT 'current_user' AS label, username, nickname
FROM `weitee-erp`.system_users
WHERE username IN ('admin', 'so_apply', 'so_leader', 'so_gm', 'poapply', 'poleader', 'pogm')
ORDER BY username;

SELECT 'restore_notify_template' AS label, code, name
FROM ruoyi_vue_pro_restore.system_notify_template
WHERE code = 'bpm_task_assigned';

SELECT 'current_notify_template' AS label, code, name
FROM `weitee-erp`.system_notify_template
WHERE code = 'bpm_task_assigned';
