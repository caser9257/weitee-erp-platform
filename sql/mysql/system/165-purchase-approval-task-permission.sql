/*
  采购审批角色 BPM 待办权限
  为采购审批组长/经理补齐已有 BPM 待办查询与处理权限。
*/

SET NAMES utf8mb4;

SET @bpm_task_query_menu_id := (
  SELECT id
  FROM system_menu
  WHERE permission = 'bpm:task:query'
    AND deleted = b'0'
  ORDER BY id
  LIMIT 1
);

SET @bpm_task_update_menu_id := (
  SELECT id
  FROM system_menu
  WHERE permission = 'bpm:task:update'
    AND deleted = b'0'
  ORDER BY id
  LIMIT 1
);

INSERT IGNORE INTO system_role_menu (role_id, menu_id)
SELECT role_id, menu_id
FROM (
  SELECT 920002 AS role_id, @bpm_task_query_menu_id AS menu_id
  UNION ALL
  SELECT 920002, @bpm_task_update_menu_id
  UNION ALL
  SELECT 920003, @bpm_task_query_menu_id
  UNION ALL
  SELECT 920003, @bpm_task_update_menu_id
) permissions
WHERE menu_id IS NOT NULL;
