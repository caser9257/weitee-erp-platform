SELECT id, name, parent_id, status, deleted, create_time, update_time
FROM system_menu
WHERE deleted = b'0' AND parent_id = 0 AND status = 1
ORDER BY sort, id
LIMIT 30;
