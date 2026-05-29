SELECT COUNT(*) AS current_menu_count FROM uoyi-vue-pro.system_menu;
SELECT COUNT(*) AS restore_menu_count FROM ruoyi_vue_pro_restore.system_menu;
SELECT id, name, permission, type, parent_id, path FROM ruoyi_vue_pro_restore.system_menu WHERE id IN (1,2,3,4,5,6,7,8,9,10,11,12,13,14,15) ORDER BY id;
