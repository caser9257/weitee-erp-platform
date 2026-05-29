SELECT id, name, path, parent_id, type, status, sort
FROM system_menu
WHERE deleted = b'0' AND parent_id = 0
ORDER BY sort, id;
