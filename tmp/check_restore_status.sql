SELECT 'current_users' AS label, COUNT(*) AS value FROM `ruoyi-vue-pro`.system_users;
SELECT 'restore_users' AS label, COUNT(*) AS value FROM ruoyi_vue_pro_restore.system_users;
SELECT 'current_role' AS label, COUNT(*) AS value FROM `ruoyi-vue-pro`.system_role;
SELECT 'restore_role' AS label, COUNT(*) AS value FROM ruoyi_vue_pro_restore.system_role;
SELECT 'current_menu' AS label, COUNT(*) AS value FROM `ruoyi-vue-pro`.system_menu;
SELECT 'restore_menu' AS label, COUNT(*) AS value FROM ruoyi_vue_pro_restore.system_menu;
SELECT 'current_role_menu' AS label, COUNT(*) AS value FROM `ruoyi-vue-pro`.system_role_menu;
SELECT 'restore_role_menu' AS label, COUNT(*) AS value FROM ruoyi_vue_pro_restore.system_role_menu;

SELECT id, username, nickname, status, deleted
FROM `ruoyi-vue-pro`.system_users
WHERE username IN ('superadmin','need','poapply','poleader','pogm','admin')
ORDER BY id;

SELECT id, username, nickname, status, deleted
FROM ruoyi_vue_pro_restore.system_users
WHERE username IN ('superadmin','need','poapply','poleader','pogm','admin')
ORDER BY id;

SELECT id, code, name, status, deleted
FROM `ruoyi-vue-pro`.system_role
WHERE code IN ('super_admin','supply_chain_manager')
ORDER BY id;

SELECT id, code, name, status, deleted
FROM ruoyi_vue_pro_restore.system_role
WHERE code IN ('super_admin','supply_chain_manager')
ORDER BY id;

SELECT id, name, permission, type, parent_id, path, component, deleted
FROM `ruoyi-vue-pro`.system_menu
WHERE component IN ('erp/mrp/plan/index','erp/mrp/plan-rule/index','erp/rd/bom/index','erp/purchase/order/index','erp/purchase/in/index','erp/mrp/suggest/index')
ORDER BY id;

SELECT id, name, permission, type, parent_id, path, component, deleted
FROM ruoyi_vue_pro_restore.system_menu
WHERE component IN ('erp/mrp/plan/index','erp/mrp/plan-rule/index','erp/rd/bom/index','erp/purchase/order/index','erp/purchase/in/index','erp/mrp/suggest/index')
ORDER BY id;
