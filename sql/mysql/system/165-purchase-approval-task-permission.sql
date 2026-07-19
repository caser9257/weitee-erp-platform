/*
  采购审批角色 BPM 待办权限
  为采购审批组长/经理补齐已有 BPM 待办查询与处理权限。
*/

SET NAMES utf8mb4;

SET @bpm_menu_parent_id := (
  SELECT id
  FROM system_menu
  WHERE id = 3000
    AND deleted = b'0'
  LIMIT 1
);

SET @bpm_task_query_menu_id := (
  SELECT id
  FROM system_menu
  WHERE permission = 'bpm:task:query'
    AND deleted = b'0'
  ORDER BY id
  LIMIT 1
);

INSERT INTO system_menu
(id, name, permission, type, sort, parent_id, path, icon, component, component_name,
 status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT 920580, 'BPM Task Query', 'bpm:task:query', 3, 1, @bpm_menu_parent_id,
       '', '', '', '', 0, b'1', b'1', b'1', 'tester', NOW(), 'tester', NOW(), b'0'
WHERE @bpm_menu_parent_id IS NOT NULL
  AND @bpm_task_query_menu_id IS NULL
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  permission = VALUES(permission),
  parent_id = VALUES(parent_id),
  status = VALUES(status),
  deleted = b'0',
  updater = 'tester',
  update_time = NOW();

SET @bpm_task_update_menu_id := (
  SELECT id
  FROM system_menu
  WHERE permission = 'bpm:task:update'
    AND deleted = b'0'
  ORDER BY id
  LIMIT 1
);

INSERT INTO system_menu
(id, name, permission, type, sort, parent_id, path, icon, component, component_name,
 status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted)
SELECT 920581, 'BPM Task Update', 'bpm:task:update', 3, 2, @bpm_menu_parent_id,
       '', '', '', '', 0, b'1', b'1', b'1', 'tester', NOW(), 'tester', NOW(), b'0'
WHERE @bpm_menu_parent_id IS NOT NULL
  AND @bpm_task_update_menu_id IS NULL
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  permission = VALUES(permission),
  parent_id = VALUES(parent_id),
  status = VALUES(status),
  deleted = b'0',
  updater = 'tester',
  update_time = NOW();

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
