SELECT parent_id, COUNT(*) AS cnt
FROM system_menu
WHERE deleted = b'0'
GROUP BY parent_id
ORDER BY parent_id
LIMIT 20;

SELECT type, COUNT(*) AS cnt
FROM system_menu
WHERE deleted = b'0'
GROUP BY type
ORDER BY type;

SELECT COUNT(*) AS root_active_cnt
FROM system_menu
WHERE deleted = b'0' AND status = 0 AND parent_id = 0;

SELECT id, name, path, type, parent_id, status, deleted
FROM system_menu
WHERE deleted = b'0' AND parent_id = 0
ORDER BY sort, id
LIMIT 30;

SELECT id, name, path, type, parent_id, status, deleted
FROM system_menu
WHERE deleted = b'0' AND status = 0
ORDER BY parent_id, sort, id
LIMIT 50;
