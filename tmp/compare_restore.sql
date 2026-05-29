SELECT 'current_users' AS label, COUNT(*) AS value FROM `ruoyi-vue-pro`.system_users;
SELECT 'restore_users' AS label, COUNT(*) AS value FROM ruoyi_vue_pro_restore.system_users;
SELECT 'current_role' AS label, COUNT(*) AS value FROM `ruoyi-vue-pro`.system_role;
SELECT 'restore_role' AS label, COUNT(*) AS value FROM ruoyi_vue_pro_restore.system_role;
SELECT 'current_menu' AS label, COUNT(*) AS value FROM `ruoyi-vue-pro`.system_menu;
SELECT 'restore_menu' AS label, COUNT(*) AS value FROM ruoyi_vue_pro_restore.system_menu;
SELECT 'current_role_menu' AS label, COUNT(*) AS value FROM `ruoyi-vue-pro`.system_role_menu;
SELECT 'restore_role_menu' AS label, COUNT(*) AS value FROM ruoyi_vue_pro_restore.system_role_menu;

SELECT id, username, nickname, status
FROM `ruoyi-vue-pro`.system_users
WHERE username IN ('superadmin','need','poapply','poleader','pogm','admin','yudao','yuanma','test')
ORDER BY id;

SELECT id, username, nickname, status
FROM ruoyi_vue_pro_restore.system_users
WHERE username IN ('superadmin','need','poapply','poleader','pogm','admin','yudao','yuanma','test')
ORDER BY id;

SELECT id, username, nickname, status
FROM `ruoyi-vue-pro`.system_users
WHERE username IN ('superadmin','need','poapply','poleader','pogm','admin','yudao','yuanma','test','sales','sales_manager','sales_leader')
ORDER BY id;

SELECT id, username, nickname, status
FROM ruoyi_vue_pro_restore.system_users
WHERE username IN ('superadmin','need','poapply','poleader','pogm','admin','yudao','yuanma','test','sales','sales_manager','sales_leader')
ORDER BY id;

SELECT id, code, name, status
FROM `ruoyi-vue-pro`.system_role
ORDER BY id;

SELECT id, code, name, status
FROM ruoyi_vue_pro_restore.system_role
ORDER BY id;

SELECT id, name, permission, type, parent_id, path
FROM `ruoyi-vue-pro`.system_menu
ORDER BY id
LIMIT 20;

SELECT id, name, permission, type, parent_id, path
FROM ruoyi_vue_pro_restore.system_menu
ORDER BY id
LIMIT 20;
