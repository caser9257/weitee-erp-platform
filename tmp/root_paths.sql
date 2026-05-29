SELECT path, COUNT(*) AS cnt
FROM system_menu
WHERE deleted = b'0' AND parent_id = 0
GROUP BY path
ORDER BY path;
