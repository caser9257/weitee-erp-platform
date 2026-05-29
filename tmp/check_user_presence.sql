SELECT 'current_superadmin' AS label, COUNT(*) AS value FROM uoyi-vue-pro.system_users WHERE username='superadmin';
SELECT 'restore_superadmin' AS label, COUNT(*) AS value FROM ruoyi_vue_pro_restore.system_users WHERE username='superadmin';
SELECT 'current_need' AS label, COUNT(*) AS value FROM uoyi-vue-pro.system_users WHERE username='need';
SELECT 'restore_need' AS label, COUNT(*) AS value FROM ruoyi_vue_pro_restore.system_users WHERE username='need';
SELECT 'current_poapply' AS label, COUNT(*) AS value FROM uoyi-vue-pro.system_users WHERE username='poapply';
SELECT 'restore_poapply' AS label, COUNT(*) AS value FROM ruoyi_vue_pro_restore.system_users WHERE username='poapply';
