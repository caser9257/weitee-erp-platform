UPDATE system_menu
SET status = 0, update_time = NOW(), updater = '1'
WHERE deleted = b'0' AND parent_id = 0 AND status = 1;

SELECT ROW_COUNT() AS affected_rows;
SELECT COUNT(*) AS disabled_roots_after FROM system_menu WHERE deleted = b'0' AND parent_id = 0 AND status = 1;
