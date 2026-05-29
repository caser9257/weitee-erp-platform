SELECT 'current_root_menu_status' AS label, COUNT(*) AS disabled_roots
FROM system_menu
WHERE deleted = b'0' AND parent_id = 0 AND status = 1;

SELECT 'restore_root_menu_status' AS label, COUNT(*) AS disabled_roots
FROM ruoyi_vue_pro_restore.system_menu
WHERE deleted = b'0' AND parent_id = 0 AND status = 1;

SELECT c.id, c.name, c.path, c.status AS current_status, r.status AS restore_status
FROM system_menu c
JOIN ruoyi_vue_pro_restore.system_menu r ON r.id = c.id
WHERE c.deleted = b'0' AND c.parent_id = 0
ORDER BY c.sort, c.id;
