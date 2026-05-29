SELECT COUNT(*) AS root_active_cnt FROM ruoyi_vue_pro_restore.system_menu WHERE deleted = b'0' AND status = 0 AND parent_id = 0;
SELECT COUNT(*) AS active_menu_count FROM ruoyi_vue_pro_restore.system_menu WHERE deleted = b'0' AND status = 0;
SELECT id, name, path, type, parent_id, status, deleted
FROM ruoyi_vue_pro_restore.system_menu
WHERE deleted = b'0' AND parent_id = 0
ORDER BY sort, id
LIMIT 30;
